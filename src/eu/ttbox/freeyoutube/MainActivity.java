package eu.ttbox.freeyoutube;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import eu.ttbox.freeyoutube.core.AppConstant;
import eu.ttbox.freeyoutube.service.ServiceHelper;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    // Service
    private SharedPreferences prefs;

 
    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Service
        prefs = getSharedPreferences(AppConstant.PREFS_NAME, Context.MODE_PRIVATE);
 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // ===========================================================
    // Action
    // ===========================================================

    public void freeYouTube(View view) {
        Log.i(TAG, "Enable ");

        // Get Default Ips List
        String[] defaultIpToBlocks = getResources().getStringArray(R.array.default_block_ips);
        
        // Apply
        ServiceHelper.applyIptablesRules(this, true, defaultIpToBlocks);
    }

}
