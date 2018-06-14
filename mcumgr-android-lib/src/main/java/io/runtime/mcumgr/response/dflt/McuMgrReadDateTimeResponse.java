/*
 * Copyright (c) Intellinium SAS, 2014-present
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.response.dflt;

import io.runtime.mcumgr.response.McuMgrResponse;

public class McuMgrReadDateTimeResponse extends McuMgrResponse {
    /**
     * Date & time in <code>yyyy-MM-dd'T'HH:mm:ss.SSSSSS</code> format.
     */
    public String datetime;
}
