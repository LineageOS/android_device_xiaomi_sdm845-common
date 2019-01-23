/*
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

#define LOG_TAG "SunlightEnhancementService"

#include <android-base/logging.h>
#include <android-base/properties.h>
#include <string>
#include <utils/Errors.h>
#include <fstream>

#include "SunlightEnhancement.h"

namespace vendor {
namespace lineage {
namespace livedisplay {
namespace V2_0 {
namespace xiaomi_sdm845 {

static constexpr const char* kDispParamPath =
        "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/disp_param";
static constexpr const char* kHbmStatusPath =
        "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/hbm_status";

static constexpr const char* kDispParamHbmOff = "0xF0000";
static constexpr const char* kDispParamHbmOn = "0x10000";
static constexpr const char* kDispParamHbmFodOff = "0xE0000";
static constexpr const char* kDispParamHbmFodOn = "0x20000";

static bool hasAmoledPanel() {
    std::string device = android::base::GetProperty("ro.product.device", "");
    return device == "dipper" || device == "equuleus" ||
            device == "perseus" || device == "ursa";
}

static bool hasFingerprintOnDisplay() {
    std::string device = android::base::GetProperty("ro.product.device", "");
    return device == "equuleus" || device == "ursa";
}

Return<bool> SunlightEnhancement::isEnabled() {
    std::ifstream file(kHbmStatusPath);
    int result = -1;
    if (!file.is_open()) {
        LOG(ERROR) << "Failed to open " << kHbmStatusPath << ", error=" << errno
                   << " (" << strerror(errno) << ")";
    }
    file >> result;
    return !file.fail() && result > 0;
}

Return<bool> SunlightEnhancement::setEnabled(bool enabled) {
    std::ofstream file(kDispParamPath);
    if (hasFingerprintOnDisplay()) {
        file << (enabled ? kDispParamHbmFodOn : kDispParamHbmFodOff);
    } else {
        file << (enabled ? kDispParamHbmOn : kDispParamHbmOff);
    }
    LOG(DEBUG) << "setEnabled fail " << file.fail();
    return !file.fail();
}

}  // namespace xiaomi_sdm845
}  // namespace V2_0
}  // namespace livedisplay
}  // namespace lineage
}  // namespace vendor
