package com.example.softhats.network


import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.GorraEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import network.Constantes
import org.json.JSONArray
import org.json.JSONObject


class GorraService(private val context: Context) {

    fun obtenerYGuardarGorras(
        onSuccess: (List<GorraEntity>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = Constantes.BASE_URL + "obtener_gorras.php"

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response: JSONArray ->
                val lista = mutableListOf<GorraEntity>()
                for (i in 0 until response.length()) {
                    val obj: JSONObject = response.getJSONObject(i)
                    lista.add(
                        GorraEntity(
                            id = obj.getInt("id"),
                            nombre = obj.getString("nombre"),
                            precio = obj.getDouble("precio"),
                            descripcion = obj.getString("descripcion"),
                            urlImagen = obj.getString("imagen")
                        )
                    )
                }

                // 🔹 GUARDAR EN ROOM (Módulo 3)
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    db.gorraDao().borrarTodo()
                    db.gorraDao().insertarTodas(lista)
                }

                Toast.makeText(context, "Datos actualizados desde servidor", Toast.LENGTH_SHORT).show()
                onSuccess(lista)
            },
            { error ->
                onError(error.message ?: "Error desconocido")
            }
        )

        Volley.newRequestQueue(context).add(request)
    }
}