package com.nacho.firebase_appwrite_base

import android.icu.text.SimpleDateFormat
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.Locale

val dateFormatGrupo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
@Parcelize
data class Grupo(
    val nombre: String = "",
    val lugarCreacion: String = "",
    val numMiembros: Int = 0,
    val avatarUrl: String = "",
    val key: String = "",
    val fecha: String = dateFormatGrupo.format(Date())
): Parcelable

