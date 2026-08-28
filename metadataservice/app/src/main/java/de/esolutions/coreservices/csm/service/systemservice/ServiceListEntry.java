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
import java.util.List;

/**
 * LOCAL BUILD STUB: Replaces AIDL-generated ServiceListEntry
 * Note: Uses PUBLIC fields to match AIDL-generated Parcelable on device
 */
public class ServiceListEntry implements Parcelable {
    
    // PUBLIC fields - AIDL Parcelables use public fields, not getters
    public String serviceID;
    public boolean enableDisableServiceState;
    public int apn;
    public boolean tetheringAllowed;
    public List<HttpOperation> httpOperations;
    
    public ServiceListEntry() {}
    
    public ServiceListEntry(String serviceID, boolean enableDisableServiceState, 
                           int apn, boolean tetheringAllowed, List<HttpOperation> httpOperations) {
        this.serviceID = serviceID;
        this.enableDisableServiceState = enableDisableServiceState;
        this.apn = apn;
        this.tetheringAllowed = tetheringAllowed;
        this.httpOperations = httpOperations;
    }
    
    protected ServiceListEntry(Parcel in) {
        serviceID = in.readString();
        enableDisableServiceState = in.readByte() != 0;
        apn = in.readInt();
        tetheringAllowed = in.readByte() != 0;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceID);
        dest.writeByte((byte) (enableDisableServiceState ? 1 : 0));
        dest.writeInt(apn);
        dest.writeByte((byte) (tetheringAllowed ? 1 : 0));
    }
    
    public static final Creator<ServiceListEntry> CREATOR = new Creator<ServiceListEntry>() {
        @Override
        public ServiceListEntry createFromParcel(Parcel in) {
            return new ServiceListEntry(in);
        }
        
        @Override
        public ServiceListEntry[] newArray(int size) {
            return new ServiceListEntry[size];
        }
    };
}
