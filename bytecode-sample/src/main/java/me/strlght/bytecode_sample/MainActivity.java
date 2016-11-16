package me.strlght.bytecode_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Object testObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testObject = new Object();
    }

    @Override
    protected void onResume() {
        super.onResume();
        assert testObject == null;
    }
}
