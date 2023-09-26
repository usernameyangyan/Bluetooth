package com.example.bluetooth.manager.exception

import java.io.Serializable

/**
Create by yangyan
Create time:2023/8/31 10:01
Describe:蓝牙异常码
 */
data class BleException(val bleExceptionCode: BleExceptionCode, val description:String):Serializable