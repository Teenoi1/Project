package com.example.app1952

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.app1952.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private var homeFragment: Home? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("DHT11")

        homeFragment = Home()
        replaceFragment(homeFragment!!)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment ?: Home())
                R.id.graph -> replaceFragment(Graph())
                R.id.camera -> replaceFragment(Camera())
                else -> {}
            }
            true
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val humidity = snapshot.child("humidity").getValue(Int::class.java) ?: 0
                    val temperature = snapshot.child("temperature").getValue(Double::class.java) ?: 0.0
                    val intensity = snapshot.child("intensity").getValue(Int::class.java) ?: 0
                    Log.d("FirebaseData", "Humidity: $humidity, Temperature: $temperature, Intensity: $intensity")

                    homeFragment?.updateData(humidity, temperature, intensity)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseData", "loadPost:onCancelled", error.toException())
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
