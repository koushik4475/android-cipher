package com.example.encryptiondecryption;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService cryptoExecutor = Executors.newSingleThreadExecutor();

    private View rootView;
    private TextInputEditText inputText;
    private TextInputEditText passwordInput;
    private TextInputEditText outputText;
    private MaterialButton encryptButton;
    private MaterialButton decryptButton;
    private MaterialButton copyButton;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.rootView);
        inputText = findViewById(R.id.inputText);
        passwordInput = findViewById(R.id.passwordInput);
        outputText = findViewById(R.id.outputText);
        encryptButton = findViewById(R.id.encryptButton);
        decryptButton = findViewById(R.id.decryptButton);
        copyButton = findViewById(R.id.copyButton);
        statusText = findViewById(R.id.statusText);

        encryptButton.setOnClickListener(view -> processText(true));
        decryptButton.setOnClickListener(view -> processText(false));
        copyButton.setOnClickListener(view -> copyOutput());
        findViewById(R.id.clearButton).setOnClickListener(view -> clearFields());
    }

    private void processText(boolean encrypt) {
        String source = inputText.getText() == null ? "" : inputText.getText().toString();
        String password = passwordInput.getText() == null ? "" : passwordInput.getText().toString();

        if (source.isEmpty()) {
            showMessage("Enter text before continuing.");
            return;
        }
        if (password.isEmpty()) {
            showMessage("Enter your encryption password.");
            return;
        }
        if (encrypt && password.length() < 12) {
            showMessage("Use a password with at least 12 characters.");
            return;
        }

        char[] passwordChars = password.toCharArray();
        setProcessing(true);
        statusText.setText(encrypt ? R.string.status_encrypting : R.string.status_decrypting);

        cryptoExecutor.execute(() -> {
            try {
                String result = encrypt
                        ? CryptoManager.encrypt(source, passwordChars)
                        : CryptoManager.decrypt(source, passwordChars);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    outputText.setText(result);
                    statusText.setText(encrypt
                            ? R.string.status_encrypted
                            : R.string.status_decrypted);
                    setProcessing(false);
                    copyButton.setEnabled(true);
                });
            } catch (IllegalArgumentException exception) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    statusText.setText(R.string.status_ready);
                    setProcessing(false);
                    showMessage(exception.getMessage() == null
                            ? "Check your input and try again." : exception.getMessage());
                });
            } catch (GeneralSecurityException exception) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    statusText.setText(R.string.status_ready);
                    setProcessing(false);
                    showMessage(encrypt
                            ? "Encryption failed. Please try again."
                            : "Decryption failed. Check the password or encrypted text.");
                });
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
        });
    }

    private void copyOutput() {
        String value = outputText.getText() == null ? "" : outputText.getText().toString();
        if (value.isEmpty()) {
            showMessage("There is no output to copy.");
            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Android Cipher output", value));
        showMessage("Output copied to clipboard.");
    }

    private void clearFields() {
        inputText.setText("");
        passwordInput.setText("");
        outputText.setText("");
        copyButton.setEnabled(false);
        statusText.setText(R.string.status_ready);
        showMessage("All fields cleared.");
    }

    private void setProcessing(boolean processing) {
        encryptButton.setEnabled(!processing);
        decryptButton.setEnabled(!processing);
        copyButton.setEnabled(!processing && outputText.getText() != null
                && outputText.getText().length() > 0);
    }

    private void showMessage(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        cryptoExecutor.shutdownNow();
        super.onDestroy();
    }
}
