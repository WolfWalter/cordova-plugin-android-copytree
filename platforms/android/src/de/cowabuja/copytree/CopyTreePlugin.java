package de.cowabuja.copytree;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.net.Uri;

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
    private static final String ACTION_GET_FILES_FROM_FILE_CHOOSER = "getFilesFromFileChooser";
    private static final String ACTION_COPY_TO_INTERNAL = "copyToInternal";
    private static final String ACTION_COPY_TO_EXTERNAL = "copyToExternal";
    private static final int ACTION_COPY_TO_EXTERNAL_CODE = 55;
    private static final int ACTION_GET_FILES = 56;
    private DocumentFile cacheDir;
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
                        copyToInternal(args.getString(0));
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
                        copyToExternal(args.getString(0), args.getBoolean(1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });

            result = true;
        } else if (action.equals(ACTION_GET_FILES_FROM_FILE_CHOOSER)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    getFilesFromFileChooser();
                }
            });

            result = true;
        }

        return result;
    }

    private void getFilesFromFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        cordova.startActivityForResult(this, intent, ACTION_GET_FILES);
    }

    private void copyToExternal(String externalPath, boolean includeDirs) {
        Log.i(TAG, "external path: " + externalPath);
        try {
            DocumentFile externalFile = DocumentFile.fromTreeUri(
                    cordova.getActivity().getApplicationContext(), Uri.parse(externalPath));

            Log.i(TAG, "external directory: " + externalFile.getUri());

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
        } catch (NullPointerException e){
            e.printStackTrace();
            callback.error(e.getMessage());
        }
    }

    private void copyToInternal(String externalPath) {
        Log.i(TAG, "external path: " + externalPath);
        internalFile = cacheDir.createDirectory(TREE_EXPORT_DIR);
        for (DocumentFile child : internalFile.listFiles()) {
            child.delete();
        }
        Log.i(TAG, "internal directory: " + internalFile.getUri());

        try {
            DocumentFile externalFile = DocumentFile.fromTreeUri(
                    cordova.getActivity().getApplicationContext(), Uri.parse(externalPath));

            Log.i(TAG, "external directory: " + externalFile.getUri());

            CopyService.copy(cordova.getActivity().getContentResolver(), externalFile, internalFile, false);
            callback.success();
        } catch (IOException e) {
            callback.error(e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
            callback.error(e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == ACTION_GET_FILES
                && resultCode == Activity.RESULT_OK
                && callback != null
                && intent != null){
            Activity activity = cordova.getActivity();
            DocumentFile externalFile = DocumentFile.fromTreeUri(
                    activity.getApplicationContext(), intent.getData());
            Log.i(TAG, "external directory to get files: " + externalFile.getUri());


            try {
                JSONObject resultJson = CopyService.getFiles(activity.getContentResolver(), externalFile);
                callback.success(resultJson);
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
