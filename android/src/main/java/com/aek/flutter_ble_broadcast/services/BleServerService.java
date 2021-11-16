package com.aek.flutter_ble_broadcast.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.widget.Toast;

import com.aek.flutter_ble_broadcast.FlutterBleBroadcastPlugin;
import com.aek.flutter_ble_broadcast.tools.ServiceSettings;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.aek.flutter_ble_broadcast.tools.ServiceSettings.GATT_UUID_CHARACTERISTIC;
import static com.aek.flutter_ble_broadcast.tools.ServiceSettings.GATT_UUID_DEVICE;
import static com.aek.flutter_ble_broadcast.tools.ServiceSettings.GATT_UUID_SERVICE;

@SuppressLint({"MissingPermission", "WrongConstant"})
public class BleServerService extends Service {

    public boolean isAdvertising = false;
    private BluetoothLeAdvertiser mBTAdvertiser;
    private final IBinder mBinder = new LocalBinder();


    public List<BluetoothGattService> getServices() {
        return this.mGattServer.getServices();
    }

    private final AdvertiseCallback mBleAdvertiserCallback = new AdvertiseCallback() {
        String errString;

        public void onStartFailure(int errCode) {
            switch (errCode) {
                case 1:
                    this.errString = "ADVERTISE_FAILED_DATA_TOO_LARGE";
                    break;
                case 2:
                    this.errString = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
                    break;
                case 3:
                    this.errString = "ADVERTISE_FAILED_ALREADY_STARTED";
                    break;
                case 4:
                    this.errString = "ADVERTISE_FAILED_INTERNAL_ERROR";
                    break;
                case 5:
                    this.errString = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
                    break;
                default:
                    this.errString = "UnIdentified Error";
                    break;
            }
            BleServerService.this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.BROADCAST_START_ERROR).sendToTarget();
            super.onStartFailure(errCode);
            BleServerService.this.stopBleAdvertising();
            //BleServerService.this.sendMsgToLogHandler("Start Advertising (" + this.errString + ")");
        }

        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            BleServerService.this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.BROADCAST_STARTED).sendToTarget();
            super.onStartSuccess(settingsInEffect);
            //BleServerService.this.sendMsgToLogHandler("Start Advertising");
            BleServerService.this.isAdvertising = true;
        }


    };
    private BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    public BluetoothGattServer mGattServer;
    BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] bytes) {

            if (characteristic.getUuid().toString().equals(GATT_UUID_CHARACTERISTIC)){
                serverHandler.obtainMessage(FlutterBleBroadcastPlugin.WRITE_CHARACTERISTIC, new String(bytes, StandardCharsets.UTF_8)).sendToTarget();
                BleServerService.this.mGattServer.sendResponse(device, requestId, 0, 0, (byte[]) null);
            }
        }

        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == 2) {
                ServiceSettings.connectedDevices.add(device);
                BleServerService.this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.DEVICE_CONNECTED, device).sendToTarget();
                //BleServerService.this.sendMsgToLogHandler("Device(" + device.getAddress() + ") Connected");
            } else if (newState == 0) {
                ServiceSettings.connectedDevices.remove(device);
                BleServerService.this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.DEVICE_DISCONNECTED, device).sendToTarget();
                //BleServerService.this.sendMsgToLogHandler("Device(" + device.getAddress() + ") Disconnected");
            }
        }


        public void onNotificationSent(BluetoothDevice device, int status) {
            //BleServerService.this.sendMsgToLogHandler("Notification Sent to Device (" + device.getAddress() + ")");
            super.onNotificationSent(device, status);
        }

        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            BleServerService.this.serviceAdded = true;
        }
    };

    public Handler serverHandler;
    public boolean serviceAdded = true;

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        /* access modifiers changed from: package-private */
        public BleServerService getService() {
            return BleServerService.this;
        }
    }

    public void setSvrHandler(Handler s_handler) {
        this.serverHandler = s_handler;
    }


    public int initialize() {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            if (this.mBluetoothManager == null) {
                return -1;
            }
        }

        ServiceSettings.bluetoothManager = mBluetoothManager;
        mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return -2;
        }
        this.mBTAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        if (this.mBTAdvertiser == null) {
            return -4;
        }

        getGattServer();
        updateServerServices();
        //BleServerService.this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.SERVICE_MSG_UPDATE_DEVICE_NAME).sendToTarget();
        return 0;
    }


    private void updateServerServices() {
        if (this.mGattServer == null) return;

        this.mGattServer.clearServices();

        BluetoothGattService gattService = new BluetoothGattService(UUID.fromString(GATT_UUID_SERVICE), 0);
        BluetoothGattCharacteristic gattCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(GATT_UUID_CHARACTERISTIC),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        gattService.addCharacteristic(gattCharacteristic);
        mGattServer.addService(gattService);

        this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.SERVICES_ADDED).sendToTarget();

    }

    public void close() {
        if (this.mGattServer != null) {
            stopGattServer();
        }
        if (this.isAdvertising) {
            stopBleAdvertising();
        }
    }

    public boolean startBleAdvertising() {
        if (this.isAdvertising) {
            Toast.makeText(this, "startBleAdvertising() -> isAdvertising=true", Toast.LENGTH_LONG).show();
            return false;
        }
        AdvertiseSettings gAdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(ServiceSettings.advertiseMode)
                .setTxPowerLevel(ServiceSettings.advertiseTXPowerLevel)
                .setTimeout(0)
                .setConnectable(true)
                .build();
        AdvertiseData mAdvData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(ParcelUuid.fromString(GATT_UUID_DEVICE))
                .build();
        this.mBTAdvertiser.startAdvertising(gAdvSettings, mAdvData, this.mBleAdvertiserCallback);

        this.isAdvertising = true;
        return true;
    }

    public boolean stopBleAdvertising() {


        this.mBTAdvertiser.stopAdvertising(this.mBleAdvertiserCallback);
        this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.BROADCAST_STOPPED).sendToTarget();
        this.isAdvertising = false;

        if (ServiceSettings.connectedDevices.size() > 0) {
            disconnectAllDevices();
            ServiceSettings.connectedDevices.clear();
        }

        return true;
    }


    private void stopGattServer() {
        this.mGattServer.clearServices();
        this.mGattServer.close();
        this.mGattServer = null;
    }


    public BluetoothGattServer getGattServer() {

        BluetoothGattServer gatt_server = this.mBluetoothManager.openGattServer(this, this.mGattServerCallback);
        this.mGattServer = gatt_server;
        ServiceSettings.gattServer = this.mGattServer;
        return gatt_server;
    }


    public void disconnectAllDevices() {
        if (this.mGattServer != null) {
            Iterator<BluetoothDevice> it = ServiceSettings.connectedDevices.iterator();
            while (it.hasNext()) {
                disconnectDevice(it.next());
            }
        }
    }

    public void disconnectDevice(BluetoothDevice b_dev) {
        this.mGattServer.cancelConnection(b_dev);
        //ServiceSettings.removeDeviceFromNotiList(b_dev);
    }

    /* access modifiers changed from: private
    public void sendMsgToLogHandler(String str) {
        if (this.serverHandler != null) {
            FlutterBleBroadcastPlugin.LogMsg l_msg = new FlutterBleBroadcastPlugin.LogMsg();
            l_msg.l_tic = new Date().getTime();
            l_msg.msg_str = str;
            this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.SERVICE_MSG_UPDATE_LOG, l_msg).sendToTarget();
        }
    }*/

}