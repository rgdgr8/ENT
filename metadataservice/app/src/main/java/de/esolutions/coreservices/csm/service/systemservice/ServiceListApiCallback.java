package de.esolutions.coreservices.csm.service.systemservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import java.util.List;

/**
 * LOCAL BUILD STUB: Replaces AIDL-generated ServiceListApiCallback
 */
public interface ServiceListApiCallback extends IInterface {
    
    void updateServiceList(List<ServiceListEntry> services);
    
    abstract class Stub extends Binder implements ServiceListApiCallback {
        
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }
        
        private static final String DESCRIPTOR = "de.esolutions.coreservices.csm.service.systemservice.ServiceListApiCallback";
        
        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}
