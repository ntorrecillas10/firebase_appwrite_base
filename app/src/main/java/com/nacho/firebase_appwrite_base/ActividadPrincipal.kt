package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nacho.firebase_appwrite_base.databinding.ActivityPrincipalBinding

class ActividadPrincipal : AppCompatActivity() {


    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonCrearSuperheroe.setOnClickListener{
            val intent = Intent(this, CrearSuperheroe::class.java)
            startActivity(intent)
        }
        binding.botonListaSuperheroe.setOnClickListener{
            val intent = Intent(this, VerListaSuperheroes::class.java)
            startActivity(intent)
        }
        binding.botonCrearGrupo.setOnClickListener{
            val intent = Intent(this, CrearGrupo::class.java)
            startActivity(intent)
        }
        binding.botonListaGrupo.setOnClickListener{
            val intent = Intent(this, VerListaGrupos::class.java)
            startActivity(intent)
        }


    }
}