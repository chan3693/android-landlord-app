package com.example.landlord_g09

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.landlord_g09.databinding.ActivityPropertyDetailsScreenBinding
import com.example.landlord_g09.models.Property
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PropertyDetailsScreen : AppCompatActivity() {
    lateinit var binding: ActivityPropertyDetailsScreenBinding
    lateinit var auth: FirebaseAuth
    lateinit var uid: String
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        auth = Firebase.auth
        // get logged in user id
        uid = auth.currentUser?.uid.toString()

        val currItem: Property = intent.getSerializableExtra("PROPERTY_DETAILS") as Property
        val id: String = currItem.id

        loadDetails (uid)

        // btn to mark Availability
        binding.btnAvailability.setOnClickListener {
            val data: MutableMap<String, Any> = HashMap()
            if (currItem.isAvailable == true) {
                data["isAvailable"] = false
            } else {
                data["isAvailable"] = true
            }
            db.collection("LandlordProfiles")
                .document(uid)
                .collection("Properties")
                .document(id)
                .set(data, SetOptions.merge())
                .addOnSuccessListener { docRef ->
                    Log.d("TESTING", "Availability updated successfully")
                    finish()
                }
                .addOnFailureListener { ex ->
                    Log.e("TESTING", "Exception occurred while updating availability : $ex", )
                }
        }

        // modify details, put the specified data to modify screen
        binding.btnModify.setOnClickListener {
            val intent = Intent(this@PropertyDetailsScreen, CreateListingScreen::class.java)
            intent.putExtra("KEY_IS_CREATE", false)
            intent.putExtra("KEY_DOCUMENT_ID", id)
            startActivity(intent)
        }

        // btn to delete details
        binding.btnRemove.setOnClickListener {
            db.collection("LandlordProfiles")
                .document(uid)
                .collection("Properties")
                .document(id)
                .delete()
                .addOnSuccessListener {
                    Log.d("TESTING", "Property deleted successfully")
                    finish()
                }
                .addOnFailureListener {
                        e ->
                    Log.w("TESTING", "Error deleting property", e)
                }
        }
    }

    // refresh data
    override fun onResume() {
        super.onResume()
        Log.d("TESTING", "onResume() executing....")
        loadDetails(uid)
    }
    override fun onPause() {
        super.onPause()
        Log.d("TESTING", "PropertyDetailsScreen onPause() executing....")
    }

    // retrieve specified data from database handler
    fun loadDetails (uid:String){
        val currItem: Property = intent.getSerializableExtra("PROPERTY_DETAILS") as Property
        val id: String = currItem.id
        db.collection("LandlordProfiles")
            .document(uid)
            .collection("Properties")
            .document(id)
            .get()
            .addOnSuccessListener {
                    document: DocumentSnapshot ->
                val propertyData: Property? = document.toObject(Property::class.java)
                if (propertyData == null) {
                    Log.d("TESTING", "No matching property profile found")
                    return@addOnSuccessListener
                }
                Log.d("TESTING", propertyData.toString())
                binding.tvAddress.setText(propertyData.address)
                binding.tvDetails.setText("""
                • Monthly rental price: $${if (propertyData.monthlyRentalPrice % 1.0 == 0.0) 
                    propertyData.monthlyRentalPrice.toInt() else propertyData.monthlyRentalPrice}
                • Rental type : ${propertyData.rentalType}
                • Number of bedrooms: ${propertyData.numberOfBedrooms}
                """.trimIndent())

                if (propertyData.isAvailable == true) {
                    binding.tvIsAvailable.setText("Status: Available")
                    binding.btnAvailability.setText("Mark Unavailable")
                } else {
                    binding.tvIsAvailable.setText("Status: Not Available")
                    binding.btnAvailability.setText("Mark Available")
                }

                Glide.with(this@PropertyDetailsScreen)
                    .load(propertyData.imageUrl)
                    .into(binding.tvImage)
            }
            .addOnFailureListener {
                    exception ->
                Log.d("TESTING", "ERROR retrieving property profile", exception)
            }
    }

    // option menu handler
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        menu.findItem(R.id.mi_option_03).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_option_01 -> {
                auth.signOut()
                val intent = Intent(this@PropertyDetailsScreen, LoginScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                true
            }
            R.id.mi_option_03 -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}