package org.iot.dsa.pi.node;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;


public class RemovableNode extends DSNode {

    public void delete() {
        getParent().remove(getName());
    }

    @Override
    protected void declareDefaults() {
        declareDefault("Remove", makeRemoveAction());
    }

    protected DSAction makeRemoveAction() {
        return new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                ((RemovableNode) target.get()).delete();
                return null;
            }
        };
    }

}
