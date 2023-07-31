package com.weihuagu.receiptnotice.view;

import android.os.Bundle;
import android.widget.EditText;

import com.weihuagu.receiptnotice.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FollowThirdAppActivity extends AppCompatActivity {
    private EditText pkg;
    private EditText keyword;
    private EditText type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followthirdapp);
    }

    private void initView() {
        pkg = findViewById(R.id.pkg);
        keyword = findViewById(R.id.pkg);
        type = findViewById(R.id.pkg);
    }
}
