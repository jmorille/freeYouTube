package eu.ttbox.freeyoutube.service.receiver;

import java.util.Collection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import eu.ttbox.freeyoutube.R;
import eu.ttbox.freeyoutube.core.AppConstant;
import eu.ttbox.freeyoutube.service.RootApiHelper;
import eu.ttbox.freeyoutube.service.ServiceHelper;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";

    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BOOT_COMPLETED.equals(intent.getAction())) {
            final boolean enabled = ServiceHelper.isEnabled(context);
            if (enabled) {
                Log.d("Android Firewall", "Applying rules.");
                if (RootApiHelper.hasRootAccess(context, true)) {
                    // Get Default Ips List
                    Resources r = context.getResources();
                    String[] defaultIpToBlocks = r.getStringArray(R.array.default_block_ips);
                    // TODO Replace by the preferences Read
//                    SharedPreferences prefs = context.getSharedPreferences(AppConstant.PREFS_NAME, Context.MODE_PRIVATE);
//                    Collection<String> ipToBlocks = prefs.getStringSet(AppConstant.PREF_IP_BLOCK_ARRAY, null);
                    
                    if (ServiceHelper.applyIptablesRules(context, true, defaultIpToBlocks)) {
                        Log.i(TAG, "Enabled - Firewall successfully enabled on boot.");
                    }
                } else {
                    Log.i(TAG, "Failed - Root acces not available");

                }
            } else {
                Log.i(TAG, "Failed - Disabling firewall.");
            }
        }
    }

}
