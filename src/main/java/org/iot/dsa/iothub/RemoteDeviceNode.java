package org.iot.dsa.iothub;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.iot.dsa.iothub.node.InvokeHandler;
import org.iot.dsa.iothub.node.MyDSActionNode;
import org.iot.dsa.iothub.node.MyDSActionNode.InboundInvokeRequestHandle;
import org.iot.dsa.iothub.node.MyValueType;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.security.DSPermission;

import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement;
import com.microsoft.azure.sdk.iot.service.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

public class RemoteDeviceNode extends RemovableNode {
	private String deviceId;
	private IotHubNode hubNode;

	public RemoteDeviceNode() {
	}
	
	public RemoteDeviceNode(IotHubNode hubNode, String deviceId) {
		this.deviceId = deviceId;
		this.hubNode = hubNode;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		makeInvokeDirectMethodAction(true);
		makeSendMessageAction(true);
	}

	private void makeSendMessageAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				sendC2DMessage(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Protocol", null, MyValueType.enumOf(Arrays.asList("AMQPS", "AMQPS_WS")), null, null);
		act.addParameter("Message", null, MyValueType.STRING, null, null);
//		act.setResultType(ResultType.VALUES);
//		act.addColumn("Feedback", DSValueType.STRING);
		addChild("Send_C2D_Message", act, onStart);
	}

	private void makeInvokeDirectMethodAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				return invokeDirectMethod(parameters);
			}
		});
		act.addParameter("Method_Name", null, MyValueType.STRING, null, null);
		act.addParameter("Response_Timeout", DSElement.make(30), null, "Response Timeout in Seconds", null);
		act.addParameter("Connect_Timeout", DSElement.make(5), null, "Connect Timeout in Seconds", null);
		act.setResultType(ResultType.VALUES);
		act.addColumn("Result_Status", DSValueType.NUMBER);
		act.addColumn("Result_Payload", DSValueType.STRING);
		addChild("Invoke_Direct_Method", act, onStart);
	}
	
	
	protected ActionResult invokeDirectMethod(DSMap parameters) {
		String methodName = parameters.getString("Method_Name");
		long responseTimeout = TimeUnit.SECONDS.toSeconds(parameters.getLong("Response_Timeout"));
		long connectTimeout = TimeUnit.SECONDS.toSeconds(parameters.getLong("Connect_Timeout"));
		DeviceMethod methodClient = hubNode.getMethodClient();
		if (methodClient == null) {
			warn("Method Client not initialized");
			//TODO send error response
			return new ActionResult() {};
		}

		try {
			System.out.println("Invoke reboot direct method");
			MethodResult result = methodClient.invoke(deviceId, methodName, responseTimeout, connectTimeout, null);

			if (result == null) {
				throw new IOException("Invoke direct method returned null");
			}
			Integer status = result.getStatus();
			DSIValue v1 = status != null ? DSElement.make(status) : DSInt.NULL;
			Object payload = result.getPayload();
			DSIValue v2 = payload != null ? DSElement.make(payload.toString()) : DSString.NULL;
			final List<DSIValue> vals = Arrays.asList(v1, v2);
			return new ActionValues() {
				@Override
				public Iterator<DSIValue> getValues() {
					return vals.iterator();
				}
			};
		} catch (IotHubException | IOException e) {
			warn("Error invoking direct method: " + e);
			//TODO send error response
			return new ActionResult() {};
		}
	}

	private void sendC2DMessage(DSMap parameters) {
		String protocolStr = parameters.getString("Protocol");
		IotHubServiceClientProtocol protocol = protocolStr.endsWith("WS") ? IotHubServiceClientProtocol.AMQPS_WS : IotHubServiceClientProtocol.AMQPS;
		String message = parameters.getString("Message");
		
		try {
			ServiceClient serviceClient = ServiceClient.createFromConnectionString(hubNode.getConnectionString(), protocol);
			if (serviceClient != null) {
				serviceClient.open();
				FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver();
				if (feedbackReceiver != null) {
					feedbackReceiver.open();
				}

				Message messageToSend = new Message(message);
				messageToSend.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);

				serviceClient.send(deviceId, messageToSend);

				FeedbackBatch feedbackBatch = feedbackReceiver.receive(10000);
				if (feedbackBatch != null) {
					info("Message feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc().toString());
				}

				if (feedbackReceiver != null) {
					feedbackReceiver.close();
				}
				serviceClient.close();
			}
		} catch (IOException | IotHubException | InterruptedException e) {
			// TODO send error response
			warn("Error sending cloud-to-device message: " + e);
		}
	}

}
