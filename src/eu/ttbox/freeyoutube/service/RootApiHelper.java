package eu.ttbox.freeyoutube.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import eu.chainfire.libsuperuser.Shell;
import eu.ttbox.freeyoutube.R;

public class RootApiHelper {

 // Do we have root access?
    private static boolean hasroot = false;
    
    /**
     * Display a simple alert box
     * 
     * @param ctx
     *            context
     * @param msg
     *            message
     */
    public static void alert(Context ctx, CharSequence msg) {
        if (ctx != null) {
            new AlertDialog.Builder(ctx)
                    .setNeutralButton(android.R.string.ok, null)
                    .setMessage(msg).show();
        }
    }


    // ===========================================================
    // Accessors
    // ===========================================================


    /**
     * Check if we have root access
     * 
     * @param ctx
     *            mandatory context
     * @param showErrors
     *            indicates if errors should be alerted
     * @return boolean true if we have root
     */
    public static boolean hasRootAccess(final Context ctx, boolean showErrors) {
        if (hasroot)
            return true;
        final StringBuilder res = new StringBuilder();
        try {
            // Run an empty script just to check root access
            if (runScriptAsRoot(ctx, "exit 0", res) == 0) {
                hasroot = true;
                return true;
            }
        } catch (Exception e) {
            Log.d("Android Firewall - No root access available", e.getMessage());
        }
        if (showErrors) {
            alert(ctx,
                    "Could not acquire root access.\n"
                            + "You need a rooted phone to run DroidWall.\n\n"
                            + "If this phone is already rooted, please make sure DroidWall has enough permissions to execute the \"su\" command.\n"
                            + "Error message: " + res.toString());
        }
        return false;
    }
    

    // ===========================================================
    // Assert Binaries
    // ===========================================================

    /**
     * Asserts that the binary files are installed in the cache directory.
     * 
     * @param ctx
     *            context
     * @param showErrors
     *            indicates if errors should be alerted
     * @return false if the binary files could not be installed
     */
    public static boolean assertBinaries(Context ctx, boolean showErrors) {
        boolean changed = false;
        try {
            // Check iptables_armv5
            File file = new File(ctx.getDir("bin", 0), "iptables_armv5");
            if (!file.exists() || file.length() != 198652) {
                copyRawFile(ctx, R.raw.iptables_armv5, file, "755");
                changed = true;
            }
            // Check busybox
            file = new File(ctx.getDir("bin", 0), "busybox_g1");
            if (!file.exists()) {
                copyRawFile(ctx, R.raw.busybox_g1, file, "755");
                changed = true;
            }
            if (changed) {
                Toast.makeText(ctx, R.string.toast_bin_installed,
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            if (showErrors)
                alert(ctx, "Error installing binary files: " + e);
            return false;
        }
        return true;
    }
    
    /**
     * Copies a raw resource file, given its ID to the given location
     * 
     * @param ctx
     *            context
     * @param resid
     *            resource id
     * @param file
     *            destination file
     * @param mode
     *            file permissions (E.g.: "755")
     * @throws IOException
     *             on error
     * @throws InterruptedException
     *             when interrupted
     */
    private static void copyRawFile(Context ctx, int resid, File file,
            String mode) throws IOException, InterruptedException {
        final String abspath = file.getAbsolutePath();
        // Write the iptables binary
        final FileOutputStream out = new FileOutputStream(file);
        final InputStream is = ctx.getResources().openRawResource(resid);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        // Change the permissions
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }
    // ===========================================================
    // Run Script As Root
    // ===========================================================


    

    /**
     * Runs a script as root (multiple commands separated by "\n") with a
     * default timeout of 20 seconds.
     * 
     * @param ctx
     *            mandatory context
     * @param script
     *            the script to be executed
     * @param res
     *            the script output response (stdout + stderr)
     * @param timeout
     *            timeout in milliseconds (-1 for none)
     * @return the script exit code
     * @throws IOException
     *             on any error executing the script, or writing it to disk
     */
    public static int runScriptAsRoot(Context ctx, String script,
            StringBuilder res) throws IOException {
        return runScriptAsRoot(ctx, script, res, 40000);
    }
    

    /**
     * Runs a script as root (multiple commands separated by "\n").
     * 
     * @param ctx
     *            mandatory context
     * @param script
     *            the script to be executed
     * @param res
     *            the script output response (stdout + stderr)
     * @param timeout
     *            timeout in milliseconds (-1 for none)
     * @return the script exit code
     */
    public static int runScriptAsRoot(Context ctx, String script,
            StringBuilder res, long timeout) {
        return runScript(ctx, script, res, timeout, true);
    }
    
    /**
     * Runs a script, wither as root or as a regular user (multiple commands
     * separated by "\n").
     */
    public static int runScript(Context ctx, String script, StringBuilder res,
            long timeout, boolean asroot) {
        int returncode = -1;
        try {
            returncode = new RunScriptAsRootAsyncTask().execute(script, res).get();
        } catch (Exception e) {
            Log.d("Android Firewall - error applying iptables in runScript",
                    e.getMessage());
            Toast.makeText(ctx, "There was an error applying the iptables.",
                    Toast.LENGTH_LONG).show();
        }
        return returncode;
    }
    
    

    /**
     * Internal thread used to execute scripts (as root or not).
     */
    private static class RunScriptAsRootAsyncTask extends
            AsyncTask<Object, String, Integer> {

        private int exitcode = -1;

        @Override
        protected Integer doInBackground(Object... parameters) {
            final String script = (String) parameters[0];
            final StringBuilder resources = (StringBuilder) parameters[1];
            final String[] commands = script.split("\n");
            try {
                // check for SU
                if (!Shell.SU.available())
                    return exitcode;
                if (script != null && script.length() > 0) {
                    // apply the rules
                    List<String> rules = Shell.SU.run(commands);
                    if (rules != null && rules.size() > 0) {
                        for (String script2 : rules) {
                            resources.append(script2);
                            resources.append("\n");
                        }
                    }
                    exitcode = 0;
                }
            } catch (Exception e) {
                if (resources != null)
                    resources.append("\n" + e);
            }
            return exitcode;
        }
    }
    

    // ===========================================================
    // Other
    // ===========================================================


    
}
