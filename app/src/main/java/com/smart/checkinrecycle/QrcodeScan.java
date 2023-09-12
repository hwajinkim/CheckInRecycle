package com.smart.checkinrecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class QrcodeScan extends AppCompatActivity {
    private String u_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);
        Intent intent = getIntent();
        u_id = intent.getExtras().getString("u_id");
        Log.d("u_id",u_id);




    }
}