package de.esolutions.coreservices.csm.service.systemservice;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * LOCAL BUILD STUB: Replaces AIDL-generated Authentication
 */
public class Authentication implements Parcelable {
    
    private String scope;
    private int type;
    
    public Authentication() {}
    
    public Authentication(String scope, int type) {
        this.scope = scope;
        this.type = type;
    }
    
    protected Authentication(Parcel in) {
        scope = in.readString();
        type = in.readInt();
    }
    
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(scope);
    }
    
    public static final Creator<Authentication> CREATOR = new Creator<Authentication>() {
        @Override
        public Authentication createFromParcel(Parcel in) {
            return new Authentication(in);
        }
        
        @Override
        public Authentication[] newArray(int size) {
            return new Authentication[size];
        }
    };
}
