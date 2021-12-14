package com.aek.flutter_ble_broadcast;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import com.aek.flutter_ble_broadcast.services.BleServerService;
import com.aek.flutter_ble_broadcast.services.FakeBleScanService;
import com.aek.flutter_ble_broadcast.tools.ServiceSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FlutterBleBroadcastPlugin
 */
@SuppressLint({"WrongConstant", "SimpleDateFormat"})
public class FlutterBleBroadcastPlugin implements FlutterPlugin, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {


    public final static int BLUETOOTH_ON = 1000;
    public final static int BLUETOOTH_OFF = 1001;
    public final static int BROADCAST_STARTED = 2000;
    public final static int BROADCAST_START_ERROR = 2001;
    public final static int BROADCAST_STOPPED = 2002;
    public final static int DEVICE_CONNECTED = 3000;
    public final static int DEVICE_DISCONNECTED = 3001;
    public final static int WRITE_CHARACTERISTIC = 4000;
    public final static int SERVICES_ADDED = 5000;

    /*
        public static final int SERVICE_MSG_BROADCAST_START = 1001;
        public static final int SERVICE_MSG_BROADCAST_STOP = 1002;
        public static final int SERVICE_MSG_UPDATE_DEVICE_NAME = 1010;
        public static final int SERVICE_MSG_UPDATE_VIEW_CONTENT = 1011;
        public static final int SERVICE_MSG_UPDATE_SERVICE_LIST = 1021;
        public static final int SERVICE_MSG_UPDATE_CHARA_RECV = 1031;
        public static final int SERVICE_MSG_UPDATE_CHARA_WRITE = 1032;
        public static final int SERVICE_MSG_CONNECTED_DEVICE = 1041;
        public static final int SERVICE_MSG_DISCONNECTED_DEVICE = 1042;
        public static final int SERVICE_MSG_UPDATE_LOG = 1091;
        public static final int SERVICE_MSG_TOAST = 1092;
        public static final int SERVICE_MSG_WRITE_DATA = 1100;
    */
    public static final int SERVICE_MSG_SCAN_ON = 1234;
    public static final int SERVICE_MSG_SCAN_OFF = 1235;

    private static final String TAG = "FlutterBleBroadcastPlugin";


    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context context = null;

    EventChannel.EventSink flutterOnBroadcastStatus;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_ble_broadcast");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();


