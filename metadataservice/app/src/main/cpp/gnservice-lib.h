// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 28.05.20.
//

#ifndef GNSERVICE_LIB_H_
#define GNSERVICE_LIB_H_

struct GnJavaContext
{
    JavaVM* javaVM;
    jobject javaClassLoader;
    jmethodID javaFindClassMethod;
    jclass  mainActivityCls;
    jobject mainActivityObj;
    int32_t initialized;
};

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved);

void initializeGnserviceJavaContext(JNIEnv* env, jobject instance);

#endif  // GNSERVICE_LIB_H_
