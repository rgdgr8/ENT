# Copyright (c) 2019-2025 e.solutions GmbH
# Copyright (c) 2026 Cariad SE
# All rights reserved.
#
# Originally developed by e.solutions GmbH
# Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
#
# Modifications:
# - 2026-02-06: Removed Conan/eso.fw dependencies for standalone build

# New SDK uses android_arm64-v8a format instead of android/arm64-v8a
set(lib_arch_TAG android_${arch_TAG})
set(gnsdk_version "3.18.19")

# ============================================================================
# GRACENOTE SDK LIBRARIES
# ============================================================================

# libgnsdk_manager
add_library(lib_gnsdk_manager SHARED IMPORTED)
set_target_properties(lib_gnsdk_manager PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_manager.${gnsdk_version}.so)
#libgnsdk_link
add_library(lib_gnsdk_link SHARED IMPORTED)
set_target_properties(lib_gnsdk_link PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_link.${gnsdk_version}.so)
#libgnsdk_musicid
add_library(lib_gnsdk_musicid SHARED IMPORTED)
set_target_properties(lib_gnsdk_musicid PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_musicid.${gnsdk_version}.so)
#libgnsdk_musicid_file
add_library(lib_gnsdk_musicid_file SHARED IMPORTED)
set_target_properties(lib_gnsdk_musicid_file PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_musicid_file.${gnsdk_version}.so)
#libgnsdk_dsp
add_library(lib_gnsdk_dsp SHARED IMPORTED)
set_target_properties(lib_gnsdk_dsp PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_dsp.${gnsdk_version}.so)
#libgnsdk_musicid_stream
add_library(lib_gnsdk_musicid_stream SHARED IMPORTED)
set_target_properties(lib_gnsdk_musicid_stream PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_musicid_stream.${gnsdk_version}.so)
#libgnsdk_radioid
add_library(lib_gnsdk_radioid SHARED IMPORTED)
set_target_properties(lib_gnsdk_radioid PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_radioid.${gnsdk_version}.so)
#libgnsdk_storage_sqlite
add_library(lib_gnsdk_storage_sqlite SHARED IMPORTED)
set_target_properties(lib_gnsdk_storage_sqlite PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_storage_sqlite.${gnsdk_version}.so)
#libgnsdk_lookup_local
add_library(lib_gnsdk_lookup_local SHARED IMPORTED)
set_target_properties(lib_gnsdk_lookup_local PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_lookup_local.${gnsdk_version}.so)
#libgnsdk_lookup_localstream
add_library(lib_gnsdk_lookup_localstream SHARED IMPORTED)
set_target_properties(lib_gnsdk_lookup_localstream PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_lookup_localstream.${gnsdk_version}.so)
#libgnsdk_lookup_managed
add_library(lib_gnsdk_lookup_managed SHARED IMPORTED)
set_target_properties(lib_gnsdk_lookup_managed PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_lookup_managed.${gnsdk_version}.so)
#libgnsdk_playlist
add_library(lib_gnsdk_playlist SHARED IMPORTED)
set_target_properties(lib_gnsdk_playlist PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_playlist.${gnsdk_version}.so)
#libgnsdk_moodgrid
add_library(lib_gnsdk_moodgrid SHARED IMPORTED)
set_target_properties(lib_gnsdk_moodgrid PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_moodgrid.${gnsdk_version}.so)
#libgnsdk_epg
add_library(lib_gnsdk_epg SHARED IMPORTED)
set_target_properties(lib_gnsdk_epg PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_epg.${gnsdk_version}.so)
#libgnsdk_storage_taste
add_library(lib_gnsdk_taste SHARED IMPORTED)
set_target_properties(lib_gnsdk_taste PROPERTIES IMPORTED_LOCATION
        ${gnsdk_lib_DIR}/${lib_arch_TAG}/libgnsdk_taste.${gnsdk_version}.so)

# ============================================================================
# GNSERVICE LIBRARY
# ============================================================================

add_library(GnService SHARED
        GnService.cxx
        event/GnServiceStatusEventHandler.cxx
        helper/GnServiceDataTypes.cxx
        helper/GnServiceExternalId.cxx
        helper/GnServiceIrcTracing.cxx
        helper/GnServiceTrackInfo.cxx
        helper/GnServiceRadioStation.cxx
        request/GnRequest.cxx
        request/GnRequestCoverArtByMetadata.cxx
        request/GnRequestExecutor.cxx
        request/GnRequestLyrics.cxx
        request/GnRequestSongRecognition.cxx
        request/GnRequestRadioStationData.cxx
        resultdb/GnLocalLookupDb.cxx
        radioresultdb/GnRadioResultDb.cxx
        user/GnUserStore.cxx
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_base.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_config.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_error.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_locale.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_log.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_manager.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_std.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_musicid.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_musicidfile.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_musicidstream.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_storage_sqlite.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_lookupdatabase.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_lookup_local.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_radioid.cpp
        ${gnsdk_cpp_wrapper_DIR}/gnsdk_dsp.cpp
        )

find_library(
        log-lib
        log)

target_include_directories(GnService
                           PUBLIC ${gnsdk_include_DIR}
                           ${gnsdk_include_DIR}/android_${arch_TAG}
                           ${gnsdk_cpp_wrapper_DIR}
                           PUBLIC ${gnsdk_cpp_DIR}
                           )

target_link_libraries(GnService
                      PUBLIC
        # gnsdk libraries
        lib_gnsdk_manager
        lib_gnsdk_musicid
        lib_gnsdk_link
        lib_gnsdk_dsp
        lib_gnsdk_musicid_file
        lib_gnsdk_musicid_stream
        lib_gnsdk_radioid
        lib_gnsdk_storage_sqlite
        lib_gnsdk_lookup_local
        lib_gnsdk_lookup_localstream
        lib_gnsdk_lookup_managed
        lib_gnsdk_playlist
        lib_gnsdk_moodgrid
        lib_gnsdk_epg
        lib_gnsdk_taste

        ${log-lib}
        )
