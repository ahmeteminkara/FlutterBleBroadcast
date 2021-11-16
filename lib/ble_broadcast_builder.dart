class BleBroadcastBuilder {
  final String deviceName;
  final String uuidDevice;
  final String uuidService;
  final String uuidCharacteristic;

  BleBroadcastBuilder(this.uuidDevice, this.uuidService, this.uuidCharacteristic,{this.deviceName = "DevBle"});

  Map get toJson {
    return {
      "deviceName": deviceName,
      "uuidDevice": uuidDevice,
      "uuidService": uuidService,
      "uuidCharacteristic": uuidCharacteristic,
    };
  }
}
