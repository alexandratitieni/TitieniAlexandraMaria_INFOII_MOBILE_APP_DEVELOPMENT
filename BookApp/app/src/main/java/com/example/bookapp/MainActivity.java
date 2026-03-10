package com.example.bookapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnC1).setOnClickListener(v -> loadFragment(new Chapter1Fragment()));
        findViewById(R.id.btnC2).setOnClickListener(v -> loadFragment(new Chapter2Fragment()));
        findViewById(R.id.btnC3).setOnClickListener(v -> loadFragment(new Chapter3Fragment()));
        findViewById(R.id.btnC4).setOnClickListener(v -> loadFragment(new Chapter4Fragment()));
        findViewById(R.id.btnC5).setOnClickListener(v -> loadFragment(new Chapter5Fragment()));
        findViewById(R.id.btnC6).setOnClickListener(v -> loadFragment(new Chapter6Fragment()));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerViewBook, fragment)
                .addToBackStack(null)
                .commit();
    }
}