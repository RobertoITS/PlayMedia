package com.hvdevs.playmedia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.constructor.ChildModel
import com.hvdevs.playmedia.constructor.ParentModel
import com.squareup.picasso.Picasso

/**
 * Este es el adaptador del expandedlistview, se extiendo de la clase
 * BaseExpandableListAdapter.
 * Para su funcionalidad, ingresa una lista de tipo constructor, con un constructor dentro (ParentModel)
 * */
class HelperAdapter(var context: Context, var childList: ArrayList<ParentModel>, var expListView: ExpandableListView): BaseExpandableListAdapter() {

    //Obtenemos la cuenta del grupo principal
    override fun getGroupCount(): Int {
        return childList.size
    }

    //Obtenemos la cuenta de los child de los grupos
    override fun getChildrenCount(parentPosition: Int): Int {
        val itemList: ArrayList<ChildModel> = childList[parentPosition].itemList
        return itemList.size
    }

    //Obtenemos los datos de los parent
    override fun getGroup(parentPosition: Int): Any {
        return childList[parentPosition]
    }

    //Obtenemos los datos de los child de cada parent
    override fun getChild(parentPosition: Int, childPosition: Int): Any {
        val itemList: ArrayList<ChildModel> = childList[parentPosition].itemList
        return itemList[childPosition]
    }

    //Aqui se obtiene los ID's de los parent
    override fun getGroupId(parentPosition: Int): Long {
        return parentPosition.toLong()
    }

    //Y aqui los ID's de los child de cada parent
    override fun getChildId(parentPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    //Obtenemos la vista de los parent
    override fun getGroupView(parentPosition: Int, p1: Boolean, view: View?, viewGroup: ViewGroup?): View {
        val parentInfo: ParentModel = getGroup(parentPosition) as ParentModel //Obtenemos los objetos de las listas
        var currentView = view
        if (currentView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater //Obtenemos el inflador de las vistas
            currentView = inflater.inflate(R.layout.layout_group, null) //Inflamos la vista
        }
        val tv: TextView = currentView!!.findViewById(R.id.tv_title)
        tv.text = parentInfo.name //Pasamos los datos obtenidos de las listas (parent)

        //Este listener lo tocamos de aca para que consiga el tamaño de la lista parent
        //El escuchador del click
        tv.setOnClickListener {
            //Aqui colapsamos todas las listas
            for (i in 0 .. childList.size){
                //Exepto la que esta tocada
                if (i != parentPosition) expListView.collapseGroup(i)
            }
            //Aqui controlamos el expand y collapse
            if (expListView.isGroupExpanded(parentPosition)) expListView.collapseGroup(parentPosition)
            else expListView.expandGroup(parentPosition)
        }
        return currentView
    }

    //Obtenemos la vista de los child
    override fun getChildView(parentPosition: Int, childPosition: Int, p2: Boolean, view: View?, viewgroup: ViewGroup?): View {
        val childInfo: ChildModel = getChild(parentPosition, childPosition) as ChildModel //Obtenemos los objetos de las listas (child)
        var currentView = view
        if (currentView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater //Obtenemos el inflador de las vistas
            currentView = inflater.inflate(R.layout.layout_child, null) //Inflamos la vista
        }
        val name: TextView = currentView!!.findViewById(R.id.tv_title_child)
        name.text = childInfo.name //pasamos los datos obtenidos de las listas (child)
        val icon: ImageView = currentView.findViewById(R.id.img)
        Picasso.get().load(childInfo.icon).into(icon)
        return currentView
    }

    //Si son seleccionables los child
    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }
}