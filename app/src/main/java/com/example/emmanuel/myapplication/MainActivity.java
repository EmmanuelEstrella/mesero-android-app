package com.example.emmanuel.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;

import java.util.Collection;

import com.example.emmanuel.myapplication.Logic.DataManager;
import com.example.emmanuel.myapplication.Logic.Order;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, CompoundButton.OnCheckedChangeListener, OrderSentListener{

    private final String TAG = "EmmanuelBeacons";
    private BeaconManager beaconManager;
    final Region region = new Region(TAG, Identifier.parse("5fd85e4c-8bd1-11e6-ae22-56b6b6499611"), null, null);
    final int rssiMin = -80;
    private int activeTable = 0;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private int REQUEST_ENABLE_BT =89;

    MainActivity instance;

    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private LinearLayout noBeaconMsg;
    private LinearLayout menuLy;
    private TextView tableTextView;


    private MaterialDialog successDialog;
    private MaterialDialog emptyOrderDialog;
    private MaterialDialog connectionErrorDialog;
    private MaterialDialog orderProgressDialog;



    private Button orderButton;

    private  BroadcastReceiver mReceiver;

    //test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuRecyclerView = findViewById(R.id.menu_recycler);
        noBeaconMsg = findViewById(R.id.no_beacons_msg_ly);
        menuLy = findViewById(R.id.menu_ly);
        tableTextView = findViewById(R.id.active_table);
        noBeaconMsg.setVisibility(View.VISIBLE);
        menuLy.setVisibility(View.GONE);
        orderButton = findViewById(R.id.order_button);

        instance = this;

        //Ask for permissions for android M+ devices
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This App needs location Access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok,null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener(){
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();



            }

        }

        //verify that bluetooth is on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }
        }


        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        if(mBluetoothAdapter.isEnabled())
            beaconManager.bind(instance);



        DataManager.getInstance().initClient(this);
        DataManager.getInstance().getMenuItems();
        LinearLayoutManager  mLayoutManager = new LinearLayoutManager(this);
        menuRecyclerView.setLayoutManager(mLayoutManager);
        menuAdapter = new MenuAdapter();
        DataManager.getInstance().setMenuItemsListeners(menuAdapter);
        DataManager.getInstance().setOrderSentListener(this);
        menuRecyclerView.setAdapter(menuAdapter);

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeTable != 0 && !DataManager.getInstance().isOrderEmpty() ){
                    Log.d("CLICKED","SEND ORDER");
                    String token = FirebaseInstanceId.getInstance().getToken();
                    if(token != null && token.length() > 0){
                        try{
                            orderProgressDialog.show();
                            DataManager.getInstance().sendOrder(getOrderString(token));
                        }catch ( JSONException e) {

                        }
                    }else{
                        connectionErrorDialog.show();
                    }


                }else{
                    emptyOrderDialog.show();
                }



            }
        });
        initDialogs();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }

    private void initDialogs(){
        successDialog = new MaterialDialog.Builder(this)
                .title("Gracias por ordenar")
                .content("Su orden fue enviada satisfactoriamente.")
                .positiveText("Cerrar")
                .build();

        emptyOrderDialog = new MaterialDialog.Builder(this)
                .title("No hay ningun item agregado.")
                .content("Debe agregar por lo menos 1 item del menu para realizar una order.")
                .positiveText("Cerrar")
                .build();
        connectionErrorDialog = new MaterialDialog.Builder(this)
                .title("Error de Conexión")
                .content("Ocurrio un problema al enviar su orden. Verifique que su dispositivo este " +
                        "conectado a la red o vuelva intentarlo en unos minutos.")
                .positiveText("Cerrar")
                .build();
        orderProgressDialog = new MaterialDialog.Builder(this)
                .content("Enviando su orden...")
                .canceledOnTouchOutside(false)
                .progress(true, -1)
                .build();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG,"On activity Request"+ requestCode);

        if (requestCode == REQUEST_ENABLE_BT) {

            if(resultCode == RESULT_CANCELED){
                Log.d(TAG,"On activity Result"+ resultCode);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Mesero App necesita el adaptador de bluetooth para funcionar, de lo" +
                        "contrario se cerrará ¿Desea activar el adaptador bluetooth?")
                        .setTitle("Error al encender bluetooth");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter != null) {
                            if (!mBluetoothAdapter.isEnabled()) {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                            }
                        }

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            finishAndRemoveTask();
                        else
                            finish();
                    }
                });


                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }else if(resultCode == RESULT_OK){
                beaconManager.bind(instance);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void messageSent(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                successDialog.show();

            }
        });

    }

    public String getOrderString(String token){


        Gson gson = new Gson();
        Order newOrder = new Order(activeTable, DataManager.getInstance().getOderedItems(),token );
        String serializedOrder = gson.toJson(newOrder);
        Log.d(TAG, "SERIALIZED ORDER: " + serializedOrder);
        return serializedOrder;


    }


    public void showMenu(final boolean areBeacons, final int tableNumber){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(areBeacons){
                    noBeaconMsg.setVisibility(View.GONE);
                    menuLy.setVisibility(View.VISIBLE);
                    tableTextView.setText("Mesa " + tableNumber);
                }else{
                    noBeaconMsg.setVisibility(View.VISIBLE);
                    menuLy.setVisibility(View.GONE);
                    tableTextView.setText("");
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");



            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
                showMenu(false, 0);
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {    }


        beaconManager.addRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                for( Beacon b: beacons){

//                    Log.d(TAG," BEACON MINOR" + b.getId3());
//                    Log.d(TAG," BEACON MAYOR" + b.getId2());
//                    Log.d(TAG," BEACON RSSI" + b.getRssi());
                    if(activeTable != b.getId3().toInt() && b.getRssi() >= rssiMin ){
                        activeTable = b.getId3().toInt();
                        showMenu(true, activeTable);
                    }
                }
            }

        });
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        }catch (RemoteException e){
            Log.e(TAG,e.getMessage());
        }


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }


    @Override
    public void onOrderSent(boolean successful) {

        if(orderProgressDialog.isShowing()){
            orderProgressDialog.dismiss();
        }
        if(successful){
            successDialog.show();
            return;
        }
        connectionErrorDialog.show();

        Log.d("ERROR", "ERROR SENDING ORDER");



    }
}
