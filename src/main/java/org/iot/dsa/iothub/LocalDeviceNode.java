package org.iot.dsa.iothub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.iot.dsa.iothub.node.InvokeHandler;
import org.iot.dsa.iothub.node.MyDSActionNode;
import org.iot.dsa.iothub.node.MyDSNode;
import org.iot.dsa.iothub.node.MyDSValueNode;
import org.iot.dsa.iothub.node.MyValueType;
import org.iot.dsa.iothub.node.MyDSActionNode.InboundInvokeRequestHandle;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.security.DSPermission;

import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

public class LocalDeviceNode extends RemovableNode {
	private IotHubNode hubNode;
	private String deviceId;
	private String connectionString;
	private IotHubClientProtocol protocol;
	private MyDSValueNode statNode;
	private MyDSValueNode c2dNode;
	private DSList c2dList = new DSList();
	private MyDSNode methodsNode;

	private DeviceClient client;
	
	public LocalDeviceNode() {
	}
	
	public LocalDeviceNode(IotHubNode hubNode, String deviceId, IotHubClientProtocol protocol) {
		this.hubNode = hubNode;
		this.deviceId = deviceId;
		this.protocol = protocol;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		statNode = new MyDSValueNode();
		statNode.setValue(DSElement.make("Connecting"));
		addChild("STATUS", statNode, true);
		
		try {
			registerDeviceIdentity();
		} catch (Exception e) {
			warn("Error getting device identity", e);
			statNode.setValue(DSElement.make("Error getting device identity: " + e.getMessage()));
		}
		
		c2dNode = new MyDSValueNode();
		c2dNode.setValue(c2dList);
		addChild("Cloud-To-Device_Messages", c2dNode, true);
		
		methodsNode = new MyDSNode();
		addChild("Methods", methodsNode, true);
		
		makeSendMessageAction(true);
		makeUploadFileAction(true);
		makeAddMethodAction(true);
		
		init(true);
		makeRefreshAction(true);
	}

	private void init(boolean onStart) {
		try {
			this.client = new DeviceClient(connectionString, protocol);
			MessageCallback callback = new C2DMessageCallback();
			client.setMessageCallback(callback, null);
			
			client.open();
			
			client.subscribeToDeviceMethod(new DirectMethodCallback(), null, new DirectMethodStatusCallback(), null);
		} catch (URISyntaxException | IOException e) {
			warn("Error initializing device client", e);
			statNode.setValue(DSElement.make("Error initializing device client: " + e.getMessage()));
		}
		makeEditAction(onStart);
	}

	private void registerDeviceIdentity() throws IOException, JsonSyntaxException, IotHubException, IllegalArgumentException, NoSuchAlgorithmException {
		String hubConnStr = hubNode.getConnectionString();
		RegistryManager registryManager = RegistryManager.createFromConnectionString(hubConnStr);

		Device device = Device.createFromId(deviceId, null, null);
		try {
			device = registryManager.addDevice(device);
		} catch (IotHubException iote) {
			device = registryManager.getDevice(deviceId);
		}
		
		int idx = hubConnStr.indexOf("HostName=");
	    if (idx == -1) {
	    	throw new IOException("Connection String missing HostName");
	    }
	    String hostName = hubConnStr.substring(idx + 9);
	    idx = hostName.indexOf(';');
	    if (idx > -1) {
	    	hostName = hostName.substring(0, idx);
	    }
	    String deviceKey = device.getPrimaryKey();
	    connectionString = "HostName=" + hostName + ";DeviceId=" + deviceId + ";SharedAccessKey=" + deviceKey;
	    statNode.setValue(DSElement.make(device.getStatus().toString()));
	}
	
