/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2017-2020 The LineageOS Project
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

package org.lineageos.pocketmode;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProximitySensor implements SensorEventListener {
    private static final String TAG = "PocketModeProximity";
    private static final boolean DEBUG = false;

    private static final String FPC_PROX_NODE =
            "/sys/devices/platform/soc/soc:fingerprint_fpc/proximity_state";
    private static final String GOODIX_PROX_NODE =
            "/sys/devices/platform/soc/soc:fingerprint_goodix/proximity_state";

    private final String mFPProximityNode;
    private final String mVendorName;

    private ExecutorService mExecutorService;
    private Context mContext;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    public ProximitySensor(Context context) {
        mContext = context;
        mSensorManager = mContext.getSystemService(SensorManager.class);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mExecutorService = Executors.newSingleThreadExecutor();

        mVendorName = SystemProperties.get("persist.vendor.sys.fp.vendor", "");
        mFPProximityNode = mVendorName.equals("fpc") ? FPC_PROX_NODE : GOODIX_PROX_NODE;
        if (DEBUG) Log.d(TAG, "Using proximity state from " + mFPProximityNode);
    }

    private Future<?> submit(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        setFPProximityState(event.values[0] < mSensor.getMaximumRange());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* Empty */
    }

    private void setFPProximityState(boolean isNear) {
        try {
            FileUtils.stringToFile(mFPProximityNode, isNear ? "1" : "0");
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to " + mFPProximityNode, e);
        }
    }

    protected void enable() {
        if (DEBUG) Log.d(TAG, "Enabling");
        submit(() -> {
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        });
    }

    protected void disable() {
        if (DEBUG) Log.d(TAG, "Disabling");
        submit(() -> {
            mSensorManager.unregisterListener(this, mSensor);
        });
        // Ensure FP is left enabled
        setFPProximityState(/* isNear */ false);
    }
}
