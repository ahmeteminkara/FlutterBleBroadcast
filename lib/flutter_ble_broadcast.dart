// ignore_for_file: avoid_print

import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_builder.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_status.dart';

class FlutterBleBroadcast {
  BleBroadcastBuilder _builder;
  StreamSubscription _subscription;

  Stream<BleBroadcastStatus> get bleBroadcastStatus => _bleBroadcastStatusController.stream;
  final _bleBroadcastStatusController = StreamController<BleBroadcastStatus>.broadcast();

  FlutterBleBroadcast({@required BleBroadcastBuilder builder}) {
    _builder = builder;
    _subscription = const EventChannel("onBroadcastStatus").receiveBroadcastStream().listen((e) {
      print("pluginden data geldi");
      try {
        Map json = jsonDecode(e);
        if (!json.containsKey("code")) return;
        final status = BleBroadcastStatus(json["code"]);
        if (json.containsKey("data")) {
          status.data = json["data"];
        }
        print("datayı stream a gönderdi");
        _bleBroadcastStatusController.sink.add(status);
      } catch (e) {
        print("FlutterBleBroadcast error: $e");
      }
    });
  }

  final MethodChannel _channel = const MethodChannel('flutter_ble_broadcast');

  String _numControl(int n) {
    if (n < 10) {
      return "0$n";
    } else {
      return n.toString();
    }
  }

  Future<bool> setDateTime(DateTime dateTime) async {
    List<String> list = [];
    list.add(dateTime.year.toString());
    list.add(_numControl(dateTime.month));
    list.add(_numControl(dateTime.day));
    list.add(_numControl(dateTime.hour));
    list.add(_numControl(dateTime.minute));
    list.add(_numControl(dateTime.second));

    return await _channel.invokeMethod('setDateTime', {"dt": list.join("-")});
  }

  Future<bool> startBroadcast() async {
    try {
      return await _channel.invokeMethod('start', _builder.toJson);
    } catch (e) {
      return false;
    }
  }

  Future<bool> stopBroadcast() async {
    try {
      return await _channel.invokeMethod('stop');
    } catch (e) {
      return false;
    }
  }

  Future<void> dispose() async {
    await stopBroadcast();
    if (_subscription != null) _subscription.cancel();
  }
}
