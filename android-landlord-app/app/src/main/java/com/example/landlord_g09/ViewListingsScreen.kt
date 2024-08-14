package com.example.landlord_g09

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landlord_g09.adapters.ListingsAdapter
import com.example.landlord_g09.databinding.ActivityViewListingsScreenBinding
import com.example.landlord_g09.models.Property
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ViewListingsScreen : AppCompatActivity() {
    lateinit var binding: ActivityViewListingsScreenBinding
    lateinit var adapter:ListingsAdapter
    lateinit var auth: FirebaseAuth
    lateinit var uid: String
    var propertyList:MutableList<Property> = mutableListOf()
    val db = Firebase.firestore
    val onClick = {
        rowNumber:Int ->
        val intent = Intent(this@ViewListingsScreen, PropertyDetailsScreen::class.java)
        intent.putExtra("PROPERTY_DETAILS", propertyList[rowNumber])
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewListingsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        auth = Firebase.auth
        // get logged in user id
        uid = auth.currentUser?.uid.toString()

        adapter = ListingsAdapter(propertyList, onClick)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        loadData(uid)
    }

    // refresh data
    override fun onResume() {
        super.onResume()
        Log.d("TESTING", "onResume() executing....")
        loadData(uid)
    }
    override fun onPause() {
        super.onPause()
        Log.d("TESTING", "ViewListingsScreen onPause() executing....")
    }

    // retrieve all data from database
    fun loadData(uid:String) {
        db.collection("LandlordProfiles")
            .document(uid)
            .collection("Properties")
            .get()
            .addOnSuccessListener {
                    result: QuerySnapshot ->
                val propertyListFromDB:MutableList<Property> = mutableListOf()
                for (document: QueryDocumentSnapshot in result) {
                    val property:Property = document.toObject(Property::class.java)
                    propertyListFromDB.add(property)
                }
                Log.d("TESTING", propertyListFromDB.toString())
                propertyList.clear()
                propertyList.addAll(propertyListFromDB)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                    exception ->
                Log.d("TESTING", "Error retrieving data", exception)
            }
    }

    // option menu handler
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        menu.findItem(R.id.mi_option_02).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_option_01 -> {
                auth.signOut()
                val intent = Intent(this@ViewListingsScreen, LoginScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                true
            }
            R.id.mi_option_02 -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