	private void makeRefreshAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				init(false);
				return new ActionResult() {};
			}
		});
		addChild("Refresh", act, onStart);
	}
	
	private void makeEditAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				edit(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Protocol", null, MyValueType.enumOf(IotHubNode.protocolEnum.getEnums()), null, null);
		addChild("Edit", act, onStart);
	}
	
	protected void edit(DSMap parameters) {
		String protocolStr = parameters.getString("Protocol");
		protocol = IotHubClientProtocol.valueOf(protocolStr);
		init(false);
	}

	private void makeAddMethodAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				addDirectMethod(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Method_Name", null, MyValueType.STRING, null, null);
		addChild("Add_Direct_Method", act, onStart);
	}

	private void makeUploadFileAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				uploadFile(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Name", null, MyValueType.STRING, null, null);
		act.addParameter("Filepath", null, MyValueType.STRING, null, "myImage.png");
		addChild("Upload_File", act, onStart);
	}

	private void makeSendMessageAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				sendD2CMessage(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Message", null, MyValueType.STRING, null, null);
		act.addParameter("Properties", new DSMap(), null, null, null);
		act.setResultType(ResultType.VALUES);
		act.addColumn("Response_Status", DSValueType.STRING);
		addChild("Send_D2C_Message", act, onStart);
	}
	
	
	private ActionResult sendD2CMessage(DSMap parameters) {
		if (client == null) {
			//TODO send error
			return new ActionResult() {};
		}
		String msgStr = parameters.getString("Message");
		Message msg = new Message(msgStr);
		DSMap properties = parameters.getMap("Properties");
		for (int i = 0; i < properties.size(); i++) {
			Entry entry = properties.getEntry(i);
			msg.setProperty(entry.getKey(), entry.getValue().toString());
		}
		msg.setMessageId(java.util.UUID.randomUUID().toString()); 
		final List<DSIValue> lockobj = new ArrayList<DSIValue>();
		client.sendEventAsync(msg, new D2CResponseCallback(), lockobj);
		
		synchronized (lockobj) {
        	try {
				lockobj.wait();
			} catch (InterruptedException e) {
			}
        	if (lockobj.isEmpty()) {
        		lockobj.add(DSString.NULL);
        	}
        	return new ActionValues() {
    			@Override
    			public Iterator<DSIValue> getValues() {
    				return lockobj.iterator();
    			}
    		};
        }
	}

	private void addDirectMethod(DSMap parameters) {
		String methodName = parameters.getString("Method_Name");
		methodsNode.addChild(methodName, new DirectMethodNode(methodName), false);
	}
	
	private void uploadFile(DSMap parameters) {
		if (client == null) {
			//TODO send error
			warn("Device Client not initialized");
		}
		String name = parameters.getString("Name");
		String path = parameters.getString("Filepath");
		File file = new File(path);
		try {
			InputStream inputStream = new FileInputStream(file);
			long streamLength = file.length();
			client.uploadToBlobAsync(name, inputStream, streamLength, new FileUploadStatusCallback(), null);
		} catch (IllegalArgumentException | IOException e) {
			// TODO send error
			warn("Error uploading file", e);
		}
	}
	
	
	private class D2CResponseCallback implements IotHubEventCallback {
		@SuppressWarnings("unchecked")
		@Override
		public void execute(IotHubStatusCode responseStatus, Object context) {
			DSIValue resp = responseStatus != null ? DSElement.make(responseStatus.toString()) : DSString.NULL;
			if (context != null) {
				synchronized (context) {
					if (context instanceof List<?>) {
						((List<DSIValue>) context).add(resp);
					}
					context.notify();
				}
			}
		}
	}
	
	private class C2DMessageCallback implements MessageCallback {
		@Override
		public IotHubMessageResult execute(Message message, Object callbackContext) {
			DSMap msgMap = new DSMap();
			String body = new String(message.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
			String id = message.getMessageId();
			String corrid = message.getCorrelationId();
			MessageType type = message.getMessageType();
			String typeStr = type != null ? type.toString() : null;
			msgMap.put("ID", id).put("Correlation_ID", corrid).put("Type", typeStr).put("Body", body);
			for (MessageProperty prop: message.getProperties()) {
				msgMap.put(prop.getName(), prop.getValue());
			}
			c2dList.add(msgMap);
			c2dNode.setValue(c2dList);
			return IotHubMessageResult.COMPLETE;
		}
	}
	
	private class DirectMethodCallback implements DeviceMethodCallback {
		@Override
		public DeviceMethodData call(String methodName, Object methodData, Object context) {
			DeviceMethodData deviceMethodData;
			DSIObject child = methodsNode.get(methodName);
			if (child instanceof DirectMethodNode) {
				deviceMethodData = ((DirectMethodNode) child).handle(methodData);
			} else {
				int status = DirectMethodNode.METHOD_NOT_DEFINED;
				deviceMethodData = new DeviceMethodData(status, "Method '" + methodName + "' not found");
			}
			return deviceMethodData;
		}
	}
	
	private class DirectMethodStatusCallback implements IotHubEventCallback {
		@Override
		public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
			info("IoT Hub responded to device method operation with status " + responseStatus.name());
		}
	}
	
	private class FileUploadStatusCallback implements IotHubEventCallback {
		@Override
		public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
			info("IoT Hub responded to file upload operation with status " + responseStatus.name());
		}
	}
}
