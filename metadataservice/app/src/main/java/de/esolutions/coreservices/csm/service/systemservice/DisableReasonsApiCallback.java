package de.esolutions.coreservices.csm.service.systemservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import java.util.List;

/**
 * LOCAL BUILD STUB: Replaces AIDL-generated DisableReasonsApiCallback
 */
public interface DisableReasonsApiCallback extends IInterface {
    
    void updateDisableReasons(List<DisableReasons> disableReasons);
    
    abstract class Stub extends Binder implements DisableReasonsApiCallback {
        
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }
        
        private static final String DESCRIPTOR = "de.esolutions.coreservices.csm.service.systemservice.DisableReasonsApiCallback";
        
        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}
