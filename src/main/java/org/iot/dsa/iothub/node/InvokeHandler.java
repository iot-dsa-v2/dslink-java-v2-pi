package org.iot.dsa.iothub.node;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.iothub.node.MyDSActionNode.InboundInvokeRequestHandle;

public interface InvokeHandler {
	public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle);
}
