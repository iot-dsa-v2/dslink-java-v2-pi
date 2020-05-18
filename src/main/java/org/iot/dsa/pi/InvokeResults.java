package org.iot.dsa.pi;

import java.util.HashSet;
import java.util.Set;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.dslink.AsyncActionResults;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.action.DSIActionRequest;
import org.iot.dsa.util.DSException;

public class InvokeResults implements AsyncActionResults, Runnable {

    private WebApiMethod method;
    private WebApiNode node;
    private DSIActionRequest req;
    private Response res;

    public InvokeResults(WebApiNode node, WebApiMethod method, DSIActionRequest req) {
        this.node = node;
        this.method = method;
        this.req = req;
        DSRuntime.run(this);
    }

    @Override
    public int getColumnCount() {
        return req.getAction().getColumnCount(req.getTargetInfo());
    }

    @Override
    public void getColumnMetadata(int idx, DSMap bucket) {
        req.getAction().getColumnMetadata(req.getTargetInfo(), idx, bucket);
    }

    @Override
    public void getResults(DSList bucket) {
        try {
            bucket.add(WebApiNode.getBodyFromResponse(res));
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        res = null;
        req.close();
    }

    @Override
    public ResultsType getResultsType() {
        return req.getAction().getResultsType();
    }

    @Override
    public boolean next() {
        return res != null;
    }

    public void run() {
        try {
            DSMap parameters = req.getParameters();
            Object body = null;
            if (method.getBodyParameterName() != null) {
                body = parameters.get(StringUtils.capitalize(method.getBodyParameterName()))
                                 .toString();
            }
            Set<String> nullkeys = new HashSet<String>();
            for (Entry entry : parameters) {
                if (entry.getValue().isString() && entry.getValue().toString().isEmpty()) {
                    nullkeys.add(entry.getKey());
                }
            }
            for (String key : nullkeys) {
                parameters.remove(key);
            }
            res = node.getClientProxy().invoke(
                    method.getType(), node.getAddress(), parameters, body);
            req.sendResults();
        } catch (Exception x) {
            req.close(x);
        }
    }
}
