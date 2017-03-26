package de.cowabuja.copytree;

import android.content.ContentResolver;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONObject;
import org.json.JSONException;

public class CopyService {
    private static final String TAG = "CopyService";

    public static JSONObject copy(ContentResolver contentResolver, DocumentFile sourceDir, DocumentFile targetDir, boolean includeDirs) throws IOException, JSONException   {
        Log.i(TAG, "copy " + sourceDir.getName() + " to " + targetDir.getName());

        JSONObject filesDataJson = new JSONObject();

        for (DocumentFile sourceFile : sourceDir.listFiles()) {
            if (sourceFile.isDirectory() && includeDirs) {
                CopyService.copy(
                        contentResolver,
                        sourceFile,
                        targetDir.createDirectory(sourceFile.getName()),
                        includeDirs);
            }
            if (sourceFile.isFile()) {
                DocumentFile targetFile = targetDir.createFile(null, sourceFile.getName());

                InputStream is = contentResolver.openInputStream(sourceFile.getUri());
                OutputStream os = contentResolver.openOutputStream(targetFile.getUri());

                copyInputToOutputStream(is, os);
                
                long lastModified = sourceFile.lastModified();
                Log.i("lastMod", "lastMod " + sourceFile.getName() + " is " + lastModified);
                filesDataJson.put(sourceFile.getName(), lastModified);
                // cannot set lastmodified in android
            }
        }

        return filesDataJson;
    }

    private static void copyInputToOutputStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024*32];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }

        os.flush();
        is.close();
        os.close();
    }
}
