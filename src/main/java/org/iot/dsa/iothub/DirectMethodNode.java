package org.iot.dsa.iothub;

import org.iot.dsa.node.DSNode;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;

public class DirectMethodNode extends DSNode {
	static final int METHOD_SUCCESS = 200;
	static final int METHOD_NOT_DEFINED = 404;
	static final int METHOD_NOT_IMPLEMENTED = 501;
	
	private String methodName;
	
	public DirectMethodNode() {
		this.methodName = "";
	}
	
	public DirectMethodNode(String methodName) {
		this.methodName = methodName;
	}

	public DeviceMethodData handle(Object methodData) {
		return new DeviceMethodData(METHOD_NOT_IMPLEMENTED, "Method '" + methodName + "' successfully invoked; however, it currently does nothing");
	}
	
}
