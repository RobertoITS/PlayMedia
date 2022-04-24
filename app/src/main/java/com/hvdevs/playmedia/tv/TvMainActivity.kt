package com.hvdevs.playmedia.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.hvdevs.playmedia.R

/**
 * Loads [MainFragment].
 */
class TvMainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }
    }
}