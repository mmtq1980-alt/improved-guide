package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyGuardianDao {

    // ============================================================
    // USERS / FAMILY MEMBERS
    // ============================================================

    @Query("SELECT * FROM users WHERE familyId = :familyId ORDER BY name ASC")
    fun getFamilyMembers(familyId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * تحديث بيانات فرد من العائلة.
     * يستخدم عند تعديل الاسم أو الهاتف أو البريد أو صلة القرابة.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: UserEntity)

    /**
     * حذف فرد من العائلة.
     *
     * ملاحظة:
     * لا نحذف المستخدم الحالي/مالك العائلة من خلال هذه العملية.
     * التحقق من ذلك يتم في Repository / ViewModel.
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /**
     * حذف فرد مباشرة بواسطة المعرّف.
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    /**
     * حذف جميع أفراد عائلة معينة.
     * تستخدم فقط عند حذف/إعادة تهيئة العائلة.
     */
    @Query("DELETE FROM users WHERE familyId = :familyId")
    suspend fun deleteAllFamilyMembers(familyId: String)

    /**
     * عدد أفراد العائلة.
     */
    @Query("SELECT COUNT(*) FROM users WHERE familyId = :familyId")
    suspend fun getFamilyMembersCount(familyId: String): Int

    /**
     * تحديث الموقع والبيانات المرتبطة به.
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
     * تفعيل/إيقاف مشاركة الموقع.
     */
    @Query(
        "UPDATE users SET isLocationSharingEnabled = :enabled WHERE id = :userId"
    )
    suspend fun updateLocationSharing(
        userId: String,
        enabled: Boolean
    )


    // ============================================================
    // FAMILY
    // ============================================================

    @Query("SELECT * FROM family WHERE id = :familyId")
    fun getFamily(familyId: String): Flow<FamilyEntity?>

    @Query("SELECT * FROM family WHERE id = :familyId")
    suspend fun getFamilySync(familyId: String): FamilyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)

    /**
     * تحديث بيانات العائلة.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFamily(family: FamilyEntity)

    /**
     * حذف العائلة.
     */
    @Delete
    suspend fun deleteFamily(family: FamilyEntity)


    // ============================================================
    // PLACES / GEOFENCES
    // ============================================================

    @Query(
        "SELECT * FROM places WHERE familyId = :familyId ORDER BY createdAt DESC"
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

    /**
     * حذف سجل المواقع الخاص بفرد.
     * مهم عند حذف فرد من العائلة حتى لا تبقى بياناته المحلية.
     */
    @Query("DELETE FROM location_history WHERE userId = :userId")
    suspend fun deleteLocationHistoryForUser(
        userId: String
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
        "DELETE FROM event_logs WHERE familyId = :familyId"
    )
    suspend fun clearEventLogs(
        familyId: String
    )

    /**
     * حذف سجلات الأحداث الخاصة بفرد معين.
     */
    @Query(
        "DELETE FROM event_logs WHERE userId = :userId"
    )
    suspend fun deleteEventLogsForUser(
        userId: String
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
        "SELECT * FROM trips ORDER BY startTime DESC"
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
        "UPDATE trips SET isCompleted = 1 WHERE id = :tripId"
    )
    suspend fun completeTrip(
        tripId: String
    )

    /**
     * حذف الرحلات الخاصة بفرد عند حذفه من العائلة.
     */
    @Query("DELETE FROM trips WHERE userId = :userId")
    suspend fun deleteTripsForUser(
        userId: String
    )


    // ============================================================
    // USER SETTINGS
    // ============================================================

    @Query(
        "SELECT * FROM user_settings WHERE userId = :userId"
    )
    fun getUserSettings(
        userId: String = "current_user"
    ): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(
        settings: UserSettingsEntity
    )

    /**
     * حذف إعدادات مستخدم.
     */
    @Query(
        "DELETE FROM user_settings WHERE userId = :userId"
    )
    suspend fun deleteUserSettings(
        userId: String
    )


    // ============================================================
    // COMPLETE MEMBER REMOVAL
    // ============================================================

    /**
     * حذف جميع البيانات المحلية المرتبطة بفرد من العائلة.
     *
     * يتم استدعاؤها من Repository بعد التأكد من أن الشخص
     * ليس مالك العائلة.
     *
     * Room لا يوفر Transaction تلقائياً بين عدة استعلامات هنا،
     * لذلك يمكن استدعاء هذه العمليات من Repository داخل
     * transaction عند الحاجة.
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun removeUserRecord(
        userId: String
    )
}
