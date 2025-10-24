package com.example.patientmanagement.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.model.Vitals
import com.example.patientmanagement.model.VisitA
import com.example.patientmanagement.model.VisitB
import com.example.patientmanagement.utils.Converters

@Database(entities = [Patient::class, Vitals::class, VisitA::class, VisitB::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun vitalsDao(): VitalsDao
    abstract fun visitADao(): VisitADao
    abstract fun visitBDao(): VisitBDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "patient_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
