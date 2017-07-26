package org.iot.dsa.iothub;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.iot.dsa.iothub.node.InvokeHandler;
import org.iot.dsa.iothub.node.MyColumn;
import org.iot.dsa.iothub.node.MyDSActionNode;
import org.iot.dsa.iothub.node.MyDSNode;
import org.iot.dsa.iothub.node.MyValueType;
import org.iot.dsa.iothub.node.MyDSActionNode.InboundInvokeRequestHandle;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSEnum;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionResultSpec;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionTable;
import org.iot.dsa.security.DSPermission;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.servicebus.ServiceBusException;

public class IotHubNode extends RemovableNode {
	
	static DSEnum protocolEnum = DSEnum.valueOf(IotHubClientProtocol.MQTT);
	
	private String connectionString;
	
	private MyDSNode localNode;
	private MyDSNode remoteNode;

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
	public void onStart() {
		super.onStart();
		localNode = new MyDSNode();
		addChild("Local", localNode, true);
		
		remoteNode = new MyDSNode();
		addChild("Remote", remoteNode, true);
		
		makeAddDeviceAction(true);
		makeReadMessagesAction(true);
		makeCreateDeviceAction(true);
		init(true);
	}
	
	private void init(boolean onStart) {
		createMethodClient();
		makeEditAction(onStart);
	}
	
	private void createMethodClient() {
		try {
			methodClient = DeviceMethod.createFromConnectionString(connectionString);
		} catch (IOException e) {
			warn("Error creating method client: " + e);
		}
	}
	
	private void makeEditAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				edit(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Connection_String", DSElement.make(connectionString), MyValueType.STRING, null, null);
		addChild("Edit", act, onStart);
	}

	private void makeCreateDeviceAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				createDevice(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Device_ID", null, MyValueType.STRING, null, null);
		act.addParameter("Protocol", null, MyValueType.enumOf(protocolEnum.getEnums()), null, null);
		addChild("Create_Local_Device", act, onStart);
	}

	private void makeReadMessagesAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				return readMessages(parameters, reqHandle);
			}
		});
		act.addParameter("EventHub_Compatible_Name", null, MyValueType.STRING, null, null);
		act.addParameter("EventHub_Compatible_Endpoint", null, MyValueType.STRING, null, null);
		act.addParameter("Partition_ID", null, MyValueType.STRING, null, null);
		act.setResultType(ResultType.STREAM_TABLE);
		addChild("Read_Messages", act, onStart);
	}

	private void makeAddDeviceAction(boolean onStart) {
		MyDSActionNode act = new MyDSActionNode(DSPermission.READ, new InvokeHandler() {
			@Override
			public ActionResult handle(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
				addDevice(parameters);
				return new ActionResult() {};
			}
		});
		act.addParameter("Device_ID", null, MyValueType.STRING, null, null);
		addChild("Add_Remote_Device", act, onStart);
	}
	
	
	private void edit(DSMap parameters) {
		connectionString = parameters.getString("Connection_String");
		init(false);
	}
	
	private void addDevice(DSMap parameters) {
		String id = parameters.getString("Device_ID");
		remoteNode.addChild(id, new RemoteDeviceNode(this, id), false);
	}
	
	private void createDevice(DSMap parameters) {
		String id = parameters.getString("Device_ID");
		String protocolStr = parameters.getString("Protocol");
		IotHubClientProtocol protocol = IotHubClientProtocol.valueOf(protocolStr);
		localNode.addChild(id, new LocalDeviceNode(this, id, protocol), false);
	}
	
	private ActionResult readMessages(DSMap parameters, InboundInvokeRequestHandle reqHandle) {
		String name = parameters.getString("EventHub_Compatible_Name");
		String endpt = parameters.getString("EventHub_Compatible_Endpoint");
		String connStr = endpt + ";EntityPath=" + name;
		String partitionId = parameters.getString("Partition_ID");
		
		EventHubClient client = null;
		try {
			client = EventHubClient.createFromConnectionStringSync(connStr);
			receiveMessages(client, partitionId, reqHandle);
		} catch(Exception e) {
			warn("Failed to create receiver: " + e.getMessage());
			//TODO send error response
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
		};
	}

	private void receiveMessages(final EventHubClient client, final String partitionId,
			final InboundInvokeRequestHandle reqHandle) throws ServiceBusException {
		client.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId, Instant.now())
				.thenAccept(new Consumer<PartitionReceiver>() {
					public void accept(PartitionReceiver receiver) {
						try {
							while (!reqHandle.isClosed()) {
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
										reqHandle.send(row);
									}
								}
							}
						} catch (Exception e) {
							warn("Failed to receive messages: " + e.getMessage());
							//TODO send error update?
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
