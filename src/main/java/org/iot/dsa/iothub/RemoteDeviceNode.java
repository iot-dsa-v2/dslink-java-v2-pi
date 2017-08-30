package org.iot.dsa.iothub;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.iothub.Util.MyValueType;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.ActionSpec.ResultType;
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
	protected void declareDefaults() {
		super.declareDefaults();
		declareDefault("Invoke_Direct_Method", makeInvokeDirectMethodAction());
		declareDefault("Send_C2D_Message", makeSendMessageAction());
	}

	@Override
	protected void onStable() {
		if (hubNode == null) {
			DSNode n = getParent().getParent();
			if (n instanceof IotHubNode) {
				hubNode = (IotHubNode) n;
			}
		}
		if (deviceId == null) {
			deviceId = getName();
		}
	}
	
	private DSAction makeSendMessageAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((RemoteDeviceNode) info.getParent()).sendC2DMessage(invocation.getParameters());
				return null;
			}
		};
		act.addParameter(Util.makeParameter("Protocol", null, MyValueType.enumOf(Arrays.asList("AMQPS", "AMQPS_WS")), null, null));
		act.addParameter("Message", DSString.NULL, null);
//		act.setResultType(ResultType.VALUES);
//		act.addColumn("Feedback", DSValueType.STRING);
		return act;
	}

	private DSAction makeInvokeDirectMethodAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				return ((RemoteDeviceNode) info.getParent()).invokeDirectMethod(info, invocation.getParameters());
			}
		};
		act.addParameter("Method_Name", DSString.NULL, null);
		act.addParameter("Response_Timeout", DSInt.valueOf(30), "Response Timeout in Seconds");
		act.addParameter("Connect_Timeout", DSInt.valueOf(5), "Connect Timeout in Seconds");
		act.addParameter("Payload", DSString.EMPTY, "Payload of direct method invocation");
		act.setResultType(ResultType.VALUES);
		act.addValueResult("Result_Status", DSValueType.NUMBER, null);
		act.addValueResult("Result_Payload", DSValueType.STRING, null);
		return act;
	}
	
	
	protected ActionResult invokeDirectMethod(DSInfo actionInfo, DSMap parameters) {
		final DSAction action = actionInfo.getAction();
		String methodName = parameters.getString("Method_Name");
		long responseTimeout = TimeUnit.SECONDS.toSeconds(parameters.getLong("Response_Timeout"));
		long connectTimeout = TimeUnit.SECONDS.toSeconds(parameters.getLong("Connect_Timeout"));
		String invPayload = parameters.getString("Payload");
		DeviceMethod methodClient = hubNode.getMethodClient();
		if (methodClient == null) {
			warn("Method Client not initialized");
			throw new DSRequestException("Method Client not initialized");
		}

		try {
			MethodResult result = methodClient.invoke(deviceId, methodName, responseTimeout, connectTimeout, invPayload);

			if (result == null) {
				throw new IOException("Invoke direct method returned null");
			}
			Integer status = result.getStatus();
			DSIValue v1 = status != null ? DSInt.valueOf(status) : DSInt.NULL;
			Object payload = result.getPayload();
			DSIValue v2 = payload != null ? DSString.valueOf(payload.toString()) : DSString.NULL;
			final List<DSIValue> vals = Arrays.asList(v1, v2);
			return new ActionValues() {
				@Override
				public Iterator<DSIValue> getValues() {
					return vals.iterator();
				}

				@Override
				public ActionSpec getAction() {
					return action;
				}

				@Override
				public void onClose() {
				}
			};
		} catch (IotHubException | IOException e) {
			warn("Error invoking direct method: " + e);
			throw new DSRequestException(e.getMessage());
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
			warn("Error sending cloud-to-device message: " + e);
			throw new DSRequestException(e.getMessage());
		}
	}

}
