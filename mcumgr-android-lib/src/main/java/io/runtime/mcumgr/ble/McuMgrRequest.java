/*
 * Copyright (c) 2017-2018 Runtime Inc.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.ble;

import android.os.ConditionVariable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;

import io.runtime.mcumgr.McuMgrCallback;
import io.runtime.mcumgr.McuMgrHeader;
import io.runtime.mcumgr.McuMgrScheme;
import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.response.McuMgrResponse;
import io.runtime.mcumgr.util.CBOR;

/**
 * A McuMgrRequest is a helper class which holds the necessary info and methods to perform both
 * asynchronous and synchronous Mcu Manager transactions.
 * <p>
 * Call {@link McuMgrRequest#synchronous(Queue)} or {@link McuMgrRequest#asynchronous(Queue)} to
 * enqueue a request to a queue. This request should eventually be taken from the queue and
 * sent by the transporter which should either fail or provide the response to finish the request.
 * <p>
 * Use {@link McuMgrRequest#fail(McuMgrException)} to fail a request.
 * <p>
 * Call {@link McuMgrRequest#receive(byte[])} when a response is received. If this method returns
 * false, additional packet fragments are required.
 *
 * @param <T> The response type
 */
// TODO make an abstract class and two subclasses for async and sync operations
@SuppressWarnings("unused")
class McuMgrRequest<T extends McuMgrResponse> {

    private final static String TAG = "McuMgrRequest";

    /**
     * Raw request data
     */
    private byte[] mBytes;

    /**
     * Response type
     */
    private Class<T> mResponseType;

    /**
     * The response
     */
    private T mResponse;

    /**
     * Callback for async operations
     */
    private McuMgrCallback<T> mCallback;

    /**
     * Blocks synchronous operations
     */
    private ConditionVariable mSyncLock;

    /**
     * The exception to pass to callback or return
     */
    private McuMgrException mException;

    /**
     * Construct a McuMgrRequest
     *
     * @param requestData  this request's data
     * @param responseType this request's response type
     */
    McuMgrRequest(byte[] requestData, Class<T> responseType) {
        this(requestData, responseType, null);
    }

    /**
     * Construct a McuMgrRequest
     *
     * @param requestData  this request's data
     * @param responseType this request's response type
     * @param callback     callback for async operations
     */
    McuMgrRequest(byte[] requestData, Class<T> responseType,
                  @Nullable McuMgrCallback<T> callback) {
        this.mBytes = requestData;
        this.mResponseType = responseType;
        this.mCallback = callback;
        if (callback != null) {
            this.mSyncLock = new ConditionVariable(false);
        }
    }

    public byte[] getBytes() {
        return mBytes;
    }

    @Override
    public String toString() {
        try {
            McuMgrHeader header = McuMgrHeader.fromBytes(mBytes);
            byte[] payload = Arrays.copyOfRange(mBytes, 8, mBytes.length);
            return header + ", Payload: " + CBOR.toString(payload);
        } catch (IOException e) {
            return Arrays.toString(mBytes);
        }
    }

    /**
     * Add a synchronous request to the queue. This method will block the calling thread
     * indefinitely until a response has been received.
     *
     * @param queue the queue to add the request to. This queue must
     * @return the response
     * @throws McuMgrException on error, see message and cause
     */
    public T synchronous(Queue<McuMgrRequest> queue) throws McuMgrException {
        queue.add(this);
        Log.d(TAG, "Waiting on request's sync lock");
        mSyncLock.block();
        if (mException != null) {
            throw mException;
        } else {
            return mResponse;
        }
    }

    /**
     * Enqueue an asynchronous request, this method returns immediately
     *
     * @param queue the queue to add to
     */
    public void asynchronous(Queue<McuMgrRequest> queue) {
        queue.add(this);
    }

    /**
     * Fail a request with an mException.
     *
     * @param e the mException to throw or pass into the error mCallback.
     */
    public void fail(McuMgrException e) {
        if (mCallback != null) {
            mCallback.onError(e);
        } else {
            mException = e;
            mSyncLock.open();
        }
    }

    /**
     * Receive a response.
     *
     * @param bytes the response bytes
     */
    public void receive(byte[] bytes) {
        try {
            // Build a mResponse of the required type
            mResponse = McuMgrResponse.buildResponse(McuMgrScheme.BLE, bytes, mResponseType);

            // Call the mCallback or open the waiting lock
            if (mCallback != null) {
                mCallback.onResponse(mResponse);
            } else {
                mSyncLock.open();
            }
        } catch (IOException e) {
            fail(new McuMgrException("Error building McuMgrResponse from response data", e));
        }
    }
}