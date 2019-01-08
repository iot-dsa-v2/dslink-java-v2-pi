package org.iot.dsa.pi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.pi.WebApiMethod.UrlParameter;
import org.iot.dsa.pi.node.RemovableNode;

public class WebApiNode extends RemovableNode {

    private String address;
    private WebClientProxy clientProxy;
    private Boolean isRoot = null;

    public WebApiNode() {
    }

    public WebApiNode(String address, WebClientProxy clientProxy) {
        this(address, clientProxy, false);
    }

    public WebApiNode(String address, WebClientProxy clientProxy, boolean isRoot) {
        this.address = address;
        this.clientProxy = clientProxy;
        this.isRoot = isRoot;
    }

    public void edit(DSMap parameters) {
        address = parameters.getString("Address");
        clientProxy.username = parameters.getString("Username");
        clientProxy.password = parameters.getString("Password");
        init();
    }

    public void get() {
        Response r = getClientProxy().get(address, new DSMap());
        String s = r.readEntity(String.class);
        DSMap m = Util.parseJsonMap(s);
        update(m);
    }

    public WebClientProxy getClientProxy() {
        return clientProxy;
    }

    public void setAddress(String address) {
        boolean changed = !address.equals(this.address);
        this.address = address;
        if (changed) {
            setupExtraActions();
            init();
        }
    }

    public void update(DSMap propMap) {
        Set<String> toRemove = new HashSet<String>();
        for (DSInfo info : this) {
            if (!info.isAction()) {
                String name = info.getName();
                if (!(name.equals("Address") || name.equals("Username") || name
                        .equals("Password"))) {
                    if (info.isNode()) {
                        DSIObject ma = info.getNode().get("Manually Added");
                        if (!(ma instanceof DSBool && ((DSBool) ma).toBoolean())) {
                            toRemove.add(name);
                        }
                    } else {
                        toRemove.add(name);
                    }
                }
            }
        }
        for (Entry e : propMap) {
            String key = e.getKey();
            DSElement value = e.getValue();
            if (key.equals("Items") && value.isList()) {
                DSList items = value.toList();
                updateItems(items, toRemove);
            } else if (key.equals("Links") && value.isMap()) {
                DSMap links = value.toMap();
                updateLinks(links, toRemove);
            } else {
                put(key, value.copy()).setReadOnly(true);
                toRemove.remove(key);
            }
        }
        if (address.endsWith("/search/sources") || address.endsWith("/search/sources/")) {
            String crawlAddr = address.endsWith("/") ? address + "crawl" : address + "/crawl";
            DSNode node = getNode("Crawl");
            if (node instanceof WebApiNode) {
                WebApiNode itemNode = (WebApiNode) node;
                itemNode.setAddress(crawlAddr);
            } else {
                put("Crawl", new WebApiNode(crawlAddr, clientProxy));
            }
            toRemove.remove("Crawl");
        }
        for (String name : toRemove) {
            remove(name);
        }
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Refresh", makeRefreshAction());
    }

    protected void init() {
        if (isRoot) {
            put("Edit", makeEditAction()).setTransient(true);
            put("Address", DSString.valueOf(address)).setReadOnly(true).setPrivate(true);
            put("Username", DSString.valueOf(clientProxy.username)).setReadOnly(true)
                                                                   .setPrivate(true);
            put("Password", DSString.valueOf(clientProxy.password)).setReadOnly(true)
                                                                   .setPrivate(true);
        }
        get();
    }

    @Override
    protected void onStable() {
        if (isRoot) {
            init();
            makeAddAddressAction();
        }
        if (address != null) {
            setupExtraActions();
        }
    }

    @Override
    protected void onStarted() {
        restoreClientProxy();
    }

    private void addAddress(DSMap parameters) {
        String name = parameters.getString("Name");
        String addr = parameters.getString("Address");
        WebApiNode n = new WebApiNode(addr, clientProxy);
        put(name, n);
        n.put("Manually Added", DSBool.TRUE).setReadOnly(true).setPrivate(true);
    }

    private ActionResult invokeMethod(WebApiMethod method, final DSAction action,
                                      DSMap parameters) {
        if (getClientProxy() == null) {
            return null;
        }
        Object body = null;
        if (method.getBodyParameterName() != null) {
            body = parameters.get(StringUtils.capitalize(method.getBodyParameterName())).toString();
        }
        Response r = getClientProxy().invoke(method.getType(), address, parameters, body);
        String s = r.readEntity(String.class);
        final List<DSIValue> values = Arrays.asList(DSString.valueOf(s));
        return new ActionValues() {

            @Override
            public ActionSpec getAction() {
                return action;
            }

            public int getColumnCount() {
                return values.size();
            }

            public void getMetadata(int col, DSMap bucket) {
                action.getColumnMetadata(col, bucket);
            }

            @Override
            public DSIValue getValue(int col) {
                return values.get(col);
            }

            @Override
            public void onClose() {
            }
        };
    }

