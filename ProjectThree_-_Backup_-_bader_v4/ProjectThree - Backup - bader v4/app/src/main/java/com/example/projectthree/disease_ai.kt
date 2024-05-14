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
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

@Suppress("DEPRECATION")
open class disease_ai : AppCompatActivity() {
    private val Request_Pick_image = 1001
    private val Request_Capture_image = 1002
    lateinit var button: Button
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView

    companion object {
        private const val REQUEST_IMAGE_PERMISSION = 0
        private const val REQUEST_CAPTURE_IMAGE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai)
        button = findViewById(R.id.selectBtn)
        imageView = findViewById(R.id.imageView3)
        resultTextView = findViewById(R.id.result)
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


    fun onCameraClick(view: View) {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_CAPTURE_IMAGE) // Use startActivityForResult to get the image back
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Request_Pick_image -> {
                    data?.data?.let { uri ->
                        imageView.setImageURI(uri)
                        val imagePath = getPathFromUri(uri)
                        sendHttpRequest(imagePath, "http://192.168.1.95:9321/predict_diseases")
                    }
                }
                REQUEST_CAPTURE_IMAGE -> {
                    // Handle camera result
                    val bitmap = data?.extras?.get("data") as Bitmap?
                    bitmap?.let {
                        imageView.setImageBitmap(it)  // Display captured image in ImageView
                        val tempUri = saveImageToTempUri(it)  // Save bitmap to temporary URI for processing
                        tempUri?.let { uri ->
                            sendHttpRequest(getPathFromUri(uri), "http://192.168.1.95:9321/predict_diseases")
                        }
                    }
                }
            }
        }
    }

    private fun saveImageToTempUri(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
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

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getPathFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (it.moveToFirst()) {
                return it.getString(columnIndex)
            }
        }
        return ""
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            return Base64.encodeToString(it.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun sendHttpRequest(imagePath: String, serverUrl: String) {
        val imageFile = File(imagePath)
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val base64Image = bitmapToBase64(bitmap)

        val json = "{\"image\": \"$base64Image\"}"
        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url(serverUrl)
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { resultTextView.text = "Error: ${e.message}" }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("HTTP_REQUEST", "Unexpected response: $response")
                        runOnUiThread {
                            resultTextView.text = "Error: Unexpected server response ${response.code}"
                        }
                        return
                    }

                    val responseData = response.body?.string()
                    Log.d("HTTP_REQUEST", "Response: $responseData")

                    // Parse JSON and update UI
                    runOnUiThread {
                        try {
                            val classIdPattern = "cls: tensor\\(\\[(\\d+)\\.".toRegex()
                            val matchResult = classIdPattern.find(responseData!!)
                            val classId = matchResult?.groups?.get(1)?.value?.toInt()

                            val classNames = mapOf(
                                0 to "Apple___Apple_scab",
                                1 to "Apple___Black_rot",
                                2 to "Apple___Cedar_apple_rust",
                                3 to "Apple___healthy",
                                4 to "Blueberry___healthy",
                                5 to "Cherry_(including_sour)___healthy",
                                6 to "Cherry_(including_sour)___Powdery_mildew",
                                7 to "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
                                8 to "Corn_(maize)___Common_rust",
                                9 to "Corn_(maize)___healthy",
                                10 to "Corn_(maize)___Northern_Leaf_Blight",
                                11 to "Grape___Black_rot",
                                12 to "Grape___Esca_(Black_Measles)",
                                13 to "Grape___healthy",
                                14 to "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)",
                                15 to "Orange___Haunglongbing_(Citrus_greening)",
                                16 to "Peach___Bacterial_spot",
                                17 to "Peach___healthy",
                                18 to "Pepper,_bell___Bacterial_spot",
                                19 to "Pepper,_bell___healthy",
                                20 to "Potato___Early_blight",
                                21 to "Potato___healthy",
                                22 to "Potato___Late_blight",
                                23 to "Raspberry___healthy",
                                24 to "Soybean___healthy",
                                25 to "Squash___Powdery_mildew",
                                26 to "Strawberry___healthy",
                                27 to "Strawberry___Leaf_scorch",
                                28 to "Tomato___Bacterial_spot",
                                29 to "Tomato___Early_blight",
                                30 to "Tomato___healthy",
                                31 to "Tomato___Late_blight",
                                32 to "Tomato___Leaf_Mold",
                                33 to "Tomato___Septoria_leaf_spot",
                                34 to "Tomato___Spider_mites Two-spotted_spider_mite",
                                35 to "Tomato___Target_Spot",
                                36 to "Tomato___Tomato_mosaic_virus",
                                37 to "Tomato___Tomato_Yellow_Leaf_Curl_Virus"

                            )

                            val className = classNames[classId] ?: "Unknown Class"
                            resultTextView.text = "Detected: $className"
                        } catch (e: Exception) {
                            resultTextView.text = "Error parsing server response: ${e.message}"
                        }
                    }
                }
            }
        })
    }

    private fun imageToBase64(imagePath: String): String {
        val file = File(imagePath)
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(file.length().toInt())
        inputStream.read(buffer)
        inputStream.close()
        return Base64.encodeToString(buffer, Base64.NO_WRAP)
    }

}