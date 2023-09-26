package com.example.bluetooth.manager.connect.impl

/**
Create by yangyan
Create time:2023/9/4 09:04
Describe:
 */
interface IAutoConnectImpl {
    fun connectByAddress(address:String, callback: IConnectCallback, connectTimeout:Long?, scanTimeout:Long?)
}