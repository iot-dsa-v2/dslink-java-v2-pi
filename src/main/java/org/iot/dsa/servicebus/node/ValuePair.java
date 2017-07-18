package org.iot.dsa.servicebus.node;

import org.iot.dsa.node.DSElement;

public class ValuePair {
	
	private DSElement old, current;
	
	public ValuePair(DSElement old, DSElement current) {
		this.old = old;
		this.current = current;
	}

	public DSElement getOld() {
		return old;
	}

	public DSElement getCurrent() {
		return current;
	}
}
