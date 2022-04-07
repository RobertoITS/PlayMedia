package com.hvdevs.playmedia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.constructor.Group

class GroupAdapter(private var groupList: ArrayList<Group>): RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    class GroupViewHolder(itemView: View, listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
        val rv: RecyclerView = itemView.findViewById(R.id.rv)

        init {
            itemView.setOnClickListener { listener.onItemClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_main, parent, false)
        return GroupViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = groupList[position]
        holder.tv.text = item.name
        //Le damos un TAG a cada recycler para diferenciarlos
        holder.rv.tag = "${item.name}, $position"

    }

    override fun getItemCount(): Int {
        return groupList.size
    }
}