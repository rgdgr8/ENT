/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace AIDL-generated class for standalone builds.
 */
package de.esolutions.coreservices.csm.service.systemservice;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * LOCAL BUILD STUB: Replaces AIDL-generated DisableReasons
 * IMPORTANT: Device uses public fields, not getter methods!
 */
public class DisableReasons implements Parcelable {
    
    // PUBLIC FIELDS - device jar uses public fields, not getters
    public String serviceId;
    public int[] reasonBackend;
    public int[] reasonConfiguration;
    public int[] reasonConnectivity;
    public int[] reasonLicense;
    
    public DisableReasons() {
        reasonBackend = new int[0];
        reasonConfiguration = new int[0];
        reasonConnectivity = new int[0];
        reasonLicense = new int[0];
    }
    
    public DisableReasons(String serviceId, int[] reasonBackend, 
                         int[] reasonConfiguration, int[] reasonConnectivity,
                         int[] reasonLicense) {
        this.serviceId = serviceId;
        this.reasonBackend = reasonBackend != null ? reasonBackend : new int[0];
        this.reasonConfiguration = reasonConfiguration != null ? reasonConfiguration : new int[0];
        this.reasonConnectivity = reasonConnectivity != null ? reasonConnectivity : new int[0];
        this.reasonLicense = reasonLicense != null ? reasonLicense : new int[0];
    }
    
    protected DisableReasons(Parcel in) {
        serviceId = in.readString();
        reasonBackend = in.createIntArray();
        reasonConfiguration = in.createIntArray();
        reasonConnectivity = in.createIntArray();
        reasonLicense = in.createIntArray();
    }
    
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceId);
        dest.writeIntArray(reasonBackend);
        dest.writeIntArray(reasonConfiguration);
        dest.writeIntArray(reasonConnectivity);
        dest.writeIntArray(reasonLicense);
    }
    
    public static final Creator<DisableReasons> CREATOR = new Creator<DisableReasons>() {
        @Override
        public DisableReasons createFromParcel(Parcel in) {
            return new DisableReasons(in);
        }
        
        @Override
        public DisableReasons[] newArray(int size) {
            return new DisableReasons[size];
        }
    };
}
