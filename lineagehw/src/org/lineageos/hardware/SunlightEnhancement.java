/*
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2019 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.hardware;

import android.os.Build;
import android.util.Log;

import org.lineageos.internal.util.FileUtils;

public class SunlightEnhancement {
    private static final String TAG = "SunlightEnhancement";

    private static final String DISP_PARAM_PATH =
            "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/disp_param";

    private static final String DISP_PARAM_HBM_ON = "10000";
    private static final String DISP_PARAM_HBM_FOD_ON = "20000";
    private static final String DISP_PARAM_HBM_FOD_OFF = "E0000";
    private static final String DISP_PARAM_HBM_OFF = "F0000";

    private static boolean hasFingerprintOnDisplay() {
        return Build.DEVICE.equals("equuleus");
    }

    /**
     * Whether device supports sunlight enhancement
     *
     * @return boolean Supported devices must return always true
     */
    public static boolean isSupported() {
        return (Build.DEVICE.equals("ursa") || Build.DEVICE.equals("equuleus")) &&
                FileUtils.isFileWritable(DISP_PARAM_PATH);
    }

    /**
     * This method return the current activation status of sunlight enhancement
     *
     * @return boolean Must be false when sunlight enhancement is not supported or not activated,
     * or the operation failed while reading the status; true in any other case.
     */
    public static boolean isEnabled() {
        try {
            return Integer.parseInt(FileUtils.readOneLine(DISP_PARAM_PATH)) > 0;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    /**
     * This method allows to setup sunlight enhancement
     *
     * @param status The new sunlight enhancement status
     * @return boolean Must be false if sunlight enhancement is not supported or the operation
     * failed; true in any other case.
     */
    public static boolean setEnabled(boolean status) {
        if (hasFingerprintOnDisplay()) {
            return FileUtils.writeLine(DISP_PARAM_PATH, status ?
                    DISP_PARAM_HBM_FOD_ON : DISP_PARAM_HBM_FOD_OFF);
        }
        return FileUtils.writeLine(DISP_PARAM_PATH, status ?
                DISP_PARAM_HBM_ON : DISP_PARAM_HBM_OFF);
    }

    /**
     * Whether adaptive backlight (CABL / CABC) is required to be enabled
     *
     * @return boolean False if adaptive backlight is not a dependency
     */
    public static boolean isAdaptiveBacklightRequired() {
        return false;
    }

    /**
     * Set this to true if the implementation is self-managed and does
     * it's own ambient sensing. In this case, setEnabled is assumed
     * to toggle the feature on or off, but not activate it. If set
     * to false, LiveDisplay will call setEnabled when the ambient lux
     * threshold is crossed.
     *
     * @return true if this enhancement is self-managed
     */
    public static boolean isSelfManaged() {
        return false;
    }
}
