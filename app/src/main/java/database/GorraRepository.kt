package com.example.softhats.database

class GorraRepository(private val gorraDao: GorraDao) {

    // 1. Función para LEER los datos (para mostrar en la app cuando no hay internet)
    suspend fun obtenerGorrasLocales(): List<GorraEntity> {
        return gorraDao.obtenerTodas()
    }

    // 2. LÓGICA DE SINCRONIZACIÓN (ESPEJO)
    // Esta función se llamará cuando el Módulo 4 traiga datos de internet.
    suspend fun sincronizarGorras(nuevasGorras: List<GorraEntity>) {
        // Paso A: Borramos la "cache" vieja para no tener datos duplicados o antiguos
        gorraDao.borrarTodo()

        // Paso B: Insertamos la lista nueva y fresca que llegó del servidor
        gorraDao.insertarTodas(nuevasGorras)
    }
}