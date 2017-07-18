package org.iot.dsa.servicebus.node;

import org.iot.dsa.dslink.DSResponder;
import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.OutboundInvokeResponse;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.security.DSPermission;

public abstract class MainDSNode extends MyDSNode implements DSResponder {

	@Override
	public OutboundInvokeResponse onInvoke(InboundInvokeRequest req) {
		String path = req.getPath();
		return onInvoke(req, path);
	}
	
	@Override
	public void onSet(InboundSetRequest req) {
		String path = req.getPath();
		DSElement value = req.getValue();
		DSPermission permit = req.getPermission();
		onSet(path, value, permit);
	}

	@Override
    public OutboundListResponse onList(InboundListRequest req) {
        final String path = req.getPath();
        return onList(req, path);
    }

    @Override
    public SubscriptionCloseHandler onSubscribe(InboundSubscribeRequest req) {
        String path = req.getPath();
        return onSubscribe(req, path);
    }

}
