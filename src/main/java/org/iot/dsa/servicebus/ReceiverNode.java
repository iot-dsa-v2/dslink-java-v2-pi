package org.iot.dsa.servicebus;

import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;

public interface ReceiverNode {

	public BrokeredMessage receiveMessage(ReceiveMessageOptions opts);
	
	public void deleteMessage(BrokeredMessage message);
	
}
