package com.example.pessschool.Parent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;

import com.example.pessschool.R;

public class HocPhiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoc_phi);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Học phí");

    }
}
