package com.baran.kotlinartbook.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaActionSound
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baran.kotlinartbook.R
import com.baran.kotlinartbook.databinding.ActivityArtDetailsBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.PropertyPermission

class ArtDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtDetailsBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    private lateinit var database: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            ActivityArtDetailsBinding.inflate(layoutInflater) //ne zamanki xml ile kod bağlantısı kullanacaksak layoutInflater devreye girer
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        //regsiter kısımları burda olmalı
        registerLauncher()
        val intent = intent
        val info = intent.getStringExtra("info")

        if (info.equals("new")){
            binding.artistText.setText("")
            binding.artistText.setText("")
            binding.yearText.setText("")
            binding.saveButton.visibility = View.VISIBLE
            binding.artImageView.setImageResource(R.drawable.select_image)
        }else{
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)

            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val  artNameIx = cursor.getColumnIndex("artname")
            val  artistNameIx = cursor.getColumnIndex("artistname")
            val  yearIx = cursor.getColumnIndex("year")
            val  imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.nameText.setText(cursor.getString(artNameIx))
                binding.artistText.setText(cursor.getString(artistNameIx))
                binding.yearText.setText(cursor.getString(yearIx))

                val  byteArray = cursor.getBlob(imageIx)
                val  bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.artImageView.setImageBitmap(bitmap)
            }
            cursor.close()

        }
        //setContentView(R.layout.activity_art_details)
    }

    fun saveButtonClick(view: View) {
        val artName = binding.nameText.text.toString()
        val artistName = binding.artistText.text.toString()
        val year = binding.yearText.text.toString()
        if (selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
               // val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS arts " +
                            "(id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)"
                )

                val sqlString = "INSERT INTO arts (artname, artistname, year, image ) VALUES(?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)
                statement.execute()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            //finish()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {

        var width = image.width
        var height = image.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 0) {
            //landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            //portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            height = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImageClick(view: View) {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                //rationale
                Snackbar.make(view, "Permission needen gor gallery", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Give Per", View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
            } else {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            //go to gallery
            val intentToGallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
            //startActivityForResult(intentToGallery)
        }
    }

    private fun registerLauncher() {

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageURI = intentFromResult.data
                        //binding.artImageView.setImageURI(imageURI)
                        if (imageURI != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(
                                        this@ArtDetailsActivity.contentResolver,
                                        imageURI
                                    )
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.artImageView.setImageBitmap(selectedBitmap)
                                } else {
                                    selectedBitmap =
                                        MediaStore.Images.Media.getBitmap(contentResolver, imageURI)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

                if (result) {
                    //permission granted
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    //permission denied
                    Toast.makeText(this@ArtDetailsActivity, "İzin gerekli", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}