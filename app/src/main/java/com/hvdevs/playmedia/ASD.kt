package com.hvdevs.playmedia

import android.content.Context
import com.hvdevs.playmedia.constructor.ChildModel
import android.widget.BaseExpandableListAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import java.util.*

class ASD(
    private val context: Context,
    private val headerItem: List<String>,
    private val childItem: HashMap<String, ArrayList<ChildModel>>
) : BaseExpandableListAdapter() {
    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childItem[headerItem[groupPosition]]!![childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View,
        parent: ViewGroup
    ): View {
        var view = convertView
        val childText = getChild(groupPosition, childPosition) as String
        if (view == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout_child, null)
        }
        val tv = view.findViewById<TextView>(R.id.tv_title_child)
        tv.text = childText
        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return childItem[headerItem[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return headerItem[groupPosition]
    }

    override fun getGroupCount(): Int {
        return headerItem.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View,
        parent: ViewGroup
    ): View {
        var view = convertView
        val headerTitle = getGroup(groupPosition) as String
        if (view == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout_group, null)
        }
        val tv = view.findViewById<TextView>(R.id.tv_title)
        tv.text = headerTitle
        return view
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}