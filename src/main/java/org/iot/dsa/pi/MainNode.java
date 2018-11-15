package org.iot.dsa.pi;

import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

public class MainNode extends DSMainNode {
		
    @Override
    protected void declareDefaults() {
    	super.declareDefaults();
    	
    	declareDefault("Add PI Endpoint", makeAddEndpointAction());
    }
    
    private static DSAction makeAddEndpointAction() {
    	DSAction act = new DSAction.Parameterless() {
    		public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
    			((MainNode) target.get()).addEndpoint(invocation.getParameters());
    			return null;
    		}
    	};
    	act.addParameter("Name", DSValueType.STRING, null);
    	act.addParameter("Address", DSValueType.STRING, null);
    	act.addParameter("Username", DSValueType.STRING, null);
    	act.addParameter("Password", DSValueType.STRING, null).setEditor("password");
    	return act;
    }
    
    private void addEndpoint(DSMap parameters) {
    	String name = parameters.getString("Name");
    	String addr = parameters.getString("Address");
    	String user = parameters.getString("Username");
    	String pass = parameters.getString("Password");
    	WebClientProxy clientProxy = new WebClientProxy(addr, user, pass);
    	WebApiNode n = new WebApiNode(addr, clientProxy, true);
    	put(name, n);
    }
}
