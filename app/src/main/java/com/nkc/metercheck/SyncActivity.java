package com.nkc.metercheck;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SyncActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private TextView txtStatus;
    private TextView txtStatus1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        txtStatus = (TextView) findViewById(R.id.txtSyncStatus);
        txtStatus1 = (TextView) findViewById(R.id.txtSyncStatus1);

        Button btnSync = (Button) findViewById(R.id.button);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("aasldfasf", txtStatus.getText().toString());
                syncDatabase();
            }
        });
    }

    private void syncDatabase(){
        pDialog.setMessage("Synchronize Database. Please wait...");
        showDialog();

        txtStatus.setText("Synchronize Table Room: OK.");
        txtStatus1.setText("Synchronize Table Meter: OK.");

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
