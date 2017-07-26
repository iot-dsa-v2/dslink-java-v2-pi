package org.iot.dsa.iothub;

import org.iot.dsa.iothub.node.InvokeHandler;
import org.iot.dsa.iothub.node.MyDSActionNode;
import org.iot.dsa.iothub.node.MyDSActionNode.InboundInvokeRequestHandle;
import org.iot.dsa.iothub.node.MyDSNode;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.security.DSPermission;


public class RemovableNode extends MyDSNode {
	
	@Override
	public void onStart() {
		makeRemoveAction(true);
	}

	protected void makeRemoveAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				delete();
				return new ActionResult() {};
			}
		});
		addChild("Remove", act, onStart);
	}
	
}
