package com.example.smartdeliveryapp.presentation.kyc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.smartdeliveryapp.data.api.ApiService
import com.example.smartdeliveryapp.databinding.ActivityKycBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class KycActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKycBinding

    @Inject lateinit var apiService: ApiService

    private var cinUri: Uri? = null
    private var permisUri: Uri? = null
    private var selfieUri: Uri? = null
    private var currentType: String = ""

    // Image picker launcher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                when (currentType) {
                    "cin" -> {
                        cinUri = uri
                        binding.cinPreviewContainer.visibility = View.VISIBLE
                        binding.ivCinPreview.setImageURI(uri)
                        binding.tvCinFilename.text = getFileName(uri)
                        binding.ivCinCheck.visibility = View.VISIBLE
                    }
                    "permis" -> {
                        permisUri = uri
                        binding.permisPreviewContainer.visibility = View.VISIBLE
                        binding.ivPermisPreview.setImageURI(uri)
                        binding.tvPermisFilename.text = getFileName(uri)
                        binding.ivPermisCheck.visibility = View.VISIBLE
                    }
                    "selfie" -> {
                        selfieUri = uri
                        binding.ivSelfiePreview.visibility = View.VISIBLE
                        binding.ivSelfiePreview.setImageURI(uri)
                        binding.ivSelfieCheck.visibility = View.VISIBLE
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKycBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUploadCin.setOnClickListener {
            currentType = "cin"
            openImagePicker()
        }
        binding.btnUploadPermis.setOnClickListener {
            currentType = "permis"
            openImagePicker()
        }
        binding.btnUploadSelfie.setOnClickListener {
            currentType = "selfie"
            openImagePicker()
        }

        binding.btnSubmitKyc.setOnClickListener {
            submitDocuments()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                if (idx >= 0) return it.getString(idx)
            }
        }
        return uri.lastPathSegment ?: "file"
    }

    private fun uriToFile(uri: Uri, fieldName: String): File {
        val ext = contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
        val file = File(cacheDir, "${fieldName}_${System.currentTimeMillis()}.$ext")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun submitDocuments() {
        if (cinUri == null || permisUri == null || selfieUri == null) {
            Toast.makeText(this, "Veuillez sélectionner les 3 documents requis", Toast.LENGTH_LONG).show()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cinFile = uriToFile(cinUri!!, "cin")
                val permisFile = uriToFile(permisUri!!, "permis")
                val selfieFile = uriToFile(selfieUri!!, "selfie")

                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                val token = prefs.getString("token", "") ?: ""
                val baseUrl = "http://192.168.100.6:5001"

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("cin", cinFile.name, cinFile.asRequestBody("image/*".toMediaTypeOrNull()))
                    .addFormDataPart("permis", permisFile.name, permisFile.asRequestBody("image/*".toMediaTypeOrNull()))
                    .addFormDataPart("selfie", selfieFile.name, selfieFile.asRequestBody("image/*".toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder()
                    .url("$baseUrl/api/kyc/upload")
                    .addHeader("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        showStatusBanner("✅ Documents soumis ! En cours de vérification.", "#32D74B")
                        binding.btnSubmitKyc.isEnabled = false
                        binding.btnSubmitKyc.text = "Documents soumis ✓"
                    } else {
                        Toast.makeText(
                            this@KycActivity,
                            "Erreur: ${response.body?.string()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@KycActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSubmitKyc.isEnabled = !show
    }

    private fun showStatusBanner(message: String, color: String) {
        binding.statusBanner.visibility = View.VISIBLE
        binding.tvStatusBanner.text = message
    }
}
