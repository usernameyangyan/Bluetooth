package com.example.bluetooth.manager.utils;
import com.example.bluetooth.manager.common.IDeviceConnectMirror;

import java.util.LinkedHashMap;

public class BleLruHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int MAX_SIZE;

    public BleLruHashMap(int saveSize) {
        super((int) Math.ceil(saveSize / 0.75) + 1, 0.75f, true);
        MAX_SIZE = saveSize;
    }
    @Override
    protected boolean removeEldestEntry(Entry eldest) {
        if (size() > MAX_SIZE && eldest.getValue() instanceof IDeviceConnectMirror) {
            ((IDeviceConnectMirror) eldest.getValue()).disconnect();
        }
        return size() > MAX_SIZE;
    }
}
