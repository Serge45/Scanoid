/*
 * Copyright (C) 2009 The Android Open Source Project
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
 *
 */
#include <string.h>
#include <jni.h>
#include <android/bitmap.h>
#include "fastcv.h"
#include "FcvBitmap.h"

#define LOG_TAG "JNI_DEBUG"

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */
extern "C" {
//Test jni function
jstring Java_com_serge45_scanoid_JniTestActivity_stringFromJNI(JNIEnv* env,
        jobject thiz) {
    char v[32];
    fcvGetVersion(v, 32);
    return env->NewStringUTF(v); //"Hello from JNI !");
}
//---------fastcv c-wrappings------------
void Java_com_serge45_scanoid_JniTestActivity_initFcvMemController() {
    fcvMemInit();
}

void Java_com_serge45_scanoid_JniTestActivity_freeFcvMemController() {
    fcvMemDeInit();
}

jobject
Java_com_serge45_scanoid_JniTestActivity_storeToFcvBitmap(JNIEnv *env,
        jobject thiz, jobject jBitmap);

void
Java_com_serge45_scanoid_JniTestActivity_freeFcvBitmap(JNIEnv *env,
        jobject thiz, jobject handle);

jobject
Java_com_serge45_scanoid_JniTestActivity_getBitmapFromFcvBitmap(JNIEnv *env,
        jobject thiz, jobject fcvBitmap);

jobject
Java_com_serge45_scanoid_JniTestActivity_toFcvBitmapU8(JNIEnv *env,
        jobject thiz, jobject fcvBitmap);

jobject
Java_com_serge45_scanoid_JniTestActivity_binarizeFcvBitmapU8(JNIEnv *env,
        jobject thiz, jobject srcBitmapU8, uint8_t threshold);
}

FcvBitmap<uint8_t> *convertRGBA8888ToGreyBitmap(const FcvBitmap<int32_t> *src) {
    FcvBitmap<uint8_t> *yuv[3];

    for (size_t i = 0; i < 3; ++i) {
        yuv[i] = new FcvBitmap<uint8_t>(src->getWidth(), src->getHeight());
    }

    fcvColorRGBA8888ToYCbCr444Planaru8((uint8_t *) src->getBytes(),
            src->getWidth(), src->getHeight(), 0, yuv[0]->getBytes(),
            yuv[1]->getBytes(), yuv[2]->getBytes(), 0, 0, 0);

    delete yuv[1];
    delete yuv[2];

    return yuv[0];
}

FcvBitmap<int32_t> *greyToRGBA8888Color(const FcvBitmap<uint8_t> *grey) {
    FcvBitmap<uint8_t> zeroPlane(grey->getWidth(), grey->getHeight());
    memset(zeroPlane.getBytes(), 128, grey->getWidth() * grey->getHeight());

    FcvBitmap<int32_t> *greyFcvBitmap = new FcvBitmap<int32_t>(grey->getWidth(),
                                                               grey->getHeight()
                                                               );

    fcvColorYCbCr444PlanarToRGBA8888u8(grey->getBytes(), zeroPlane.getBytes(),
            zeroPlane.getBytes(), grey->getWidth(), grey->getHeight(), 0, 0, 0,
            (uint8_t *) greyFcvBitmap->getBytes(), 0);
    return greyFcvBitmap;
}

/*Convert RGBA8888 color image to RGBA grey image, i.e. u, v channels == 128*/
FcvBitmap<int32_t> *convertToRGBA8888Grey(const FcvBitmap<int32_t> *src) {
    auto *grey = convertRGBA8888ToGreyBitmap(src);

    auto *greyFcvBitmap = greyToRGBA8888Color(grey);

    delete grey;

    return greyFcvBitmap;
}

