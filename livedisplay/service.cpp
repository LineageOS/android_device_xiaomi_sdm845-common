/*
 * SPDX-FileCopyrightText: 2019-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "vendor.lineage.livedisplay-service.xiaomi_sdm845"

#include <android-base/logging.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <binder/ProcessState.h>

#include "SunlightEnhancement.h"

using ::aidl::vendor::lineage::livedisplay::SunlightEnhancement;

int main() {
    std::shared_ptr<SunlightEnhancement> se = ndk::SharedRefBase::make<SunlightEnhancement>();
    std::string instance = std::string() + SunlightEnhancement::descriptor + "/default";
    binder_status_t status;

    android::ProcessState::self()->setThreadPoolMaxThreadCount(1);
    android::ProcessState::self()->startThreadPool();

    LOG(INFO) << "LiveDisplay HAL custom service is starting.";

    if (se == nullptr) {
        LOG(ERROR) << "Can not create an instance of LiveDisplay HAL SunlightEnhancement Iface,"
                   << "exiting.";
        goto shutdown;
    }

    if (!se->isSupported()) {
        LOG(ERROR) << "SunlightEnhancement Iface is not supported, gracefully bailing out.";
        return EXIT_SUCCESS;
    }

    status = AServiceManager_addService(se->asBinder().get(), instance.c_str());
    if (status != STATUS_OK) {
        LOG(ERROR) << "Could not register service for LiveDisplay HAL SunlightEnhancement Iface ("
                   << status << ")";
        goto shutdown;
    }

    LOG(INFO) << "LiveDisplay HAL custom service is ready.";
    ABinderProcess_joinThreadPool();
    // Should not pass this line

shutdown:
    // In normal operation, we don't expect the thread pool to shutdown
    LOG(ERROR) << "LiveDisplay HAL custom service is shutting down.";
    return EXIT_FAILURE;  // Should not reach
}
