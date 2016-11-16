package me.strlght.basic_kotlin_sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import me.strlght.BindView

class MainActivity : AppCompatActivity() {
    @BindView(R.id.activity_main)
    lateinit var rootLayout: RelativeLayout

    @BindView(R.id.sample_text)
    lateinit var sampleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MainActivity_ViewBinding.bind(this)
        sampleText.text = "Hello annotations!"
    }
}
