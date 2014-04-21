package com.hawktrack.katar;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class KatarClient extends Service {
	public String url;
	// default namespace - can be changed if needed
	public String namespace = "/v1";
	public String queueRouteNamespace = "/queue";
	// public http client so importing programs can use the http client for custom requests
	public AsyncHttpClient httpClient = new AsyncHttpClient();
	// list of queues returned by the server
	public ArrayList<Queue> queues = new ArrayList<Queue>();
	
	/**
	 * Instantiate a Katar client
	 * 
	 * @param url URL of the Katar HTTP Worker Server
	 * @throws JSONException
	 */
	public KatarClient(String url) throws JSONException {
		this.url = url;
	}
	
	/**
	 * Get URL for a particular queue
	 * 
	 * @param queue
	 * @return
	 */
	public String queueRoute(String queue) {
		return url + namespace + queueRouteNamespace + "/" + queue;
	}
	
	/**
	 * Get URL for fetching configuration
	 *
	 * @return
	 */
	public String configurationRoute() {
		return url + namespace + "/configuration";
	}

	public Queue queue(String id) throws JSONException {
		return new Queue(id, this);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}