package com.envy.playermusic.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.envy.playermusic.MainActivity;
import com.envy.playermusic.R;
import com.envy.playermusic.databinding.ActivityOnBoardingBinding;

public class OnBoardingActivity extends AppCompatActivity {
    private ActivityOnBoardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, MainActivity.class));
        binding.changeActivity.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));


    }
}