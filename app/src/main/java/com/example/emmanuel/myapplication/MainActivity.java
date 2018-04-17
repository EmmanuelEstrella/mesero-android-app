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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

import com.example.emmanuel.myapplication.Logic.DataManager;
import com.example.emmanuel.myapplication.Logic.Order;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, CompoundButton.OnCheckedChangeListener{

    EditText text;
    private final String TAG = "EmmanuelBeacons";
    private BeaconManager beaconManager;
    final Region region = new Region(TAG, Identifier.parse("5fd85e4c-8bd1-11e6-ae22-56b6b6499611"), null, null);
    final int rssiMin = -80;
    private int activeTable = 0;
    private int positionA = 0;
    private int positionB = 0;
    private int positionC = 0;
    private double [] rssiA;
    private double [] rssiB;
    private double [] rssiC;
    private int a = 0;
    private int b2 = 0;
    private int c2 = 0;
    private double aux =  0;
    private double aux2 =  0;
    private double aux3 =  0;
    private double amarillo =  0;
    private double morado =  0;
    private double rosa =  0;


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private int REQUEST_ENABLE_BT =89;

    MainActivity instance;

    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private LinearLayout noBeaconMsg;
    private LinearLayout menuLy;
    private TextView tableTextView;
    private CheckBox hamburger;
    private CheckBox salad;
    private CheckBox pizza;
    private CheckBox pasta;

    private MaterialDialog successDialog;

    private ArrayList<CheckBox> checkBoxes;

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
        tableTextView = findViewById(R.id.table);
        noBeaconMsg.setVisibility(View.VISIBLE);
        menuLy.setVisibility(View.GONE);

        checkBoxes = new ArrayList<>();
        rssiA = new double[100];
        rssiB = new double[100];
        rssiC = new double[100];




        hamburger = findViewById(R.id.check_hambur);
        hamburger.setOnCheckedChangeListener(this);

        checkBoxes.add(hamburger);

        pizza = findViewById(R.id.check_pizza);
        pizza.setOnCheckedChangeListener(this);
        checkBoxes.add(pizza);

        salad = findViewById(R.id.check_salad);
        salad.setOnCheckedChangeListener(this);
        checkBoxes.add(salad);

        pasta = findViewById(R.id.check_pasta);
        pasta.setOnCheckedChangeListener(this);
        checkBoxes.add(pasta);

        text = findViewById(R.id.text);

        orderButton = findViewById(R.id.button);
        orderButton.setEnabled(false);

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
                String s = "this is a test";


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

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeTable != 0)
                    Log.d("CLICKED",getOrderString());

            }
        });
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        if(mBluetoothAdapter.isEnabled())
            beaconManager.bind(instance);

        successDialog = new MaterialDialog.Builder(this)
                .title("Gracias por ordenar")
                .content("Su orden fue enviada satisfactoriamente.")
                .positiveText("Cerrar")
                .build();

        DataManager.getInstance().initClient(this);
        DataManager.getInstance().getMenuItems();
        LinearLayoutManager  mLayoutManager = new LinearLayoutManager(this);
        menuRecyclerView.setLayoutManager(mLayoutManager);
        menuAdapter = new MenuAdapter();
        DataManager.getInstance().setMenuItemsListeners(menuAdapter);
        menuRecyclerView.setAdapter(menuAdapter);

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
                for (CheckBox e:
                     checkBoxes) {
                    e.setChecked(false);
                }

            }
        });

    }

    public String getOrderString(){

        ArrayList<String> items = new ArrayList<>();
        String order = "ORDEN MESA "+ activeTable + positionA + positionB + positionC +"\n";
        for (CheckBox c:
              checkBoxes) {
            if(c.isChecked())
                items.add(c.getText().toString());

        }
        Gson gson = new Gson();
        Order newOrder = new Order(activeTable,items,positionA,positionB,positionC);
        return gson.toJson(newOrder);


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
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container,initialFragment)
//                        .commit();
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
                //Log.i(TAG,"Waiting for beacon");
                if(beacons.size()>0){
//                    Log.i(TAG,"-----printing array " + logCounter);
//                    Log.i(TAG,"-----printing array size " + beacons.size());
                    for( Beacon b: beacons){
//                        Log.i(TAG,"Menor "+b.getId3().toInt());
 //                       Log.i(TAG,"RSSI "+b.getRssi());
 //                       Log.i(TAG,"d "+b.getDistance());

                        if(b.getId2().toInt() == 10  && activeTable != 0){
//                        Log.i(TAG,"MAJOR:"+activeBeacon.getId2().toInt());

                            if(b.getId3().toInt() == 3){
                                rssiA[a] =b.getDistance();
//                                a++;
//                                Log.i(TAG,"Distancia1:"+b.getDistance());
//                                Log.i(TAG,"POSICION1:"+positionA);
//                                Log.i(TAG,"numero:"+a);
                                a++;
                            }
                            else if (b.getId3().toInt() == 4){
                                rssiB[b2] = b.getDistance();

//                                Log.i(TAG,"POSICION2:"+positionB);
//                                Log.i(TAG,"numero:"+b2);
                                b2++;
//                                a++;

                            }
                            else if (b.getId3().toInt() == 5){

                                    rssiC[c2] =b.getDistance();

//                                Log.i(TAG,"POSICION3:"+positionC);
//                                Log.i(TAG,"numero:"+c2);
//                                a++;
                                c2++;

                            }
//
                        }


                        if(a > 29 && b2 > 29 && c2 > 29) {
                            a = 0;
                            b2 = 0;
                            c2 = 0;
                            aux = 0;
                            aux2 = 0;
                            aux3 = 0;


                                 for(int i = 0; i <= 29; i++){
                                    aux += rssiA[i];
                                    aux2 += rssiB[i];
                                    aux3 += rssiC[i];
                                }
                                rosa = aux/30;
                                Log.i(TAG,"POSICION1:"+rosa);
                                amarillo = aux2/30;
                                Log.i(TAG,"POSICION2:"+amarillo);
                                morado = aux3/30;
                                Log.i(TAG,"POSICION3:"+morado);



                            }



                        if(b.getRssi() > rssiMin && activeTable != b.getId3().toInt() && b.getId2().toInt() !=0){
                            Log.i(TAG,"BEACON FOUND!");
                            Log.i(TAG,"MAC:"+b.getBluetoothAddress());
                            Log.i(TAG,"MAJOR:"+b.getId2().toInt());
                            Log.i(TAG,"MINOR:"+b.getId3().toInt());
                            Log.i(TAG,"RSSI:"+b.getRssi());
                            activeTable = b.getId3().toInt();
//
                          showMenu(true, activeTable);
                        }

                    }
//                    logCounter++;
                    //final Beacon activeBeacon = beacons.iterator().next();

                        //Log.i(TAG,"MAC:"+activeBeacon.getBluetoothAddress());
//                        Log.i(TAG,"MAJOR:"+activeBeacon.getId2().toInt());
//                        Log.i(TAG,"MINOR:"+activeBeacon.getId3().toInt());
                       // Log.i(TAG,"RSSI:"+activeBeacon.getRssi());
//                    activeBeacon.isExtraBeaconData()
//
//
//
//                    if(activeBeacon.getId2().toInt() == 0 && activeTable != 0){
//                        Log.i(TAG,"MAJOR:"+activeBeacon.getId2().toInt());
//
//                        if(activeBeacon.getId3().toInt() == 3){
//                            puntoA = activeBeacon.getRssi();
//                            Log.i(TAG,"POSICION1:"+puntoA);
//                        }
//                        else if (activeBeacon.getId3().toInt() == 4){
//                            puntoB = activeBeacon.getRssi();
//                            Log.i(TAG,"POSICION2:"+puntoB);
//                        }
//                        else{
//                            puntoC =activeBeacon.getRssi();
//                            Log.i(TAG,"POSICION3:"+puntoC);
//                        }
//
//                    }
//
//                    if(activeBeacon.getRssi() > rssiMin && activeTable != activeBeacon.getId3().toInt() && activeBeacon.getId2().toInt() !=0){
//                        Log.i(TAG,"BEACON FOUND!");
//                        Log.i(TAG,"MAC:"+activeBeacon.getBluetoothAddress());
//                        Log.i(TAG,"MAJOR:"+activeBeacon.getId2().toInt());
//                        Log.i(TAG,"MINOR:"+activeBeacon.getId3().toInt());
//                        Log.i(TAG,"RSSI:"+activeBeacon.getRssi());
//                        activeTable = activeBeacon.getId3().toInt();
//                        showMenu(true, activeTable);
//                    }




                }else if( activeTable != 0){
                    showMenu(false, 0);
                    activeTable = 0;
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
        if(hamburger.isChecked() || salad.isChecked() || pizza.isChecked() || pasta.isChecked())
            orderButton.setEnabled(true);
        else
            orderButton.setEnabled(false);
    }


}
