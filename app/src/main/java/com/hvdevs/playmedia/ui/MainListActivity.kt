package com.hvdevs.playmedia.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListView
import com.google.firebase.database.*
import com.hvdevs.playmedia.ASD
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.constructor.ChildModel

class MainListActivity : AppCompatActivity() {

    private lateinit var dbParent: DatabaseReference
    private lateinit var dbChild: DatabaseReference

    lateinit var header: ArrayList<String>
    lateinit var body: HashMap<String, ArrayList<ChildModel>>

    private lateinit var expandableList: ExpandableListView
    private lateinit var asd: ASD
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list_view)

//        val date = LocalDate.parse("10/05/2022", DateTimeFormatter.ofPattern("dd/M/yyyy"))
//        Toast.makeText(this, date.toString(), Toast.LENGTH_SHORT).show()

        expandableList = findViewById(R.id.sample_list)

        getData()



    }

    private fun getData() {
        header = arrayListOf()
        body = hashMapOf()

        for (i in 0..9){
            dbParent = FirebaseDatabase.getInstance().getReference("channels/$i")
            dbParent.addChildEventListener(object : ChildEventListener{
                var counter = 0
                lateinit var childItem: ArrayList<ChildModel>
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val headerItem: String = snapshot.key.toString()
                    header.add(headerItem)
                    Log.d("FIREBASE", headerItem)
                    childItem = arrayListOf()
                    for (ds in snapshot.children){
                        val name = ds.getValue(ChildModel::class.java)
                        Log.d("FIREBASE", name!!.name)
                        childItem.add(name)
                    }
                    //Se agrega en la lista hash, el item principal, con su sublista, el index lo da el counter
                    body[header[counter]] = childItem
                    counter ++

                    asd = ASD(this@MainListActivity, header, body)

                    asd.notifyDataSetChanged()

                    expandableList.setAdapter(asd)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

    }


}