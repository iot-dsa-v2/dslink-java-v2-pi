package org.iot.dsa.pi;

import org.iot.dsa.dslink.restadapter.CredentialProvider;

public class WebClientProxy extends org.iot.dsa.dslink.restadapter.WebClientProxy {
	
    String base;
	
	WebClientProxy(String base, CredentialProvider credentials) {
	    super(credentials);
	    this.base = base;
	}
	
	public String removeBase(String address) {
	    if (address.startsWith(base)) {
	        return address.substring(base.length());
	    } else {
	        return address;
	    }
	}
	
	
	
//	public Response get(String address, DSMap urlParameters) {
//		WebClient client = prepareWebClient(address, urlParameters);
//		Response r = client.get();
//		client.close();
//		return r;
//	}
//	
//	public Response put(String address, DSMap urlParameters, Object body) {
//	    WebClient client = prepareWebClient(address, urlParameters);
//	    Response r = client.put(body);
//	    client.close();
//	    return r;
//	}
//	
//	public Response post(String address, DSMap urlParameters, Object body) {
//        WebClient client = prepareWebClient(address, urlParameters);
//        Response r = client.post(body);
//        client.close();
//        return r;
//    }
//	
//	public Response delete(String address, DSMap urlParameters) {
//        WebClient client = prepareWebClient(address, urlParameters);
//        Response r = client.delete();
//        client.close();
//        return r;
//    }
//	
//	public Response patch(String address, DSMap urlParameters, Object body) {
//        return invoke("PATCH", address, urlParameters, body);
//    }
}
