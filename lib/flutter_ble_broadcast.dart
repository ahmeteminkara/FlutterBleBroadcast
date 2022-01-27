// ignore_for_file: avoid_print

import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_builder.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_status.dart';

typedef BleStatusListener = void Function(BleBroadcastStatus status);

class FlutterBleBroadcast {
  static BleBroadcastBuilder _builder;
  static StreamSubscription _subscription;
  static StreamSubscription _subscriptionLauncher;

  static Stream<BleBroadcastStatus> get stream => _streamController.stream;
  static final _streamController = StreamController<BleBroadcastStatus>.broadcast();

  static ValueNotifier<bool> launchMode = ValueNotifier(null);

  static void init({@required BleBroadcastBuilder builder, BleStatusListener listener}) {
    _builder = builder;
    _subscriptionLauncher = const EventChannel("onLaunchMode").receiveBroadcastStream().listen((e) {
      print("onLaunchMode listen: $e");
      launchMode.value = e;
    });
    _subscription = const EventChannel("onBroadcastStatus").receiveBroadcastStream().listen((e) {
      print("FlutterBleBroadcast listen: $e");
      try {
        Map json = jsonDecode(e);
        if (!json.containsKey("code")) return;
        final status = BleBroadcastStatus(json["code"]);
        if (json.containsKey("data")) {
          status.data = json["data"];
        }
        _streamController.add(status);
        if (listener != null) listener(status);
      } catch (error) {
        print("FlutterBleBroadcast error: $error");
      }
    });
  }

  static const MethodChannel _channel = MethodChannel('flutter_ble_broadcast');

  static String _numControl(int n) {
    if (n < 10) {
      return "0$n";
    } else {
      return n.toString();
    }
  }

  static changeLauncherApp({String toastMessage = ""}) => _channel.invokeMethod("changeLauncherApp", {"toastMessage": toastMessage});
  static checkLauncherApp() => _channel.invokeMethod("checkLauncherApp");
  
  static Future<bool> checkRoot() => _channel.invokeMethod("checkRoot");
  static disableSystemUI() => _channel.invokeMethod("disableSystemUI");
  static enableSystemUI() => _channel.invokeMethod("enableSystemUI");

  static Future<bool> setDateTime(DateTime dateTime) async {
    List<String> list = [];
    list.add(dateTime.year.toString());
    list.add(_numControl(dateTime.month));
    list.add(_numControl(dateTime.day));
    list.add(_numControl(dateTime.hour));
    list.add(_numControl(dateTime.minute));
    list.add(_numControl(dateTime.second));

    return await _channel.invokeMethod('setDateTime', {"dt": list.join("-")});
  }

  static Future<bool> startBroadcast() async {
    try {
      return await _channel.invokeMethod('start', _builder.toJson);
    } catch (e) {
      return false;
    }
  }

  static Future<bool> stopBroadcast() async {
    try {
      return await _channel.invokeMethod('stop');
    } catch (e) {
      return false;
    }
  }

  static Future<bool> dispose() async {
    if (_subscription != null) _subscription.cancel();
    if (_subscriptionLauncher != null) _subscriptionLauncher.cancel();
    return await stopBroadcast();
  }
}
