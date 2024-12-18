package com.nacho.firebase_appwrite_base
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import java.io.Serializable
import java.util.Date
import java.util.Locale
import kotlin.text.format

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

data class Usuario (
    var nombre: String = "",
    var grupo: String = "",
    var avatar: String = "",
    var rating: Float = 0f,
    var key: String = "",
    //Guardamos la fecha de creacion en formato YYYY-MM-DD
    var fecha: String = dateFormat.format(Date())
):Serializable