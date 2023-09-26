package com.example.bluetooth.manager.common

/**
Create by yangyan
Create time:2023/8/29 14:44
Describe:
 */
object BleConstant {
    const val DEFAULT_SCAN_TIME = 20000L
    const val DEFAULT_SCAN_REPEAT_INTERVAL = -1L
    const val DEFAULT_CONN_TIME = 20000L
    const val DEFAULT_UUID_SPP ="00001101-0000-1000-8000-00805F9B34FB"
    const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    const val DEFAULT_MAX_CONNECT_COUNT = 5
    const val DEFAULT_MTU_COUNT = 23
    const val DEFAULT_PAIR = true

    const val MSG_CONNECT_TIMEOUT = 0x01
    const val MSG_DISCOVER_FAIL = 0x02
    const val MSG_DISCOVER_SUCCESS = 0x03
    const val MSG_CHAR_READ_RESULT = 0x04
    const val MSG_NOTIFY_DATA_CHANGE = 0x05
    const val MSG_READ_RSSI_RESULT = 0x06
    const val MSG_CHA_NOTIFY_RESULT = 0x07
    const val MSG_SET_MTU_RESULT = 0x08
    const val MSG_SPLIT_WRITE_NEXT = 0x09
    const val MSG_DIS_CONNECT= 0x010

    const val KEY_READ_BUNDLE_STATUS = "read_status"
    const val KEY_READ_BUNDLE_VALUE = "read_value"
    const val KEY_NOTIFY_BUNDLE_VALUE = "notify_value"
    const val KEY_NOTIFY_BUNDLE_STATUS = "notify_status"
    const val KEY_READ_RSSI_BUNDLE_STATUS = "rssi_status"
    const val KEY_READ_RSSI_BUNDLE_VALUE = "rssi_value"
    const val KEY_SET_MTU_BUNDLE_STATUS = "mtu_status"
    const val KEY_SET_MTU_BUNDLE_VALUE = "mtu_value"
}