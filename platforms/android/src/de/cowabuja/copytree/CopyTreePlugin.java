package de.cowabuja.copytree;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

public class CopyTreePlugin extends CordovaPlugin {
    private static final String TAG = "CopyTreePlugin";
    private static final String TREE_EXPORT_DIR = "TREE_EXPORT";
    private static final String ACTION_COPY_TO_INTERNAL = "copyToInternal";
    private static final String ACTION_COPY_TO_EXTERNAL = "copyToExternal";
    private static final int ACTION_COPY_TO_EXTERNAL_CODE = 55;
    private DocumentFile cacheDir;
    private DocumentFile externalFile;
    private DocumentFile internalFile;
    private CallbackContext callback;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cacheDir = DocumentFile.fromFile(cordova.getActivity().getCacheDir());
    }

    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        boolean result = false;

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
        callback = callbackContext;

        if (action.equals(ACTION_COPY_TO_INTERNAL)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        copyToInternal(args.getBoolean(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });

            result = true;
        } else if (action.equals(ACTION_COPY_TO_EXTERNAL)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        copyToExternal(args.getBoolean(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });

            result = true;
        }

        return result;
    }

    private void copyToExternal(boolean includeDirs) {
        if (externalFile == null) {
            Log.e(TAG, "external file not defined");
        }

        try {
            for (DocumentFile internalChild : internalFile.listFiles()) {
                DocumentFile externalChild = externalFile.findFile(internalChild.getName());

                if (externalChild != null && externalChild.exists()) {
                    externalChild.delete();
                }
            }

            CopyService.copy(cordova.getActivity().getContentResolver(), internalFile, externalFile, includeDirs);
            callback.success();
        } catch (IOException e) {
            e.printStackTrace();
            callback.error(e.getMessage());
        } catch (JSONException e) {
            callback.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void copyToInternal(boolean showFileChooser) {
        if (showFileChooser) {
            internalFile = cacheDir.createDirectory(TREE_EXPORT_DIR);
            for (DocumentFile child : internalFile.listFiles()) {
                child.delete();
            }
            Log.i(TAG, "internal directory: " + internalFile.getUri());

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            cordova.startActivityForResult(this, intent, ACTION_COPY_TO_EXTERNAL_CODE);
        } else {
            try {
                CopyService.copy(cordova.getActivity().getContentResolver(), externalFile, internalFile, false);
                callback.success();
            } catch (IOException e) {
                e.printStackTrace();
                callback.error(e.getMessage());
            } catch (JSONException e) {
                callback.error(e.getMessage());
                e.printStackTrace();
            }
        }
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
                JSONObject filesDataJson = CopyService.copy(activity.getContentResolver(), externalFile, internalFile, false);
                callback.success(filesDataJson);
            } catch (IOException e) {
                callback.error(e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                callback.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
