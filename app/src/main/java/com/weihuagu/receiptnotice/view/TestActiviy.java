package com.weihuagu.receiptnotice.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.weihuagu.receiptnotice.ForTest;
import com.weihuagu.receiptnotice.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestActiviy extends AppCompatActivity implements View.OnClickListener {

    private TextView money;
    private Button button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        money = findViewById(R.id.money);
        button = findViewById(R.id.action_nitification);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_nitification:
                new ForTest();
                break;

        }
    }
}
