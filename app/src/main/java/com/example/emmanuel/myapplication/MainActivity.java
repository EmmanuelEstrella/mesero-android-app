package com.example.emmanuel.myapplication;

import android.os.AsyncTask;
import android.os.RemoteException;
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

public class MainActivity extends AppCompatActivity implements BeaconConsumer, CompoundButton.OnCheckedChangeListener{

    EditText text;
    private final String TAG = "EmmanuelBeacons";
    private BeaconManager beaconManager;
    final Region region = new Region(TAG, Identifier.parse("5fd85e4c-8bd1-11e6-ae22-56b6b6499611"), null, null);
    final int rssiMin = -80;
    private int activeTable = 0;

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
        beaconManager.bind(this);

        successDialog = new MaterialDialog.Builder(this)
                .title("Gracias por ordenar")
                .content("Su orden fue enviada satisfactoriamente.")
                .positiveText("Cerrar")
                .build();



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
        String order = "ORDEN MESA "+ activeTable + "\n";
        for (CheckBox c:
              checkBoxes) {
            if(c.isChecked())
                order += c.getText() + "\n";

        }
        return order;


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
                IPAddress = InetAddress.getByName("192.168.1.111");
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
