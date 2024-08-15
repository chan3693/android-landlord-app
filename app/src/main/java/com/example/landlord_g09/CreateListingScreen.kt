package com.example.landlord_g09

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.landlord_g09.databinding.ActivityCreateListingScreenBinding
import com.example.landlord_g09.models.Property
import com.example.landlord_g09.models.UserProfile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.util.Locale

class CreateListingScreen : AppCompatActivity() {
    lateinit var binding: ActivityCreateListingScreenBinding
    lateinit var geocoder: Geocoder
    lateinit var auth: FirebaseAuth
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var isCreate:Boolean = true
    var docId:String = ""
    val db = Firebase.firestore

    // define list of permissions
    private val APP_PERMISSIONS_LIST = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // request multiple permissions
    private val multiplePermissionsResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissionGranted:Map<String, Boolean>  ->
            var userGrantedAllPermissions = true
            for (item in permissionGranted.entries) {
                if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                    userGrantedAllPermissions = false
                }
            }
            if (userGrantedAllPermissions == true) {
                // if location permissions granted, get and post the current location
                getCurrentLocation()
            } else {
                binding.tvResults.setText("Cannot get current location because you Denied Permissions")
                binding.tvResults.isVisible = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateListingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        geocoder = Geocoder(applicationContext, Locale.getDefault())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        auth = Firebase.auth

        // get logged in user id (landlordProfiles collection)
        val uid = auth.currentUser?.uid.toString()

        if (intent != null) {
            // isCreate = true --> keep create screen
            isCreate = intent.getBooleanExtra("KEY_IS_CREATE", true)

            // get specified data
            var idFromIntent = intent.getStringExtra("KEY_DOCUMENT_ID")
            if (idFromIntent == null) {
                docId = ""
            } else {
                docId = idFromIntent
            }
        }

        // Change the create screen to modify screen
        if (isCreate == false) {
            binding.textView.text = "Modify Property Details"
            binding.textView2.isVisible = false
            binding.userEmail.isVisible = false
            binding.btnToViewListings.isVisible = false
            binding.btnCreateListings.text = "Submit"
            getDocument(uid, docId)
            binding.address.isEnabled = false
            binding.btnGetCurrentLocation.isVisible = false
        }

        // get the logged in user data
        loadUserData(uid)

        binding.btnCreateListings.setOnClickListener {
            if (isCreate == true) {
                addListing(uid)
            } else {
                modifyDetails(uid)
                finish()
            }
        }

        binding.btnToViewListings.setOnClickListener {
            val intent = Intent(this@CreateListingScreen, ViewListingsScreen::class.java)
            startActivity(intent)
        }

        // btn to request permissions and get location
        binding.btnGetCurrentLocation.setOnClickListener {
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
        }
    }

    // retrieve logged in user data
    fun loadUserData(uid:String) {
        db.collection("LandlordProfiles")
            .document(uid)
            .get()
            .addOnSuccessListener {
                    document: DocumentSnapshot ->
                val profileData: UserProfile? = document.toObject(UserProfile::class.java)
                if (profileData == null) {
                    Log.d("TESTING", "No matching user profile found")
                    return@addOnSuccessListener
                }
                Log.d("TESTING", profileData.toString())
                binding.userEmail.setText(auth.currentUser?.email)

            }.addOnFailureListener {
                    exception ->
                Log.w("TESTING", "Error getting user profile", exception)
            }
    }

    // retrieve coordinates handler
    fun getCoordinates(addressFromUI:String):Pair<Double, Double>? {
        Log.d("TESTING", "Getting coordinates for ${addressFromUI}")
        try {
            val searchResults: MutableList<Address>? =
                geocoder.getFromLocationName(addressFromUI, 1)

            //error handlers
            if (searchResults == null) {
                binding.tvResults.setText("ERROR: Result is null")
                binding.tvResults.isVisible = true
                return null
            }
            if (searchResults.isEmpty() == true) {
                binding.tvResults.setText("No matching coordinates found from address")
                binding.tvResults.isVisible = true
                return null
            }

            //change address to latitude and longitude
            val matchingItem: Address = searchResults[0]

            val latitudeFromUI = matchingItem.latitude
            val longitudeFromUI = matchingItem.longitude

            return Pair(latitudeFromUI, longitudeFromUI)

        } catch (ex: Exception) {
            Log.e("TESTING", "Error encountered while getting coordinates")
            Log.e("TESTING", ex.toString())
            return null
        }
    }

    // retrieve current location handler
    fun getCurrentLocation(){
        // location permission checking
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("TESTING", "Permissions granted")
            return
        }

        // get current location
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // change location coordinates to address
                        try {
                            val searchResult:MutableList<Address>? =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)

                            // error handlers
                            if (searchResult == null) {
                                binding.tvResults.setText("ERROR: Result is null")
                                binding.tvResults.isVisible = true
                            }

                            if (searchResult.toString().isEmpty() == true) {
                                binding.tvResults.setText("No matching addresses found from current location")
                                binding.tvResults.isVisible = true
                            }

                            val matchingAddress: Address = searchResult!![0]

                            val addressResult = matchingAddress.getAddressLine(0)
                            binding.address.setText(addressResult)

                        } catch (ex:Exception){
                            Log.e("TESTING", "Error encountered while getting street address.")
                            Log.e("TESTING", ex.toString())
                        }
                    } else {
                        binding.tvResults.setText("Unable to retrieve current location")
                        binding.tvResults.isVisible = true
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("TESTING", "Error getting location.", exception)
                }
    }

    // create listing handler
    fun addListing(uid: String){
        val addressFromUI:String = binding.address.text.toString()
        val imageUrlFromUI:String = binding.imageUrl.text.toString()
        val priceFromUIToString:String = binding.price.text.toString()
        val numberOfBedroomsFromUIToString:String = binding.numberOfBedrooms.text.toString()

        // set required fields
        if (addressFromUI.isEmpty() || priceFromUIToString.isEmpty() || numberOfBedroomsFromUIToString.isEmpty()) {
            Log.d("TESTING", "Data from UI is null, cannot be added")
            binding.tvResults.text = """
                All fields are required
                Please complete the create listing form""".trimIndent()
            binding.tvResults.isVisible = true
            return
        }
        val priceFromUI:Double = priceFromUIToString.toDouble()
        val numberOfBedroomsFromUI:Int = numberOfBedroomsFromUIToString.toInt()

        // radio choice to string
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = findViewById<RadioButton>(radioButtonId)
        val rentalTypeFromUI:String = radioButton.text.toString()

        // change address to coordinates
        val coordinates = getCoordinates(addressFromUI)
        if (coordinates == null) {
            return
        }
        val (latitudeFromUI, longitudeFromUI) = coordinates

        // database fields
        val data:MutableMap<String, Any> = HashMap()
        data["address"] = addressFromUI
        data["latitude"] = latitudeFromUI
        data["longitude"] = longitudeFromUI
        data["imageUrl"] = imageUrlFromUI
        data["monthlyRentalPrice"] = priceFromUI
        data["rentalType"] = rentalTypeFromUI
        data["numberOfBedrooms"] = numberOfBedroomsFromUI
        data["isAvailable"] = true

        // add to database
        db.collection("LandlordProfiles")
            .document(uid)
            .collection("Properties")
            .add(data)
            .addOnSuccessListener { docRef ->
                    Log.d("TESTING", "Listing successfully added with ID : ${docRef.id}")
                    binding.tvResults.text = "Property added successfully"
                    binding.tvResults.isVisible = true
                    binding.address.text.clear()
                    binding.imageUrl.text.clear()
                    binding.price.text.clear()
                    binding.numberOfBedrooms.text.clear()
                }
            .addOnFailureListener { ex ->
                Log.e("TESTING", "Exception occurred while adding listing : $ex", )
            }
    }

    // retrieve specified data for modify function
    fun getDocument(uid: String, documentIdToFind:String){
        db.collection("LandlordProfiles")
            .document(uid)
            .collection("Properties")
            .document(documentIdToFind)
            .get()
            .addOnSuccessListener {
                    document: DocumentSnapshot ->
                val propertyData:Property? = document.toObject(Property::class.java)
                if (propertyData == null) {
                    Log.d("TESTING", "No results found")
                    return@addOnSuccessListener
                }
                Log.d("TESTING", propertyData.toString())
                binding.address.setText(propertyData.address.toString())
                binding.imageUrl.setText(propertyData.imageUrl.toString())
                binding.price.setText(propertyData.monthlyRentalPrice.toString())
                if (propertyData.rentalType == "house") {
                    binding.rbHouse.isChecked = true
                } else if (propertyData.rentalType == "condo") {
                    binding.rbCondo.isChecked = true
                }
                binding.numberOfBedrooms.setText(propertyData.numberOfBedrooms.toString())
            }
            .addOnFailureListener {
                    exception ->
                Log.w("TESTING", "Error getting property data", exception)
            }
    }

    // modify handler
    fun modifyDetails(uid: String){
        if (this.docId == "") {
            Log.d("TESTING", "Document id is null, cannot update")
            return
        }
        val imageUrlFromUI:String = binding.imageUrl.text.toString()
        val priceFromUI:Double = binding.price.text.toString().toDouble()
        val numberOfBedroomsFromUI:Int = binding.numberOfBedrooms.text.toString().toInt()

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = findViewById<RadioButton>(radioButtonId)
        val rentalTypeFromUI:String = radioButton.text.toString()

        val data:MutableMap<String, Any> = HashMap()
        data["imageUrl"] = imageUrlFromUI
        data["monthlyRentalPrice"] = priceFromUI
        data["rentalType"] = rentalTypeFromUI
        data["numberOfBedrooms"] = numberOfBedroomsFromUI

        // change data to database
        db.collection("LandlordProfiles")
            .document(uid)
            .collection("Properties")
            .document(this.docId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { docRef ->
                Log.d("TESTING", "Property updated successfully")
            }
            .addOnFailureListener { ex ->
                Log.e("TESTING", "Exception occurred while updating property : $ex", )
            }
    }

    // option menu handler
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        if (isCreate == false) {
            menu.findItem(R.id.mi_option_04).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isCreate == false){
            return when (item.itemId) {
                R.id.mi_option_01 -> {
                    auth.signOut()
                    val intent = Intent(this@CreateListingScreen, LoginScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    true
                }
                R.id.mi_option_04 -> {
                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            return when (item.itemId) {
                R.id.mi_option_01 -> {
                    auth.signOut()
                    val intent = Intent(this@CreateListingScreen, LoginScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }
}
