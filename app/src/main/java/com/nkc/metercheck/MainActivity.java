package com.nkc.metercheck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nkc.metercheck.helper.DatabaseHelper;
import com.nkc.metercheck.helper.SQLiteHandler;
import com.nkc.metercheck.helper.SessionManager;
import com.nkc.metercheck.model.Room;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private SQLiteHandler db;
    private SessionManager session;
    private List<Room> roomList = new ArrayList<Room>();
    private ListView listView;
    private SwipeMenuListView mListView;
    private CustomListAdapter adapter;
    private DatabaseHelper dbRoom;
    Context context;
    SharedPreferences sharedPreferences;
    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 50;
    enum Direction {LEFT, RIGHT;};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerMonth);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.months_titles, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        final int thisYear = Calendar.getInstance().get(Calendar.YEAR) + 543;
        spinner.setSelection(currentMonth);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                sharedPreferences.edit().putString(QuickstartPreferences.MONTHS, String.valueOf(position + 1)).apply();
                String terms = sharedPreferences.getString(QuickstartPreferences.TERMS, "1");
                String years = sharedPreferences.getString(QuickstartPreferences.YEARS, String.valueOf(thisYear));
                int month = position + 1;
                listRoom(month, Integer.valueOf(terms), years, "");
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // Set years
        final ArrayList<String> years = new ArrayList<String>();
        for (int i = thisYear - 3; i <= thisYear + 1; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);

        Spinner spinYear = (Spinner)findViewById(R.id.spinnerYear);
        spinYear.setAdapter(adapterYear);
        spinYear.setSelection(3);
        spinYear.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                String years = arg0.getItemAtPosition(position).toString();
                sharedPreferences.edit().putString(QuickstartPreferences.YEARS, years).apply();
                String months = sharedPreferences.getString(QuickstartPreferences.MONTHS, String.valueOf(currentMonth));
                String terms = sharedPreferences.getString(QuickstartPreferences.TERMS, "1");
                listRoom(Integer.valueOf(months), Integer.valueOf(terms), years, "");
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        ArrayList<String> terms = new ArrayList<String>();
        for (int i = 1; i<=3; i++){
            terms.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapterTerms = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, terms);

        Spinner spinTerm = (Spinner)findViewById(R.id.spinnerTerms);
        spinTerm.setAdapter(adapterTerms);
        String t = sharedPreferences.getString(QuickstartPreferences.TERMS, "1");
        spinTerm.setSelection(Integer.valueOf(t) - 1);
        spinTerm.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                String terms = arg0.getItemAtPosition(position).toString();
                sharedPreferences.edit().putString(QuickstartPreferences.TERMS, terms).apply();
                String months = sharedPreferences.getString(QuickstartPreferences.MONTHS, String.valueOf(currentMonth));
                String years = sharedPreferences.getString(QuickstartPreferences.YEARS, "1");
                listRoom(Integer.valueOf(months), Integer.valueOf(terms), years, "");
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        context = getApplicationContext();
        session = new SessionManager(context);
        if (!session.isLoggedIn()) {
            logoutUser();
        }

        db = new SQLiteHandler(context);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH)-1;
                EditText etSearch = (EditText) findViewById(R.id.etSearch);

                String months = sharedPreferences.getString(QuickstartPreferences.MONTHS, String.valueOf(currentMonth));
                String terms = sharedPreferences.getString(QuickstartPreferences.TERMS, "1");
                String years = sharedPreferences.getString(QuickstartPreferences.YEARS, String.valueOf(thisYear));
                listRoom(Integer.valueOf(months), Integer.valueOf(terms), years, etSearch.getText().toString());
            }
        });

    }

    public void listRoom(int month, int term, String year, String search){
        //listView = (ListView) findViewById(R.id.listView);
        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        adapter = new CustomListAdapter(this, roomList);
        //listView.setAdapter(adapter);
        mListView.setAdapter(adapter);
        dbRoom = new DatabaseHelper(context);

        roomList.clear();

        List<Room> row = dbRoom.getAllRooms(month, term, year, search);
        for (Room r : row){
            Room room = new Room();
            room.setRoomId(r.getRoomId());
            room.setMeterStart(r.getMeterStart());
            room.setMeterEnd(r.getMeterEnd());
            room.setMeterId(r.getMeterId());

            roomList.add(room);
        }

        adapter.notifyDataSetChanged();

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(255,99,71)));
                // set item width
                deleteItem.setWidth(dp2px(60));
                // set a icon
                deleteItem.setIcon(android.R.drawable.ic_menu_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                final Room item = roomList.get(position);
                switch (index) {
                    case 0:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Confirm")
                                .setMessage("Do you want to Delete this item?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            if(item.getMeterId() == 0){
                                                Toast.makeText(getApplicationContext(), "ไม่พบข้อมูล", Toast.LENGTH_SHORT).show();
                                            }else {
                                                // delete
                                                delete(item);
                                                roomList.remove(position);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(getApplicationContext(), "ลบข้อมูลห้อง " + item.getRoomId() + " เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                                            }
                                        }catch (Exception e){
                                            Toast.makeText(getApplicationContext(), "ไม่สามารถลบข้อมูลได้ โปรดลองอีกครั้ง", Toast.LENGTH_SHORT).show();
                                        }
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();

                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        mListView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });

        // other setting
//		listView.setCloseInterpolator(new BounceInterpolator());

        // test item long click
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Toast.makeText(getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void delete(Room item) {
        // delete app
//        try {
//            Intent intent = new Intent(Intent.ACTION_DELETE);
//            intent.setData(Uri.fromParts("package", item.packageName, null));
//            startActivity(intent);
//        } catch (Exception e) {
//        }
        dbRoom.deleteMeter(item.getMeterId());
        //Toast.makeText(getApplicationContext(), " Item ID: " + item.getMeterId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                //showDialog(R.string.result_succeeded, result.toString());
            } else {
                //showDialog(R.string.result_failed, getString(R.string.result_failed_why));
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int thisYear = Calendar.getInstance().get(Calendar.YEAR) + 543;
        String months = sharedPreferences.getString(QuickstartPreferences.MONTHS, String.valueOf(currentMonth));
        String terms = sharedPreferences.getString(QuickstartPreferences.TERMS, "1");
        String years = sharedPreferences.getString(QuickstartPreferences.YEARS, String.valueOf(thisYear));
        listRoom(Integer.valueOf(months), Integer.valueOf(terms), years, "");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_export) {
            Intent intent = new Intent(MainActivity.this, ExportActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sync) {
            Intent intent = new Intent(MainActivity.this, SyncActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout){
            logoutUser();
        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
