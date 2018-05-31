/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.runtime.mcumgr.ble;

import android.bluetooth.BluetoothDevice;

import no.nordicsemi.android.ble.BleManagerCallbacks;

/**
 * <b>DO NOT PERFORM ANY BLOCKING OPERATIONS INSIDE THESE CALLBACKS!</b>
 * <p>
 * Blocking these callbacks may risk the integrity of the BleManager.
 * <p>
 * Callbacks from {@link McuMgrBleTransport}.
 */
public class McuMgrBleCallbacks implements BleManagerCallbacks {
    @Override
    public void onDeviceConnecting(BluetoothDevice device) {
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
    }

    @Override
    public void onDeviceDisconnecting(BluetoothDevice device) {
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
    }

    @Override
    public void onLinkLossOccurred(BluetoothDevice device) {
    }

    @Override
    public void onServicesDiscovered(BluetoothDevice device, boolean optionalServicesFound) {
    }

    @Override
    public void onDeviceReady(BluetoothDevice device) {
    }

    @Override
    public boolean shouldEnableBatteryLevelNotifications(BluetoothDevice device) {
        return false;
    }

    @Override
    public void onBatteryValueReceived(BluetoothDevice device, int value) {
    }

    @Override
    public void onBondingRequired(BluetoothDevice device) {
    }

    @Override
    public void onBondingFailed(BluetoothDevice device) {
    }

    @Override
    public void onBonded(BluetoothDevice device) {
    }

    @Override
    public void onError(BluetoothDevice device, String message, int errorCode) {
    }

    @Override
    public void onDeviceNotSupported(BluetoothDevice device) {
    }
}
