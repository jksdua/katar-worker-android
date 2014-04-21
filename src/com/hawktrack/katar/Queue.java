package com.hawktrack.katar;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

public class Queue {
	public String id;
	public Worker worker;
	public KatarClient kc;

	private Timer timer = new Timer();
	
	protected int interval; // in seconds
	protected JSONObject customConfig;
	protected JSONObject configutation;
	
	public Queue(String id, KatarClient kc) throws JSONException {
		this.id = id;
		this.kc = kc;

		// get configuration
		getConfiguration(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject config) {
            	try {
					configutation = config;
					interval = config.getInt("interval") / 1000;
					customConfig = config.getJSONObject("custom");
            	} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
	}

	public void getConfiguration(JsonHttpResponseHandler responseHandler) throws JSONException {
		kc.httpClient.get(kc.configurationRoute(), responseHandler);
	}
	
	public void startPolling() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					next();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, interval);
	}
	
	public void stopPolling() {
		timer.cancel();
	}
	
	public void next(Task task) throws JSONException, UnsupportedEncodingException {
		JSONObject request = new JSONObject();

		// call stop polling as we dont want to receive multiple requests from the server concurrently
			// most likely the server will reject this request too since the client queue would already be full
		stopPolling();
		
		// this indicates that the client finished a task and is ready to submit its status
		if (task != null) {
			JSONArray tasks = new JSONArray();
			tasks.put(task.toJSON());
			request.put("tasks", tasks);
		}

		StringEntity entity = new StringEntity(request.toString(), "UTF-8");
		
		kc.httpClient.post(kc.getApplicationContext(), kc.queueRoute(id), entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
				try {
					// only one task is supported right now
					JSONArray tasks = response.getJSONArray("tasks");
	            	if (tasks.length() > 0) {
		            	JSONObject taskJson = tasks.getJSONObject(0);
		            	Task task = new Task(taskJson.getString("_id"), null, null);
		            	worker.onTask(task);
	            	} else {
	    	            // if task was received, we wait for it to finish before requesting more tasks
	    	            // otherwise, start polling again
	            		startPolling();
	            	}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
    	});
	}
	
	public void next() throws JSONException, UnsupportedEncodingException {
		next(null);
	}
	
	public class Task {
		public String _id = null;
		public String status = null;
		public String error = null;
		public Task(String _id, String status, String error) {
			this._id = _id;
			this.status = status;
			this.error = error;
		}
		public JSONObject toJSON() throws JSONException {
			JSONObject taskJson = new JSONObject();
			taskJson.put("_id", _id);

			if (status != null) {
				taskJson.put("status", status);
			}
				
			if (error != null) {
				taskJson.put("error", error);
			}
			
			return taskJson;
		}
	}
	
	public class Worker {
		private static final String LOG_TAG = "KatarClient:Worker";
		public Queue queue;
		
		public Worker(Queue queue) {
			this.queue = queue;
		}
		
		public void onTask(Task task) {
			Log.e(LOG_TAG, "Override me");
		}

		public void done(Task task) throws JSONException, UnsupportedEncodingException {
			queue.next(task);
		}

		public void next() throws JSONException, UnsupportedEncodingException {
			queue.next();
		}
	}
}
