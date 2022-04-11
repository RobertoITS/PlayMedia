package com.raqueveque.foodexample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object Utilities {
    //Esconder el teclado
    private fun hideSoftKeyboard(activity: Activity): Boolean {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(view: View, context: Context) {
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideSoftKeyboard(context as Activity)
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView, context)
            }
        }
    }

    private var isKeyboardShowing = false
    private fun onKeyboardVisibilityChanged(isOpen: Boolean) {
        print("keyboard $isOpen");
    }

    fun check(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect();
            view.getWindowVisibleDisplayFrame(r);
            val screenHeight = view.rootView.height;

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom;

            Log.d(TAG, "keypadHeight = $keypadHeight");

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (!isKeyboardShowing) {
                    isKeyboardShowing = true
                    onKeyboardVisibilityChanged(true)
                }
            } else {
                // keyboard is closed
                if (isKeyboardShowing) {
                    isKeyboardShowing = false
                    onKeyboardVisibilityChanged(false)
                }
            }
        }
    }
}


//val season1: MutableList<Child> = ArrayList()
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Winter is Coming"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Kingsroad"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Lord Snow"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Cripples, Bastards, and Broken Things"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Wolf and the Lion"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"A Golden Crown"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"You Win or You Die"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Pointy End"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Baelor"))
//season1.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Fire and Blood"))
//
//val season2: MutableList<Child> = ArrayList()
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Winter is Coming"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Kingsroad"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Lord Snow"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Cripples, Bastards, and Broken Things"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Wolf and the Lion"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"A Golden Crown"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"You Win or You Die"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Pointy End"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Baelor"))
//season2.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Fire and Blood"))
//
//val season3: MutableList<Child> = ArrayList()
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Winter is Coming"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Kingsroad"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Lord Snow"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Cripples, Bastards, and Broken Things"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Wolf and the Lion"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"A Golden Crown"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"You Win or You Die"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"The Pointy End"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Baelor"))
//season3.add(Child("https://www.eltiempo.com/files/image_640_428/uploads/2019/05/23/5ce6e2a1369e8.jpeg",
//"Fire and Blood"))
//
//header.add(Group("Season 1"))
//header.add(Group("Season 2"))
//header.add(Group("Season 3"))
//
//body.add(season1)
//body.add(season2)
//body.add(season3)

//parentReference.addValueEventListener(new ValueEventListener() {
//    @Override
//    public void onDataChange(DataSnapshot dataSnapshot) {
//        final List<ParentList> Parent = new ArrayList<>();
//        for (final DataSnapshot snapshot : dataSnapshot.getChildren()){
//
//
//        final String ParentKey = snapshot.getKey().toString();
//
//        snapshot.child("titre").getValue();
//
//        DatabaseReference childReference =
//        FirebaseDatabase.getInstance().getReference().child(ParentKey);
//        childReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                final List<ChildList> Child = new ArrayList<>();
//                //numberOnline = 0;
//
//                for (DataSnapshot snapshot1:dataSnapshot.getChildren())
//                {
//                    final String ChildValue =  snapshot1.getValue().toString();
//
//                    snapshot1.child("titre").getValue();
//
//                    Child.add(new ChildList(ChildValue));
//
//                }
//
//                Parent.add(new ParentList(ParentKey, Child));
//
//                DocExpandableRecyclerAdapter adapter = new DocExpandableRecyclerAdapter(Parent);
//
//                recycler_view.setAdapter(adapter);
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                System.out.println("Failed to read value." + error.toException());
//            }
//
//        });}}
//
//    @Override
//    public void onCancelled(DatabaseError databaseError) {
//
//    }
//});