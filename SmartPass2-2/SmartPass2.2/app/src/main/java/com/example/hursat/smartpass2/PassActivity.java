package com.example.hursat.smartpass2;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class PassActivity extends AppCompatActivity {

    private BluetoothAdapter bleAdapter;
    private BluetoothLEService bleService;
    private BluetoothLeScanner bleScanner;
    private ScanSettings bleScanSettings;
    private List<ScanFilter> bleScanFilter;
    private Handler bleHandler;

    private TextView passInfoText;

    private boolean scanning = false;
    private boolean connected = false;

    private String deviceAddress;
    private static final String TAG = "PassAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        passInfoText = (TextView) findViewById(R.id.pass_info_text);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "Bluetooth Low Energy is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();

        if(bleAdapter == null){
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        bleHandler = new Handler();

    }

    @Override
    protected void onResume(){
        super.onResume();

        registerReceiver(bleGattUpdateReceiver, bleGattUpdateIntentFilter());

        if (!bleAdapter.isEnabled()) {
            if (!bleAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        if(bleAdapter.isEnabled() && !scanning){

            scanning = true;

            if(Build.VERSION.SDK_INT >= 21){
                bleScanner = bleAdapter.getBluetoothLeScanner();
                bleScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                bleScanFilter = new ArrayList<>();
            }

            scanBleDevice(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(bleAdapter != null && bleScanner != null){
        scanBleDevice(false);
        unregisterReceiver(bleGattUpdateReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(bleService != null && bleService.isBound()){
            unbindService(serviceConnection);
        }
        bleService = null;
    }



    private final BroadcastReceiver bleGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if(BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)){
                Log.i(TAG, "BroadcastReciever::GattConnected");
                connected = true;

            } else if(BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "BroadcastReciever::GattDisconnected");
                connected = false;

            } else if(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                Log.i(TAG, "BroadcastReciever::GattConnected");
                displayGattServices(bleService.getSupportedGattServices());

            } else if(BluetoothLEService.OUT_OF_DISTANCE.equals(action)){
                Log.i(TAG, "BroadcastReciever::OutOfDistance");

            } else if(BluetoothLEService.ACTION_DISCONNECT.equals(action)){
                Log.i(TAG, "BroadcastReciever::Disconnect");

                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //use ticket?
                    }
                });*/

            }

        }
    };

    private ScanCallback bleNewScanCallBack = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            Log.i(TAG, "NewScanCallBack::OnScanResult");

            bleScanner.stopScan(bleNewScanCallBack);
            deviceAddress = result.getDevice().getAddress();

            if(deviceAddress.equals("B8:27:EB:AE:8B:98")){

                if(bleService == null || bleService.isBound() == false){

                    Intent gattServiceIntent = new Intent(PassActivity.this, BluetoothLEService.class);
                    bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

                } else {
                    bleService.connect(deviceAddress);
                }

            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            Log.i(TAG, "NewScanCallBack::OnBatchScanResults");

            for(ScanResult tmp : results){
                Log.i(TAG, "ScanResults: " + tmp.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            Log.e(TAG, "NewScanCallBack::OnScanFailed: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback bleOldScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            Log.i(TAG, "OldScanCallBack");

            bleAdapter.stopLeScan(bleOldScanCallback);
            deviceAddress = device.getAddress();

            if(bleService == null || bleService.isBound() == false){

                Intent gattServiceIntent = new Intent(PassActivity.this, BluetoothLEService.class);
                bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

            } else {

                bleService.connect(deviceAddress);
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "ServiceConnection::onServiceConnected");

            passInfoText.setText("Connected to device. Please wait for confirmation!");

            bleService = ((BluetoothLEService.LocalBinder) service).getService();

            if (!bleService.initialize()) {
                finish();
            }

            bleService.connect(deviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "ServiceConnection::onServiceDisconnected");
            bleService = null;
        }
    };

    private void displayGattServices(List<BluetoothGattService> bleGattServices){

        Log.i(TAG, "displayGattServices");

        for(BluetoothGattService tmpService : bleGattServices){

            if(tmpService.getUuid().toString().equals("0019da7b-0929-4cbe-b93c-322a11cfadec")){

                BluetoothGattCharacteristic getUidCharacteristic = null;

                for(BluetoothGattCharacteristic tmpCharacteristic : tmpService.getCharacteristics()){

                    if(tmpCharacteristic.getUuid().toString().equals("e153486e-fce4-41d4-8b74-21323b3cf222")){

                        getUidCharacteristic = tmpCharacteristic;
                    }
                }

                if(getUidCharacteristic != null && connected == true){

                    bleService.writeCharacteristic(getUidCharacteristic);
                }
            }
        }
    }


    private static IntentFilter bleGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLEService.ACTION_DISCONNECT);
        intentFilter.addAction(BluetoothLEService.OUT_OF_DISTANCE);

        return intentFilter;
    }

    public void scanBleDevice(final boolean mode){

        passInfoText.setText("Searching for device");

        if(mode){
            bleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if(Build.VERSION.SDK_INT < 21){
                        bleAdapter.stopLeScan(bleOldScanCallback);
                    } else {
                        bleScanner.stopScan(bleNewScanCallBack);
                    }
                }
            }, 10000);

            if(Build.VERSION.SDK_INT < 21){
                bleAdapter.startLeScan(bleOldScanCallback);
            } else {
                bleScanner.startScan(bleScanFilter, bleScanSettings, bleNewScanCallBack);
            }

        } else {

            if(Build.VERSION.SDK_INT < 21){
                bleAdapter.stopLeScan(bleOldScanCallback);
            } else {
                bleScanner.stopScan(bleNewScanCallBack);
            }
        }
    }
}
