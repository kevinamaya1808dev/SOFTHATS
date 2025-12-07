package com.example.softhats.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.softhats.database.GorraEntity
import org.json.JSONArray
import org.json.JSONObject

class GorraService(private val context: Context) {

    fun obtenerGorras(
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
                onSuccess(lista)
            },
            { error ->
                onError(error.message ?: "Error desconocido")
            }
        )

        Volley.newRequestQueue(context).add(request)
    }
}
