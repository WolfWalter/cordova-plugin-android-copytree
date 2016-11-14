package de.cowabuja.copytree;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class CopyTreePlugin extends CordovaPlugin {
    private static final String TAG = "CopyTreePlugin";
    private static final String ACTION_COPY_TO_INTERNAL = "copyToInternal";
    private static final String ACTION_COPY_TO_EXTERNAL = "copyToExternal";
    private static final int ACTION_COPY_TO_EXTERNAL_CODE = 55;
    private DocumentFile externalFile;
    private DocumentFile internalFile;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Activity activity = cordova.getActivity();
        internalFile = DocumentFile.fromFile(new File(activity.getApplicationInfo().dataDir));

        if (action.equals(ACTION_COPY_TO_INTERNAL)) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            cordova.startActivityForResult(this, intent, ACTION_COPY_TO_EXTERNAL_CODE);
        } else if (action.equals(ACTION_COPY_TO_EXTERNAL)) {
            if (externalFile == null) {
                Log.e(TAG, "external file not defined");
            }

            try {
                CopyService.copy(activity.getContentResolver(), internalFile, externalFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.execute(action, args, callbackContext);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACTION_COPY_TO_EXTERNAL_CODE && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                Activity activity = cordova.getActivity();
                externalFile = DocumentFile.fromTreeUri(
                        activity.getApplicationContext(), intent.getData());

                try {
                    CopyService.copy(activity.getContentResolver(), externalFile, internalFile);
                } catch (IOException e) {
                    //TODO exception handling is bad here...
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
