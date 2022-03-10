package com.example.adamasmacaoyunu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BasligiDegistir();
    }

    public void BasligiDegistir(){
        this.setTitle("Adam Asmaca");
    }
}