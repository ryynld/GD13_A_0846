package com.example.gd11_a_0846

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gd11_a_0846.api.MahasiswaApi
import com.example.gd11_a_0846.models.Mahasiswa
import com.google.gson.Gson
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class AddEditActivity : AppCompatActivity() {
    companion object {
        private val FAKULTAS_LIST = arrayOf("FTI", "FT", "FBE", "FISIP", "FH")
        private val PRODI_LIST = arrayOf(
            "Informatika",
            "Arsitektur",
            "Biologi",
            "Manajemen",
            "Ilmu Komunikasi",
            "Ilmu Hukum",
        )
    }

    private var etNama: EditText? = null
    private var etNPM: EditText? = null
    private var etFakultas: AutoCompleteTextView? = null
    private var etProdi: AutoCompleteTextView? = null
    private var layoutLoading: LinearLayout? = null
    private var queue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit)

        queue = Volley.newRequestQueue(this)
        etNama = findViewById(R.id.et_nama)
        etNPM = findViewById(R.id.et_npm)
        etFakultas = findViewById(R.id.et_fakultas)
        etProdi = findViewById(R.id.et_prodi)
        layoutLoading = findViewById(R.id.layout_loading)

        seExposedDropDownMenu()

        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        btnCancel.setOnClickListener { finish() }
        val btnSave = findViewById<Button>(R.id.btn_save)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val id = intent.getLongExtra("id", -1)
        if (id == -1L) {
            tvTitle.setText("Tambah Mahasiwa")
            btnSave.setOnClickListener { createMahaiswa() }
        } else {
            tvTitle.setText("Edit Mahasiswa")
            getMahasiswaById(id)
            btnSave.setOnClickListener { updateMahasiwa(id) }
        }
    }

    fun seExposedDropDownMenu() {
        val adapterFakultas: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            R.layout.item_list, FAKULTAS_LIST
        )
        etFakultas!!.setAdapter(adapterFakultas)

        val adapterProdi: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            R.layout.item_list, PRODI_LIST
        )
        etProdi!!.setAdapter(adapterProdi)

    }

    private fun getMahasiswaById(id: Long) {
        setLoading(true)
        val stringRequest: StringRequest = object :
            StringRequest(
                Method.GET,
                MahasiswaApi.GET_BY_ID_URL + id,
                Response.Listener { response ->
                    val gson = Gson()
                    val mahasiswa = gson.fromJson(response, Mahasiswa::class.java)

                    etNama!!.setText(mahasiswa.nama)
                    etNPM!!.setText(mahasiswa.npm)
                    etFakultas!!.setText(mahasiswa.fakultas)
                    etProdi!!.setText(mahasiswa.prodi)
                    seExposedDropDownMenu()

                    Toast.makeText(
                        this@AddEditActivity,
                        "Data berhasil diambil!",
                        Toast.LENGTH_SHORT
                    ).show()
                    setLoading(false)
                },
                Response.ErrorListener { error ->
                    setLoading(false)
                    try {
                        val responseBody =
                            String(error.networkResponse.data, StandardCharsets.UTF_8)
                        val errors = JSONObject(responseBody)
                        Toast.makeText(
                            this@AddEditActivity,
                            errors.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@AddEditActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                return headers
            }
        }
        queue!!.add(stringRequest)
    }

    private fun createMahaiswa() {
        setLoading(true)

        val mahasiswa = Mahasiswa(
            etNama!!.text.toString(),
            etNPM!!.text.toString(),
            etFakultas!!.text.toString(),
            etProdi!!.text.toString(),
        )

        val stringRequest: StringRequest =
            object :
                StringRequest(Method.POST, MahasiswaApi.ADD_URL, Response.Listener { response ->
                    val gson = Gson()
                    var mahasiswa = gson.fromJson(response, Mahasiswa::class.java)

                    if (mahasiswa != null)
                        Toast.makeText(
                            this@AddEditActivity,
                            "Data Berhasil Ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()

                    val returnIntent = Intent()
                    setResult(RESULT_OK, returnIntent)
                    finish()

                    setLoading(false)
                }, Response.ErrorListener { error ->
                    setLoading(false)
                    try {
                        val responseBody =
                            String(error.networkResponse.data, StandardCharsets.UTF_8)
                        val errors = JSONObject(responseBody)
                        Toast.makeText(
                            this@AddEditActivity,
                            errors.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@AddEditActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    return headers
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    val gson = Gson()
                    val requestBody = gson.toJson(mahasiswa)
                    return requestBody.toByteArray(StandardCharsets.UTF_8)
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }

        queue!!.add(stringRequest)
    }

    private fun updateMahasiwa(id: Long) {
        setLoading(true)

        val mahasiswa = Mahasiswa(
            etNama!!.text.toString(),
            etNPM!!.text.toString(),
            etFakultas!!.text.toString(),
            etProdi!!.text.toString(),
        )

        val stringRequest: StringRequest = object :
            StringRequest(Method.PUT, MahasiswaApi.UPDATE_URL + id, Response.Listener { response ->
                val gson = Gson()
                var mahasiswa = gson.fromJson(response, Mahasiswa::class.java)

                if (mahasiswa != null)
                    Toast.makeText(
                        this@AddEditActivity,
                        "Data berhasil diupdate",
                        Toast.LENGTH_SHORT
                    ).show()

                val returnIntent = Intent()
                setResult(RESULT_OK, returnIntent)
                finish()

                setLoading(false)
            }, Response.ErrorListener { error ->
                setLoading(false)
                try {
                    val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                    val errors = JSONObject(responseBody)
                    Toast.makeText(
                        this@AddEditActivity,
                        errors.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(this@AddEditActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                return headers
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val gson = Gson()
                val requestBody = gson.toJson(mahasiswa)
                return requestBody.toByteArray(StandardCharsets.UTF_8)
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }
        queue!!.add(stringRequest)
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
            layoutLoading!!.visibility = View.VISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            layoutLoading!!.visibility = View.INVISIBLE
        }
    }
}