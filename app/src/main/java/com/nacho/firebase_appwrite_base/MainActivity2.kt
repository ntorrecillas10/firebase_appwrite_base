package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nacho.firebase_appwrite_base.databinding.ActivityListBinding
import com.nacho.firebase_appwrite_base.databinding.ActivityMain2Binding
import com.nacho.firebase_appwrite_base.databinding.ActivityMainBinding

class MainActivity2 : AppCompatActivity() {


    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonCrear.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.botonLista.setOnClickListener{
            val intent = Intent(this, List::class.java)
            startActivity(intent)
        }

    }
}