#ifndef FCV_BITMAP_H
#define FCV_BITMAP_H

#include <string.h>
#include <jni.h>
#include <android/bitmap.h>
#include "fastcv.h"

template<typename T>
class FcvBitmap {
public:
    FcvBitmap(T *b, uint32_t w, uint32_t h, AndroidBitmapInfo i) {
        bytes = b;
        width = w;
        height = h;
        info = i;
    }

    FcvBitmap(uint32_t w, uint32_t h) {
        width = w;
        height = h;
        bytes = static_cast<T *>(fcvMemAlloc(width * height * sizeof(T), 16));
    }

    //Deepcopy
    FcvBitmap(const FcvBitmap &rhs) {
        width = rhs.getWidth();
        height = rhs.getHeight();
        bytes = static_cast<T *>(fcvMemAlloc(width * height * sizeof(T), 16));
        memcpy(bytes, rhs.getBytes(), width * height * sizeof(T));
    }

    //Deepcopy
    FcvBitmap &operator=(const FcvBitmap &rhs) {
        if (&rhs != this) {
            if (rhs.getWidth() != width || rhs.getHeight() != height) {
                if (bytes) {
                    fcvMemFree(bytes);
                }
                width = rhs.getWidth();
                height = rhs.getHeight();
                bytes = static_cast<T *>(fcvMemAlloc(width * height * sizeof(T), 16));
            }

            memcpy(bytes, rhs.getBytes(), width * height * sizeof(T));
        }
        return *this;
    }

    ~FcvBitmap() {
        if (bytes) {
            fcvMemFree(bytes);
        }
    }

    uint32_t getWidth() const {
        return width;
    }

    uint32_t getHeight() const {
        return height;
    }

    uint32_t getByteCount() const {
        return width * height * sizeof(T);
    }

    T *getBytes() const {
        return bytes;
    }

private:
    T *bytes;
    uint32_t width;
    uint32_t height;
    AndroidBitmapInfo info;
};

//FcvImage to bitmap implementation
template<typename T>
jobject fcvBitmapToBitmap(JNIEnv *env, FcvBitmap<T> *fcvBitmap) {
    if (!fcvBitmap->getBytes()) {
        return nullptr;
    }

    jclass bitmapCls = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapFunction = env->GetStaticMethodID(bitmapCls,
            "createBitmap",
            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOfBitmapConfigFunction = env->GetStaticMethodID(
            bitmapConfigClass, "valueOf",
            "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass,
            valueOfBitmapConfigFunction, configName);
    jobject newBitmap = env->CallStaticObjectMethod(bitmapCls,
            createBitmapFunction, fcvBitmap->getWidth(), fcvBitmap->getHeight(),
            bitmapConfig);
//
// putting the pixels into the new bitmap:
//
    int ret;
    void* bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, newBitmap, &bitmapPixels)) < 0) {
        return nullptr;
    }
    T* newBitmapPixels = (T*) bitmapPixels;
    int pixelsCount = fcvBitmap->getWidth() * fcvBitmap->getHeight();
    memcpy(newBitmapPixels, fcvBitmap->getBytes(), sizeof(T) * pixelsCount);
    AndroidBitmap_unlockPixels(env, newBitmap);
//LOGD("returning the new bitmap");
    return newBitmap;

}
#endif
