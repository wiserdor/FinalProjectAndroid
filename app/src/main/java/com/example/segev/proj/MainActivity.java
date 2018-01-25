package com.example.segev.proj;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.transitionseverywhere.AutoTransition;
import com.transitionseverywhere.ChangeText;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    ViewGroup transitionsContainer;
    TextView textView1;
    String url="http://52.214.218.83:3000/status/";
    private Context mContext;
    private String lastDate="";
    MediaPlayer mediaPlayer ;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        requestQueue = Volley.newRequestQueue(mContext);
        mediaPlayer=MediaPlayer.create(this, R.raw.audio);
        transitionsContainer = (ViewGroup) findViewById(R.id.transitions_container);
        textView1=(TextView) transitionsContainer.findViewById(R.id.textView1);

        textView1.setText(getString(R.string.waiting___));
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getJsonString();
            }
        },0,1000);
    }

    private void getJsonString(){
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String nameResponse=null;
                        String dateResponse=null;
                        String statusResponse=null;
                        try {
                            nameResponse=response.getJSONObject(0).getString("name");
                            dateResponse=response.getJSONObject(0).getString("Created_date");
                            statusResponse=response.getJSONObject(0).optJSONArray("status").getString(0);
                            Log.d("isit", "onResponse: "+statusResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(dateResponse!=null && nameResponse!=null
                                && !dateResponse.equals(lastDate) && !statusResponse.equals("completed")) {
                            lastDate=dateResponse;
                            String word="";
                            switch (nameResponse.toLowerCase()) {
                                case "start": //play music
                                    word=getString(R.string.start);
                                    if(!mediaPlayer.isPlaying())
                                        mediaPlayer.start();
                                    break;
                                case "stop": //stop music
                                    word=getString(R.string.stop);
                                    if(mediaPlayer.isPlaying())
                                        mediaPlayer.pause();

                                    break;
                                default:
                                    Log.e("error", "no such word");
                            }
                            TransitionManager.beginDelayedTransition(transitionsContainer,
                                    new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_IN));
                            textView1.setText(word);
                            postJsonRequest(response);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        requestQueue.add(jsObjRequest);
    }

    private void postJsonRequest(JSONArray json){
        JSONObject j=new JSONObject();
        try {
            j.put("name",json.getJSONObject(0).getString("name")).put("status",new JSONArray().put("completed"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                j,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("aaa", "onResponse: "+response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", "onErrorResponse: "+error);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", "My useragent");
                return headers;
            }
        };
        requestQueue.add(myRequest);
    }
}
