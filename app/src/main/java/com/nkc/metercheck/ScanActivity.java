package com.nkc.metercheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nkc.metercheck.helper.DatabaseHelper;

import java.util.Calendar;

public class ScanActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    TextView txtMonth;
    EditText etRoomId, etMeter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setupActionBar();
        final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        final int thisYear = Calendar.getInstance().get(Calendar.YEAR) + 543;

        final String months = sharedPreferences.getString(QuickstartPreferences.MONTHS, String.valueOf(currentMonth));
        final String terms = sharedPreferences.getString(QuickstartPreferences.TERMS, "1");
        final String years = sharedPreferences.getString(QuickstartPreferences.YEARS, String.valueOf(thisYear));
        String[] mounthArray = getResources().getStringArray(R.array.months_titles);

        txtMonth = (TextView) findViewById(R.id.txtMonth);
        txtMonth.setText("ประจำเดือน" + mounthArray[Integer.valueOf(months) - 1] + "\nปีการศึกษา " + terms + "/" + years);

        Button btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(ScanActivity.this);
                integrator.addExtra("SCAN_WIDTH", 640);
                integrator.addExtra("SCAN_HEIGHT", 480);
                integrator.addExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");
                integrator.addExtra("SCAN_ORIENTATION", "PORTAIT");
                //customize the prompt message before scanning
                integrator.addExtra("PROMPT_MESSAGE", "Scanner Start!");
                integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
        });

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSave(Integer.valueOf(months), Integer.valueOf(terms), years);
            }
        });
    }

    public void doSave(Integer month, Integer term, String year) {
        db = new DatabaseHelper(getApplicationContext());

        etRoomId = (EditText) findViewById(R.id.room_id);
        etMeter = (EditText) findViewById(R.id.meter_end);

        String room_id = etRoomId.getText().toString();
        String meterEnd = etMeter.getText().toString();

        if (room_id.matches("") || meterEnd.matches("")) {
            Toast.makeText(this, "กรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_LONG).show();
        }else{
            //check local store meter
            int cnt = db.chkMeter(room_id, month, term, year);
            int cntRoom = db.checkRoom(room_id.toString());
            String meterStart = db.lastMeter(room_id, month, term, year);
            if (cntRoom != 0) {
                //ตรวจสอบว่ามีข้อมูลเดิมหรือไม่
                if (cnt == 0) {

                    //เลขมิเตอร์หลังต้องมากกว่าหรือเท่ากับ
                    if ( Float.valueOf(meterEnd) >= Float.valueOf(meterStart)) {
                        db.createMeter(room_id, month, term, year, meterStart, meterEnd, 0);

                        Toast.makeText(this, "บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ScanActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "ตรวจสอบตัวเลขให้ถูกต้อง", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "มีข้อมูลแล้ว", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "ไม่มีห้องนี้ในฐานข้อมูล", Toast.LENGTH_LONG).show();
            }
        }

        db.closeDB();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                etRoomId = (EditText) findViewById(R.id.room_id);
                etMeter = (EditText) findViewById(R.id.meter_end);
                etRoomId.setText(result.getContents().toString());
                etMeter.requestFocus();
                //showDialog(R.string.result_succeeded, result.toString());
            } else {
                //showDialog(R.string.result_failed, getString(R.string.result_failed_why));
            }
        }
    }

}
