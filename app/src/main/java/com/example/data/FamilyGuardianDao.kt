package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyGuardianDao {
    // Users
    @Query("SELECT * FROM users WHERE familyId = :familyId")
    fun getFamilyMembers(familyId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET latitude = :lat, longitude = :lng, speedKmh = :speed, batteryLevel = :battery, lastUpdated = :timestamp WHERE id = :userId")
    suspend fun updateUserLocation(userId: String, lat: Double, lng: Double, speed: Float, battery: Int, timestamp: Long)

    @Query("UPDATE users SET isLocationSharingEnabled = :enabled WHERE id = :userId")
    suspend fun updateLocationSharing(userId: String, enabled: Boolean)

    // Family
    @Query("SELECT * FROM family WHERE id = :familyId")
    fun getFamily(familyId: String): Flow<FamilyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)

    // Places / Geofences
    @Query("SELECT * FROM places WHERE familyId = :familyId ORDER BY createdAt DESC")
    fun getPlaces(familyId: String): Flow<List<PlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    // Location History
    @Query("SELECT * FROM location_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLocationHistoryForUser(userId: String): Flow<List<LocationHistoryEntity>>

    @Query("SELECT * FROM location_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getLocationHistoryFromTime(startTime: Long): Flow<List<LocationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(point: LocationHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoints(points: List<LocationHistoryEntity>)

    // Event Logs
    @Query("SELECT * FROM event_logs WHERE familyId = :familyId ORDER BY timestamp DESC")
    fun getEventLogs(familyId: String): Flow<List<EventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventLog(log: EventLogEntity)

    @Query("DELETE FROM event_logs WHERE familyId = :familyId")
    suspend fun clearEventLogs(familyId: String)

    // Shared Trips
    @Query("SELECT * FROM trips WHERE userId = :userId AND isCompleted = 0 LIMIT 1")
    fun getActiveTripForUser(userId: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Query("UPDATE trips SET currentLatitude = :lat, currentLongitude = :lng, progressPercent = :progress, etaMinutes = :eta WHERE id = :tripId")
    suspend fun updateTripProgress(tripId: String, lat: Double, lng: Double, progress: Int, eta: Int)

    @Query("UPDATE trips SET isCompleted = 1 WHERE id = :tripId")
    suspend fun completeTrip(tripId: String)

    // User Settings
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getUserSettings(userId: String = "current_user"): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettingsEntity)
}
