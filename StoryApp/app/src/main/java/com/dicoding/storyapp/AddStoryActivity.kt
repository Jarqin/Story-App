package com.dicoding.storyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userModel: UserModel
    private var getFile: File? = null
    private var result: Bitmap? = null
    private var location: Location? = null
    private val viewModel: AddStoryViewModel by viewModels { ViewModelFactory.getInstance(this) }

    companion object {
        const val USER_EXTRA = "user"
        const val CAMERA_X = 200
        private const val TAG = "AddStoryActivity"
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSION = 10
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (!allPermissionsGranted()) {
                Helper.showToastLong(this, getString(R.string.invalid_permission))
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar()

        userModel = intent.getParcelableExtra(USER_EXTRA)!!
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setPermission()
        setButtonListener()
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, REQUEST_CODE_PERMISSION)
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == CAMERA_X) {
            val file = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = file
            result = Helper.rotateBitmap(BitmapFactory.decodeFile(getFile?.path), isBackCamera)
        }
        binding.previewPhoto.setImageBitmap(result)
    }

    private val launcherIntentGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val selectedPhoto: Uri = it.data?.data as Uri
            val file = Helper.uriToFile(selectedPhoto, this@AddStoryActivity)
            getFile = file
            binding.previewPhoto.setImageURI(selectedPhoto)
        }
    }

    private fun setButtonListener() {
        with(binding){
            btnGallery.setOnClickListener { startGallery() }
            btnCamera.setOnClickListener { startCameraX() }
            btnUpload.setOnClickListener { startUpload() }
            switchCompat.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    setLocation()
                }
                else {
                    location = null
                }
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun startCameraX() {
        launcherIntentCameraX.launch(Intent(this, CameraActivity::class.java))
    }

    private fun startUpload() {
        when {
            binding.inputDeskripsi.text.toString().isEmpty() -> {
                binding.inputDeskripsi.error = getString(R.string.invalid_description)
            }
            getFile != null -> {
                val image = Helper.reduceFileImage(getFile as File)
                val description = binding.inputDeskripsi.text.toString().toRequestBody("application/json;charset=utf-8".toMediaType())
                val requestFile = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart = MultipartBody.Part.createFormData("photo", image.name, requestFile)
                var lat: RequestBody? = null
                var lon: RequestBody? = null

                if (location != null) {
                    lat = location?.latitude.toString().toRequestBody("text/plain".toMediaType())
                    lon = location?.longitude.toString().toRequestBody("text/plain".toMediaType())
                }

                viewModel.postStory(userModel.token, description, imageMultipart, lat, lon).observe(this) {
                    if (it != null) {
                        when (it) {
                            is ResultResponse.Loading -> {
                                setLoading(true)
                            }
                            is ResultResponse.Success -> {
                                AlertDialog.Builder(this@AddStoryActivity).apply {
                                    setTitle("Yeah!")
                                    setMessage("Selamat anda berhasil upload story")
                                    setPositiveButton("Lanjut") { _, _ ->
                                        val intent = Intent(context, ListStoryActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                }
                                setLoading(false)
                                Toast.makeText(this@AddStoryActivity, R.string.upload_success, Toast.LENGTH_LONG).show()
                                finish()
                            }
                            is ResultResponse.Error -> {
                                setLoading(false)
                                AlertDialog.Builder(this).apply {
                                    setTitle(getString(R.string.information))
                                    setMessage(getString(R.string.upload_failed) + ", ${it.error}")
                                    setPositiveButton(getString(R.string.continue_)) { _, _ -> setLoading(false) }
                                    create()
                                    show()
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Helper.showToastShort(this@AddStoryActivity, getString(R.string.no_attach_file))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setLocation() {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    location = it
                    Log.d(TAG, "Lat : ${it.latitude}, Lon : ${it.longitude}")
                }
                else {
                    Helper.showToastLong(this, getString(R.string.enable_gps_permission))
                    binding.switchCompat.isChecked = false
                }
            }
        }
        else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        Log.d(TAG, "$it")
        if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            setLocation()
        }
        else binding.switchCompat.isChecked = false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }

    private fun setLoading(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        }
        else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}