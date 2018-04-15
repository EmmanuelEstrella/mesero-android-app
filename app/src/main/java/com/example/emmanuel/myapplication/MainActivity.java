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
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
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

    private int point1A [] = {96,97,94,95,90,92,91,89,90,91,92,87,91,94,92,92,92,95,94,93,92,93,94,95,94,93,88,88,95,90,86};
    private int point2A [] = {87,90,90,88,94,89,88,89,94,90,92,92,90,90,88,94,92,90,94,90,94,90,90,90,95,91,91,94,90,90,90};
    private int point3A [] = {80,83,93,77,78,77,93,76,77,90,82,80,76,82,88,77,90,77,77,81,79,78,92,77,77,77,90,82,77,81,81};
    private int point4A [] = {82,83,80,82,79,91,81,80,79,81,79,81,79,79,78,78,89,78,90,89,91,78,82,81,92,80,80,80,96,80,81};

    private int point1B [] = {86,86,88,90,91,85,86,89,90,84,92,93,89,88,92,84,99,91,86,87,89,90,86,87,88,90,93,84,84,90,94};
    private int point2B [] = {93,95,98,95,93,94,95,95,89,91,90,95,90,94,95,88,93,86,90,95,94,88,96,93,89,90,90,91,88,95,90};
    private int point3B [] = {93,94,89,92,90,92,95,91,92,90,94,94,91,96,97,94,91,90,89,97,91,91,90,94,94,94,94,96,97,93,92};
    private int point4B [] = {88,91,91,95,93,85,88,91,94,92,88,94,92,92,93,89,91,90,90,93,92,89,89,95,92,100,89,98,90,97,90};

    private int point1C [] = {87,87,86,87,86,85,83,82,83,81,81,82,84,79,80,83,79,81,84,86,82,82,83,84,84,85,87,84,90,90,84};
    private int point2C [] = {85,83,82,86,86,83,86,86,83,87,86,84,83,85,83,84,85,87,86,85,84,82,82,84,82,86,83,85,83,86,85};
    private int point3C [] = {90,89,90,89,96,93,92,95,92,92,93,91,95,94,98,94,94,94,98,95,93,90,97,93,97,95,96,92,92,97,97};
    private int point4C [] = {91,85,87,91,93,92,87,92,96,85,86,85,91,86,97,95,85,90,90,97,86,94,98,90,87,96,86,88,89,87,90};


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


                             /*   for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point1A[i] - rssiA[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero1a: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point2A[i] - rssiA[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero2a: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point3A[i] - rssiA[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero3a: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point4A[i] - rssiA[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero4a: "+ aux2);
                                aux2 = 0;
                                aux = 0;





                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point1B[i] - rssiB[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero1b: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point2B[i] - rssiB[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero2b: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point3B[i] - rssiB[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero3b: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point4B[i] - rssiB[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero4b: "+ aux2);
                                aux2 = 0;
                                aux = 0;



                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point1C[i] - rssiC[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero1c: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point2C[i] - rssiC[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero2c: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point3C[i] - rssiC[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero3c: "+ aux2);
                                aux2 = 0;
                                aux = 0;

                                for (int i = 0; i <= 30; i++) {

                                    aux += Math.pow((point4C[i] - rssiC[i]), 2);
                                }

                                aux2 = (Math.sqrt(aux)) / 31;

                                Log.i(TAG,"numero4c: "+ aux2);
                                aux2 = 0;
                                aux = 0;


                                Log.i(TAG, "yaaaaaaaaaaaaaaaaa");


                          */  for(int i = 0; i <= 29; i++){
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

    public class Task extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));
            DatagramSocket clientSocket = null;
            try {
                clientSocket = new DatagramSocket(9877);;
            } catch (SocketException e) {
                e.printStackTrace();
            }
            InetAddress IPAddress = null;
            try {
                IPAddress = InetAddress.getByName("192.168.0.11");
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



            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                clientSocket.receive(receivePacket);
                String data1 = new String( receivePacket.getData());
                Log.i(TAG,"Data:"+data1);
                if (data1.length() > 0){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            successDialog = new MaterialDialog.Builder(MainActivity.this)
                                    .title("Su orden a llegado")
                                    .content("Favor retirar su orden")
                                    .positiveText("retirar mesero")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            DatagramSocket clientSocket = null;
                                            try {
                                                clientSocket = new DatagramSocket(9877);;
                                            } catch (SocketException e) {
                                                e.printStackTrace();
                                            }
                                            InetAddress IPAddress = null;
                                            try {
                                                IPAddress = InetAddress.getByName("192.168.0.10");
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                            byte[] sendData;
                                            String sentence = "5";
                                            sendData = sentence.getBytes();

                                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                                            Log.i(TAG,"Data:");
                                        }
                                    })
                                    .show();
                            Log.i(TAG,"mostro:");
                        }
                    });








                }

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
