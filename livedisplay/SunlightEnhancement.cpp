/*
 * SPDX-FileCopyrightText: 2019-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "SunlightEnhancementService"

#include <fstream>

#include <android-base/logging.h>
#include <android-base/properties.h>
#include <android-base/strings.h>
#include <utils/Errors.h>

#include "SunlightEnhancement.h"

namespace {

static constexpr const char* kDispParamPath =
        "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/disp_param";
static constexpr const char* kHbmStatusPath =
        "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/hbm_status";

static constexpr const char* kDispParamHbmOff = "0xF0000";
static constexpr const char* kDispParamHbmOn = "0x10000";
static constexpr const char* kDispParamHbmFodOff = "0xE0000";
static constexpr const char* kDispParamHbmFodOn = "0x20000";

bool hasAmoledPanel() {
    std::string device = android::base::GetProperty("ro.product.device", "");
    return device == "dipper" || device == "equuleus" ||
            device == "perseus" || device == "ursa";
}

bool hasFingerprintOnDisplay() {
    std::string device = android::base::GetProperty("ro.product.device", "");
    return device == "equuleus" || device == "ursa";
}

}  // anonymous namespace

namespace aidl {
namespace vendor {
namespace lineage {
namespace livedisplay {

bool SunlightEnhancement::isSupported() {
    if (hasAmoledPanel()) {
        std::ofstream disp_param_file(kDispParamPath);
        std::ifstream hbm_status_file(kHbmStatusPath);
        if (!disp_param_file.is_open()) {
            LOG(ERROR) << "Failed to open " << kDispParamPath << ", error=" << errno
                       << " (" << strerror(errno) << ")";
        }
        if (!hbm_status_file.is_open()) {
            LOG(ERROR) << "Failed to open " << kHbmStatusPath << ", error=" << errno
                       << " (" << strerror(errno) << ")";
        }
        return !disp_param_file.fail() && !hbm_status_file.fail();
    }
    return false;
}

ndk::ScopedAStatus SunlightEnhancement::getEnabled(bool* _aidl_return) {
    std::ifstream hbm_status_file(kHbmStatusPath);
    int result = -1;
    hbm_status_file >> result;

    if (hbm_status_file.fail()) {
        LOG(ERROR) << "Failed to read current HBM state";
        return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }

    *_aidl_return = result > 0;
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus SunlightEnhancement::setEnabled(bool enabled) {
    std::ofstream disp_param_file(kDispParamPath);
    if (hasFingerprintOnDisplay()) {
        disp_param_file << (enabled ? kDispParamHbmFodOn : kDispParamHbmFodOff);
    } else {
        disp_param_file << (enabled ? kDispParamHbmOn : kDispParamHbmOff);
    }

    if (disp_param_file.fail())
        LOG(ERROR) << "Failed to write HBM state";
        return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }

    return ndk::ScopedAStatus::ok();
}

}  // namespace livedisplay
}  // namespace lineage
}  // namespace vendor
}  // namespace aidl
