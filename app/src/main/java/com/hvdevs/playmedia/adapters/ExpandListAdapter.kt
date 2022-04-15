package com.hvdevs.playmedia.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hvdevs.playmedia.exoplayer2.PlayerActivity
import com.hvdevs.playmedia.R

//Adaptador de prueba, descartado
//Este funcionaba con el ingreso de 2 listas
class ExpandListAdapter(var context: Context, var expListView: ExpandableListView, var header: MutableList<String>, var body: MutableList<MutableList<String>>): BaseExpandableListAdapter() {
//El constructor solo pasaria la lista de grupo
    override fun getGroupCount(): Int {
        return header.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return body[groupPosition].size
    }

    //El grupo es de tipo String, ya lo definimos
    override fun getGroup(groupPosition: Int): String {
        return header[groupPosition]
    }

    //El child es de tipo String, ya lo definimos
    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return body[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    //Si retornamos false, los child de la lista no se pueden seleccionar
    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    //Estas son las funciones que controlan los estados la las listas
    @SuppressLint("InflateParams")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout_group, null)
        }
        val title: TextView = view!!.findViewById(R.id.tv_title)

        //El escuchador del click
        title.setOnClickListener {
            //Aqui colapsamos todas las listas
            for (i in 0 .. header.size){
                //Exepto la que esta tocada
                if (i != groupPosition) expListView.collapseGroup(i)
            }
            //Aqui controlamos el expand y collapse
            if (expListView.isGroupExpanded(groupPosition)) expListView.collapseGroup(groupPosition)
            else expListView.expandGroup(groupPosition)
        }
        title.text = header[groupPosition] //.name
        return view
    }

    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout_child, null)
        }
        val title: TextView = view!!.findViewById(R.id.tv_title_child)
        val image: ImageView = view.findViewById(R.id.img)
        title.text = body[groupPosition][childPosition]//.name
//        Picasso.get().load(body[groupPosition][childPosition].image).into(image)
        title.setOnClickListener {
            Toast.makeText(context, title.text.toString(), Toast.LENGTH_SHORT).show()
            //Podemos iniciar otro activity desde el adaptador
            val intent = Intent(context, PlayerActivity::class.java)
//            intent.putExtra("video", body[groupPosition][childPosition].image)
            context.startActivity(Intent(intent))
        }
        return view
    }
}