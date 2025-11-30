package com.example.softhats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// IMPORTANTE: Subimos la versión a 3 y agregamos FavoritoEntity a la lista
@Database(entities = [GorraEntity::class, CarritoEntity::class, FavoritoEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gorraDao(): GorraDao
    abstract fun carritoDao(): CarritoDao
    abstract fun favoritoDao(): FavoritoDao // Nuevo DAO

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
                    .fallbackToDestructiveMigration() // Borra y crea nueva BD si cambia la versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}