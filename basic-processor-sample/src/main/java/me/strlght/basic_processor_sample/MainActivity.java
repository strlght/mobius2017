package me.strlght.basic_processor_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.strlght.BindView;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.activity_main)
    RelativeLayout rootLayout;

    @BindView(R.id.sample_text)
    TextView sampleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity_ViewBinding.bind(this);
        sampleText.setText("Hello annotations!");
    }
}
