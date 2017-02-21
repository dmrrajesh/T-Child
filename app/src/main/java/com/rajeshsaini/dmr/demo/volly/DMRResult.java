package com.rajeshsaini.dmr.demo.volly;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by rajesh on 8/10/2016.
 */
public interface DMRResult {
    void onSuccess(String jsonResponse);
    void onError(VolleyError volleyError);
}
