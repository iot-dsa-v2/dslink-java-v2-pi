package org.iot.dsa.pi;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.restadapter.CredentialProvider;
import org.iot.dsa.dslink.restadapter.Util.AUTH_SCHEME;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.iot.dsa.pi.WebApiMethod.UrlParameter;
import org.iot.dsa.util.DSException;

public class WebApiNode extends DSNode implements CredentialProvider {

    private static DSAction getUrlAction = makeGetUrlAction();
    private String address;
    private WebClientProxy clientProxy;
    private Boolean isRoot = null;
    private boolean newlyCreated;
    private String password;
    private String username;

    public WebApiNode() {
        this.newlyCreated = false;
    }

    public WebApiNode(String address, String username, String password) {
        this(address, null, true);
        this.username = username;
        this.password = password;
    }

    public WebApiNode(String address, WebClientProxy clientProxy) {
        this(address, clientProxy, false);
    }

    public WebApiNode(String address, WebClientProxy clientProxy, boolean isRoot) {
        this.address = address;
        this.clientProxy = clientProxy;
        this.isRoot = isRoot;
        this.newlyCreated = true;
    }

    public void edit(DSMap parameters) {
        address = parameters.getString("Address");
        username = parameters.getString("Username");
        password = parameters.getString("Password");
        init(true);
    }

    public void get(boolean shouldExpand) {
        Response r = getClientProxy().invoke("GET", address, new DSMap(), null);
        try {
            String s = getBodyFromResponse(r);
            DSMap m = Util.parseJsonMap(s);
            update(m, shouldExpand);
        } catch (Exception e) {
            warn("", e);
        }
    }

    @Override
    public AUTH_SCHEME getAuthScheme() {
        return AUTH_SCHEME.BASIC_USR_PASS;
    }

    public static String getBodyFromResponse(Response resp) throws IOException {
        try {
            return resp.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            if (resp != null) {
                resp.close();
            }
        }
    }

    @Override
    public String getClientId() {
        return null;
    }

    public WebClientProxy getClientProxy() {
        return clientProxy;
    }

    @Override
    public String getClientSecret() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getTokenURL() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setAddress(String address, boolean shouldExpand) {
        boolean changed = !address.equals(this.address);
        this.address = address;
        if (changed) {
            setupExtraActions();
            init(shouldExpand);
        }
    }

    public void update(DSMap propMap, boolean shouldExpand) {
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
                DSList remaining = updateItems(items, toRemove, shouldExpand);
                if (!remaining.isEmpty()) {
                    put(key, remaining).setReadOnly(true);
                }
            } else if (key.equals("Links") && value.isMap()) {
                DSMap links = value.toMap();
                updateLinks(links, toRemove, shouldExpand);
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
                itemNode.setAddress(crawlAddr, shouldExpand);
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
        declareDefault("Get URL", getUrlAction);
    }

    protected void init(boolean shouldExpand) {
        if (isRoot) {
            put("Edit", makeEditAction()).setTransient(true);
            put("Address", DSString.valueOf(address)).setReadOnly(true).setPrivate(true);
            put("Username", DSString.valueOf(username)).setReadOnly(true).setPrivate(true);
            put("Password", DSString.valueOf(password)).setReadOnly(true).setPrivate(true);
        }
        get(shouldExpand);
    }

    @Override
    protected void onStable() {
        super.onStable();
        if (isRoot) {
            init(newlyCreated);
            makeAddAddressAction();
        }
        if (address != null) {
            setupExtraActions();
        }
    }

    @Override
    protected void onStarted() {
        super.onStarted();
        restoreClientProxy();
    }

    private void addAddress(DSMap parameters) {
        String name = parameters.getString("Name");
        String addr = parameters.getString("Address");
        WebApiNode n = new WebApiNode(addr, clientProxy);
        put(name, n);
        n.put("Manually Added", DSBool.TRUE).setReadOnly(true).setPrivate(true);
    }

    private ActionResults invokeMethod(WebApiMethod method, final DSIActionRequest req) {
        DSMap parameters = req.getParameters();
        if (getClientProxy() == null) {
            return null;
        }
        Object body = null;
        if (method.getBodyParameterName() != null) {
            body = parameters.get(StringUtils.capitalize(method.getBodyParameterName())).toString();
        }

        Set<String> nullkeys = new HashSet<String>();
        for (Entry entry : parameters) {
            if (entry.getValue().isString() && entry.getValue().toString().isEmpty()) {
                nullkeys.add(entry.getKey());
            }
        }
        for (String key : nullkeys) {
            parameters.remove(key);
        }

        Response r = getClientProxy().invoke(method.getType(), address, parameters, body);
        try {
            String s = getBodyFromResponse(r);
            final List<DSIValue> values = Arrays.asList(DSString.valueOf(s));
            return DSIAction.toResults(req, DSString.valueOf(s));
        } catch (IOException e) {
            warn("", e);
            DSException.throwRuntime(e);
            return null;
        }
    }

