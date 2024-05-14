package com.example.projectthree

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class flower : AppCompatActivity() {
    private val Request_Pick_image = 1001
    private val Request_Capture_image = 1002
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var imageLabeler: ImageLabeler
    private lateinit var photoFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flower)
        imageView = findViewById(R.id.imageView3)
        textView = findViewById(R.id.result)
        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_IMAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(flower::class.simpleName, "Image permissions granted")
                } else {
                    Log.d(flower::class.simpleName, "Image permissions denied")
                }
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(flower::class.simpleName, "Camera permissions granted")
                } else {
                    Log.d(flower::class.simpleName, "Camera permissions denied")
                }
            }
        }
    }

    fun onPickImage(view: View) {
        val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permission, REQUEST_IMAGE_PERMISSION)
            }
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, Request_Pick_image)
    }

    fun onPickCamera(view: View) {
        photoFile = createPhotoFile()
        val fileUri: Uri = FileProvider.getUriForFile(this, "com.example.projectthree.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(intent, Request_Capture_image)
    }

    private fun createPhotoFile(): File {
        val photoFileDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PROJECTTHREE_IMAGE_HELPER")
        if (!photoFileDir.exists()) {
            photoFileDir.mkdirs()
        }
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(photoFileDir, name)
    }

    companion object {
        private const val REQUEST_IMAGE_PERMISSION = 0
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Request_Pick_image -> {
                    val uri: Uri? = data?.data
                    uri?.let {
                        val bitmap = loadFromUri(uri)
                        bitmap?.let {
                            imageView.setImageBitmap(it)
                            runClassification(it)
                        }
                    }
                }
                Request_Capture_image -> {
                    Log.d("ml", "Received callback from camera")
                    val bitmap: Bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    imageView.setImageBitmap(bitmap)
                    runClassification(bitmap)
                }
                else -> {
                    Log.w("ml", "Unknown request code: $requestCode")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    protected open fun runClassification(bitmap: Bitmap) {
        /* val inputImage = InputImage.fromBitmap(bitmap, 0)
         imageLabeler.process(inputImage)
             .addOnSuccessListener { labels ->
                 if (labels.isNotEmpty()) {
                     val builder = StringBuilder()
                     for (label in labels) {
                         val labelText = label.text
                         val confidence = label.confidence
                         builder.append("Label: $labelText, Confidence: $confidence\n")
                     }
                     textView.text = builder.toString()
                 } else {
                     textView.text = "No labels found"
                 }
             }
             .addOnFailureListener { e ->
                 Log.e("ImageLabel", "Labeling failed", e)
                 textView.text = "Labeling failed: ${e.message}"
             }

         */
    }
    protected open fun getOutputTextView(): TextView {
        return textView
    }
    protected open fun getinputImageView(): ImageView {
        return imageView
    }
}