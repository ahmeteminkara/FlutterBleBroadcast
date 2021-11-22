// ignore_for_file: avoid_print

import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_builder.dart';
import 'package:flutter_ble_broadcast/ble_broadcast_status.dart';

class FlutterBleBroadcast {
  BleBroadcastBuilder _builder;

  FlutterBleBroadcast(BleBroadcastBuilder builder) {
    _builder = builder;
    const EventChannel("onBroadcastStatus").receiveBroadcastStream().listen((e) {
      try {
        Map json = jsonDecode(e);
        if (!json.containsKey("code")) return;
        final status = BleBroadcastStatus(json["code"]);
        if (json.containsKey("data")) {
          status.data = json["data"];
        }
        _bleBroadcastStatusController.add(status);
      } catch (e) {
        print("Error: $e");
      }
    });
  }

  final MethodChannel _channel = const MethodChannel('flutter_ble_broadcast');

  Future<bool> setDateTime(DateTime dateTime) async {
    List<int> list = [];
    list.add(dateTime.year);
    list.add(dateTime.month);
    list.add(dateTime.day);
    list.add(dateTime.hour);
    list.add(dateTime.minute);
    list.add(dateTime.second);

    return await _channel.invokeMethod('setDateTime', {"dt": list.join("-")});
  }

  Future<bool> startBroadcast() async => await _channel.invokeMethod('start', _builder.toJson);

  Future<bool> stopBroadcast() async => await _channel.invokeMethod('stop');

  Stream<BleBroadcastStatus> get bleBroadcastStatus => _bleBroadcastStatusController.stream;
  final _bleBroadcastStatusController = StreamController<BleBroadcastStatus>.broadcast();
}
