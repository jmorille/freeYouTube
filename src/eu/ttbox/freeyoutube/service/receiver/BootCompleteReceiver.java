package eu.ttbox.freeyoutube.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import eu.ttbox.freeyoutube.service.RootApiHelper;
import eu.ttbox.freeyoutube.service.ServiceHelper;

public class BootCompleteReceiver extends BroadcastReceiver  {

    private static final String TAG=  "BootCompleteReceiver";
    
    
    private static final String BOOT_COMPLETED=  "android.intent.action.BOOT_COMPLETED";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BOOT_COMPLETED.equals(intent.getAction())){
            final boolean enabled = ServiceHelper.isEnabled(context);
            if (enabled) {
                Log.d("Android Firewall", "Applying rules.");
                if (RootApiHelper.hasRootAccess(context, true)
                        && ServiceHelper.applyIptablesRules(context,
                                true)) {
                    Log.i(TAG,
                            "Enabled - Firewall successfully enabled on boot.");
                }
            } else {
                Log.i(TAG, "Failed - Disabling firewall."); 
            }
        }
    }

}
