package com.hvdevs.playmedia.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.utilities.IOnBackPressed


class MediaFragment : Fragment(), IOnBackPressed {
    private lateinit var countDownTimer: CountDownTimer
    var total: Long = 0

    val database = Firebase.database
    val myRef = database.getReference("time")
    var contador: Long = 0
    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_media, container, false)

        val tv: TextView = view.findViewById(R.id.tv)
//
//        myRef.child("time").addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.d("FIREBASE", snapshot.value.toString())
//                val value = snapshot.value.toString()
//                contador = value.toLong()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
        contador = 10800000
        countDownTimer = object : CountDownTimer(contador, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                //Lo pasamos a formato hora
                val seconds = (millisUntilFinished / 1000).toInt() % 60
                val minutes = (millisUntilFinished / (1000 * 60) % 60).toInt()
                val hours = (millisUntilFinished / (1000 * 60 * 60) % 24).toInt()
                val newtime = "$hours:$minutes:$seconds"
                total = millisUntilFinished / 1000
                tv.text = "seconds remaining: $newtime"
                myRef.child("time").setValue(total)
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                tv.text = "Time's finished!"
            }
        }.start()

        return view
    }

    override fun onBackPressed(): Boolean {
        //Detiene el contador al presionar back
        countDownTimer.cancel()
        return true
    }

}