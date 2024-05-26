package com.darryncampbell.plugin.intent;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class IntentContentReader {
    private static final String LOG_TAG = "IntentContentReader";

    public IntentContentReader() {
    }

    // ===================================================================================================================
    // dom, 2023-12-19, [6960]: Datei-Content-Auslesen für ACTION_VIEW-Intents (in Java klappts, im Gegensatz zu JS)
    // ===================================================================================================================

    /**
     * Liest bei ACTION_VIEW-Intents den Content der übergebenen Datei (Uri) aus und
     * hängt diesen als Extra-Wert `content` an das Intent an. [6960]
     * <p>
     * + Falls ein Fehler auftritt wird die Message als Extra-Wert `contentError` an das Intent angehängt.
     * + `content` ist dann leer
     * + `contentError` ist leer, falls kein Fehler aufgetreten ist
     */
    public void attachViewIntentContentToIntent(@NonNull Intent intent, ContentResolver contentResolver) {
        Log.d(LOG_TAG, "attachViewIntentContentToIntent()");

        // dom, 2023-12-19, [6960]: Datei-Content ggf. lesen und als String an die JS-Schicht übergeben
        String content = null;

        if (null == intent.getAction()) {
            return;
        }

        Log.d(LOG_TAG, "action: " + intent.getAction());

        if (intent.getAction().equals(Intent.ACTION_VIEW)
                || intent.getAction().equals(Intent.ACTION_EDIT)
        ) {
            // dom, 2023-12-21: nur lesen, falls die Datei nicht breits ausgelesen wurde
            if (intent.hasExtra("content")) {
                return;
            }

            Uri uri = intent.getData();
            if (uri != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    InputStream inputStream = contentResolver.openInputStream(uri);

                    if (null != inputStream) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            FileUtils.copy(inputStream, byteArrayOutputStream);
                            content = byteArrayOutputStream.toString();
                            byteArrayOutputStream = null;
                        } else {
                            content = createStringFromInputStream(inputStream);
                        }

                        inputStream.close();
                    }

                    intent.putExtra("content", content);
                    intent.putExtra("contentError", "");
                } catch (Exception e) {
                    Log.d(LOG_TAG, "attachViewIntentContentToIntent() - inputStream-Exception", e);

                    intent.putExtra("content", content);
                    intent.putExtra("contentError", e.getMessage());
                }
            }
        }
    }

    /**
     * Liest den Streaminhalt aus und gibt ihn als String zurück (für Android API < 10, Level <= 29).
     *
     * @return Stream-/Dateiinhalt
     * @throws IOException falls es Lesefehler gibt
     */
    @NonNull
    private String createStringFromInputStream(InputStream inputStream) throws IOException {
        int t = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        StringWriter writer = new StringWriter();
        while (-1 != (t = reader.read(buffer))) {
            writer.write(buffer, 0, t);
        }
        return writer.toString();
    }
}
