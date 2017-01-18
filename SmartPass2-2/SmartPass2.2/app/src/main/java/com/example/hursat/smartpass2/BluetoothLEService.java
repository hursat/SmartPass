package com.example.hursat.smartpass2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

/**
 * Created by hursat on 10.12.2016.
 */

public class BluetoothLEService extends Service{

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_DISCONNECT =
            "com.example.bluetooth.le.DISCONNECT";
    public final static String OUT_OF_DISTANCE =
            "com.example.bluetooth.le.OUT_OF_DISTANCE";

    private static final String spName = "SmartPassSharedPreferences";
    private static final String TAG = "BLEService";

    private boolean isBound;
    private String bleDeviceAddress;
    private BluetoothGatt bleGatt;
    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private SharedPreferences sp;
    private Context context;

    private final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "gattCallback::STATE_CONNECTED");

                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                bleGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "gattCallBack::STATE_DISCONNECTED");

                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);

            } else {
                Log.i(TAG, "gattCallBack::STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Log.i(TAG, "onServicesDiscovered");

            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            Log.i(TAG, "onCharacteristicRead");

            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            } else {
                Log.w(TAG, "onCharacteristicRead received: " + status);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            Log.i(TAG, "onCharacteristicWrite");

            String uuid = characteristic.getUuid().toString();

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        return bleBinder;
    }

    private final IBinder bleBinder = new LocalBinder();

    public boolean onUnbind(Intent intent){
        isBound = false;
        close();
        return super.onUnbind(intent);
    }

    public void onRebind(Intent intent) {
        isBound = true;
    }

    public boolean isBound(){
        return isBound;
    }

    public boolean initialize(){

        Log.i(TAG, "initialize");

        sp = getSharedPreferences(spName, Context.MODE_PRIVATE);

        if(bleManager == null){
            bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(bleManager == null){
                Log.e(TAG, "initialize::Bluetooth Manager could not be initialized");
                return false;
            }
        }

        bleAdapter = bleManager.getAdapter();
        if(bleAdapter == null){
            Log.e(TAG, "initialize::Bluetooth Adapter could not be initialized");
            return false;
        }

        return true;

    }

    public boolean connect(final String address){

        Log.i(TAG, "connect");

        if(bleAdapter == null || address == null){
            Log.w(TAG, "connect::BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if(bleDeviceAddress != null && address.equals(bleDeviceAddress) && bleGatt != null){

            Log.w(TAG, "connect::Trying to use an existing BluetoothGatt for connection.");

            if (bleGatt.connect()){
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = bleAdapter.getRemoteDevice(address);
        if(device == null){
            Log.w(TAG, "connect::BluetoothDevice not found");
            return false;
        }

        bleGatt = device.connectGatt(this, false, bleGattCallback);
        bleDeviceAddress = address;
        return true;
    }

    public void disconnect(){
        Log.i(TAG, "disconnect");

        if(bleAdapter == null || bleGatt == null){
            Log.w(TAG, "disconnect::BluetoothAdapter is not initialized");
            return;
        }

        bleGatt.disconnect();
    }

    public void close(){
        Log.i(TAG,"close");

        if(bleGatt == null){
            return;
        }

        bleGatt.close();
        bleGatt = null;
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){

        Log.i(TAG, "writeCharacteristic");

        if(bleAdapter == null || bleGatt == null){
            Log.w(TAG, "writeCharacteristic::BluetoothAdapter not initialized");
            return;
        }

        String uid = sp.getString("UID", "");
        characteristic.setValue(uid);
        bleGatt.writeCharacteristic(characteristic);

    }

    public List<BluetoothGattService> getSupportedGattServices(){

        Log.i(TAG, "getSupportedGattServices");

        if (bleGatt == null){ return null; }

        return bleGatt.getServices();

    }

}