/*Create FsvBitmap from existing bitmap*/
jobject Java_com_serge45_scanoid_JniTestActivity_storeToFcvBitmap(JNIEnv *env,
        jobject thiz, jobject jBitmap) {
    AndroidBitmapInfo bitmapInfo;

    int ret = 0;

    if ((ret = AndroidBitmap_getInfo(env, jBitmap, &bitmapInfo)) < 0) {
        return nullptr;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return nullptr;
    }

    void *jPixels;

    if ((ret = AndroidBitmap_lockPixels(env, jBitmap, &jPixels)) < 0) {
        return nullptr;
    }

    int32_t *srcPixels = (int32_t *) jPixels;
    uint32_t orgWidth = bitmapInfo.width;
    uint32_t orgHeight = bitmapInfo.height;
    uint32_t pixelCount = orgWidth * orgHeight;
    uint32_t newWidth = orgWidth;
    uint32_t newHeight = orgHeight;
    int32_t *storePixels = nullptr;

    if (orgWidth % 8) {
        newWidth += (8 - orgWidth % 8);
    }

    if (orgHeight % 8) {
        newHeight += (8 - orgHeight % 8);
    }

    storePixels = (int32_t *) fcvMemAlloc(
            newWidth * newHeight * sizeof(int32_t), 16);
    int32_t *srcPtr = srcPixels;
    int32_t *dstPtr = storePixels;

    for (uint32_t i = 0; i < orgHeight;
            srcPtr += orgWidth, dstPtr += newWidth, ++i) {
        memcpy(dstPtr, srcPtr, orgWidth * sizeof(uint32_t));
    }

    AndroidBitmap_unlockPixels(env, jBitmap);

    FcvBitmap<int32_t> *fcvBitmap = new FcvBitmap<int32_t>(storePixels,
            newWidth, newHeight, bitmapInfo);

    return env->NewDirectByteBuffer(fcvBitmap, 0);
}

/*Destroy FsvBitmap*/
void Java_com_serge45_scanoid_JniTestActivity_freeFcvBitmap(JNIEnv *env,
        jobject thiz, jobject handle) {
    FcvBitmap<int32_t> *fsvBitmap =
            static_cast<FcvBitmap<int32_t> *>(env->GetDirectBufferAddress(
                    handle));

    if (fsvBitmap) {
        delete fsvBitmap;
    }
}

//FcvBitmap to Bitmap interface for jni
jobject Java_com_serge45_scanoid_JniTestActivity_getBitmapFromFcvBitmap(
        JNIEnv *env, jobject thiz, jobject handle) {
    FcvBitmap<int32_t> *fcvBitmap =
            static_cast<FcvBitmap<int32_t> *>(env->GetDirectBufferAddress(
                    handle));
    return fcvBitmapToBitmap<int32_t>(env, fcvBitmap);
}

jobject
Java_com_serge45_scanoid_JniTestActivity_toFcvBitmapU8(JNIEnv *env,
        jobject thiz, jobject fcvBitmap) {
   FcvBitmap<int32_t> *srcBitmap = static_cast<FcvBitmap<int32_t> *>(env->GetDirectBufferAddress(fcvBitmap));
   auto *greyBitmap = convertRGBA8888ToGreyBitmap(srcBitmap);
   return env->NewDirectByteBuffer(greyBitmap, 0);
}

jobject
Java_com_serge45_scanoid_JniTestActivity_binarizeFcvBitmapU8(JNIEnv *env,
        jobject thiz, jobject srcBitmapU8, uint8_t threshold) {
   FcvBitmap<uint8_t> *srcBitmap = static_cast<FcvBitmap<uint8_t> *>(env->GetDirectBufferAddress(srcBitmapU8));
   FcvBitmap<uint8_t> *binBitmap = new FcvBitmap<uint8_t>(srcBitmap->getWidth(), srcBitmap->getHeight());

   fcvFilterThresholdu8_v2(srcBitmap->getBytes(),
                        srcBitmap->getWidth(),
                        srcBitmap->getHeight(),
                        0,
                        binBitmap->getBytes(),
                        0,
                        threshold
                        );

   auto *dstBitmap = greyToRGBA8888Color(binBitmap);
   delete binBitmap;
   return env->NewDirectByteBuffer(dstBitmap, 0);
}
