package de.cowabuja.copytree;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class CopyTreePlugin extends CordovaPlugin {
    private static final String TAG = "CopyTreePlugin";
    private static final String TREE_EXPORT_DIR = "TREE_EXPORT";
    private static final String ACTION_COPY_TO_INTERNAL = "copyToInternal";
    private static final String ACTION_COPY_TO_EXTERNAL = "copyToExternal";
    private static final int ACTION_COPY_TO_EXTERNAL_CODE = 55;
    private DocumentFile externalFile;
    private DocumentFile internalFile;
    private CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "execute");
        boolean result = false;
        Activity activity = cordova.getActivity();

        internalFile = DocumentFile.fromFile(new File(activity.getCacheDir(), TREE_EXPORT_DIR));
        Log.i(TAG, "internal directory: " + internalFile.getUri());

        if (action.equals(ACTION_COPY_TO_INTERNAL)) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            cordova.startActivityForResult(this, intent, ACTION_COPY_TO_EXTERNAL_CODE);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            callback = callbackContext;
            result = true;
        } else if (action.equals(ACTION_COPY_TO_EXTERNAL)) {
            if (externalFile == null) {
                Log.e(TAG, "external file not defined");
            }

            try {
                CopyService.copy(activity.getContentResolver(), internalFile, externalFile);
                callbackContext.success();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        }

        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACTION_COPY_TO_EXTERNAL_CODE
                && resultCode == Activity.RESULT_OK
                && callback != null
                && intent != null) {

            Activity activity = cordova.getActivity();
            externalFile = DocumentFile.fromTreeUri(
                    activity.getApplicationContext(), intent.getData());
            Log.i(TAG, "external directory: " + externalFile.getUri());

            try {
                CopyService.copy(activity.getContentResolver(), externalFile, internalFile);
                callback.success();
            } catch (IOException e) {
                callback.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
