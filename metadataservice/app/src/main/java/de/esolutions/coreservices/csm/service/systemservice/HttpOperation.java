package de.esolutions.coreservices.csm.service.systemservice;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

/**
 * LOCAL BUILD STUB: Replaces AIDL-generated HttpOperation
 */
public class HttpOperation implements Parcelable {
    
    private String url;
    private Operation operation;
    
    public HttpOperation() {}
    
    public HttpOperation(String url, Operation operation) {
        this.url = url;
        this.operation = operation;
    }
    
    protected HttpOperation(Parcel in) {
        url = in.readString();
    }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public Operation getOperation() { return operation; }
    public void setOperation(Operation operation) { this.operation = operation; }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }
    
    public static final Creator<HttpOperation> CREATOR = new Creator<HttpOperation>() {
        @Override
        public HttpOperation createFromParcel(Parcel in) {
            return new HttpOperation(in);
        }
        
        @Override
        public HttpOperation[] newArray(int size) {
            return new HttpOperation[size];
        }
    };
}
