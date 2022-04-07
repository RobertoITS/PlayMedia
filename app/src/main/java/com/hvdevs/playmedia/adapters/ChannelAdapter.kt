package com.hvdevs.playmedia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.constructor.Channel

class ChannelAdapter(private var list: ArrayList<Channel>): RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    class ChannelViewHolder(itemView: View, listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
        val cv: CardView = itemView.findViewById(R.id.cv)

        init {
            itemView.setOnClickListener { listener.onItemClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_channel, parent, false)
        return ChannelViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val item = list[position]
        holder.tv.text = item.link
        holder.cv.tag = "${item.link}"
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
