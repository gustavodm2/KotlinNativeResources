package com.example.formulario


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import com.example.formulario.ui.theme.FormularioTheme
import com.google.android.gms.location.FusedLocationProviderClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        setContent {
            FormularioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterApp(fusedLocationClient)
                }
            }
        }
    }

    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        var currentPhotoPath: String? = null
    }
}

@Composable
fun RegisterApp(fusedLocationClient: FusedLocationProviderClient) {
    var emailField by remember { mutableStateOf("") }
    var nameField by remember { mutableStateOf("") }
    var commentField by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var capturedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var savedData by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    val context = LocalContext.current
    val activity = (context as? MainActivity) ?: return
    val file = activity.createImageFile()
    val dbHelper = DatabaseHelper(context)

    val fileProviderAuthority = "com.example.formulario.provider"
    val uri = FileProvider.getUriForFile(context, fileProviderAuthority, file)

    val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) {
        if (it) {
            capturedImageUri = uri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(onClick = {
            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text(text = "Capture Image From Camera")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            locationText = "Latitude: ${it.latitude}, Longitude: ${it.longitude}"
                        } ?: run {
                            locationText = "Location not available"
                        }
                    }
            } else {
                Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Get Current Location")
        }

        Text(text = locationText, modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = emailField,
            onValueChange = { emailField = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nameField,
            onValueChange = { nameField = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = commentField,
            onValueChange = { commentField = it },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (capturedImageUri != Uri.EMPTY) {
            Image(
                painter = rememberImagePainter(capturedImageUri),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val name = nameField
                val email = emailField
                val comment = commentField
                val photoPath = MainActivity.currentPhotoPath ?: ""

                val result = dbHelper.insertData(name, email, comment, photoPath)

                if (result != -1L) {
                    Toast.makeText(context, "Data Saved Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to Save Data", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            savedData = dbHelper.getAllData()
        }) {
            Text("Show Data")
        }

        savedData.forEach { data ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Name: ${data[DatabaseHelper.COLUMN_NAME]}")
                Text(text = "Email: ${data[DatabaseHelper.COLUMN_EMAIL]}")
                Text(text = "Comment: ${data[DatabaseHelper.COLUMN_COMMENT]}")
                Text(text = "Photo: ${data[DatabaseHelper.COLUMN_PHOTO_PATH]}")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}








