package eu.ttbox.freeyoutube.service;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import eu.ttbox.freeyoutube.core.AppConstant;

public class ServiceHelper {

    private static final String TAG = "ServiceHelper";

    /**
     * Check if the firewall is enabled
     * 
     * @param ctx
     *            mandatory context
     * @return boolean
     */
    public static boolean isEnabled(Context ctx) {
        if (ctx == null)
            return false;
        return ctx.getSharedPreferences(AppConstant.PREFS_NAME, 0).getBoolean(AppConstant.PREF_ENABLED, false);
    }

    /**
     * Defines if the firewall is enabled and broadcasts the new status
     * 
     * @param ctx
     *            mandatory context
     * @param enabled
     *            enabled flag
     */
    public static void setEnabled(Context ctx, boolean enabled) {
        if (ctx == null)
            return;
        final SharedPreferences prefs = ctx.getSharedPreferences(AppConstant.PREFS_NAME, 0);
        if (prefs.getBoolean(AppConstant.PREF_ENABLED, false) == enabled) {
            return;
        }
        final Editor edit = prefs.edit();
        edit.putBoolean(AppConstant.PREF_ENABLED, enabled);
        if (!edit.commit()) {
            RootApiHelper.alert(ctx, "Error writing to preferences");
            return;
        }
        /* notify */
        // final Intent message = new Intent(Api.STATUS_CHANGED_MSG);
        // message.putExtra(Api.STATUS_EXTRA, enabled);
        // ctx.sendBroadcast(message);
    }

    // ===========================================================
    // IPTables Rules
    // ===========================================================

    /**
     * Create the generic shell script header used to determine which iptables
     * binary to use.
     * 
     * @param ctx
     *            context
     * @return script header
     */
    private static String scriptHeader(Context ctx) {
        final String dir = ctx.getDir("bin", 0).getAbsolutePath();
        final String myiptables = dir + "/iptables_armv5";
        return "" + "IPTABLES=iptables\n" + "IP6TABLES=ip6tables\n" //
                + "BUSYBOX=busybox\n" + "GREP=grep\n" + "ECHO=echo\n" //
                + "# Try to find busybox\n" + "if " //
                + dir //
                + "/busybox_g1 --help >/dev/null 2>/dev/null ; then\n" //
                + " BUSYBOX=" //
                + dir //
                + "/busybox_g1\n" //
                + " GREP=\"$BUSYBOX grep\"\n" //
                + " ECHO=\"$BUSYBOX echo\"\n" //
                + "elif busybox --help >/dev/null 2>/dev/null ; then\n" //
                + " BUSYBOX=busybox\n" //
                + "elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then\n" //
                + " BUSYBOX=/system/xbin/busybox\n" //
                + "elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then\n" //
                + " BUSYBOX=/system/bin/busybox\n" //
                + "fi\n" //
                + "# Try to find grep\n" //
                + "if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" //
                + " if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then\n" //
                + "     GREP=\"$BUSYBOX grep\"\n" //
                + " fi\n" //
                + " # Grep is absolutely required\n" //
                + " if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" //
                + "     $ECHO The grep command is required. Android Script for update iptables will not work.\n" //
                + "     exit 1\n" //
                + " fi\n" //
                + "fi\n" //
                + "# Try to find iptables\n" //
                + "if " //
                + myiptables //
                + " --version >/dev/null 2>/dev/null ; then\n" //
                + " IPTABLES=" //
                + myiptables + "\n" + "fi\n" + ""; //
    }

    //iptables -A OUTPUT -p tcp -d 173.194.52.0/22 -j REJECT --reject-with tcp-reset
    // http://korben.info/free-et-youtube-comment-regler-le-souci-sous-windows-mac-et-linux.html
    // Working iptables -A OUTPUT -p tcp -d 173.194.52.0/22 -j REJECT 
    public static boolean applyIptablesRules(Context context, boolean showErrors) {
        Log.d(TAG, "*** ********************************************* ***");
        Log.d(TAG, "*** Run Script : BEGIN                            ***");
        Log.d(TAG, "*** ********************************************* ***");
        Log.d(TAG, "*** Run Script : Assert Binaries                  ***");
        RootApiHelper.assertBinaries(context, showErrors);
        // Script
        Log.d(TAG, "*** Run Script : Apply Iptables Rules              ***");
        final StringBuilder script = new StringBuilder(1024);
        script.append(scriptHeader(context));
        script.append("" //
                + "dmesg -c >/dev/null || exit\n" //
                + "$IPTABLES --version || exit 1\n" //
                + "# Create the freeYouTube_DTC chains if necessary\n" //
                + "$IPTABLES -L freeYouTube_DTC >/dev/null 2>/dev/null || $IPTABLES -N freeYouTube_DTC || exit 3\n" //
//                + "$IPTABLES -A freeYouTube_DTC -j DROP || exit 4\n" //
                + "$IPTABLES -A freeYouTube_DTC -j REJECT || exit 4\n" // --reject-with tcp-reset
                + "# Add freeYouTube chain to OUTPUT chain if necessary\n" //
                // +
                // "$IPTABLES -L OUTPUT | $GREP -q freeYouTube || $IPTABLES -A OUTPUT -p tcp -d 173.194.52.0/22 -j freeYouTube --reject-with tcp-reset || exit 11\n"
                // //
                // +
                // "$IPTABLES -A OUTPUT -d 173.194.52.0/22 -j freeYouTube || exit 11\n"
                // //
//                + "$IPTABLES -A OUTPUT -p tcp -d 173.194.34.0/22 -j REJECT || exit 15\n" //
//                + "$IPTABLES -A OUTPUT -p tcp -d 173.194.52.0/22 -j REJECT || exit 15\n" //
               // V new Chain
                + "$IPTABLES -A OUTPUT -d 173.194.52.0/22 -j freeYouTube_DTC || exit 11\n" //
//                + "$IPTABLES -A OUTPUT -d 173.194.34.0/22 -j freeYouTube_DTC || exit 11\n" //
//                + "$IPTABLES -L OUTPUT | $GREP -q freeYouTube_DTC ||  $IPTABLES -A OUTPUT -p tcp -d 173.194.52.0/22 -j freeYouTube_DTC || exit 11\n" //
//                 + "$IPTABLES -A freeYouTube -j REJECT || exit 15\n" //
//                 + "# Flush existing rules\n" //
//                + "$IPTABLES -F freeYouTube_DTC || exit 17\n" //
                + "" //
        );

        // Run Script
        final StringBuilder res = new StringBuilder();
        try {
            // Run Script
            int code = RootApiHelper.runScriptAsRoot(context, script.toString(), res);
            Log.d(TAG, "*** Run Script : Result Code = " + code + "             ***");
            // Show Errors
            if (showErrors && code != 0) {
                String msg = res.toString();
                Log.e(TAG, "*** Run Script : Error String = " + msg);
                // Remove unnecessary help message from output
                if (msg.indexOf("\nTry `iptables -h' or 'iptables --help' for more information.") != -1) {
                    msg = msg.replace("\nTry `iptables -h' or 'iptables --help' for more information.", "");
                }
            } else {
                Log.d(TAG, "*** Run Script : Result String = \n" + res);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if (showErrors)
                RootApiHelper.alert(context, "Error refreshing iptables: " + e);
        }
        Log.d(TAG, "*** ********************************************* ***");
        Log.d(TAG, "*** Run Script : END                              ***");
        Log.d(TAG, "*** ********************************************* ***");

        return false;
    }

}
