package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyGuardianDao {

    // ============================================================
    // USERS / FAMILY MEMBERS
    // ============================================================

    /**
     * جلب جميع أفراد العائلة.
     */
    @Query("SELECT * FROM users WHERE familyId = :familyId ORDER BY name ASC")
    fun getFamilyMembers(familyId: String): Flow<List<UserEntity>>

    /**
     * جلب عضو محدد.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    /**
     * جلب عضو محدد بشكل مباشر.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    /**
     * إضافة عضو جديد.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * إضافة عدة أعضاء.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * تحديث بيانات عضو بالكامل.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: UserEntity)

    /**
     * حذف عضو بواسطة الكيان.
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /**
     * حذف عضو بواسطة المعرف.
     *
     * ملاحظة:
     * يتم استخدام هذه الدالة من Repository بعد التأكد
     * من أن العضو ليس مدير العائلة.
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    /**
     * التحقق من وجود عضو.
     */
    @Query("SELECT COUNT(*) FROM users WHERE id = :userId")
    suspend fun userExists(userId: String): Int

    /**
     * عدد أفراد العائلة.
     */
    @Query("SELECT COUNT(*) FROM users WHERE familyId = :familyId")
    suspend fun getFamilyMembersCount(familyId: String): Int

    /**
     * جلب معرف مدير العائلة.
     */
    @Query("SELECT adminUserId FROM family WHERE id = :familyId LIMIT 1")
    suspend fun getFamilyAdminId(familyId: String): String?

    /**
     * تحديث موقع العضو.
     */
    @Query(
        """
        UPDATE users
        SET latitude = :lat,
            longitude = :lng,
            speedKmh = :speed,
            batteryLevel = :battery,
            lastUpdated = :timestamp
        WHERE id = :userId
        """
    )
    suspend fun updateUserLocation(
        userId: String,
        lat: Double,
        lng: Double,
        speed: Float,
        battery: Int,
        timestamp: Long
    )

    /**
     * تحديث حالة مشاركة الموقع.
     */
    @Query(
        """
        UPDATE users
        SET isLocationSharingEnabled = :enabled
        WHERE id = :userId
        """
    )
    suspend fun updateLocationSharing(
        userId: String,
        enabled: Boolean
    )

    // ============================================================
    // DELETE RELATED USER DATA
    // ============================================================

    /**
     * حذف سجل المواقع التاريخي الخاص بالعضو.
     */
    @Query("DELETE FROM location_history WHERE userId = :userId")
    suspend fun deleteLocationHistoryForUser(userId: String)

    /**
     * حذف الرحلات الخاصة بالعضو.
     */
    @Query("DELETE FROM trips WHERE userId = :userId")
    suspend fun deleteTripsForUser(userId: String)

    /**
     * حذف سجلات الأحداث الخاصة بالعضو.
     */
    @Query("DELETE FROM event_logs WHERE userId = :userId")
    suspend fun deleteEventLogsForUser(userId: String)

    /**
     * حذف جميع البيانات المرتبطة بالعضو ثم حذف العضو نفسه.
     *
     * هذه العملية تتم داخل Transaction واحدة،
     * حتى لا يبقى لدينا بيانات مرتبطة بعضو تم حذفه.
     */
    @Transaction
    suspend fun deleteUserCompletely(userId: String) {
        deleteLocationHistoryForUser(userId)
        deleteTripsForUser(userId)
        deleteEventLogsForUser(userId)
        deleteUserById(userId)
    }

    // ============================================================
    // FAMILY
    // ============================================================

    /**
     * جلب بيانات العائلة.
     */
    @Query("SELECT * FROM family WHERE id = :familyId")
    fun getFamily(familyId: String): Flow<FamilyEntity?>

    /**
     * جلب بيانات العائلة بشكل مباشر.
     */
    @Query("SELECT * FROM family WHERE id = :familyId")
    suspend fun getFamilySync(familyId: String): FamilyEntity?

    /**
     * إضافة / تحديث عائلة.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)

    /**
     * تحديث بيانات العائلة.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFamily(family: FamilyEntity)

    // ============================================================
    // PLACES / GEOFENCES
    // ============================================================

    @Query(
        """
        SELECT * FROM places
        WHERE familyId = :familyId
        ORDER BY createdAt DESC
        """
    )
    fun getPlaces(familyId: String): Flow<List<PlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    @Query("DELETE FROM places WHERE id = :placeId")
    suspend fun deletePlaceById(placeId: String)

    // ============================================================
    // LOCATION HISTORY
    // ============================================================

    @Query(
        """
        SELECT * FROM location_history
        WHERE userId = :userId
        ORDER BY timestamp DESC
        """
    )
    fun getLocationHistoryForUser(
        userId: String
    ): Flow<List<LocationHistoryEntity>>

    @Query(
        """
        SELECT * FROM location_history
        WHERE timestamp >= :startTime
        ORDER BY timestamp ASC
        """
    )
    fun getLocationHistoryFromTime(
        startTime: Long
    ): Flow<List<LocationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(
        point: LocationHistoryEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoints(
        points: List<LocationHistoryEntity>
    )

    // ============================================================
    // EVENT LOGS
    // ============================================================

    @Query(
        """
        SELECT * FROM event_logs
        WHERE familyId = :familyId
        ORDER BY timestamp DESC
        """
    )
    fun getEventLogs(
        familyId: String
    ): Flow<List<EventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventLog(
        log: EventLogEntity
    )

    @Query(
        """
        DELETE FROM event_logs
        WHERE familyId = :familyId
        """
    )
    suspend fun clearEventLogs(
        familyId: String
    )

    // ============================================================
    // SHARED TRIPS
    // ============================================================

    @Query(
        """
        SELECT * FROM trips
        WHERE userId = :userId
        AND isCompleted = 0
        LIMIT 1
        """
    )
    fun getActiveTripForUser(
        userId: String
    ): Flow<TripEntity?>

    @Query(
        """
        SELECT * FROM trips
        ORDER BY startTime DESC
        """
    )
    fun getAllTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(
        trip: TripEntity
    )

    @Query(
        """
        UPDATE trips
        SET currentLatitude = :lat,
            currentLongitude = :lng,
            progressPercent = :progress,
            etaMinutes = :eta
        WHERE id = :tripId
        """
    )
    suspend fun updateTripProgress(
        tripId: String,
        lat: Double,
        lng: Double,
        progress: Int,
        eta: Int
    )

    @Query(
        """
        UPDATE trips
        SET isCompleted = 1
        WHERE id = :tripId
        """
    )
    suspend fun completeTrip(
        tripId: String
    )

    // ============================================================
    // USER SETTINGS
    // ============================================================

    @Query(
        """
        SELECT * FROM user_settings
        WHERE userId = :userId
        """
    )
    fun getUserSettings(
        userId: String = "current_user"
    ): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(
        settings: UserSettingsEntity
    )

    // ============================================================
    // FAMILY CLEANUP
    // ============================================================

    /**
     * حذف جميع أفراد العائلة.
     *
     * لن نستخدم هذه العملية من واجهة المستخدم العادية،
     * وإنما نحتفظ بها للاستخدام الإداري أو عند إعادة تهيئة العائلة.
     */
    @Query("DELETE FROM users WHERE familyId = :familyId")
    suspend fun deleteAllFamilyMembers(
        familyId: String
    )
}
