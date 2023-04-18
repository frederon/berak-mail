package com.fsck.k9.activity;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.fsck.k9.ui.R;
import com.fsck.k9.ui.base.K9Activity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import timber.log.Timber;


public class Keygen extends K9Activity {
    private ActivityResultLauncher<Intent> savePrivateKeyLauncher;
    private ActivityResultLauncher<Intent> savePublicKeyLauncher;
    private String privateKey;
    private String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.digitalsign_keygen);

        // Generate key
        savePrivateKeyLauncher = registerForActivityResult(new StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getData();
                    if (privateKey != null) {
                        saveKeyToFile(uri, privateKey);
                        if (VERSION.SDK_INT >= VERSION_CODES.O) {
                            promptToSaveKeyFiles(false);
                        }
                    }
                }
            });

        savePublicKeyLauncher = registerForActivityResult(new StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getData();
                    if (publicKey != null) {
                        saveKeyToFile(uri, publicKey);
                    }
                }
            });

        Button keygenGenerateButton = findViewById(R.id.keygen_generate);
        keygenGenerateButton.setOnClickListener(v -> hitApiEndpoint());

        // Go back button
        View backButton = findViewById(R.id.keygen_done);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void hitApiEndpoint() {
        OkHttpClient client = new OkHttpClient();
        String BERAK_API_URL = "http://192.168.0.88:8000/elliptic_curve/generate_key";
        Request request = new Request.Builder()
            .url(BERAK_API_URL)
            .build();

        runOnUiThread(() ->
            Toast.makeText(Keygen.this, "Generating key...",
                Toast.LENGTH_SHORT).show());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        privateKey = jsonResponse.getString("private_key");
                        publicKey = jsonResponse.getString("public_key");

                        runOnUiThread(() -> promptToSaveKeyFiles(true));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() ->
                        Toast.makeText(Keygen.this, "Failed to get response from Generate Key API",
                            Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                    Toast.makeText(Keygen.this, "Failed to get response from Generate Key API",
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void promptToSaveKeyFiles(boolean isPrivateKey) {
        String fileName = isPrivateKey ? "private_key.txt" : "public_key.txt";
        ActivityResultLauncher<Intent> launcher = isPrivateKey ? savePrivateKeyLauncher : savePublicKeyLauncher;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            launcher.launch(createFileSaveIntent(fileName));
        } else {
            runOnUiThread(() ->
                Toast.makeText(Keygen.this, "Saving to files require a minimum of API level 26",
                    Toast.LENGTH_SHORT).show());
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    private Intent createFileSaveIntent(String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
        Uri uri = Uri.fromFile(file);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);

        return intent;
    }

    private void saveKeyToFile(Uri uri, String key) {
        Timber.tag("berak").d("Saving to files... content: %s", key);
        ContentResolver contentResolver = getContentResolver();
        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
            outputStream = contentResolver.openOutputStream(uri);
            if (outputStream != null) {
                bufferedOutputStream = new BufferedOutputStream(outputStream);
                bufferedOutputStream.write(key.getBytes());
                bufferedOutputStream.flush();
                Toast.makeText(Keygen.this, "File saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Keygen.this, "Failed to save file", Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(Keygen.this, "File not found", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(Keygen.this, "Error writing to file", Toast.LENGTH_LONG).show();
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
