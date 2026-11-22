package com.example.encryptiondecryption;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipDescription;
import android.os.Build;
import android.os.PersistableBundle;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
    private TextView outputText;
    private MaterialButton encryptButton;
    private MaterialButton decryptButton;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private MaterialButton pasteButton;
    private MaterialButton clearButton;
    private TextView statusText;
    private TextView inputCount;
    private LinearProgressIndicator processingIndicator;
    private boolean processing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.rootView);
        inputText = findViewById(R.id.inputText);
        passwordInput = findViewById(R.id.passwordInput);
        outputText = findViewById(R.id.outputText);
        encryptButton = findViewById(R.id.encryptButton);
        decryptButton = findViewById(R.id.decryptButton);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        pasteButton = findViewById(R.id.pasteButton);
        clearButton = findViewById(R.id.clearButton);
        statusText = findViewById(R.id.statusText);
        inputCount = findViewById(R.id.inputCount);
        processingIndicator = findViewById(R.id.processingIndicator);

        applySystemBarInsets();
        applyContentMaxWidth();
        bindActions();
        updateCharacterCount();
    }

    private void applySystemBarInsets() {
        final int nightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        final boolean dark = nightMode == Configuration.UI_MODE_NIGHT_YES;
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), rootView);
        controller.setAppearanceLightStatusBars(!dark);
        controller.setAppearanceLightNavigationBars(!dark);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(view.getPaddingLeft(), bars.top, view.getPaddingRight(),
                    Math.max(bars.bottom, ime.bottom) + 12);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(rootView);
    }

    private void applyContentMaxWidth() {
        rootView.addOnLayoutChangeListener((view, left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom) -> {
            View content = ((ViewGroup) rootView).getChildAt(0);
            int maxWidth = (int) (560 * getResources().getDisplayMetrics().density);
            int availableWidth = right - left;
            int targetWidth = Math.min(availableWidth, maxWidth);
            ViewGroup.LayoutParams params = content.getLayoutParams();
            if (params.width != targetWidth) {
                params.width = targetWidth;
                content.setLayoutParams(params);
            }
        });
    }

    private void bindActions() {
        encryptButton.setOnClickListener(view -> processText(true));
        decryptButton.setOnClickListener(view -> processText(false));
        copyButton.setOnClickListener(view -> copyOutput());
        shareButton.setOnClickListener(view -> shareOutput());
        pasteButton.setOnClickListener(view -> pasteInput());
        clearButton.setOnClickListener(view -> clearFields());

        inputText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharacterCount();
                if (!processing) {
                    outputText.setText("");
                    copyButton.setEnabled(false);
                    shareButton.setEnabled(false);
                    statusText.setText(R.string.status_ready);
                    statusText.setTextColor(getColor(R.color.accent));
                }
            }
            @Override public void afterTextChanged(Editable editable) { }
        });
    }

    private void processText(boolean encrypt) {
        if (processing) return;

        String source = inputText.getText() == null ? "" : inputText.getText().toString();
        Editable passwordEditable = passwordInput.getText();
        int passwordLength = passwordEditable == null ? 0 : passwordEditable.length();

        if (source.trim().isEmpty()) {
            inputText.setError(getString(R.string.error_enter_text));
            inputText.requestFocus();
            return;
        }
        inputText.setError(null);

        if (passwordLength == 0) {
            passwordInput.setError(getString(R.string.error_enter_password));
            passwordInput.requestFocus();
            return;
        }
        passwordInput.setError(null);

        if (encrypt && passwordLength < 12) {
            passwordInput.setError(getString(R.string.error_short_password));
            passwordInput.requestFocus();
            return;
        }

        char[] passwordChars = new char[passwordLength];
        passwordEditable.getChars(0, passwordLength, passwordChars, 0);
        passwordInput.setText("");
        outputText.setText("");
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);
        setProcessing(true);
        statusText.setTextColor(getColor(R.color.accent));
        statusText.setText(encrypt ? R.string.status_encrypting : R.string.status_decrypting);
        hideKeyboard();

        cryptoExecutor.execute(() -> {
            try {
                String result = encrypt
                        ? CryptoManager.encrypt(source, passwordChars)
                        : CryptoManager.decrypt(source, passwordChars);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    outputText.setText(result);
                    statusText.setText(encrypt
                            ? R.string.status_encrypted : R.string.status_decrypted);
                    setProcessing(false);
                    copyButton.setEnabled(true);
                    shareButton.setEnabled(true);
                    showMessage(encrypt
                            ? getString(R.string.message_encrypted)
                            : getString(R.string.message_decrypted));
                });
            } catch (IllegalArgumentException exception) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    statusText.setText(R.string.status_error);
                    statusText.setTextColor(getColor(R.color.status_error));
                    setProcessing(false);
                    showMessage(exception.getMessage() == null
                            ? getString(R.string.error_generic) : exception.getMessage());
                });
            } catch (GeneralSecurityException exception) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    statusText.setText(R.string.status_error);
                    statusText.setTextColor(getColor(R.color.status_error));
                    setProcessing(false);
                    showMessage(encrypt
                            ? getString(R.string.error_encrypt)
                            : getString(R.string.error_decrypt));
                });
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
        });
    }

    private void pasteInput() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()
                || clipboard.getPrimaryClip() == null
                || clipboard.getPrimaryClip().getItemCount() == 0) {
            showMessage(getString(R.string.error_clipboard_empty));
            return;
        }
        CharSequence clip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(this);
        if (clip == null || clip.length() == 0) {
            showMessage(getString(R.string.error_clipboard_empty));
            return;
        }
        inputText.setText(clip);
        inputText.setSelection(inputText.length());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            showMessage(getString(R.string.message_pasted));
        }
    }

    private void copyOutput() {
        String value = outputText.getText() == null ? "" : outputText.getText().toString();
        if (value.isEmpty()) {
            showMessage(getString(R.string.error_no_output));
            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            showMessage(getString(R.string.error_clipboard_unavailable));
            return;
        }
        ClipData clip = ClipData.newPlainText(getString(R.string.clip_label), value);
        PersistableBundle clipboardExtras = new PersistableBundle();
        String sensitiveFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? ClipDescription.EXTRA_IS_SENSITIVE : "android.content.extra.IS_SENSITIVE";
        clipboardExtras.putBoolean(sensitiveFlag, true);
        clip.getDescription().setExtras(clipboardExtras);
        clipboard.setPrimaryClip(clip);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            showMessage(getString(R.string.message_copied));
        }
    }

    private void shareOutput() {
        String value = outputText.getText() == null ? "" : outputText.getText().toString();
        if (value.isEmpty()) {
            showMessage(getString(R.string.error_no_output));
            return;
        }
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, value);
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
    }

    private void clearFields() {
        if (processing) return;
        clearAppClipboardIfOwned();
        inputText.setText("");
        passwordInput.setText("");
        outputText.setText("");
        inputText.setError(null);
        passwordInput.setError(null);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);
        statusText.setText(R.string.status_ready);
        statusText.setTextColor(getColor(R.color.accent));
        inputText.requestFocus();
        showMessage(getString(R.string.message_cleared));
    }

    private void clearAppClipboardIfOwned() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) return;

        ClipDescription description = clipboard.getPrimaryClipDescription();
        if (description == null
                || !getString(R.string.clip_label).contentEquals(description.getLabel())) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip();
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
        }
    }

    private void updateCharacterCount() {
        CharSequence text = inputText.getText();
        int count = text == null ? 0 : Character.codePointCount(text, 0, text.length());
        inputCount.setText(getString(R.string.character_count, count));
    }

    private void setProcessing(boolean value) {
        processing = value;
        processingIndicator.setVisibility(value ? View.VISIBLE : View.GONE);
        inputText.setEnabled(!value);
        passwordInput.setEnabled(!value);
        encryptButton.setEnabled(!value);
        decryptButton.setEnabled(!value);
        pasteButton.setEnabled(!value);
        clearButton.setEnabled(!value);
        copyButton.setEnabled(!value && outputText.getText() != null
                && outputText.getText().length() > 0);
        shareButton.setEnabled(!value && outputText.getText() != null
                && outputText.getText().length() > 0);
    }

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) {
            InputMethodManager manager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            focused.clearFocus();
        }
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
