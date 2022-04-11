package com.hvdevs.playmedia.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListView
import android.widget.Toast
import com.google.firebase.database.*
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.adapters.HelperAdapter
import com.hvdevs.playmedia.constructor.ChildModel
import com.hvdevs.playmedia.constructor.ParentModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainListActivity : AppCompatActivity() {

    private lateinit var dbParent: DatabaseReference
    private lateinit var dbChild: DatabaseReference

    private var parentItem:LinkedHashMap<String, ParentModel> = LinkedHashMap()
    private var itemList: ArrayList<ParentModel> = arrayListOf()
    private lateinit var helperAdapter: HelperAdapter
    private lateinit var expandableList: ExpandableListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list_view)

//        val date = LocalDate.parse("10/05/2022", DateTimeFormatter.ofPattern("dd/M/yyyy"))
//        Toast.makeText(this, date.toString(), Toast.LENGTH_SHORT).show()

        expandableList = findViewById(R.id.sample_list)

        getParentData()

        expandableList.setOnChildClickListener { expandableListView, view, parentPosition, childPosition, long ->
            val parentInfo = itemList[parentPosition]
            val childInfo = parentInfo.itemList[childPosition]
            Toast.makeText(baseContext, childInfo.name, Toast.LENGTH_SHORT).show()

            false
        }

        expandableList.setOnGroupClickListener { expandableListView, view, parentPosition, long ->
            val parentInfo = itemList[parentPosition]
            Toast.makeText(baseContext, parentInfo.name, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun addItem(parentItemList: String, subItemList: ChildModel): Int{
        val parentPosition: Int
        var parentInfo: ParentModel? = parentItem[parentItemList]
        if (parentInfo == null){
            parentInfo = ParentModel()
            parentInfo.name = parentItemList
            parentItem[parentItemList] = parentInfo
            itemList.add(parentInfo)
        }
        val childItemList: ArrayList<ChildModel> = parentInfo.itemList
        var listSize = childItemList.size
        listSize++

        val childInfo = ChildModel()
        childInfo.drm_licence_url = subItemList.drm_licence_url
        childInfo.drm_scheme = subItemList.drm_scheme
        childInfo.icon = subItemList.icon
        childInfo.name = subItemList.name
        childInfo.uri = subItemList.uri
        childItemList.add(childInfo)
        parentInfo.itemList = childItemList

        parentPosition = childItemList.indexOf(childInfo)

        return parentPosition
    }

    private fun getParentData() {
        var j = 0
        dbParent = FirebaseDatabase.getInstance().getReference("channels")
        dbParent.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (parentSnapshot in snapshot.children){
                        val parent: String = parentSnapshot.child("name").value.toString()
                        Log.d("FIREBASE", parent)
                        getChildData(parent, j)
                        j += 1
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getChildData(parent: String, j: Int) {
        dbChild = FirebaseDatabase.getInstance().getReference("channels/$j/samples")
        dbChild.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (childSnapshot in snapshot.children){
                        val child = childSnapshot.getValue(ChildModel::class.java)
                        Log.d("FIREBASE/CHILD", child?.name.toString())
                        addItem(parent, child!!)

                        helperAdapter = HelperAdapter(this@MainListActivity, itemList)

                        helperAdapter.notifyDataSetChanged()

                        expandableList.setAdapter(helperAdapter)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}