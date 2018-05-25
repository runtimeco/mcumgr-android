/*
 * Copyright (c) Intellinium SAS, 2014-present
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.image.tlv;

import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.util.ByteUtil;
import io.runtime.mcumgr.util.Endian;

/**
 * Represents a type-length-value info table for firmware images using McuBoot or the legacy Apache
 * Mynewt bootloader.
 * <p>
 * For more info about McuBoot and image format see:
 * <a href="https://runtimeco.github.io/mcuboot/design.html">https://runtimeco.github.io/mcuboot/design.html</a>
 */
@SuppressWarnings("unused")
public class McuMgrImageTlvInfo {
    private short mMagic;
    private short mTotal;

    private McuMgrImageTlvInfo() {
        // empty private constructor
    }

    public static McuMgrImageTlvInfo fromBytes(byte[] b) throws McuMgrException {
        return fromBytes(b, 0);
    }

    public static McuMgrImageTlvInfo fromBytes(byte[] b, int offset) throws McuMgrException {
        if (b.length - offset < getSize())
            throw new McuMgrException("The byte array is too short to be a McuMgrImageTlvInfo: " +
                    "length= " + b.length + ", offset= " + offset + ", min size= " + getSize());

        McuMgrImageTlvInfo info = new McuMgrImageTlvInfo();
        info.mMagic = (short) ByteUtil.unsignedByteArrayToInt(b, offset, 2, Endian.LITTLE);
        info.mTotal = (short) ByteUtil.unsignedByteArrayToInt(b, offset + 2, 2, Endian.LITTLE);

        if (info.mMagic != McuMgrImageTlv.IMG_TLV_INFO_MAGIC) {
            throw new McuMgrException("Wrong magic number, magic= " + info.mMagic + " instead of " +
                    McuMgrImageTlv.IMG_TLV_INFO_MAGIC);
        }
        return info;
    }

    public static int getSize() {
        return 4;
    }

    public short getMagic() {
        return mMagic;
    }

    public short getTotal() {
        return mTotal;
    }
}
