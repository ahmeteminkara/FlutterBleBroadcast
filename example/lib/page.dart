// ignore_for_file: avoid_print

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_builder.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_codes.dart';
import 'package:flutter_ble_broadcast/flutter_ble_broadcast.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_status.dart';

class BlePage extends StatefulWidget {
  const BlePage({Key key}) : super(key: key);

  @override
  State<BlePage> createState() => _BlePageState();
}

class _BlePageState extends State<BlePage> {
  final GlobalKey<ScaffoldState> scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  void initState() {
    super.initState();

    initBleBroadcast();
  }

  listen(BleBroadcastStatus event) {
    String s = "";
    switch (int.parse(event.code)) {
      case BleBroadcastCodes.BLUETOOTH_ON:
        s = "BLUETOOTH_ON";
        break;
      case BleBroadcastCodes.BLUETOOTH_OFF:
        s = "BLUETOOTH_OFF";
        break;
      case BleBroadcastCodes.BROADCAST_STARTED:
        s = "BROADCAST_STARTED";
        break;
      case BleBroadcastCodes.BROADCAST_START_ERROR:
        s = "BROADCAST_START_ERROR";
        break;
      case BleBroadcastCodes.BROADCAST_STOPPED:
        s = "BROADCAST_STOPPED";
        break;
      case BleBroadcastCodes.DEVICE_CONNECTED:
        s = "DEVICE_CONNECTED";
        break;
      case BleBroadcastCodes.DEVICE_DISCONNECTED:
        s = "DEVICE_DISCONNECTED";
        break;
      case BleBroadcastCodes.WRITE_CHARACTERISTIC:
        s = "WRITE_CHARACTERISTIC -> ${event.data}";
        break;
      case BleBroadcastCodes.SERVICES_ADDED:
        s = "SERVICES_ADDED";
        break;
      default:
    }

    ScaffoldMessenger.of(context).hideCurrentSnackBar();
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(s, style: const TextStyle(color: Colors.white, fontSize: 17, fontWeight: FontWeight.w500)),
      duration: const Duration(seconds: 2),
    ));

    print("event: " + event.toString());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: scaffoldKey,
      appBar: AppBar(title: const Text('Flutter Ble Broadcast')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            ElevatedButton(
                child: const Text("start"),
                onPressed: () => FlutterBleBroadcast.startBroadcast().then((value) {
                      print("startBroadcast: $value");
                    })),
            ElevatedButton(child: const Text("restart"), onPressed: () => restart()),
            ElevatedButton(child: const Text("stop"), onPressed: () => FlutterBleBroadcast.stopBroadcast()),
            /*
            ElevatedButton(
              child: const Text("Set Date"),
              onPressed: () {
                FlutterBleBroadcast flutterBleBroadcast = FlutterBleBroadcast(null);
                flutterBleBroadcast.setDateTime(DateTime.parse("2021-11-22 12:49:07"));
              },
            ),
            */
          ],
        ),
      ),
    );
  }

  void restart() {
    FlutterBleBroadcast.dispose().then((value) {
      print("dispose then");
      Timer(const Duration(seconds: 2), () {
        initBleBroadcast();
        FlutterBleBroadcast.startBroadcast().then((value) {
          print("startBroadcast: $value");
        });
      });
    });
  }

  void initBleBroadcast() {
    String uuidDevice = "d7aec172-ca69-450c-a2ba-41b156658923";
    String uuidService = "19422237-a2cd-4630-8907-0aa767775984";
    String uuidCharacteristic = "89f1a7ee-0c1c-4c68-8f70-8d034be60294";

    final builder = BleBroadcastBuilder(
      uuidDevice,
      uuidService,
      uuidCharacteristic,
      deviceName: "DevBle",
    );
    FlutterBleBroadcast.init(builder: builder, listener: (status) => listen(status));
  }
}
