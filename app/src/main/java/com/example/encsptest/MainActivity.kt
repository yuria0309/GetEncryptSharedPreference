package com.example.encsptest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.encsptest.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    var json_str = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btn_get = binding.btnGet
        val et_name = binding.etName

        val fileLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(StartActivityForResult()) { result: ActivityResult? ->
                if (result != null) {
                    this.fileSave(
                        result
                    )
                }else{
                    Toast.makeText(this, "Activity result is null", Toast.LENGTH_SHORT).show()
                }
            }

        btn_get.setOnClickListener {
            try {
                val masterKey = MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val sharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    et_name.text.toString(),
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                val jo = JSONObject()
                sharedPreferences.all.forEach {
                    jo.put(it.key, sharedPreferences.getString(it.key, "???"))
                }
                this.json_str = jo.toString()

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("*/*")
                intent.putExtra(Intent.EXTRA_TITLE, "result.json")
                fileLauncher.launch(intent)
            }catch (e: Exception){
                Toast.makeText(this, "Exception", Toast.LENGTH_SHORT).show()
                Log.e("ENCSP", e.toString())
            }
        }
    }

    fun fileSave(result: ActivityResult){
        if (result.resultCode == RESULT_OK && result.data != null) {
            val fname = result.data!!.data
            if (fname == null) return

            try {
                val stream: OutputStream? = getContentResolver().openOutputStream(fname, "rwt")

                if (stream != null) {
                    stream.write(this.json_str.toByteArray())
                    stream.close()
                }
            } catch (e: IOException) {
                Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show()
                Log.e("ENCSP", e.toString())
            }
        }
    }
}