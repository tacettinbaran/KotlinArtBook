package com.baran.kotlinartbook.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Adapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.baran.kotlinartbook.R
import com.baran.kotlinartbook.adapter.ArtAdapter
import com.baran.kotlinartbook.databinding.ActivityMainBinding
import com.baran.kotlinartbook.model.Art
import java.lang.Exception
import java.util.Arrays

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var artList: ArrayList<Art>

    private lateinit var artAdapter: ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            ActivityMainBinding.inflate(layoutInflater) //ne zamanki xml ile kod bağlantısı kullanacaksak layoutInflater devreye girer
        val view = binding.root
        setContentView(view)
        //setContentView(R.layout.activity_main)
        artList = ArrayList<Art>()
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter

        getDataFromSql()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflater
        //ne zamanki xml ile kod bağlantısı kullanacaksak menuInflater devreye girer

        val menuInflater = menuInflater.inflate(R.menu.art_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.add_art_item) {
            val intent = Intent(this@MainActivity, ArtDetailsActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun getDataFromSql() {

        try {
            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

            val cursor = database.rawQuery("SELECT * FROM arts", null)
            val artNameIx = cursor.getColumnIndex("artname")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {
                val name = cursor.getString(artNameIx)
                val id = cursor.getInt(idIx)
                val art = Art(id, name)
                artList.add(art)
            }

            artAdapter.notifyDataSetChanged()

            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}