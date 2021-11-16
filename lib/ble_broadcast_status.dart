class BleBroadcastStatus {
  String code;
  String data;

  BleBroadcastStatus(this.code, {this.data});

  @override
  String toString() {
    if (data == null) return "BleBroadcastStatus { code: $code }";
    return "BleBroadcastStatus { code: $code, data: $data }";
  }
}
