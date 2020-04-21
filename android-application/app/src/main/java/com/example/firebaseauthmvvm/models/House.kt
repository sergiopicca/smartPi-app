package com.example.firebaseauthmvvm.models

import android.os.Parcelable
import com.google.android.gms.location.places.Place
import kotlinx.android.parcel.Parcelize

@Parcelize
class House(var houseuid: String, var name: String, var address: String, var lat: String,
            var long: String, var place: String, var telephone: String, var owner: String): Parcelable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )
}