    private void makeAddAddressAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                ((WebApiNode) req.getTarget()).addAddress(req.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSString.NULL, null);
        act.addDefaultParameter("Address", DSString.valueOf(address), null);
        put("Add Address", act);
    }

    private DSAction makeEditAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                ((WebApiNode) req.getTarget()).edit(req.getParameters());
                return null;
            }
        };
        act.addDefaultParameter("Address", DSString.valueOf(address), null);
        act.addDefaultParameter("Username", DSString.valueOf(username), null);
        act.addDefaultParameter("Password", DSString.valueOf(password), null).setEditor("password");
        return act;
    }

    private static DSAction makeGetUrlAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                String url = ((WebApiNode) req.getTarget()).address;
                return makeResults(req, DSString.valueOf(url));
            }
        };
        act.setResultsType(ResultsType.VALUES);
        act.addColumnMetadata("URL", DSString.NULL);
        return act;
    }

    private DSAction makeRefreshAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                ((WebApiNode) req.getTarget()).init(true);
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
            if (username == null) {
                DSIObject usr = get("Username");
                username = usr instanceof DSString ? ((DSString) usr).toString() : null;
            }
            if (password == null) {
                DSIObject pass = get("Password");
                password = pass instanceof DSString ? ((DSString) pass).toString() : null;
            }
            if (clientProxy == null) {
                clientProxy = new WebClientProxy(address, this);
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
            DSAction act = new DSAction() {
                @Override
                public ActionResults invoke(DSIActionRequest req) {
                    return ((WebApiNode) req.getTarget()).invokeMethod(method, req);
                }
            };
            for (UrlParameter param : method.getUrlParameters()) {
                Class<?> typeclass = param.getType();
                DSElement type;
                if (typeclass.equals(Boolean.class)) {
                    type = DSBool.NULL;
                } else if (typeclass.equals(Integer.class)) {
                    type = DSLong.NULL;
                } else {
                    type = DSString.NULL;
                }
                act.addParameter(StringUtils.capitalize(param.getName()), type,
                                 param.getDescription());
            }
            if (method.getBodyParameterName() != null) {
                act.addDefaultParameter(StringUtils.capitalize(method.getBodyParameterName()),
                                        DSString.EMPTY, method.getBodyParameterDescription())
                   .setEditor("textarea");
            }
            act.setResultsType(ResultsType.VALUES);
            act.addColumnMetadata("Result", DSString.NULL).setEditor("textarea");
            put(method.getName(), act).setTransient(true);
        }
    }

    private DSList updateItems(DSList items, Set<String> oldNodesToRemove, boolean shouldExpand) {
        DSList smallItems = new DSList();
        for (DSElement elem : items) {
            if (elem.isMap()) {
                DSMap item = elem.toMap();
                String name = item.getString("Name");
                DSMap links = item.getMap("Links");
                String selfLink = null;
                if (links != null) {
                    selfLink = links.getString("Self");
                    if (selfLink == null) {
                        selfLink = links.getString("Source");
                    }
                }
                if (name != null && selfLink != null) {
                    DSNode node = getNode(name);
                    WebApiNode itemNode = null;
                    if (node instanceof WebApiNode) {
                        itemNode = (WebApiNode) node;
                        itemNode.setAddress(selfLink, shouldExpand);
                    } else if (shouldExpand) {
                        itemNode = new WebApiNode(selfLink, clientProxy);
                        put(name, itemNode);
                    }
                    oldNodesToRemove.remove(name);
                    if (itemNode != null) {
                        itemNode.update(item, shouldExpand);
                    }
                } else {
                    smallItems.add(elem.copy());
                }
            } else {
                smallItems.add(elem.copy());
            }
        }
        return smallItems;
    }

    private void updateLinks(DSMap links, Set<String> oldNodesToRemove, boolean shouldExpand) {
        for (Entry e : links) {
            String key = e.getKey();
            DSElement value = e.getValue();
            if (!key.equals("Self") && !key.equals("Source")) {
                DSIObject node = get(key);
                if (node instanceof WebApiNode) {
                    WebApiNode itemNode = (WebApiNode) node;
                    itemNode.setAddress(value.toString(), shouldExpand);
                } else if (shouldExpand) {
                    put(key, new WebApiNode(value.toString(), clientProxy));
                }
                oldNodesToRemove.remove(key);
            }
        }
    }
}
