package org.iot.dsa.pi;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
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

public class InvokeStreamResults implements AsyncActionResults, Runnable {

    private static int STATE_RUNNING = 0;
    private static int STATE_RESULTS = 1;
    private static int STATE_CLOSING = 2;
    private static int STATE_CLOSED = 3;

    private Reader in;
    private JsonReader json;
    private WebApiMethod method;
    private WebApiNode node;
    private DSIActionRequest req;
    private int state = STATE_RUNNING;

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
            close();
        }
    }

    @Override
    public ResultsType getResultsType() {
        return req.getAction().getResultsType();
    }

    @Override
    public boolean next() {
        if ((state == STATE_RUNNING) || (state == STATE_CLOSED)) {
            return false;
        }
        if (state == STATE_RESULTS) {
            try {
                if (json.next() == Token.BEGIN_MAP) {
                    return true;
                }
            } catch (Exception x) {
                node.debug(x);
            }
        }
        close();
        return false;
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
            Response res = node.getClientProxy().invoke(
                    method.getType(), node.getAddress(), parameters, body);
            in = res.body().charStream();
            json = new JsonReader(in);
            readToItems();
            state = STATE_RESULTS;
            req.sendResults();
        } catch (Exception x) {
            node.error(x);
            state = STATE_CLOSING;
        }
    }

    private void close() {
        if (state == STATE_CLOSED) {
            return;
        }
        state = STATE_CLOSED;
        try {
            if (req != null) {
                req.close();
            }
            if (json != null) {
                if (json.last() == Token.END_INPUT) {
                    json.close();
                    in.close();
                }
            } else if (in != null) {
                //drain the input stream
                if (in.ready()) {
                    int ch = in.read();
                    while ((ch >= 0) && in.ready()) {
                        ch = in.read();
                    }
                    in.close();
                }
            }
        } catch (Exception x) {
            node.debug(x);
        } finally {
            in = null;
            json = null;
            req = null;
        }
    }

    private void readToItems() {
        Token token = json.next();
        if (token != Token.BEGIN_MAP) {
            close();
            return;
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
        state = STATE_CLOSING;
    }
}
