package org.iot.dsa.servicebus;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec.ResultSpec;
import org.iot.dsa.security.DSPermission;
import org.iot.dsa.servicebus.node.MyDSActionNode;
import org.iot.dsa.servicebus.node.MyDSNode;
import org.iot.dsa.servicebus.node.MyDSActionNode.InboundInvokeRequestHandle;
import org.iot.dsa.servicebus.node.MyDSActionNode.InvokeHandler;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.QueueInfo;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;

public class QueueNode extends MyDSNode implements ReceiverNode {
	
	private QueueInfo info;
	private ServiceBusNode serviceNode;
	
	/**
	 * Do not use
	 */
	public QueueNode() {
		super();
		this.info = new QueueInfo();
//		this.serviceNode = (ServiceBusNode) getParent().getParent();
	}

	public QueueNode(QueueInfo info, ServiceBusNode serviceNode) {
		super();
		this.info = info;
		this.serviceNode = serviceNode;
	}
	
	@Override
	public void onStart() {
		makeSendAction(true);
		makeReadAction(true);
		makeDeleteAction(true);
	}
	
	private void makeSendAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				handleSend(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Message", null, DSValueType.STRING, null, null);
		act.addParameter("Properties", null, DSValueType.MAP, null, null);
		addChild("Send_Message", act, onStart);
	}
	
	private void makeReadAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new ReceiveHandler(this));
		act.addParameter("Use_Peek-Lock", DSElement.make(true), null, null, null);
		act.setResultSpec(ResultSpec.STREAM_TABLE);
		addChild("Recieve_Messages", act, onStart);
	}
	
	private void makeDeleteAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				handleDelete();
				return new ActionResult() {};
			}
    	});
		addChild("Delete", act, onStart);
	}
	
	
	private void handleSend(DSMap parameters) {
		String messageText = parameters.getString("Message");
		DSMap properties = parameters.getMap("Properties");
		BrokeredMessage message = new BrokeredMessage(messageText);
		for (int i = 0; i < properties.size(); i++) {
			Entry entry = properties.getEntry(i);
			message.setProperty(entry.getKey(), entry.getValue().toString());
		}
		try {
			serviceNode.getService().sendQueueMessage(info.getPath(), message);
		} catch (ServiceException e) {
			// TODO Send Error
			warn("Error Sending Message: " + e);
		}
	}
	
	private void handleDelete() {
		try {
			serviceNode.getService().deleteQueue(info.getPath());;
			delete();
		} catch (ServiceException e) {
			// TODO Send Error
			warn("Error Deleting Queue: " + e);
		}
	}
	
	@Override
	public BrokeredMessage receiveMessage(ReceiveMessageOptions opts) {
		ReceiveQueueMessageResult resultQM;
		try {
			resultQM = serviceNode.getService().receiveQueueMessage(info.getPath(), opts);
			return resultQM.getValue();
		} catch (ServiceException e) {
			warn("Error Receiving Message: " + e);
		}
		return null;
	}
	
	@Override
	public void deleteMessage(BrokeredMessage message) {
		try {
			serviceNode.getService().deleteMessage(message);
		} catch (ServiceException e) {
			warn("Error Deleting Message: " + e);
		}
	}
	
	@Override
	public void getMetaData(DSMap metaData) {
		super.getMetaData(metaData);
	}

}
