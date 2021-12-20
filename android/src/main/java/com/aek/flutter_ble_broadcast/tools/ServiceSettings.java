package com.aek.flutter_ble_broadcast.tools;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.aek.flutter_ble_broadcast.FlutterBleBroadcastPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceSettings {
    public static String deviceName = "BleDevice";
    public static String GATT_UUID_DEVICE = "06f3e038-fcf5-4539-b024-55595c479545";//"11111111-1111-1111-1111-111111111111";
    public static String GATT_UUID_SERVICE = "acc0ddb2-d9d0-4a09-b914-1d068eb79c97";//"22222222-2222-2222-2222-222222222222";
    public static String GATT_UUID_CHARACTERISTIC = "d3f0d225-3d3a-4a89-9248-3f441ccf5690";//"33333333-3333-3333-3333-333333333333";
    public static BluetoothManager bluetoothManager;
    public static BluetoothGattServer gattServer;
    public static ArrayList<BluetoothDevice> connectedDevices = new ArrayList<>();


    public static int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
    public static int advertiseTXPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;


    public static boolean isMyAppLauncherDefault(Context context) {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String myPackageName = context.getPackageName();
        List<ComponentName> activities = new ArrayList<>();
        final PackageManager packageManager = (PackageManager) context.getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }


}
