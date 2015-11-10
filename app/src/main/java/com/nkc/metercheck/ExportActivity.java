package com.nkc.metercheck;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.nkc.metercheck.model.Meter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportActivity extends AppCompatActivity {
    private static final String TAG = ExportActivity.class.getSimpleName();
    private DatabaseHelper dbRoom;
    private ProgressDialog pDialog;
    private TextView tvStatus;
    private Integer counter;
    Context context;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setupActionBar();
        if (isInternetConnection()) {
            dbRoom = new DatabaseHelper(this);
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(true);

            final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            final int thisYear = Calendar.getInstance().get(Calendar.YEAR) + 543;

            final String month = sharedPreferences.getString(QuickstartPreferences.MONTHS, String.valueOf(currentMonth));
            final String term = sharedPreferences.getString(QuickstartPreferences.TERMS, "1");
            final String year = sharedPreferences.getString(QuickstartPreferences.YEARS, String.valueOf(thisYear));
            String[] monthArray = getResources().getStringArray(R.array.months_titles);

            TextView txtCurrent = (TextView) findViewById(R.id.txtCurrent);
            txtCurrent.setText("ประจำเดือน" + monthArray[Integer.valueOf(month) - 1] + "\nปีการศึกษา " + term + "/" + year);

            Button btnExport = (Button) findViewById(R.id.btnExport);
            btnExport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //pDialog.setMessage("Uploading meter to server. Please wait...");
                    //showDialog();
                    // Fetching user details from sqlite
                    List<Meter> row = dbRoom.getAllMeter(Integer.valueOf(month), Integer.valueOf(term), year);
                    tvStatus = (TextView) findViewById(R.id.txtExportStatus);
                    tvStatus.setText("");
                    counter = 0;
                    for (Meter r : row) {
                        JSONObject params = new JSONObject();
                        try {
                            params.put("room_id", r.getRoomId());
                            params.put("month", String.valueOf(r.getMonths()));
                            params.put("term", String.valueOf(r.getTerms()));
                            params.put("year", r.getYears());
                            params.put("meter_start", r.getMeterStart());
                            params.put("meter_end", r.getMeterEnd());
                            params.put("pay_type", String.valueOf(r.getPayType()));
                            params.put("create_at", r.getCreate());
                        } catch (JSONException e) {
                            System.out.print(e.getMessage());
                        }

                        sendMeter(params);
                    }
                    //hideDialog();
                }
            });
        }else{
            showAlertNoNet();
        }

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

    private void sendMeter(JSONObject obj) {
        pDialog.setMessage("Uploading meter to server. Please wait...");
        showDialog();
        counter++;
        JsonArrayRequest meterReq = new JsonArrayRequest(Request.Method.POST, AppConfig.URL_SEND_METER,obj,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        tvStatus.setText("Upload to server: " + counter + " records.");

                        hideDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
                    }

                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to getInbox url
                Map<String, String> params = new HashMap<String, String>();
                //params.put("room_id", room_id);
                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(meterReq, "sendMeter");
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private boolean isInternetConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void showAlertNoNet() {
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);

        alertDlg.setMessage("ต้องทำการเชื่อมต่ออินเตอร์เน็ตก่อน...")
                .setTitle("แจ้งการใช้งาน")
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("ตั้งค่า",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO Auto-generated method stub
                                Intent gpsOptionsIntent = new Intent(
                                        android.provider.Settings.ACTION_SETTINGS);
                                startActivityForResult(gpsOptionsIntent, 0);
                            }
                        });
        alertDlg.setNegativeButton("ยกเลิก",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = alertDlg.create();
        alert.show();
    }
}
