package com.example.landlord_g09.models

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId
    var id:String = "",
)
