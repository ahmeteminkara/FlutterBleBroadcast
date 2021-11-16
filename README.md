# FlutterBleBroadcast
>In this project, it is the Flutter plugin that broadcasts BLE advertising(only Android)

```xml

<!-- Add Android permissions -->

<service
    android:name="com.aek.flutter_ble_broadcast.services.BleServerService"
    android:enabled="true"
    android:exported="true" />

<service
    android:name="com.aek.flutter_ble_broadcast.services.FakeBleScanService"
    android:enabled="true"
    android:exported="true" />
```

```c

FlutterBleBroadcast flutterBleBroadcast;
flutterBleBroadcast = FlutterBleBroadcast(BleBroadcastBuilder(uuidDevice, uuidService, uuidCharacteristic, deviceName: "MePass"));

final builder = BleBroadcastBuilder(
  uuidDevice,
  uuidService,
  uuidCharacteristic,
  //deviceName: Default:"DevBle",
);
flutterBleBroadcast = FlutterBleBroadcast(builder);

flutterBleBroadcast.bleBroadcastStatus.listen((event) {
/*
event
• BleBroadcastStatus.BLUETOOTH_ON
• BleBroadcastStatus.BLUETOOTH_OFF
• BleBroadcastStatus.BROADCAST_STARTED
• BleBroadcastStatus.BROADCAST_START_ERROR
• BleBroadcastStatus.BROADCAST_STOPPED
• BleBroadcastStatus.DEVICE_CONNECTED
• BleBroadcastStatus.DEVICE_DISCONNECTED
• BleBroadcastStatus.WRITE_CHARACTERISTIC
• BleBroadcastStatus.SERVICES_ADDED
*/
});

flutterBleBroadcast.startBroadcast().then((value) {
    print("startBroadcast: $value");
});

flutterBleBroadcast.startBroadcast();

```
# FlutterBleBroadcast
