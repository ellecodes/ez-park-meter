/*
 * 2013 (c) Drexel University
 *
 * Author: Jeffrey Segall <js572@drexel.edu>
 * License: Proprietary
 * 
 */

package com.philly.ezpark;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;
import android.util.Log;
 
public class MessageController {
	
	public static final String TAKECONTROL_BACKEND_URL = "";
	
	private ClientConnectionManager cm;
	private HttpClient defaultClient;
	
	public MessageController() {
		BasicHttpParams params = new BasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		defaultClient = new DefaultHttpClient(cm, params);
	}
	
	private String buildUriFromPath(String... path){
		StringBuilder sb = new StringBuilder(TAKECONTROL_BACKEND_URL);
		for (String part : path){
			sb.append(part);
			sb.append('/');
		}
		sb.deleteCharAt(sb.length()-1);
		
		URI uri;
		try {
			uri = new URI(sb.toString());
			return uri.toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public JSONObject get(List<NameValuePair> params, Map<String, Object> jsonData, String... path){
		HttpResponse resp = null;
		JSONObject json = new JSONObject();
		if (params == null)
			params = new LinkedList<NameValuePair>();
 
		//		if (!token.equalsIgnoreCase(""))
		//			params.add(new BasicNameValuePair("access_token", token));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		String uri = buildUriFromPath(path);
		Log.e("GET", uri + "?" + paramString);
		HttpGet request;
		if (paramString.length() > 0){
			request = new HttpGet(uri + "?" + paramString);
		}
		else{
			request = new HttpGet(uri);
		}
		
		try {
			resp = defaultClient.execute(request);
			json = new JSONObject(EntityUtils.toString(resp.getEntity()));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject getArray(List<NameValuePair> params, Map<String, Object> jsonData, String... path){
		HttpResponse resp = null;
		JSONObject json = new JSONObject();
		if (params == null)
			params = new LinkedList<NameValuePair>();
 
		//		if (!token.equalsIgnoreCase(""))
		//			params.add(new BasicNameValuePair("access_token", token));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		String uri = buildUriFromPath(path);
		Log.e("GET", uri + "?" + paramString);
		HttpGet request;
		if (paramString.length() > 0){
			request = new HttpGet(uri + "?" + paramString);
		}
		else{
			request = new HttpGet(uri);
		}
		
		try {
			resp = defaultClient.execute(request);
			String result = "{ 'array' : "+EntityUtils.toString(resp.getEntity())+" }";
			json = new JSONObject(result);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject post(List<NameValuePair> params, Map<String, Object> jsonData, String... path){
		HttpResponse resp = null;
		JSONObject json = new JSONObject();
		
		if (params == null)
			params = new LinkedList<NameValuePair>();
		//TODO: Re-add authentication
//		if (token != "")
//			params.add(new BasicNameValuePair("access_token", token));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		String uri = buildUriFromPath(path);
		HttpPost request = new HttpPost(uri + paramString);
		
		try {
			resp = defaultClient.execute(request);
			json = new JSONObject(EntityUtils.toString(resp.getEntity()));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;		
	}
}