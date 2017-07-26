package org.iot.dsa.iothub;

import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.iothub.node.InvokeHandler;
import org.iot.dsa.iothub.node.MainDSNode;
import org.iot.dsa.iothub.node.MyDSActionNode;
import org.iot.dsa.iothub.node.MyDSActionNode.InboundInvokeRequestHandle;
import org.iot.dsa.iothub.node.MyValueType;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.security.DSPermission;

public class Main extends MainDSNode {
    
    private void handleAddIotHub(DSMap parameters) {
    	String name = parameters.getString("Name");
    	String connString = parameters.getString("Connection_String");
    	
    	IotHubNode hub = new IotHubNode(connString);
    	addChild(name, hub, false);
    }
	
    @Override
    public void onStart() {
    	MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				handleAddIotHub(parameters);
				return new ActionResult() {};
			}
    	});
    	act.addParameter("Name", null, MyValueType.STRING, null, null);
    	act.addParameter("Connection_String", null, MyValueType.STRING, null, null);
    	addChild("Add_IoT_Hub", act, true);
    }
    
    public static void main(String[] args) throws Exception {
		DSLinkConfig cfg = new DSLinkConfig(args);
        DSLink link = new DSLink(cfg);
        link.start();
	}

}
