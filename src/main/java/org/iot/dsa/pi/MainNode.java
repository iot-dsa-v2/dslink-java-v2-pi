package org.iot.dsa.pi;

import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

public class MainNode extends DSMainNode {

    @Override
    protected void declareDefaults() {
        super.declareDefaults();

        declareDefault("Add PI Endpoint", makeAddEndpointAction());
    }

    private void addEndpoint(DSMap parameters) {
        String name = parameters.getString("Name");
        String addr = parameters.getString("Address");
        String user = parameters.getString("Username");
        String pass = parameters.getString("Password");
        WebApiNode n = new WebApiNode(addr, user, pass);
        put(name, n);
    }

    private static DSAction makeAddEndpointAction() {
        DSAction act = new DSAction() {
            public ActionResults invoke(DSIActionRequest req) {
                ((MainNode) req.getTarget()).addEndpoint(req.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSString.NULL, null);
        act.addParameter("Address", DSString.NULL, null);
        act.addParameter("Username", DSString.NULL, null);
        act.addParameter("Password", DSString.NULL, null).setEditor("password");
        return act;
    }
}