        new EventChannel(flutterPluginBinding.getBinaryMessenger(), "onBroadcastStatus").setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                flutterOnBroadcastStatus = events;
            }

            @Override
            public void onCancel(Object arguments) {
                flutterOnBroadcastStatus = null;
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        switch (call.method) {
            case "start":
                try {
                    if (call.hasArgument("deviceName")
                            && call.hasArgument("uuidDevice")
                            && call.hasArgument("uuidService")
                            && call.hasArgument("uuidCharacteristic")) {

                        ServiceSettings.GATT_UUID_DEVICE = call.argument("uuidDevice");
                        ServiceSettings.GATT_UUID_SERVICE = call.argument("uuidService");
                        ServiceSettings.GATT_UUID_CHARACTERISTIC = call.argument("uuidCharacteristic");
                        ServiceSettings.deviceName = call.argument("deviceName");

                        if (checkLocationPermission()) {
                            methodInit();
                            startAdvertising();
                            result.success(true);
                        } else {
                            //requestLocationPermission();
                            Log.e(TAG, "start false 3");
                            result.success(false);
                        }


                    } else {
                        Log.e(TAG, "start false 2");
                        result.success(false);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "start false 1 " + e.toString());
                    result.success(false);
                }

                break;
            case "stop":
                stopAdvertising();
                break;
            case "setDateTime":

                try {
                    if (call.hasArgument("dt")) {
                        String dt = call.argument("dt");
                        if (dt == null || dt.isEmpty()) return;

                        String[] list = dt.split("-");
                        String y = list[0],
                                m = list[1],
                                d = list[2],
                                hour = list[3],
                                min = list[4],
                                sec = list[5];


                        /*
                        Calendar c = Calendar.getInstance();
                        c.set(2013, 8, 15, 12, 34, 56);
                        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        am.setTime(c.getTimeInMillis());
                        */
                        //changeSystemTime(y, m, d, hour, min, sec);

                    }
                } catch (Exception e) {
                    Log.e(TAG, "setDateTime error: " + e.toString());
                    //context.startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));

                }

                break;
            default:
        }

    }

    private void changeSystemTime(String year, String month, String day, String hour, String minute, String second) {
        try {
            Process process = Runtime.getRuntime().exec("date -s 20120423.130000");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String command = "date -s " + year + month + day + "." + hour + minute + second + "\n";
            Log.e("command", command);
            os.writeBytes(command);
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            Log.e(TAG, "changeSystemTime error: " + e.toString());
        }
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private void methodInit() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService("bluetooth");
        if (bluetoothManager != null) {
            this.mBTAdapter = bluetoothManager.getAdapter();
            if (this.mBTAdapter != null) {
                this.mBTAdapter.setName(ServiceSettings.deviceName);
            } else {
                toast("fail_bt_adapter");
            }
        } else {
            toast("fail_bt_manager");
        }
        context.bindService(new Intent(context, BleServerService.class), this.mServerServiceConnection, Context.BIND_AUTO_CREATE);
        context.bindService(new Intent(context, FakeBleScanService.class), this.mBleScanServiceConnection, Context.BIND_AUTO_CREATE);
        this.mHandler = new Handler(new FlutterHandlerCallback(this, (FlutterHandlerCallback) null));


        bluetoothOnOffSend();

        context.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothDevice.ERROR);
                            if (extra == 10 || extra == 12) {
                                bluetoothOnOffSend();
                            }
                        } catch (Exception ignored) {
                        }

                    }
                }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));


        if (this.mBTAdapter != null && !this.mBTAdapter.isEnabled()) {
            this.mBTAdapter.enable();
        }

        context.registerReceiver(this.mBTReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));


    }

    private void bluetoothOnOffSend() {
        Log.e(TAG, "bluetoothOnOffSend run");
        BluetoothManager bluetoothManager = (BluetoothManager)
                context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        try {
            JSONObject json = new JSONObject();
            json.put("code", String.valueOf(bluetoothAdapter.isEnabled() ? BLUETOOTH_ON : BLUETOOTH_OFF));

            if (flutterOnBroadcastStatus != null)
                flutterOnBroadcastStatus.success(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                if (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", ExploreByTouchHelper.INVALID_ID) == 10) {
                    mHandler.postDelayed(() -> {
                        mBTAdapter.enable();
                    }, 500);
                } else {

                    fakeBleScanService.stopTimer();
                }
            }
        }
    };

    public BluetoothAdapter mBTAdapter;

    BleServerService mBleServerService;
    FakeBleScanService fakeBleScanService;
    Handler mHandler;

    private final ServiceConnection mServerServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBleServerService = ((BleServerService.LocalBinder) service).getService();
            mBleServerService.setSvrHandler(mHandler);
            int r_init = mBleServerService.initialize();
            if (r_init == 0) {
                toast("Service initialized");

                new Handler().postDelayed(FlutterBleBroadcastPlugin.this::startAdvertising, 1000);

            } else {
                toast("Unable to service");
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mBleServerService = null;
        }
    };

    private final ServiceConnection mBleScanServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            Log.e(TAG, "mBleScanServiceConnection onServiceConnected()");

            fakeBleScanService = ((FakeBleScanService.ScanBinder) service).getService();
            fakeBleScanService.setSvrHandler(mHandler);
            fakeBleScanService.initialize();


            toast("Service initialized fake scan");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            fakeBleScanService.stopTimer();
            fakeBleScanService = null;
        }
    };

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        return false;
    }


    private class FlutterHandlerCallback implements Handler.Callback {
        private FlutterHandlerCallback() {
        }

        /* synthetic
         */
        FlutterHandlerCallback(FlutterBleBroadcastPlugin serverActivity, FlutterHandlerCallback mainActivityHandlerCallback) {
            this();
        }

        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case BROADCAST_STARTED:
                case BROADCAST_STOPPED:
                case SERVICES_ADDED:
                case DEVICE_CONNECTED:
                case DEVICE_DISCONNECTED:
                    try {
                        JSONObject json = new JSONObject();
                        json.put("code", String.valueOf(msg.what));

                        if (flutterOnBroadcastStatus != null)
                            flutterOnBroadcastStatus.success(json.toString());

                    } catch (Exception e) {
                        Log.e(TAG, "handleMessage error : " + e.toString());
                    }
                    break;
                case WRITE_CHARACTERISTIC:
                    try {
                        String data = (String) msg.obj;
                        JSONObject jsonWrite = new JSONObject();
                        jsonWrite.put("code", String.valueOf(WRITE_CHARACTERISTIC));
                        jsonWrite.put("data", data);
                        if (flutterOnBroadcastStatus != null)
                            flutterOnBroadcastStatus.success(jsonWrite.toString());

                    } catch (Exception e) {
                        Log.e(TAG, "handleMessage 2 error : " + e.toString());
                    }
                    break;

                default:
                    return false;
            }
            return true;
        }

    }

    private void toast(String str) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_FINE_LOCATION") == 0;
    }


    public void startAdvertising() {
        if (this.mBleServerService != null && !this.mBleServerService.isAdvertising) {
            this.mBleServerService.startBleAdvertising();
        }
    }

    public void stopAdvertising() {
        if (this.mBleServerService != null && this.mBleServerService.isAdvertising) {
            this.mBleServerService.stopBleAdvertising();
        }
    }


}
