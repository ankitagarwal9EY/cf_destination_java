package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.cloudfoundry.identity.client.UaaContext;
import org.cloudfoundry.identity.client.UaaContextFactory;
import org.cloudfoundry.identity.client.token.GrantType;
import org.cloudfoundry.identity.client.token.TokenRequest;
import org.cloudfoundry.identity.uaa.oauth.token.CompositeAccessToken;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class Services
 */
public class Services extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * Default constructor. 
     */
    public Services() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	   try{
			 //reading environment variables
			 JSONObject jsonObj = new JSONObject(System.getenv("VCAP_SERVICES"));
			
			 //reading xsuaa
			 JSONArray jsonArr = jsonObj.getJSONArray("xsuaa");
			 JSONObject xsuaaCredentials =
			 jsonArr.getJSONObject(0).getJSONObject("credentials");
			 URI xsuaaUrl = new URI(xsuaaCredentials.getString("url"));
			
			 //reading destination
			 jsonArr = jsonObj.getJSONArray("destination");
			 JSONObject destinationCredentials =
			 jsonArr.getJSONObject(0).getJSONObject("credentials");
			 String clientid = destinationCredentials.getString("clientid");
			 String clientsecret =
			 destinationCredentials.getString("clientsecret");
			 String uri = destinationCredentials.getString("uri");
			
			 //fethcing destination JWT token
			 UaaContextFactory factory = UaaContextFactory.factory(xsuaaUrl);
			 TokenRequest tokenRequest = factory.tokenRequest();
			 tokenRequest.setGrantType(GrantType.CLIENT_CREDENTIALS);
			 tokenRequest.setClientId(clientid);
			 tokenRequest.setClientSecret(clientsecret);
			 UaaContext xsUaaContext = factory.authenticate(tokenRequest);
			 CompositeAccessToken jwtToken = xsUaaContext.getToken();
			
			 //calling destination instance to retrieve destination configuration
			// -- destination name - users
			 URL url = new
			 URL(uri+"/destination-configuration/v1/destinations/users");
			 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			 conn.setRequestProperty("Authorization","Bearer"+jwtToken.getValue());
			 conn.setRequestProperty("Content-Type","application/json");
			 conn.setRequestMethod("GET");
			
			
			 BufferedReader in = new BufferedReader(new
			 InputStreamReader(conn.getInputStream()));
			 String output;
			
			 StringBuffer res = new StringBuffer();
			 while ((output = in.readLine()) != null) {
			 res.append(output);
			 }
			
			 //reading url of destination
			 JSONObject jsonObj2 = new JSONObject(res.toString());
			 jsonObj2 = jsonObj2.getJSONObject("destinationConfiguration");
			 String url2 = jsonObj2.getString("URL");
			
			
			 //calling on prem endpoint /posts
			 URL url22 = new URL(url2);
			 HttpURLConnection conn2 = (HttpURLConnection) url22.openConnection();
			
			 conn2.setRequestProperty("Content-Type","application/json");
			 conn2.setRequestProperty("Accept", "application/json");
			 conn2.setRequestMethod("GET");
			
			
			 BufferedReader in2 = new BufferedReader(new
			 InputStreamReader(conn2.getInputStream()));
			 String output2 = "";
			
			 StringBuffer res2 = new StringBuffer();
			 while ((output2 = in2.readLine()) != null) {
			 res2.append(output2);
			 }
			
			 in.close();
			 response.getWriter().println(res2.toString());
			
			
			 }
			 catch(Exception e)
			 {
			 e.printStackTrace();
			 }


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
