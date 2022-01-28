LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := lib-imsvtshim.cpp
LOCAL_MODULE := lib-imsvtshim
LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)
