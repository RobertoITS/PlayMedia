package com.hvdevs.playmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hvdevs.playmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    override fun onBackPressed() {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_container)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let { isCanceled: Boolean ->
                    Toast.makeText(this, "onBackPressed $isCanceled", Toast.LENGTH_SHORT).show()
                    if (!isCanceled) {
                        super.onBackPressed()
                    }
                }
            }
        }
    }
}