package com.hvdevs.playmedia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.constructor.ChildModel
import com.hvdevs.playmedia.constructor.ParentModel
import com.squareup.picasso.Picasso

class HelperAdapter(var context: Context, var childList: ArrayList<ParentModel>): BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return childList.size
    }

    override fun getChildrenCount(parentPosition: Int): Int {
        val itemList: ArrayList<ChildModel> = childList[parentPosition].itemList
        return itemList.size
    }

    override fun getGroup(parentPosition: Int): Any {
        return childList[parentPosition]
    }

    override fun getChild(parentPosition: Int, childPosition: Int): Any {
        val itemList: ArrayList<ChildModel> = childList[parentPosition].itemList
        return itemList[childPosition]
    }

    override fun getGroupId(parentPosition: Int): Long {
        return parentPosition.toLong()
    }

    override fun getChildId(parentPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(parentPosition: Int, p1: Boolean, view: View?, viewGroup: ViewGroup?): View {
        val parentInfo: ParentModel = getGroup(parentPosition) as ParentModel
        var view = view
        if (view == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout_group, null)
        }
        val tv: TextView = view!!.findViewById(R.id.tv_title)
        tv.text = parentInfo.name
        return view
    }

    override fun getChildView(parentPosition: Int, childPosition: Int, p2: Boolean, view: View?, viewgroup: ViewGroup?): View {
        val childInfo: ChildModel = getChild(parentPosition, childPosition) as ChildModel
        var view = view
        if (view == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout_child, null)
        }
        val name: TextView = view!!.findViewById(R.id.tv_title_child)
        name.text = childInfo.name
        val icon: ImageView = view.findViewById(R.id.img)
        Picasso.get().load(childInfo.icon).into(icon)
        return view
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }
}