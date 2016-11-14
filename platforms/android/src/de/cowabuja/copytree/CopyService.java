package de.cowabuja.copytree;

import android.content.ContentResolver;
import android.support.v4.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by uwe on 11.11.16.
 */

public class CopyService {
    public static void copy(ContentResolver contentResolver, DocumentFile sourceDir, DocumentFile targetDir) throws IOException {
        for (DocumentFile sourceFile : sourceDir.listFiles()) {
            if (sourceFile.isFile()) {
                DocumentFile targetFile = targetDir.createFile(sourceFile.getType(), sourceFile.getName());

                InputStream is = contentResolver.openInputStream(sourceFile.getUri());
                OutputStream os = contentResolver.openOutputStream(targetFile.getUri());

                copyInputToOutputStream(is, os);
            }
        }
    }

    private static void copyInputToOutputStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }
}
