package com.example.android.wikisearch;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText txt;
    final String WIKI_URL_SEARCH = "https://en.wikipedia.org/w/api.php?action=query&list=search&utf8=&format=json&srsearch=";
    final String WIKI_SET_SEARCH = "https://en.wikipedia.org/w/api.php?action=query&prop=info&inprop=url&format=json&pageids=";
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = findViewById(R.id.req_text);
        result = findViewById(R.id.result);

        Button srch = findViewById(R.id.srch);

        srch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_req_data(txt.getText().toString());
            }
        });
    }

    public void search_req_data(String s){
        findData must = new findData();
        must.execute();
    }

    private class findData extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            String reqText = txt.getText().toString().replaceAll(" ","+");
            int integer = 0;
            BufferedReader br = null;
            HttpURLConnection connection = null;
            StringBuilder reqJSON = new StringBuilder();
            try {
                URL url = new URL(WIKI_URL_SEARCH+reqText);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.connect();
                if(connection.getResponseCode()==200) {
                    InputStream inx = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(inx, Charset.forName("UTF-8")));
                    String line = br.readLine();
                    while (line != null) {
                        reqJSON.append(line);
                        line = br.readLine();
                    }
                }
                integer = ConvertToSearch1(reqJSON.toString());
                reqJSON = new StringBuilder();
                url = new URL(WIKI_SET_SEARCH+integer);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                if(connection.getResponseCode()==200) {
                    InputStream inx = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(inx, Charset.forName("UTF-8")));
                    String line = br.readLine();
                    while (line != null) {
                        reqJSON.append(line);
                        line = br.readLine();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect();
                return ConvertToSearch2(reqJSON.toString(),integer);

            }
        }

        @Override
        protected void onPostExecute(String s) {
            result.setText(s);
        }
    }
    public int ConvertToSearch1(String JSONstring) {
        int s = 0;
        try {
            JSONObject reqJSON = new JSONObject(JSONstring);
            if (reqJSON.getString("batchcomplete").equals("  ")) {
                return 0;
            } else {
                JSONObject query = reqJSON.getJSONObject("query");
                JSONArray search = query.getJSONArray("search");
                JSONObject pageid = search.getJSONObject(0);
                s = pageid.getInt("pageid");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return s;
        }
    }
    public String ConvertToSearch2(String JSONstring,int integer) {
        String s = "";
        try {
            JSONObject reqJSON = new JSONObject(JSONstring);
            if (reqJSON.getString("batchcomplete").equals("  ")) {
                return " ";
            } else {
                JSONObject query = reqJSON.getJSONObject("query");
                JSONObject pages = query.getJSONObject("pages");
                JSONObject pageid_num = pages.getJSONObject(integer+"");
                s = pageid_num.getString("fullurl");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return s;
        }
    }
}

