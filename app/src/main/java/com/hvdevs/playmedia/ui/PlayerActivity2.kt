//package com.hvdevs.playmedia.ui
//
//import android.annotation.SuppressLint
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.widget.TextView
//import com.google.firebase.database.ktx.database
//import com.google.firebase.ktx.Firebase
//import com.hvdevs.playmedia.R
//
//class PlayerActivity2 : AppCompatActivity() {
//    private lateinit var countDownTimer: CountDownTimer
//    var total: Long = 0
//
//    val database = Firebase.database
//    val myRef = database.getReference("time")
//    var contador: Long = 0
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_player_unused)
//
//        val tv: TextView = findViewById(R.id.tv)
//
//        contador = 10800000
//        countDownTimer = object : CountDownTimer(contador, 1000) {
//            @SuppressLint("SetTextI18n")
//            override fun onTick(millisUntilFinished: Long) {
//                //Lo pasamos a formato hora
//                val seconds = (millisUntilFinished / 1000).toInt() % 60
//                val minutes = (millisUntilFinished / (1000 * 60) % 60).toInt()
//                val hours = (millisUntilFinished / (1000 * 60 * 60) % 24).toInt()
//                val newtime = "$hours:$minutes:$seconds"
//                total = millisUntilFinished / 1000
//                tv.text = "seconds remaining: $newtime"
//                myRef.child("time").setValue(total)
//            }
//
//            @SuppressLint("SetTextI18n")
//            override fun onFinish() {
//                tv.text = "Time's finished!"
//            }
//        }.start()
//    }
//
//    override fun onBackPressed() {
//        countDownTimer.cancel()
//        super.onBackPressed()
//    }
//
//}