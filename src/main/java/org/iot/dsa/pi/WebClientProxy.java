package org.iot.dsa.pi;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.restadapter.CredentialProvider;

import java.util.HashSet;
import java.util.Set;

public class WebClientProxy extends org.iot.dsa.dslink.restadapter.WebClientProxy {

    String base;
    private boolean isPolling = false;
    private Set<WebApiNode> pollCache;
    private final Set<WebApiNode> pollNodes = new HashSet<>();
    private DSRuntime.Timer pollTimer;

    WebClientProxy(String base, CredentialProvider credentials, int readTimeout, int writeTimeout) {
        super(credentials, readTimeout, writeTimeout);
        this.base = base;
    }

    public String removeBase(String address) {
        if (address.startsWith(base)) {
            return address.substring(base.length());
        } else {
            return address;
        }
    }

    void poll() {
        synchronized (this) {
            if (isPolling) {
                return;
            }
        }
        try {
            isPolling = true;
            Set<WebApiNode> nodes = pollCache;
            if (nodes == null) {
                synchronized (pollNodes) {
                    if (pollNodes.isEmpty()) {
                        return;
                    }
                    nodes = new HashSet<>(pollNodes);
                    pollCache = nodes;
                }
            }
            for (WebApiNode node : nodes) {
                node.poll();
            }
        } finally {
            isPolling = false;
        }
    }

    synchronized void registerPoll(WebApiNode node) {
        synchronized (pollNodes) {
            if (pollNodes.add(node)) {
                pollCache = null;
                if (pollTimer == null) {
                    pollTimer = DSRuntime.run(this::poll, System.currentTimeMillis(), 30000);
                }
            }
        }
    }

    synchronized void unregisterPoll(WebApiNode node) {
        synchronized (pollNodes) {
            if (pollNodes.remove(node)) {
                pollCache = null;
                if (pollNodes.isEmpty()) {
                    if (pollTimer != null) {
                        pollTimer.cancel();
                        pollTimer = null;
                    }
                }
            }
        }
    }

}
