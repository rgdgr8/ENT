# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

set(gnsdk_DIR ../../../../../../gnsdk-auto-2.6.19.14264-20250729)
set(gnsdk_include_DIR ${gnsdk_DIR}/include)
set(gnsdk_lib_DIR ${CMAKE_SOURCE_DIR}/../../../../../gnsdk-auto-2.6.19.14264-20250729/lib)
get_filename_component(gnsdk_lib_DIR ${gnsdk_lib_DIR} REALPATH)

set(gnsdk_cpp_wrapper_DIR ${gnsdk_DIR}/wrappers/gnsdk_cplusplus/src_wrapper)
set(arch_TAG ${ANDROID_ABI})

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        jni-gracenoteservice

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        gnservice-lib.cpp)

add_subdirectory(gnservice)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


target_include_directories(jni-gracenoteservice PRIVATE
        ${gnsdk_include_DIR}
        )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        jni-gracenoteservice

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}

        GnService
        )
