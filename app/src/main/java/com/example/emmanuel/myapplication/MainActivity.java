package com.example.emmanuel.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import Logic.Order;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, CompoundButton.OnCheckedChangeListener{

    EditText text;
    private final String TAG = "EmmanuelBeacons";
    private BeaconManager beaconManager;
    final Region region = new Region(TAG, Identifier.parse("5fd85e4c-8bd1-11e6-ae22-56b6b6499611"), null, null);
    final int rssiMin = -80;
    private int activeTable = 0;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private int REQUEST_ENABLE_BT =89;

    MainActivity instance;

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


        noBeaconMsg = findViewById(R.id.no_beacons_msg_ly);
        menuLy = findViewById(R.id.menu_ly);
        tableTextView = findViewById(R.id.table);
        noBeaconMsg.setVisibility(View.VISIBLE);
        menuLy.setVisibility(View.GONE);

        checkBoxes = new ArrayList<>();


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
                new Task().execute();
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
        String order = "ORDEN MESA "+ activeTable + "\n";
        for (CheckBox c:
              checkBoxes) {
            if(c.isChecked())
                items.add(c.getText().toString());

        }
        Gson gson = new Gson();
        Order newOrder = new Order(activeTable,items);
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
                if(beacons.size()>0){

                    final Beacon activeBeacon = beacons.iterator().next();

                    if(activeBeacon.getRssi() > rssiMin && activeTable != activeBeacon.getId3().toInt()){
                        Log.i(TAG,"BEACON FOUND!");
                        Log.i(TAG,"MAC:"+activeBeacon.getBluetoothAddress());
                        Log.i(TAG,"MAJOR:"+activeBeacon.getId2().toInt());
                        Log.i(TAG,"MINOR:"+activeBeacon.getId3().toInt());
                        Log.i(TAG,"RSSI:"+activeBeacon.getRssi());
                        activeTable = activeBeacon.getId3().toInt();
                        showMenu(true, activeTable);
                    }


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

    public class Task extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));
            DatagramSocket clientSocket = null;
            try {
                clientSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            InetAddress IPAddress = null;
            try {
                IPAddress = InetAddress.getByName("192.168.1.101");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            byte[] sendData;
            byte[] receiveData = new byte[1024];
            String sentence = getOrderString();
            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);

            try {
                clientSocket.send(sendPacket);
                Log.d("MAIN", "PACKET" + sendPacket.getData());
                messageSent();
            } catch (IOException e) {
                e.printStackTrace();
            }

            clientSocket.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("MAIN", "SENT");

        }
    }
}
