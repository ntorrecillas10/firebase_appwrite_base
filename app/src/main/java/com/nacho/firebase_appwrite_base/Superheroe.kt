package com.nacho.firebase_appwrite_base
import android.icu.text.SimpleDateFormat
import java.io.Serializable
import java.util.Date
import java.util.Locale

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

data class Superheroe (
    var nombre: String = "",
    var grupo: String = "",
    var avatar: String = "",
    var rating: Float = 0f,
    var key: String = "",
    //Guardamos la fecha de creacion en formato YYYY-MM-DD
    var fecha: String = dateFormat.format(Date())
):Serializable