package org.iot.dsa.pi;

import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.dslink.AsyncActionResults;
import org.iot.dsa.io.DSIReader.Token;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.action.DSIActionRequest;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class InvokeStreamResults implements AsyncActionResults, Runnable {

    private Reader in;
    private JsonReader json;
    private WebApiMethod method;
    private WebApiNode node;
    private DSIActionRequest req;
    private Response res;

    public InvokeStreamResults(WebApiNode node, WebApiMethod method, DSIActionRequest req) {
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
            bucket.add(json.getMap());
        } catch (Exception x) {
            node.debug(x);
            req.close(x);
        }
    }

    @Override
    public ResultsType getResultsType() {
        return req.getAction().getResultsType();
    }

    @Override
    public boolean next() {
        try {
            if (json.next() == Token.BEGIN_MAP) {
                return true;
            }
        } catch (Exception x) {
            node.debug(x);
            req.close(x);
        }
        return false;
    }

    @Override
    public void onClose() {
        if (json != null) {
            try {
                json.close();
            } catch (Exception ignore) {
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (Exception ignore) {
            }
        }
        if (res != null) {
            try {
                res.close();
            } catch (Exception ignore) {
            }
        }
        in = null;
        json = null;
        req = null;
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
            in = res.body().charStream();
            json = new JsonReader(in);
            readToItems();
            req.sendResults();
        } catch (Exception x) {
            node.error(x);
            req.close(x);
        }
    }

    private void readToItems() {
        Token token = json.next();
        if (token != Token.BEGIN_MAP) {
            throw new IllegalStateException("Invalid json");
        }
        token = json.next();
        String key;
        while (token != Token.END_INPUT) {
            if (token == Token.STRING) {
                key = json.getString();
                if ("Items".equals(key)) {
                    if (json.next() == Token.BEGIN_LIST) {
                        return;
                    }
                    break;
                } else {
                    json.next();
                    json.getElement();
                }
            } else {
                break;
            }
            token = json.next();
        }
    }
}
