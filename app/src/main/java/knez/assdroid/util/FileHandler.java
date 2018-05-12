package knez.assdroid.util;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    private static final String UTF_BOM = "\uFEFF";

    @NonNull private final ContentResolver contentResolver;

    public FileHandler(@NonNull ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public List<String> readFileContent(@NonNull Uri uri) throws IOException {
        List<String> lines = new ArrayList<>();

        InputStream inputStream = contentResolver.openInputStream(uri);
        if(inputStream == null) throw new IOException("Could not open stream to " + uri);

        String currentLine;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((currentLine = reader.readLine()) != null)
                lines.add(currentLine);
        }

        inputStream.close(); // opened before try-with part, so must close ourselves

        if (lines.size() > 0 && lines.get(0).startsWith(UTF_BOM))
            lines.set(0, lines.get(0).substring(1));

        return lines;
    }

    public void writeFileContent(@NonNull Uri destPath, @NonNull List<String> serializedSubtitle)
            throws IOException {

        OutputStream outputStream = contentResolver.openOutputStream(destPath);
        if(outputStream == null) throw new IOException("Could not open stream to " + destPath);

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for(String line : serializedSubtitle) {
                writer.write(line);
                writer.write("\r\n");
            }
        }

        outputStream.close();
    }

    public String getFileNameFromUri(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                if(cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public boolean hasPermissionsToOpenUri(@NonNull Uri uri) {
        List<UriPermission> permissionList = contentResolver.getPersistedUriPermissions();
        for(UriPermission uriPermission : permissionList) {
            if(uriPermission.getUri().equals(uri)) {
                return true;
            }
        }

        return false;
    }

    public boolean takePermissionForUri(@NonNull Uri uri) {
        try {
            contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
