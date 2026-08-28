package de.esolutions.coreservices.csm.service.systemservice;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

/**
 * LOCAL BUILD STUB: Replaces AIDL-generated Operation
 */
public class Operation implements Parcelable {
    
    private String id;
    private List<Authentication> authentication;
    
    public Operation() {}
    
    public Operation(String id, List<Authentication> authentication) {
        this.id = id;
        this.authentication = authentication;
    }
    
    protected Operation(Parcel in) {
        id = in.readString();
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public List<Authentication> getAuthentication() { return authentication; }
    public void setAuthentication(List<Authentication> auth) { this.authentication = auth; }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
    }
    
    public static final Creator<Operation> CREATOR = new Creator<Operation>() {
        @Override
        public Operation createFromParcel(Parcel in) {
            return new Operation(in);
        }
        
        @Override
        public Operation[] newArray(int size) {
            return new Operation[size];
        }
    };
}
