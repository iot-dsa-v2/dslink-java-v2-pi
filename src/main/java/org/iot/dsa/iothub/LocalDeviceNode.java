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

import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.iothub.Util.MyValueType;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.DSAction;
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
	private DSInfo status;
	private DSInfo c2d;
	private DSList c2dList = new DSList();
	private DSNode methodsNode;

	private DeviceClient client;
	
	public LocalDeviceNode() {
	}
	
	public LocalDeviceNode(IotHubNode hubNode, String deviceId, IotHubClientProtocol protocol) {
		this.hubNode = hubNode;
		this.deviceId = deviceId;
		this.protocol = protocol;
	}
	
	@Override
	protected void declareDefaults() {
		super.declareDefaults();
		declareDefault("Methods", new DSNode());
		
		declareDefault("Send_D2C_Message", makeSendMessageAction());
		declareDefault("Upload_File", makeUploadFileAction());
		declareDefault("Add_Direct_Method", makeAddMethodAction());
		declareDefault("Refresh", makeRefreshAction());
	}
	
	@Override
	public void onStable() {		
		status = add("STATUS", DSString.valueOf("Connecting"));
		
		try {
			registerDeviceIdentity();
		} catch (Exception e) {
			warn("Error getting device identity", e);
			put(status, DSString.valueOf("Error getting device identity: " + e.getMessage()));
		}
		
		c2d = add("Cloud-To-Device_Messages", DSString.valueOf(c2dList.toString()));
		methodsNode = getNode("Methods");
				
		init();
	}

	private void init() {
		try {
			this.client = new DeviceClient(connectionString, protocol);
			MessageCallback callback = new C2DMessageCallback();
			client.setMessageCallback(callback, null);
			
			client.open();
			
			client.subscribeToDeviceMethod(new DirectMethodCallback(), null, new DirectMethodStatusCallback(), null);
		} catch (URISyntaxException | IOException e) {
			warn("Error initializing device client", e);
			put(status, DSString.valueOf("Error initializing device client: " + e.getMessage()));
		}
		put("Edit", makeEditAction());
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
	    put(status, DSString.valueOf(device.getStatus().toString()));
	}
	
	private DSAction makeRefreshAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((LocalDeviceNode) info.getParent()).init();
				return null;
			}
		};
		return act;
	}
	
	private DSAction makeEditAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((LocalDeviceNode) info.getParent()).edit(invocation.getParameters());
				return null;
			}
		};
		act.addParameter(Util.makeParameter("Protocol", DSElement.make(protocol.toString()), MyValueType.enumOf(IotHubNode.protocolEnum.getEnums()), null, null));
		return act;
	}
	
	protected void edit(DSMap parameters) {
		String protocolStr = parameters.getString("Protocol");
		protocol = IotHubClientProtocol.valueOf(protocolStr);
		init();
	}

	private DSAction makeAddMethodAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((LocalDeviceNode) info.getParent()).addDirectMethod(invocation.getParameters());
				return null;
			}
		};
		act.addParameter("Method_Name", DSString.NULL, null);
		return act;
	}

	private DSAction makeUploadFileAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				return ((LocalDeviceNode) info.getParent()).uploadFile(info, invocation.getParameters());
			}
		};
		act.addParameter("Name", DSString.NULL, null);
		act.addParameter("Filepath", DSString.EMPTY, null).setPlaceHolder("myImage.png");
		act.setResultType(ResultType.VALUES);
		act.addValueResult("Response_Status", DSValueType.STRING, null);
		return act;
	}

	private DSAction makeSendMessageAction() {
		DSAction act = new DSAction() {
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				return ((LocalDeviceNode) info.getParent()).sendD2CMessage(info, invocation.getParameters());
			}
		};
		act.addParameter("Message", DSString.NULL, null);
		act.addParameter("Properties", DSString.valueOf("{}"), null).setType(DSValueType.MAP);
		act.setResultType(ResultType.VALUES);
		act.addValueResult("Response_Status", DSValueType.STRING, null);
		return act;
	}
	
	
	private ActionResult sendD2CMessage(DSInfo actionInfo, DSMap parameters) {
		if (client == null) {
			throw new DSRequestException("Client not initialized");
		}
		final DSAction action = actionInfo.getAction();
		String msgStr = parameters.getString("Message");
		Message msg = new Message(msgStr);
		DSMap properties = parameters.getMap("Properties");
		for (int i = 0; i < properties.size(); i++) {
			Entry entry = properties.getEntry(i);
			msg.setProperty(entry.getKey(), entry.getValue().toString());
		}
		msg.setMessageId(java.util.UUID.randomUUID().toString()); 
		final List<DSIValue> lockobj = new ArrayList<DSIValue>();
		client.sendEventAsync(msg, new ResponseCallback(), lockobj);
		
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

				@Override
				public ActionSpec getAction() {
					return action;
				}

				@Override
				public void onClose() {					
				}
    		};
        }
	}

	private void addDirectMethod(DSMap parameters) {
		String methodName = parameters.getString("Method_Name");
		methodsNode.add(methodName, new DirectMethodNode(methodName));
	}
	
	private ActionResult uploadFile(DSInfo actionInfo, DSMap parameters) {
		if (client == null) {
			warn("Device Client not initialized");
			throw new DSRequestException("Client not initialized");
		}
		final DSAction action = actionInfo.getAction();
		String name = parameters.getString("Name");
		String path = parameters.getString("Filepath");
		File file = new File(path);
		final List<DSIValue> lockobj = new ArrayList<DSIValue>();
		try {
			InputStream inputStream = new FileInputStream(file);
			long streamLength = file.length();
			client.uploadToBlobAsync(name, inputStream, streamLength, new ResponseCallback(), lockobj);
		} catch (IllegalArgumentException | IOException e) {
			warn("Error uploading file", e);
			throw new DSRequestException(e.getMessage());
		}
		
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

				@Override
				public ActionSpec getAction() {
					return action;
				}

				@Override
				public void onClose() {					
				}
    		};
        }
		
	}
	
	
	private class ResponseCallback implements IotHubEventCallback {
		@SuppressWarnings("unchecked")
		@Override
		public void execute(IotHubStatusCode responseStatus, Object context) {
			DSIValue resp = responseStatus != null ? DSString.valueOf(responseStatus.toString()) : DSString.NULL;
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
			put(c2d, DSString.valueOf(c2dList.toString()));
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
}
