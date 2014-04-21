package com.hawktrack.katar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpClient {
  private static String URL_BASE = "http://queue.api.hawktrack.com/v1/queue/sms";
  private static String URL_NEXT = URL_BASE + "/next";

  private static String lastFetchTimestamp;
  
  private static AsyncHttpClient client = new AsyncHttpClient();
  
  public static void smsSent(RequestParams params, AsyncHttpResponseHandler responseHandler) {
	  client.post(URL_NEXT, params, responseHandler);
  }

  public static void incomingSms(String from, String sms) {}
  
  public static String getLastFetchTimestamp() {
	  return lastFetchTimestamp;
  }
}