package com.hvdevs.playmedia.exoplayer2.leanbackforandroidtv

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hvdevs.playmedia.R
import kotlin.properties.Delegates

class LeanbackActivity : FragmentActivity() {
    private var testContent by Delegates.notNull<Boolean>() //Variable que obtiene el tipo de contenido que se pasa de la actividad anterior
    private lateinit var countDownTimer: CountDownTimer //El contador del reproductor, en caso de modo de prueba
    var total: Long = 0
    private val database = Firebase.database //Instancias a la base de datos
    val myRef = database.getReference("users")
    private var time by Delegates.notNull<Long>() //Variable que obtiene el tiempo que se pasa de la actividad anterior
    private lateinit var uid: String //Variable que obtiene la uid que se pasa de la actividad anterior
    private lateinit var timeTv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leanback)
        timeTv = findViewById(R.id.testContentLeanback)
        val bundle: Bundle? = intent.extras
        uid = bundle?.getString("uid").toString()
        time = bundle!!.getLong("time")
        Log.d("TIMETEST", time.toString())
        testContent = bundle.getBoolean("testContent")
    }

    override fun onStart() {
        if (testContent) {
            Log.d("TIMETEST", time.toString())
            countDownTimer = object : CountDownTimer(time, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    //Lo pasamos a formato hora
                    val seconds = (millisUntilFinished / 1000).toInt() % 60
                    val minutes = (millisUntilFinished / (1000 * 60) % 60).toInt()
                    val hours = (millisUntilFinished / (1000 * 60 * 60) % 24).toInt()
                    val newTime = "$hours:$minutes:$seconds"
                    total = millisUntilFinished / 1000
                    Log.d("TIMETEST", millisUntilFinished.toString())
                    timeTv.visibility = View.VISIBLE
                    timeTv.text = "Contenido de prueba: $newTime"
                    //Vamos refrescando la hora en la base de datos en tiempo real
                    myRef.child("$uid/time").setValue(millisUntilFinished)
                }

                @SuppressLint("SetTextI18n")
                override fun onFinish() {
                    Toast.makeText(
                        this@LeanbackActivity,
                        "Su tiempo de prueba expiro",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }.start()
        }
        super.onStart()
    }

    override fun onDestroy() {
        if (testContent) countDownTimer.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (testContent) countDownTimer.cancel()
        super.onBackPressed()
    }

}