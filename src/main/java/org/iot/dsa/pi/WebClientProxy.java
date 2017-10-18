package org.iot.dsa.pi;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;

public class WebClientProxy {
	
    String base;
	String username;
	String password;
	
	WebClientProxy(String base, String username, String password) {
	    this.base = base;
		this.username = username;
		this.password = password;
	}
	
	public String removeBase(String address) {
	    if (address.startsWith(base)) {
	        return address.substring(base.length());
	    } else {
	        return address;
	    }
	}
	
	public Response get(String address, DSMap urlParameters) {
		WebClient client = prepareWebClient(address, urlParameters);
		Response r = client.get();
		client.close();
		return r;
	}
	
	public Response put(String address, DSMap urlParameters, Object body) {
	    WebClient client = prepareWebClient(address, urlParameters);
	    Response r = client.put(body);
	    client.close();
	    return r;
	}
	
	public Response post(String address, DSMap urlParameters, Object body) {
        WebClient client = prepareWebClient(address, urlParameters);
        Response r = client.post(body);
        client.close();
        return r;
    }
	
	public Response delete(String address, DSMap urlParameters) {
        WebClient client = prepareWebClient(address, urlParameters);
        Response r = client.delete();
        client.close();
        return r;
    }
	
	public Response patch(String address, DSMap urlParameters, Object body) {
        return invoke("PATCH", address, urlParameters, body);
    }
	
	public Response invoke(String httpMethod, String address, DSMap urlParameters, Object body) {
	    WebClient client = prepareWebClient(address, urlParameters);
        Response r = client.invoke(httpMethod, body);
        client.close();
        return r;
	}
	
	private WebClient prepareWebClient(String address, DSMap urlParameters) {
	    WebClient client;
        if (username != null && password != null) {
            client = WebClient.create(address, username, password, null);
        } else {
            client =  WebClient.create(address);
        }
        client.accept(MediaType.APPLICATION_JSON);
        for (int i = 0; i < urlParameters.size(); i++) {
            Entry entry = urlParameters.getEntry(i);
            Object value = Util.dsElementToObject(entry.getValue());
            client.query(entry.getKey(), value);
        }
        return client;
	}

}
