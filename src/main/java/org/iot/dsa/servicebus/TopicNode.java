package org.iot.dsa.servicebus;

import java.util.List;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSObject;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.security.DSPermission;
import org.iot.dsa.servicebus.node.MyDSActionNode;
import org.iot.dsa.servicebus.node.MyDSNode;
import org.iot.dsa.servicebus.node.MyDSActionNode.InboundInvokeRequestHandle;
import org.iot.dsa.servicebus.node.MyDSActionNode.InvokeHandler;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.servicebus.models.TopicInfo;

public class TopicNode extends MyDSNode {
	
	private TopicInfo info;
	private ServiceBusNode serviceNode;

	/**
	 * Do not use
	 */
	public TopicNode() {
		super();
		this.info = new TopicInfo();
//		this.serviceNode = (ServiceBusNode) getParent().getParent();
	}
	
	public TopicNode(TopicInfo info, ServiceBusNode serviceNode) {
		super();
		this.info = info;
		this.serviceNode = serviceNode;
	}
	
	public ServiceBusContract getService() {
		return serviceNode.getService();
	}
	
	public String getTopicName() {
		return info.getPath();
	}
	
	@Override
	public void onStart() {
		makeSendAction(true);
		makeCreateSubscriptionAction(true);
		makeDeleteAction(true);
		
		init(true);
	}
	
	private void init(boolean onStart) {
		try {
			ListSubscriptionsResult result = getService().listSubscriptions(info.getPath());
			populateSubscriptions(result.getItems(), onStart);
		} catch (ServiceException e) {
			warn("Error listing subscriptions: " + e);
		}
	}
	
	private void populateSubscriptions(List<SubscriptionInfo> subscriptions, boolean onStart) {
		clearSubscriptions();
		for (SubscriptionInfo info: subscriptions) {
			addChild(info.getName(), new SubscriptionNode(info, this), onStart);
		}
	}
	
	private void clearSubscriptions() {
		for (int i=0; i< childCount(); i++) {
			DSObject obj = get(i);
			if (obj instanceof SubscriptionNode) {
				((SubscriptionNode) obj).delete();
			}
		}
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
	
	private void makeCreateSubscriptionAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				createSubscription(parameters);
				return new ActionResult() {};
			}
    	});
		act.addParameter("Name", null, DSValueType.STRING, null, null);
		addChild("Create_Subscription", act, onStart);
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
			getService().sendTopicMessage(info.getPath(), message);
		} catch (ServiceException e) {
			// TODO Send Error
			warn("Error Sending Message: " + e);
		}
	}
	
	private void createSubscription(DSMap parameters) {
		String name = parameters.getString("Name");
		SubscriptionInfo sinfo = new SubscriptionInfo(name);
		try {
			getService().createSubscription(info.getPath(), sinfo);
			addChild(sinfo.getName(), new SubscriptionNode(sinfo, this), false);
		} catch (ServiceException e) {
			// TODO Send Error
			warn("Error Creating Subscription: " + e);
		}
	}
	
	private void handleDelete() {
		try {
			getService().deleteTopic(info.getPath());
			delete();
		} catch (ServiceException e) {
			// TODO Send Error
			warn("Error Deleting Topic: " + e);
		}
	}
	

}
