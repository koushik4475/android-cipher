package com.example.encryptiondecryption;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button encryptBtn, decryptBtn;
        encryptBtn = findViewById(R.id.encryptBtn);
        decryptBtn = findViewById(R.id.decryptBtn);

        EditText encryptET, decryptET;
        encryptET = findViewById(R.id.encryptET);
        decryptET = findViewById(R.id.decryptET);

        encryptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String encryptedText = EncryptDecrypt.encrypt(encryptET.getText().toString());
                    decryptET.setText(encryptedText);
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });

        decryptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String decryptedText = EncryptDecrypt.decrypt(decryptET.getText().toString());
                    encryptET.setText(decryptedText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}