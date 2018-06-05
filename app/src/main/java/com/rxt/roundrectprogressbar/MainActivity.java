package com.rxt.roundrectprogressbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private RoundRectProgressbar progressbar;
    private ConstantPressButton btnPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressbar = findViewById(R.id.progressbar);
        btnPlus = findViewById(R.id.btn_plus);
        btnPlus.setText(progressbar.getProgress() + "");
        btnPlus.setLongPressListener(new ConstantPressButton.OnLongPressListener() {
            @Override
            public void onLongPress(View view) {
                plusProgress(view);
            }
        });
    }

    public void plusProgress(View view) {
        progressbar.setProgress(progressbar.getProgress() + 1);
        btnPlus.setText(progressbar.getProgress() + "");
    }
}
