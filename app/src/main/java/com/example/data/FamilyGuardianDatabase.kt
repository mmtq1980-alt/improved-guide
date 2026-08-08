package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        FamilyEntity::class,
        PlaceEntity::class,
        LocationHistoryEntity::class,
        EventLogEntity::class,
        TripEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FamilyGuardianDatabase : RoomDatabase() {
    abstract fun dao(): FamilyGuardianDao

    companion object {
        @Volatile
        private var INSTANCE: FamilyGuardianDatabase? = null

        fun getDatabase(context: Context): FamilyGuardianDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamilyGuardianDatabase::class.java,
                    "family_guardian_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
