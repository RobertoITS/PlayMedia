package com.hvdevs.playmedia

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment


class MediaFragment : Fragment() {

    var total: Long = 0

    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_media, container, false)

        val tv: TextView = view.findViewById(R.id.tv)

        val contador: Long = 200000

        object : CountDownTimer(contador, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                total = millisUntilFinished / 1000
                tv.text = "seconds remaining: $total"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                tv.text = "Time's finished!"
            }
        }.start()

        return view
    }

}