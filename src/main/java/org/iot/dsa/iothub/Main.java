package org.iot.dsa.iothub;

import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSRootNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

public class Main extends DSRootNode {
    
    private void handleAddIotHub(DSMap parameters) {
    	String name = parameters.getString("Name");
    	String connString = parameters.getString("Connection_String");
    	
    	IotHubNode hub = new IotHubNode(connString);
    	add(name, hub);
    }
	
    @Override
    public void declareDefaults() {
    	DSAction act = new DSAction() {
			@Override
			 public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((Main) info.getParent()).handleAddIotHub(invocation.getParameters());
				return null;
			}
    	};
    	act.addParameter("Name", DSString.valueOf("DanielFreeHub"), null);
    	act.addParameter("Connection_String", DSString.valueOf("HostName=DanielFreeHub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=mBIqQQgZsYgvJ/la4G7KkHZMBzTX4pk3HvF2aabB/LU="), null);
    	declareDefault("Add_IoT_Hub", act);
    }
    
    public static void main(String[] args) throws Exception {
		DSLinkConfig cfg = new DSLinkConfig(args);
        DSLink link = new DSLink(cfg);
        link.run();
	}

}
