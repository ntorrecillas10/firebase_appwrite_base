package com.nacho.firebase_appwrite_base

import java.io.Serializable

data class Pelea  (var id:String?="",
                   var id_equipo1: String? = "",
                   var id_equipo2:String?="",
                   var fecha_pelea:String?="",
                   var url_equipo1:String?="",
                   var url_equipo2:String?="",
                   var nombre_equipo1:String?="",
                   var nombre_equipo2:String?=""
): Serializable