package com.nacho.firebase_appwrite_base

import android.icu.text.SimpleDateFormat
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.Locale

val dateFormatGrupo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
@Parcelize
data class Grupo(
    var nombre: String = "",
    var lugarCreacion: String = "",
    var worthNum: Int = 0,
    var avatarUrl: String = "",
    var key: String = "",
    var fecha: String = dateFormatGrupo.format(Date())
): Parcelable {
    override fun toString(): String {
        return nombre!!
    }
}

