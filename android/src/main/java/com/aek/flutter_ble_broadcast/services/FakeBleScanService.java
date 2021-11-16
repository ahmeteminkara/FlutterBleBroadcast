package com.aek.flutter_ble_broadcast.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.aek.flutter_ble_broadcast.FlutterBleBroadcastPlugin;
import com.aek.flutter_ble_broadcast.tools.ServiceSettings;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint({"MissingPermission", "WrongConstant"})
public class FakeBleScanService extends Service {

    final String TAG = "FakeBleScanService";

    public boolean isScaning = false;
    private final IBinder mBinder = new ScanBinder();

    private BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;

    public Handler serverHandler;


    long startDelay = 10000;
    long stopTimeout = 2000;

    Timer timerStart;

    BluetoothLeScanner bluetoothLeScanner;

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public class ScanBinder extends Binder {
        public ScanBinder() {
        }

        /* access modifiers changed from: package-private */
        public FakeBleScanService getService() {
            return FakeBleScanService.this;
        }
    }

    public void setSvrHandler(Handler s_handler) {
        this.serverHandler = s_handler;
    }


    public void initialize() {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            if (this.mBluetoothManager == null) {
                //Log.e(TAG, "mBluetoothManager is null");
                return;
            }
        }

        ServiceSettings.bluetoothManager = mBluetoothManager;
        mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            //Log.e(TAG, "mBluetoothAdapter is null");
            return;
        }

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        startTimer();
    }


    public void close() {

        if (this.isScaning) {
            stopScan();
        }
    }


    public void startTimer() {
        // Log.e(TAG, "start timer");
        stopTimer();
        timerStart = new Timer();
        timerStart.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    startScan();
                    Thread.sleep(startDelay - stopTimeout);
                    stopScan();
                } catch (Exception e) {
                    // Log.e(TAG, "start timer start stop: " + e.getMessage());
                }

            }
        }, 0, startDelay);

    }

    public void stopTimer() {
        if (timerStart != null) timerStart.cancel();
    }

    public void startScan() {


        // Log.e(TAG, "start scan");
        try {
            this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.SERVICE_MSG_SCAN_ON).sendToTarget();

            bluetoothLeScanner.startScan(scanCallback);


        } catch (Exception e) {

            this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.SERVICE_MSG_SCAN_OFF).sendToTarget();
        }

    }

    public void stopScan() {
        // Log.e(TAG, "stop scan");
        try {


            this.serverHandler.obtainMessage(FlutterBleBroadcastPlugin.SERVICE_MSG_SCAN_OFF).sendToTarget();
            bluetoothLeScanner.flushPendingScanResults(scanCallback);
            bluetoothLeScanner.stopScan(scanCallback);

        } catch (Exception e) {
            // Log.e(TAG, "error: "+e.getMessage());
        }

    }

    final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            // Log.e(TAG, "BLE DEVICE: " + result.getDevice().getName());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            serverHandler.obtainMessage(FlutterBleBroadcastPlugin.SERVICE_MSG_SCAN_OFF).sendToTarget();
            // Log.e(TAG, "errorCode: "+errorCode);
        }

    };


}