    private void makeAddAddressAction() {
        DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                ((WebApiNode) target.get()).addAddress(invocation.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSValueType.STRING, null);
        act.addDefaultParameter("Address", DSString.valueOf(address), null);
        put("Add Address", act);
    }

    private DSAction makeEditAction() {
        DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                ((WebApiNode) target.get()).edit(invocation.getParameters());
                return null;
            }
        };
        act.addDefaultParameter("Address", DSString.valueOf(address), null);
        act.addDefaultParameter("Username", DSString.valueOf(clientProxy.username), null);
        act.addDefaultParameter("Password", DSString.valueOf(clientProxy.password), null)
           .setEditor("password");
        return act;
    }

    private DSAction makeRefreshAction() {
        DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                ((WebApiNode) target.get()).init();
                return null;
            }
        };
        return act;
    }

    private WebClientProxy restoreClientProxy() {
        if (isRoot == null) {
            isRoot = !(getParent() instanceof WebApiNode);
        }
        if (isRoot) {
            if (address == null) {
                DSIObject adr = get("Address");
                address = adr instanceof DSString ? ((DSString) adr).toString() : "";
            }
            if (clientProxy == null) {
                DSIObject usr = get("Username");
                DSIObject pass = get("Password");
                String username = usr instanceof DSString ? ((DSString) usr).toString() : null;
                String password = pass instanceof DSString ? ((DSString) pass).toString() : null;
                clientProxy = new WebClientProxy(address, username, password);
            }
        } else if (clientProxy == null) {
            clientProxy = ((WebApiNode) getParent()).restoreClientProxy();
        }
        return clientProxy;
    }

    private void setupExtraActions() {
        if (address == null) {
            return;
        }
        List<WebApiMethod> methods = WebApiMethod.find(getClientProxy().removeBase(address));
        for (final WebApiMethod method : methods) {
            DSAction act = new DSAction.Parameterless() {
                @Override
                public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                    return ((WebApiNode) target.get())
                            .invokeMethod(method, this, invocation.getParameters());
                }
            };
            for (UrlParameter param : method.getUrlParameters()) {
                Class<?> typeclass = param.getType();
                if (typeclass.equals(List.class)) {
                    act.addDefaultParameter(param.getName(), new DSList(), param.getDescription());
                } else {
                    DSValueType type;
                    if (typeclass.equals(Boolean.class)) {
                        type = DSValueType.BOOL;
                    } else if (typeclass.equals(Integer.class)) {
                        type = DSValueType.NUMBER;
                    } else {
                        type = DSValueType.STRING;
                    }
                    act.addParameter(StringUtils.capitalize(param.getName()), type,
                                     param.getDescription());
                }
            }
            if (method.getBodyParameterName() != null) {
                act.addDefaultParameter(StringUtils.capitalize(method.getBodyParameterName()),
                                        DSString.EMPTY, method.getBodyParameterDescription())
                   .setEditor("textarea");
            }
            act.setResultType(ResultType.VALUES);
            act.addColumnMetadata("Result", DSValueType.STRING).setEditor("textarea");
            put(method.getName(), act).setTransient(true);
        }
    }

    private void updateItems(DSList items, Set<String> oldNodesToRemove) {
        for (DSElement elem : items) {
            if (elem.isMap()) {
                DSMap item = elem.toMap();
                String name = item.getString("Name");
                DSMap links = item.getMap("Links");
                String selfLink = null;
                if (links != null) {
                    selfLink = links.getString("Self");
                }
                if (name != null && selfLink != null) {
                    DSNode node = getNode(name);
                    WebApiNode itemNode;
                    if (node instanceof WebApiNode) {
                        itemNode = (WebApiNode) node;
                        itemNode.setAddress(selfLink);
                    } else {
                        itemNode = new WebApiNode(selfLink, clientProxy);
                        put(name, itemNode);
                    }
                    oldNodesToRemove.remove(name);
                    itemNode.update(item);
                }
            }
        }
    }

    private void updateLinks(DSMap links, Set<String> oldNodesToRemove) {
        for (Entry e : links) {
            String key = e.getKey();
            DSElement value = e.getValue();
            if (!key.equals("Self")) {
                DSNode node = getNode(key);
                if (node instanceof WebApiNode) {
                    WebApiNode itemNode = (WebApiNode) node;
                    itemNode.setAddress(value.toString());
                } else {
                    put(key, new WebApiNode(value.toString(), clientProxy));
                }
                oldNodesToRemove.remove(key);
            }
        }
    }
}
