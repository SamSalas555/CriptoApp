package com.example.criptoapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.OutputStreamWriter

class TextActivity : AppCompatActivity() {
    private val requestCode = 123
    private var selectedFileUri: Uri? = null
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    selectedFileUri = data.data
                    selectedFileUri?.let { readFileContent(it) }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
        val selectFileBtn = findViewById<Button>(R.id.sel_btn)
        val encryptBtn = findViewById<Button>(R.id.c_btn)
        selectFileBtn.setOnClickListener {
            openFilePicker()
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        } else {
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
        }
        encryptBtn.setOnClickListener {
            processAndSaveText()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.requestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, puedes realizar las operaciones que requieren este permiso.
                } else {
                    // Permiso denegado, muestra un mensaje al usuario o desactiva las funciones que requieren este permiso.
                }
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"

        filePickerLauncher.launch(intent)
    }

    private fun readFileContent(fileUri: Uri) {
        val contentResolver = contentResolver
        val inputStream = contentResolver.openInputStream(fileUri)
        if (inputStream != null) {
            val content = inputStream.bufferedReader().use { it.readText() }
            val textView = findViewById<TextView>(R.id.content_tv)
            textView.text = content
        }
    }

    private fun processAndSaveText() {
        val textView = findViewById<TextView>(R.id.content_tv)
        val originalText = textView.text.toString()

        val processedText = originalText.replace("[\\s\\n]".toRegex(), "")
            .map { (it.toInt() + 3).toChar() }
            .joinToString("")
            .uppercase()

        if (selectedFileUri != null) {
            try {
                // Obtén el URI del archivo seleccionado
                val selectedFileUri = selectedFileUri!!

                // Crea un nuevo URI para el archivo de salida (diferente del archivo original)
                val newFileUri = generateNewFileUri(selectedFileUri)

                // Abre un OutputStream para escribir el texto procesado en el nuevo archivo
                val outputStream = contentResolver.openOutputStream(newFileUri)
                if (outputStream != null) {
                    val writer = OutputStreamWriter(outputStream)
                    writer.write(processedText)
                    writer.close()
                    Toast.makeText(this, "Proceso exitoso. Nuevo archivo guardado.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al escribir en el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateNewFileUri(originalFileUri: Uri): Uri {
        // Obtén el nombre del archivo original sin extensión
        val originalFileName = getContentNameFromUri(originalFileUri)

        // Crea un nuevo nombre de archivo con _m.txt como extensión
        val newFileName = "${originalFileName}_m.txt"

        // Obtén el URI del directorio donde se encuentra el archivo original
        val parentDirectory = Uri.parse("${originalFileUri.scheme}://${originalFileUri.authority}/${originalFileUri.pathSegments.first()}")

        // Crea el nuevo URI para el archivo de salida en el mismo directorio
        return Uri.withAppendedPath(parentDirectory, newFileName)
    }

    private fun getContentNameFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        var displayName = ""
        cursor?.use {
            if (it.moveToFirst()) {
                displayName = it.getString(it.getColumnIndexOrThrow("_display_name"))
            }
        }
        return displayName
    }
}
