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

    /**
     * جلب جميع أفراد العائلة.
     */
    @Query(
        "SELECT * FROM users " +
        "WHERE familyId = :familyId " +
        "ORDER BY name ASC"
    )
    fun getFamilyMembers(
        familyId: String
    ): Flow<List<UserEntity>>

    /**
     * جلب فرد محدد بواسطة ID.
     */
    @Query(
        "SELECT * FROM users " +
        "WHERE id = :userId " +
        "LIMIT 1"
    )
    fun getUserById(
        userId: String
    ): Flow<UserEntity?>

    /**
     * جلب فرد محدد بشكل متزامن.
     */
    @Query(
        "SELECT * FROM users " +
        "WHERE id = :userId " +
        "LIMIT 1"
    )
    suspend fun getUserByIdSync(
        userId: String
    ): UserEntity?

    /**
     * إضافة فرد جديد للعائلة.
     */
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertUser(
        user: UserEntity
    )

    /**
     * إضافة مجموعة من أفراد العائلة.
     */
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertUsers(
        users: List<UserEntity>
    )

    /**
     * حذف فرد من العائلة بواسطة ID.
     *
     * يتم منع حذف مدير العائلة هنا أيضاً
     * كإجراء حماية إضافي.
     */
    @Query(
        "DELETE FROM users " +
        "WHERE id = :userId " +
        "AND id != (" +
        "SELECT adminUserId FROM family " +
        "WHERE id = :familyId" +
        ")"
    )
    suspend fun deleteUser(
        userId: String,
        familyId: String
    ): Int

    /**
     * حذف فرد باستخدام UserEntity.
     *
     * ملاحظة:
     * لا نستخدم @Delete للحذف الرئيسي حتى نضمن
     * عدم حذف مدير العائلة.
     */
    @Query(
        "DELETE FROM users " +
        "WHERE id = :userId " +
        "AND id != (" +
        "SELECT adminUserId FROM family " +
        "WHERE id = :familyId" +
        ")"
    )
    suspend fun deleteUserSafely(
        userId: String,
        familyId: String
    ): Int

    /**
     * معرفة عدد أفراد العائلة.
     */
    @Query(
        "SELECT COUNT(*) FROM users " +
        "WHERE familyId = :familyId"
    )
    suspend fun getFamilyMembersCount(
        familyId: String
    ): Int

    /**
     * معرفة عدد الأصدقاء المقربين.
     *
     * الحد الأقصى الذي سنسمح به في التطبيق = 3.
     */
    @Query(
        "SELECT COUNT(*) FROM users " +
        "WHERE familyId = :familyId " +
        "AND role = 'CLOSE_FRIEND'"
    )
    suspend fun getCloseFriendsCount(
        familyId: String
    ): Int

    /**
     * التحقق من وجود فرد بالاسم أو الهاتف.
     *
     * مفيد لمنع إنشاء أفراد مكررين.
     */
    @Query(
        "SELECT COUNT(*) FROM users " +
        "WHERE familyId = :familyId " +
        "AND (" +
        "phone = :phone OR " +
        "email = :email" +
        ")"
    )
    suspend fun memberAlreadyExists(
        familyId: String,
        phone: String,
        email: String
    ): Int

    // ============================================================
    // USER LOCATION
    // ============================================================

    @Query(
        "UPDATE users SET " +
        "latitude = :lat, " +
        "longitude = :lng, " +
        "speedKmh = :speed, " +
        "batteryLevel = :battery, " +
        "lastUpdated = :timestamp " +
        "WHERE id = :userId"
    )
    suspend fun updateUserLocation(
        userId: String,
        lat: Double,
        lng: Double,
        speed: Float,
        battery: Int,
        timestamp: Long
    )

    @Query(
        "UPDATE users SET " +
        "isLocationSharingEnabled = :enabled " +
        "WHERE id = :userId"
    )
    suspend fun updateLocationSharing(
        userId: String,
        enabled: Boolean
    )

    // ============================================================
    // FAMILY
    // ============================================================

    @Query(
        "SELECT * FROM family " +
        "WHERE id = :familyId " +
        "LIMIT 1"
    )
    fun getFamily(
        familyId: String
    ): Flow<FamilyEntity?>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertFamily(
        family: FamilyEntity
    )

    /**
     * تحديث بيانات العائلة.
     */
    @Query(
        "UPDATE family SET " +
        "name = :name, " +
        "inviteCode = :inviteCode, " +
        "qrCodeData = :qrCodeData " +
        "WHERE id = :familyId"
    )
    suspend fun updateFamily(
        familyId: String,
        name: String,
        inviteCode: String,
        qrCodeData: String
    )

    /**
     * جلب مدير العائلة.
     */
    @Query(
        "SELECT adminUserId FROM family " +
        "WHERE id = :familyId " +
        "LIMIT 1"
    )
    suspend fun getFamilyAdminId(
        familyId: String
    ): String?

    // ============================================================
    // PLACES / GEOFENCES
    // ============================================================

    @Query(
        "SELECT * FROM places " +
        "WHERE familyId = :familyId " +
        "ORDER BY createdAt DESC"
    )
    fun getPlaces(
        familyId: String
    ): Flow<List<PlaceEntity>>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertPlace(
        place: PlaceEntity
    )

    @Delete
    suspend fun deletePlace(
        place: PlaceEntity
    )

    // ============================================================
    // LOCATION HISTORY
    // ============================================================

    @Query(
        "SELECT * FROM location_history " +
        "WHERE userId = :userId " +
        "ORDER BY timestamp DESC"
    )
    fun getLocationHistoryForUser(
        userId: String
    ): Flow<List<LocationHistoryEntity>>

    @Query(
        "SELECT * FROM location_history " +
        "WHERE timestamp >= :startTime " +
        "ORDER BY timestamp ASC"
    )
    fun getLocationHistoryFromTime(
        startTime: Long
    ): Flow<List<LocationHistoryEntity>>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertLocationPoint(
        point: LocationHistoryEntity
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertLocationPoints(
        points: List<LocationHistoryEntity>
    )

    /**
     * حذف سجل المواقع الخاص بفرد.
     *
     * يستخدم عند حذف العضو من العائلة.
     */
    @Query(
        "DELETE FROM location_history " +
        "WHERE userId = :userId"
    )
    suspend fun deleteLocationHistoryForUser(
        userId: String
    )

    // ============================================================
    // EVENT LOGS
    // ============================================================

    @Query(
        "SELECT * FROM event_logs " +
        "WHERE familyId = :familyId " +
        "ORDER BY timestamp DESC"
    )
    fun getEventLogs(
        familyId: String
    ): Flow<List<EventLogEntity>>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertEventLog(
        log: EventLogEntity
    )

    @Query(
        "DELETE FROM event_logs " +
        "WHERE familyId = :familyId"
    )
    suspend fun clearEventLogs(
        familyId: String
    )

    @Query(
        "DELETE FROM event_logs " +
        "WHERE userId = :userId"
    )
    suspend fun deleteEventLogsForUser(
        userId: String
    )

    // ============================================================
    // SHARED TRIPS
    // ============================================================

    @Query(
        "SELECT * FROM trips " +
        "WHERE userId = :userId " +
        "AND isCompleted = 0 " +
        "LIMIT 1"
    )
    fun getActiveTripForUser(
        userId: String
    ): Flow<TripEntity?>

    @Query(
        "SELECT * FROM trips " +
        "ORDER BY startTime DESC"
    )
    fun getAllTrips(): Flow<List<TripEntity>>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertTrip(
        trip: TripEntity
    )

    @Query(
        "UPDATE trips SET " +
        "currentLatitude = :lat, " +
        "currentLongitude = :lng, " +
        "progressPercent = :progress, " +
        "etaMinutes = :eta " +
        "WHERE id = :tripId"
    )
    suspend fun updateTripProgress(
        tripId: String,
        lat: Double,
        lng: Double,
        progress: Int,
        eta: Int
    )

    @Query(
        "UPDATE trips SET " +
        "isCompleted = 1 " +
        "WHERE id = :tripId"
    )
    suspend fun completeTrip(
        tripId: String
    )

    /**
     * حذف رحلات فرد معين.
     */
    @Query(
        "DELETE FROM trips " +
        "WHERE userId = :userId"
    )
    suspend fun deleteTripsForUser(
        userId: String
    )

    // ============================================================
    // USER SETTINGS
    // ============================================================

    @Query(
        "SELECT * FROM user_settings " +
        "WHERE userId = :userId " +
        "LIMIT 1"
    )
    fun getUserSettings(
        userId: String = "current_user"
    ): Flow<UserSettingsEntity?>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertUserSettings(
        settings: UserSettingsEntity
    )
}
