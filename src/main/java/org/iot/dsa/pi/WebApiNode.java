package org.iot.dsa.pi;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.pi.node.RemovableNode;

public class WebApiNode extends RemovableNode {
	
	private String address;
	private WebClientProxy clientProxy;
	private boolean loaded = false;
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
	
	public WebClientProxy getClientProxy() {
        return clientProxy;
    }
	
	@Override
	protected void declareDefaults() {
		super.declareDefaults();
		declareDefault("Refresh", makeRefreshAction());
	}
	
	private DSAction makeRefreshAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((WebApiNode) info.getParent()).init();
				return null;
			}
		};
		return act;
	}
	
	private DSAction makeEditAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((WebApiNode) info.getParent()).edit(invocation.getParameters());
				return null;
			}
		};
    	act.addDefaultParameter("Address", DSString.valueOf(address), null);
    	act.addDefaultParameter("Username", DSString.valueOf(clientProxy.username), null);
    	act.addDefaultParameter("Password", DSString.valueOf(clientProxy.password), null).setEditor("password");
		return act;
	}
	
	public void edit(DSMap parameters) {
		address = parameters.getString("Address");
		clientProxy.username = parameters.getString("Username");
		clientProxy.password = parameters.getString("Password");
		init();
	}
	
	@Override
	protected void onStarted() {
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
				clientProxy = new WebClientProxy(username, password);
			}
		}
	}
	
	@Override
	protected void onStable() {
		if (isRoot) {
			init();
		} else if (clientProxy == null) {
		    DSNode parent = getParent();
		    if (parent instanceof WebApiNode) {
		        clientProxy = ((WebApiNode) parent).getClientProxy();
		    }
		}
	}
	
	protected void init() {
		if (isRoot) {
			put("Edit", makeEditAction()).setTransient(true);
			put("Address", DSString.valueOf(address)).setReadOnly(true).setHidden(true);
			put("Username", DSString.valueOf(clientProxy.username)).setReadOnly(true).setHidden(true);
			put("Password", DSString.valueOf(clientProxy.password)).setReadOnly(true).setHidden(true);
		}
		get();
	}

    public void get() {
		Response r = clientProxy.get(address);
		String s = r.readEntity(String.class);
		DSMap m = Util.parseJsonMap(s);
		update(m);
		loaded = true;
	}
	
	public void update(DSMap propMap) {
		Set<String> toRemove = new HashSet<String>();
		for (DSInfo info: this) {
			if (!info.isAction()) {
				String name = info.getName();
				if (!(name.equals("Address") || name.equals("Username") || name.equals("Password"))) {
					toRemove.add(name);
				}
			}
		}
		for (int i = 0; i < propMap.size(); i++) {
			Entry e = propMap.getEntry(i);
			String key = e.getKey();
			DSElement value = e.getValue();
			if (key.equals("Items") && value.isList()) {
				DSList items = value.toList();
				updateItems(items, toRemove);
			} else if (key.equals("Links") && value.isMap()) {
				DSMap links = value.toMap();
				updateLinks(links, toRemove);
			} else {
				put(key, value.copy());
				toRemove.remove(key);
			}
		}
		for (String name: toRemove) {
			remove(name);
		}
	}
	
	private void updateItems(DSList items, Set<String> oldNodesToRemove) {
		for (DSElement elem: items) {
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
						itemNode.setAddress(selfLink, false);
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
		for (int i = 0; i < links.size(); i++) {
			Entry e = links.getEntry(i);
			String key = e.getKey();
			DSElement value = e.getValue();
			if (!key.equals("Self")) {
				DSNode node = getNode(key);
				if (node instanceof WebApiNode) {
					WebApiNode itemNode = (WebApiNode) node;
					itemNode.setAddress(value.toString(), true);
				} else {
					put(key, new WebApiNode(value.toString(), clientProxy));
				}
				oldNodesToRemove.remove(key);
			}
		}
	}
	
	public void setAddress(String address, boolean refreshIfChanged) {
		boolean changed = !address.equals(this.address);
		this.address = address;
		if (changed && loaded && refreshIfChanged) {
			init();
		}
	}
}
