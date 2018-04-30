/*
 * Copyright (c) 2017-2018 Runtime Inc.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.response;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.util.Arrays;

import io.runtime.mcumgr.McuMgrErrorCode;
import io.runtime.mcumgr.McuMgrHeader;
import io.runtime.mcumgr.McuMgrScheme;
import io.runtime.mcumgr.exception.McuMgrCoapException;
import io.runtime.mcumgr.util.CBOR;

@JsonIgnoreProperties(ignoreUnknown = true)
public class McuMgrResponse {

    private final static String TAG = "McuMgrResponse";

    /**
     * The raw return code found in most McuMgr response payloads. If a rc value is not explicitly
     * stated, a value of 0 is assumed.
     */
    public int rc = 0;

    /**
     * Scheme of the transport which produced this response.
     */
    private McuMgrScheme mScheme;

    /**
     * The bytes of the response packet. This includes the McuMgrHeader for standard schemes and
     * includes the CoAP header for CoAP schemes.
     */
    private byte[] mBytes;

    /**
     * The McuMgrHeader for this response
     */
    private McuMgrHeader mHeader;

    /**
     * The return code (enum) for this response. For the raw return code use the "rc" property.
     */
    private McuMgrErrorCode mRc;

    /**
     * McuMgr payload for this response. This does not include the McuMgr header for standard
     * schemes and does not include the CoAP header for CoAP schemes.
     */
    private byte[] mPayload;

    /**
     * The CoAP Code used for CoAP schemes, formatted as ((class * 100) + detail).
     */
    private int mCoapCode = 0;

    /**
     * Return the string representation of the response payload.
     *
     * @return the string representation of the response payload.
     */
    @Override
    public String toString() {
        try {
            return CBOR.toString(mPayload);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the McuMgrHeader for this response
     *
     * @return the McuMgrHeader
     */
    public McuMgrHeader getHeader() {
        return mHeader;
    }

    /**
     * Return the Mcu Manager return code as an int
     *
     * @return Mcu Manager return code
     */
    public int getRcValue() {
        if (mRc == null) {
            Log.w(TAG, "Response does not contain a McuMgr return code.");
            return 0;
        } else {
            return mRc.value();
        }
    }

    /**
     * Get the return code as an enum
     *
     * @return the return code enum
     */
    public McuMgrErrorCode getRc() {
        return mRc;
    }

    /**
     * Get the response bytes.
     * <p>
     * If using a CoAP scheme this method and {@link McuMgrResponse#getPayload()} will return the
     * same value.
     *
     * @return the response bytes
     */
    public byte[] getBytes() {
        return mBytes;
    }

    /**
     * Get the response payload in bytes.
     * <p>
     * If using a CoAP scheme this method and {@link McuMgrResponse#getPayload()} will return the
     * same value.
     *
     * @return the payload bytes
     */
    public byte[] getPayload() {
        return mPayload;
    }

    /**
     * Get the scheme used to initialize this response object.
     *
     * @return the scheme
     */
    public McuMgrScheme getScheme() {
        return mScheme;
    }

    /**
     * Set the return code for CoAP response schemes.
     *
     * @param code the code to set
     */
    void setCoapCode(int code) {
        mCoapCode = code;
    }

    /**
     * If this response is from a CoAP transport scheme, get the CoAP response code. Otherwise this
     * method will return 0. The code returned from this method should always indicate a successful
     * response because, on error, a McuMgrCoapException will be thrown (triggering the onError
     * callback for asynchronous request).
     *
     * @return the CoAP response code for a CoAP scheme, 0 otherwise
     */
    public int getCoapCode() {
        return mCoapCode;
    }

    /**
     * Initialize the fields for this response.
     *
     * @param scheme  the scheme
     * @param bytes   packet bytes
     * @param header  McuMgrHeader
     * @param payload McuMgr CBOR payload
     * @param rc      the return code
     */
    void initFields(McuMgrScheme scheme, byte[] bytes, McuMgrHeader header, byte[] payload,
                    McuMgrErrorCode rc) {
        mScheme = scheme;
        mBytes = bytes;
        mHeader = header;
        mPayload = payload;
        mRc = rc;
    }

    /**
     * Build a McuMgrResponse.
     *
     * @param scheme the transport scheme used
     * @param bytes  the response packet's bytes
     * @param type   the type of response to build
     * @param <T>    The response type to build
     * @return The response
     * @throws IOException              Error parsing response
     * @throws IllegalArgumentException if the scheme is coap
     */
    public static <T extends McuMgrResponse> T buildResponse(McuMgrScheme scheme, byte[] bytes,
                                                             Class<T> type) throws IOException {
        if (scheme.isCoap()) {
            throw new IllegalArgumentException("Cannot use this method with a coap scheme");
        }

        byte[] payload = Arrays.copyOfRange(bytes, McuMgrHeader.NMGR_HEADER_LEN, bytes.length);
        McuMgrHeader header = McuMgrHeader.fromBytes(bytes);

        // Initialize response and set fields
        T response = CBOR.toObject(payload, type);
        McuMgrErrorCode rc = McuMgrErrorCode.valueOf(response.rc);
        response.initFields(scheme, bytes, header, payload, rc);

        return response;
    }


    /**
     * Build a CoAP McuMgrResponse. This method will also throw a McuMgrCoapException if the CoAP
     * response code indicates an error.
     *
     * @param scheme     The transport scheme used (should be either COAP_BLE or COAP_UDP).
     * @param bytes      The packet's bytes
     * @param header     The raw McuManager header
     * @param payload    the raw McuManager payload
     * @param codeClass  The class of the CoAP response code
     * @param codeDetail The detail of the CoAP response code
     * @param type       The type of response to parse the payload into
     * @param <T>        The type of response to parse the payload into
     * @return The McuMgrResponse
     * @throws IOException         if parsing the payload into the object (type T) failed
     * @throws McuMgrCoapException if the CoAP code class indicates a CoAP error response
     */
    public static <T extends McuMgrResponse> T buildCoapResponse(McuMgrScheme scheme, byte[] bytes,
                                                                 byte[] header, byte[] payload,
                                                                 int codeClass, int codeDetail,
                                                                 Class<T> type) throws IOException, McuMgrCoapException {
        // If the code class indicates a CoAP error response, throw a McuMgrCoapException
        if (codeClass == 4 || codeClass == 5) {
            Log.e(TAG, "Received CoAP Error response, throwing McuMgrCoapException");
            throw new McuMgrCoapException(bytes, codeClass, codeDetail);
        }

        T response = CBOR.toObject(payload, type);
        McuMgrErrorCode rc = McuMgrErrorCode.valueOf(response.rc);
        response.initFields(scheme, bytes, McuMgrHeader.fromBytes(header), payload, rc);
        int code = (codeClass * 100) + codeDetail;
        response.setCoapCode(code);
        return response;
    }

    public static boolean requiresDefragmentation(McuMgrScheme scheme, byte[] bytes) throws IOException {
        int expectedLength = getExpectedLength(scheme, bytes);
        if (scheme.isCoap()) {
            throw new RuntimeException("Method not implemented for coap");
        } else {
            return (expectedLength > (bytes.length - McuMgrHeader.NMGR_HEADER_LEN));
        }
    }

    public static int getExpectedLength(McuMgrScheme scheme, byte[] bytes) throws IOException {
        if (scheme.isCoap()) {
            throw new RuntimeException("Method not implemented for coap");
        } else {
            McuMgrHeader header = McuMgrHeader.fromBytes(bytes);
            if (header == null) {
                throw new IOException("Invalid McuMgrHeader");
            }
            return header.getLen() + McuMgrHeader.NMGR_HEADER_LEN;
        }
    }
}

