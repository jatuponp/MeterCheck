package com.nkc.metercheck;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.nkc.metercheck.helper.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SyncActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private DatabaseHelper db;
    private TextView txtStatus;
    private TextView txtStatus1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupActionBar();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        txtStatus = (TextView) findViewById(R.id.txtSyncStatus);
        txtStatus1 = (TextView) findViewById(R.id.txtSyncStatus1);

        Button btnSync = (Button) findViewById(R.id.button);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncDatabase();
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void syncDatabase() {
        pDialog.setMessage("Synchronize Database. Please wait...");
        showDialog();
        db = new DatabaseHelper(getApplicationContext());

        JsonArrayRequest roomReq = new JsonArrayRequest(Request.Method.POST, AppConfig.URL_GETROOM,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                String room_id = obj.getString("room_id");
                                Integer dorm_id = Integer.valueOf(obj.getString("dorm_id"));
                                Integer capacity = Integer.valueOf(obj.getString("capacity"));
                                Integer toilet = Integer.valueOf(obj.getString("toilet"));
                                String room_type = obj.getString("room_type");
                                Integer room_status = Integer.valueOf(obj.getString("room_status"));
                                db.createRoom(room_id, dorm_id, capacity,toilet,room_type, room_status);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        txtStatus.setText("Synchronize Table Room: OK.");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
                    }

                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to getInbox url
                Map<String, String> params = new HashMap<String, String>();
                //params.put(ARG_USERID, getArguments().getString(ARG_USERID));
                //params.put(ARG_STATUS, getArguments().getString(ARG_STATUS));
                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(roomReq, "getInbox");
        db.closeDB();
        hideDialog();
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
