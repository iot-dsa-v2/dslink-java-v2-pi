package org.iot.dsa.iothub;

import java.util.Iterator;

import org.iot.dsa.dslink.DSRequesterInterface;
import org.iot.dsa.dslink.requester.InboundInvokeResponse;
import org.iot.dsa.dslink.requester.OutboundInvokeRequest;
import org.iot.dsa.dslink.requester.OutboundRequest;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.security.DSPermission;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;

public class DirectMethodNode extends RemovableNode {
	static final int METHOD_SUCCESS = 200;
	static final int METHOD_NOT_DEFINED = 404;
	static final int METHOD_FAILED = 500;
	static final int METHOD_NOT_IMPLEMENTED = 501;
	
	private String methodName;
	private String path;
	
	public DirectMethodNode() {
	}
	
	public DirectMethodNode(String methodName, String path) {
		this.methodName = methodName;
		this.path = path;
	}
	
	@Override
	protected void onStable() {
		if (methodName == null) {
			methodName = getName();
		}
		if (path == null) {
			DSIObject p = get("Path");
			path = p instanceof DSString ? p.toString() : "";
		} else {
			put("Path", DSString.valueOf(path)).setReadOnly(true);
		}
	}

	public DeviceMethodData handle(Object methodData) {
		
		if (!path.isEmpty()) { 
			DSMap params = null;
			if (methodData != null) {
				JsonReader reader = null;
				try {
					reader = new JsonReader(new String((byte[]) methodData));
					String s = reader.getElement().toString();
					reader.close();
					reader = new JsonReader(s);
					params = reader.getMap();
				} catch (Exception e) {
				} finally {
					if (reader != null) { 
						reader.close();
					}
				}
			}
			final DSMap parameters = (params != null) ? params : null;
			final DSList results = new DSList();
			final String thepath = path;
			OutboundRequest req = new OutboundInvokeRequest() {
				
				@Override
				public boolean onResponse(InboundInvokeResponse response) {
					Iterator<DSList> iter = response.getRows();
					synchronized(results) {
						while(iter.hasNext()) {
							results.add(iter.next().copy());
						}
						results.notifyAll();
					}
					return false;
				}
				
				@Override
				public DSPermission getPermission() {
					return null;
				}
				
				@Override
				public String getPath() {
					return thepath;
				}
				
				@Override
				public DSMap getParameters() {
					// TODO Auto-generated method stub
					return parameters;
				}
			};
			try {
				DSRequesterInterface session = Main.getRequesterSession();
				session.sendRequest(req);
			} catch (Exception e) {
				return new DeviceMethodData(METHOD_FAILED, e.getMessage());
			}
			
			synchronized(results) {
				try {
					results.wait();
				} catch (InterruptedException e) {
				}
			}
			return new DeviceMethodData(METHOD_SUCCESS, results.toString());
		} else {
			return new DeviceMethodData(METHOD_NOT_IMPLEMENTED, "Method '" + methodName + "' successfully invoked; however, it currently does nothing");
		}
	}
	
}
