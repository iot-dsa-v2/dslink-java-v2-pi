package org.iot.dsa.iothub;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.iothub.Util.MyValueType;
import org.iot.dsa.iothub.Util.MyColumn;
import org.iot.dsa.node.DSEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionResultSpec;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionTable;
import org.iot.dsa.node.action.DSAction;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.servicebus.ServiceBusException;

public class IotHubNode extends RemovableNode {
	
	static DSEnum protocolEnum = DSEnum.valueOf(IotHubClientProtocol.MQTT);
	
	private String connectionString;
	
	private DSNode localNode;
	private DSNode remoteNode;

	private DeviceMethod methodClient;
	
	public IotHubNode() {
		//
	}

	public IotHubNode(String connectionString) {
		this.connectionString = connectionString;
	}
	
	public String getConnectionString() {
		return connectionString;
	}
	
	public DeviceMethod getMethodClient() {
		if (methodClient == null) {
			createMethodClient();
		}
		return methodClient;
	}
	
	@Override
	protected void declareDefaults() {
		super.declareDefaults();
		declareDefault("Local", new DSNode());
		declareDefault("Remote", new DSNode());
		
		declareDefault("Add_Remote_Device", makeAddDeviceAction());
		declareDefault("Read_Messages", makeReadMessagesAction());
		declareDefault("Create_Local_Device", makeCreateDeviceAction());
	}

	@Override
	public void onStable() {
		localNode = getNode("Local");
		remoteNode = getNode("Remote");
		init();
	}
	
	private void init() {
		createMethodClient();
		put("Edit", makeEditAction());
	}
	
	private void createMethodClient() {
		try {
			methodClient = DeviceMethod.createFromConnectionString(connectionString);
		} catch (IOException e) {
			warn("Error creating method client: " + e);
		}
	}
	
	private DSAction makeEditAction() {
		DSAction act = new DSAction() {
			@Override
			 public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((IotHubNode) info.getParent()).edit(invocation.getParameters());
				return null;
			}
		};
		act.addParameter("Connection_String", DSString.valueOf(connectionString), null);
		return act;
	}

	private DSAction makeCreateDeviceAction() {
		DSAction act = new DSAction() {
			@Override
			 public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((IotHubNode) info.getParent()).createDevice(invocation.getParameters());
				return null;
			}
		};
		act.addParameter("Device_ID", DSString.NULL, null);
		act.addParameter(Util.makeParameter("Protocol", null, MyValueType.enumOf(protocolEnum.getEnums()), null, null));
		return act;
	}

	private DSAction makeReadMessagesAction() {
		DSAction act = new DSAction() {
			@Override
			 public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				return ((IotHubNode) info.getParent()).readMessages(info, invocation);
			}
		};
		act.addParameter("EventHub_Compatible_Name", DSString.valueOf("iothub-ehub-danielfree-172452-a48c3b34bf"), null);
		act.addParameter("EventHub_Compatible_Endpoint", DSString.valueOf("Endpoint=sb://ihsuprodbyres053dednamespace.servicebus.windows.net/;SharedAccessKeyName=iothubowner;SharedAccessKey=mBIqQQgZsYgvJ/la4G7KkHZMBzTX4pk3HvF2aabB/LU="), null);
		act.addParameter("Partition_ID", DSString.EMPTY, null).setPlaceHolder("0");
		act.setResultType(ResultType.STREAM_TABLE);
		return act;
	}

	private DSAction makeAddDeviceAction() {
		DSAction act = new DSAction() {
			@Override
			 public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				((IotHubNode) info.getParent()).addDevice(invocation.getParameters());
				return null;
			}
		};
		act.addParameter("Device_ID", DSString.NULL, null);
		return act;
	}
	
	
	private void edit(DSMap parameters) {
		connectionString = parameters.getString("Connection_String");
		init();
	}
	
	private void addDevice(DSMap parameters) {
		String id = parameters.getString("Device_ID");
		remoteNode.add(id, new RemoteDeviceNode(this, id));
	}
	
	private void createDevice(DSMap parameters) {
		String id = parameters.getString("Device_ID");
		String protocolStr = parameters.getString("Protocol");
		IotHubClientProtocol protocol = IotHubClientProtocol.valueOf(protocolStr);
		localNode.add(id, new LocalDeviceNode(this, id, protocol));
	}
	
	private ActionResult readMessages(DSInfo actionInfo, ActionInvocation invocation) {
		final DSAction action = actionInfo.getAction();
		DSMap parameters = invocation.getParameters();
		String name = parameters.getString("EventHub_Compatible_Name");
		String endpt = parameters.getString("EventHub_Compatible_Endpoint");
		String connStr = endpt + ";EntityPath=" + name;
		String partitionId = parameters.getString("Partition_ID");
		
		EventHubClient client = null;
		try {
			client = EventHubClient.createFromConnectionStringSync(connStr);
			receiveMessages(client, partitionId, invocation);
		} catch(Exception e) {
			warn("Failed to create receiver: " + e.getMessage());
			throw new  DSRequestException(e.getMessage());
		}
		
		return new ActionTable() {
			private List<ActionResultSpec> cols;
			
			
			@Override
			public Iterator<DSList> getRows() {
				return new ArrayList<DSList>().iterator();
			}
			
			@Override
			public Iterator<ActionResultSpec> getColumns() {
				if (cols == null) {
					cols = new ArrayList<ActionResultSpec>();
					cols.add(new MyColumn("Offset", DSValueType.STRING));
					cols.add(new MyColumn("Sequence_Number", DSValueType.NUMBER));
					cols.add(new MyColumn("Enqueued_Time", DSValueType.STRING));
					cols.add(new MyColumn("Device_ID", DSValueType.STRING));
					cols.add(new MyColumn("Message_Payload", DSValueType.STRING));
				}
				return cols.iterator();
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

	private void receiveMessages(final EventHubClient client, final String partitionId,
			final ActionInvocation invocation) throws ServiceBusException {
		client.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId, Instant.now())
				.thenAccept(new Consumer<PartitionReceiver>() {
					public void accept(PartitionReceiver receiver) {
						try {
							while (invocation.isOpen()) {
								Iterable<EventData> receivedEvents = receiver.receive(100).get();
								if (receivedEvents != null) {
									for (EventData receivedEvent : receivedEvents) {
										String offset = receivedEvent.getSystemProperties().getOffset();
										long seqNo = receivedEvent.getSystemProperties().getSequenceNumber();
										Instant enqTime = receivedEvent.getSystemProperties().getEnqueuedTime();
										Object deviceId = receivedEvent.getSystemProperties().get("iothub-connection-device-id");
										String payload = new String(receivedEvent.getBytes(), Charset.defaultCharset());
										DSList row = new DSList().add(offset).add(seqNo).add(enqTime.toString());
										row.add(deviceId != null ? deviceId.toString() : null);
										row.add(payload);
										invocation.send(row);
									}
								}
							}
						} catch (Exception e) {
							warn("Failed to receive messages: " + e.getMessage());
							invocation.close(new DSRequestException(e.getMessage()));
						} finally {
							try {
								client.closeSync();
							} catch (ServiceBusException e) {
								warn("Failed to close Client: " + e.getMessage());
							}
						}
					}
				});
	}
}
