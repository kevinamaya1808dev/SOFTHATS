package com.example.softhats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ACTUALIZACIÓN: Agregamos CarritoEntity a la lista y subimos la versión a 2
@Database(entities = [GorraEntity::class, CarritoEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    // DAOs existentes
    abstract fun gorraDao(): GorraDao

    // NUEVO: DAO del Carrito
    abstract fun carritoDao(): CarritoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hats_go_database"
                )
                    // Importante: Esto permite borrar la BD vieja y crear la nueva si cambias la versión
                    // Evita errores mientras estás desarrollando y probando cambios.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}