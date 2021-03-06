/**
 * A HTTP plugin for Cordova / Phonegap
 */

package com.raccoonfink.CordovaHTTP;

import java.net.UnknownHostException;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLHandshakeException;

import android.util.Log;

import com.raccoonfink.CordovaHTTP.HttpRequest;
import com.raccoonfink.CordovaHTTP.HttpRequest.HttpRequestException;

public class CordovaHttpGetJson extends CordovaHttp implements Runnable {

    public CordovaHttpGetJson(String urlString, Map<?, ?> params, Map<String, String> headers, CallbackContext callbackContext) {
        super(urlString, params, headers, callbackContext);
    }

    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.get(this.getUrlString(), this.getParams(), true);
            this.setupSecurity(request);
            this.setupTimeouts(request);
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());
            int code = request.code();
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
            response.put("status", code);

            if (code >= 200 && code < 300) {
                if (body instanceof String && body.length() > 2) {
                  if (body.equals("null")) {
                    body = "{}";
                  }

                  response.put("data", new JSONObject(body));
                }
                else {
                  response.put("data", body);
                }

                this.getCallbackContext().success(response);
            } else {
                response.put("error", new JSONObject(body));
                this.getCallbackContext().error(response);
            }
        } catch (JSONException e) {
            this.respondWithError("There was an error generating the response");
        }  catch (HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(0, "The host could not be resolved");
            } else if (e.getCause() instanceof SSLHandshakeException) {
                this.respondWithError("SSL handshake failed");
            } else {
                this.respondWithError("There was an error with the request");
            }
        }
    }
}