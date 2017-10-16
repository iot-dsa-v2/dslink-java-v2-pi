package org.iot.dsa.pi;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

public class WebClientProxy {
	
	String username;
	String password;
	
	WebClientProxy(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public Response get(String address) {
		WebClient client;
		if (username != null && password != null) {
			client = WebClient.create(address, username, password, null);
		} else {
			client =  WebClient.create(address);
		}
		client.accept(MediaType.APPLICATION_JSON);
		Response r = client.get();
		client.close();
		return r;
	}

}
