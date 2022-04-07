package com.hvdevs.playmedia.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.adapters.ChannelAdapter
import com.hvdevs.playmedia.adapters.GroupAdapter
import com.hvdevs.playmedia.constructor.Channel
import com.hvdevs.playmedia.constructor.Group
import com.hvdevs.playmedia.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.database

    private lateinit var gAdapter: GroupAdapter
    private var groupList: ArrayList<Group> = arrayListOf()

    private var list: ArrayList<Channel> = arrayListOf()

    private lateinit var cAdapter: ChannelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        val dividerItemDecoration = DividerItemDecoration(binding.rvTest.context, LinearLayoutManager.VERTICAL)
        binding.rvTest.addItemDecoration(dividerItemDecoration)
        binding.rvTest.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvTest.setHasFixedSize(true)

        binding.btn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_mediaFragment)
        }

        getData()

        return binding.root
    }

    private fun getData() {
        if (groupList.size == 0) {
            val myRef = db.getReference("prueba_borrar")
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        //children saca el id de cada coleccion
                        for (groupSnapshot in snapshot.children) {
                            val group = groupSnapshot.getValue(Group::class.java)
                            Log.d("FIREBASE", group?.name.toString())
                            groupList.add(group!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            updateList(groupList)
        } else {
            updateList(groupList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(list: ArrayList<Group>) {

        gAdapter = GroupAdapter(list)
        binding.rvTest.adapter = gAdapter
        gAdapter.notifyDataSetChanged()

        gAdapter.setOnItemClickListener(object : GroupAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                //Click
                Toast.makeText(context, "Click!", Toast.LENGTH_SHORT).show()
                //Copiamos el formato del TAG creado en el adapter, para no confundirnos
                val rv = binding.root.findViewWithTag<RecyclerView>("${groupList[position].name}, $position")
                if (rv.isVisible) rv.visibility = View.GONE else rv.visibility = View.VISIBLE
                getData2(position, rv)
            }
        })
    }


    private fun getData2(position: Int, rv: RecyclerView) {
        val ref = groupList[position].name
        val myRef = db.getReference("prueba_borrar/$ref")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    //children saca el id de cada coleccion
                    for (groupSnapshot in snapshot.children) {
                        val item = Channel(
                            groupSnapshot.children.toString()
                        )
                        Log.d("FIREBASE", item.link.toString())
                        list.add(item)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        updateList2(list, rv)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList2(list: ArrayList<Channel>, rv: RecyclerView) {
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        rv.layoutManager = llm
        cAdapter = ChannelAdapter(list)
        rv.adapter = cAdapter
        cAdapter.notifyDataSetChanged()

        cAdapter.setOnItemClickListener(object : ChannelAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
            }
        })
    }
}