package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FamilyGuardianRepository(
    private val dao: FamilyGuardianDao
) {

    // ============================================================
    // FAMILY / CURRENT USER
    // ============================================================

    val defaultFamilyId = "fam_1001"

    val currentUserId = "usr_001"

    private val defaultFamilyName = "عائلتي"

    // الحد الأقصى للأصدقاء المقربين
    private val maxCloseFriends = 3

    // ============================================================
    // OBSERVABLE DATA
    // ============================================================

    fun getFamilyMembers(
        familyId: String = defaultFamilyId
    ): Flow<List<UserEntity>> =
        dao.getFamilyMembers(familyId)

    fun getUser(
        userId: String = currentUserId
    ): Flow<UserEntity?> =
        dao.getUserById(userId)

    fun getFamily(
        familyId: String = defaultFamilyId
    ): Flow<FamilyEntity?> =
        dao.getFamily(familyId)

    fun getPlaces(
        familyId: String = defaultFamilyId
    ): Flow<List<PlaceEntity>> =
        dao.getPlaces(familyId)

    fun getEventLogs(
        familyId: String = defaultFamilyId
    ): Flow<List<EventLogEntity>> =
        dao.getEventLogs(familyId)

    fun getLocationHistory(
        userId: String
    ): Flow<List<LocationHistoryEntity>> =
        dao.getLocationHistoryForUser(userId)

    fun getActiveTrip(
        userId: String = currentUserId
    ): Flow<TripEntity?> =
        dao.getActiveTripForUser(userId)

    fun getAllTrips(): Flow<List<TripEntity>> =
        dao.getAllTrips()

    fun getUserSettings(
        userId: String = "current_user"
    ): Flow<UserSettingsEntity?> =
        dao.getUserSettings(userId)

    // ============================================================
    // FAMILY
    // ============================================================

    suspend fun insertFamily(
        family: FamilyEntity
    ) {
        dao.insertFamily(family)
    }

    suspend fun getFamilySync(
        familyId: String = defaultFamilyId
    ): FamilyEntity? =
        dao.getFamilySync(familyId)

    // ============================================================
    // ADD FAMILY MEMBER
    // ============================================================

    /**
     * إضافة فرد جديد للعائلة.
     *
     * يمكن استخدام هذه الدالة للأب، الأم، الجد، الجدة،
     * الأخ، الأخت، الابن، البنت، الحفيد، الحفيدة،
     * ابن العم، ابن الخال، الأصدقاء وغيرهم.
     */
    suspend fun addFamilyMember(
        name: String,
        phone: String = "",
        email: String = "",
        role: FamilyRole,
        avatarUrl: String = "",
        locationSharingEnabled: Boolean = true
    ): Result<UserEntity> {

        val cleanName = name.trim()

        if (cleanName.isEmpty()) {
            return Result.failure(
                IllegalArgumentException("اسم فرد العائلة مطلوب")
            )
        }

        // --------------------------------------------------------
        // التحقق من وجود العائلة
        // --------------------------------------------------------

        val family = dao.getFamilySync(defaultFamilyId)

        if (family == null) {
            return Result.failure(
                IllegalStateException("العائلة غير موجودة")
            )
        }

        // --------------------------------------------------------
        // منع تجاوز عدد الأصدقاء المقربين
        // --------------------------------------------------------

        if (role == FamilyRole.CLOSE_FRIEND) {

            val currentMembers =
                dao.getFamilyMembers(defaultFamilyId)

            /*
             * لأن getFamilyMembers يعيد Flow،
             * نستخدم first() للحصول على الحالة الحالية.
             */
            val members = currentMembers.first()

            val closeFriendsCount =
                members.count {
                    it.role == FamilyRole.CLOSE_FRIEND
                }

            if (closeFriendsCount >= maxCloseFriends) {
                return Result.failure(
                    IllegalStateException(
                        "لا يمكن إضافة أكثر من 3 أصدقاء مقربين"
                    )
                )
            }
        }

        // --------------------------------------------------------
        // إنشاء معرف فريد
        // --------------------------------------------------------

        val userId =
            "usr_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        // --------------------------------------------------------
        // إنشاء العضو
        // --------------------------------------------------------

        val user = UserEntity(
            id = userId,
            name = cleanName,
            phone = phone.trim(),
            email = email.trim(),
            role = role,
            avatarUrl = avatarUrl,
            familyId = defaultFamilyId,
            batteryLevel = 100,
            isCharging = false,
            isInsideHome = true,
            currentPlaceName = "المنزل",
            isLocationSharingEnabled = locationSharingEnabled,
            speedKmh = 0f,
            movementDirection = "شمال",
            latitude = 24.7136,
            longitude = 46.6753,
            isOnline = false
        )

        dao.insertUser(user)

        // --------------------------------------------------------
        // تسجيل العملية
        // --------------------------------------------------------

        dao.insertEventLog(
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = currentUserId,
                userName = "مدير العائلة",
                eventType = "FAMILY_MEMBER_ADDED",
                title = "إضافة فرد للعائلة",
                description =
                    "تمت إضافة $cleanName (${role.labelAr}) إلى العائلة.",
                severity = EventSeverity.INFO
            )
        )

        return Result.success(user)
    }

    // ============================================================
    // UPDATE FAMILY MEMBER
    // ============================================================

    /**
     * تحديث بيانات أحد أفراد العائلة.
     */
    suspend fun updateFamilyMember(
        user: UserEntity
    ): Result<Unit> {

        val existing =
            dao.getUserByIdSync(user.id)

        if (existing == null) {
            return Result.failure(
                IllegalArgumentException(
                    "فرد العائلة غير موجود"
                )
            )
        }

        // لا نسمح بتغيير العائلة المرتبط بها العضو
        if (existing.familyId != defaultFamilyId) {
            return Result.failure(
                IllegalStateException(
                    "لا يمكن تعديل عضو خارج العائلة الحالية"
                )
            )
        }

        // مدير العائلة لا يتم تغيير دوره بهذه الطريقة
        val family =
            dao.getFamilySync(defaultFamilyId)

        if (
            family != null &&
            family.adminUserId == user.id &&
            user.role != FamilyRole.FATHER &&
            user.role != FamilyRole.GUARDIAN
        ) {
            return Result.failure(
                IllegalStateException(
                    "لا يمكن تغيير دور مدير العائلة بهذه الطريقة"
                )
            )
        }

        dao.updateUser(user)

        dao.insertEventLog(
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = currentUserId,
                userName = "مدير العائلة",
                eventType = "FAMILY_MEMBER_UPDATED",
                title = "تعديل بيانات فرد العائلة",
                description =
                    "تم تعديل بيانات ${user.name}.",
                severity = EventSeverity.INFO
            )
        )

        return Result.success(Unit)
    }

    // ============================================================
    // DELETE FAMILY MEMBER
    // ============================================================

    /**
     * حذف فرد من العائلة.
     *
     * لا يمكن حذف مدير العائلة.
     */
    suspend fun deleteFamilyMember(
        userId: String
    ): Result<Unit> {

        if (userId == currentUserId) {
            return Result.failure(
                IllegalStateException(
                    "لا يمكنك حذف حساب مدير العائلة من داخل العائلة"
                )
            )
        }

        val family =
            dao.getFamilySync(defaultFamilyId)

        if (family == null) {
            return Result.failure(
                IllegalStateException(
                    "العائلة غير موجودة"
                )
            )
        }

        // --------------------------------------------------------
        // حماية مدير العائلة
        // --------------------------------------------------------

        if (family.adminUserId == userId) {
            return Result.failure(
                IllegalStateException(
                    "لا يمكن حذف مدير العائلة"
                )
            )
        }

        // --------------------------------------------------------
        // البحث عن العضو
        // --------------------------------------------------------

        val user =
            dao.getUserByIdSync(userId)

        if (user == null) {
            return Result.failure(
                IllegalArgumentException(
                    "فرد العائلة غير موجود"
                )
            )
        }

        if (user.familyId != defaultFamilyId) {
            return Result.failure(
                IllegalStateException(
                    "هذا العضو لا ينتمي إلى العائلة الحالية"
                )
            )
        }

        // --------------------------------------------------------
        // حذف العضو وبياناته المرتبطة
        // --------------------------------------------------------

        dao.deleteUserCompletely(userId)

        // --------------------------------------------------------
        // تسجيل عملية الحذف
        // --------------------------------------------------------

        dao.insertEventLog(
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = currentUserId,
                userName = "مدير العائلة",
                eventType = "FAMILY_MEMBER_DELETED",
                title = "حذف فرد من العائلة",
                description =
                    "تم حذف ${user.name} (${user.role.labelAr}) من العائلة.",
                severity = EventSeverity.WARNING
            )
        )

        return Result.success(Unit)
    }

    // ============================================================
    // FAMILY MEMBERS COUNT
    // ============================================================

    suspend fun getFamilyMembersCount(): Int =
        dao.getFamilyMembersCount(defaultFamilyId)

    // ============================================================
    // CLOSE FRIENDS
    // ============================================================

    /**
     * التحقق من إمكانية إضافة صديق مقرب.
     */
    suspend fun canAddCloseFriend(): Boolean {

        val members =
            dao.getFamilyMembers(defaultFamilyId).first()

        val count =
            members.count {
                it.role == FamilyRole.CLOSE_FRIEND
            }

        return count < maxCloseFriends
    }

    suspend fun getCloseFriendsCount(): Int {

        val members =
            dao.getFamilyMembers(defaultFamilyId).first()

        return members.count {
            it.role == FamilyRole.CLOSE_FRIEND
        }
    }

    // ============================================================
    // LOCATION
    // ============================================================

    suspend fun updateMemberLocation(
        userId: String,
        lat: Double,
        lng: Double,
        speed: Float,
        battery: Int,
        placeName: String
    ) {

        val timestamp =
            System.currentTimeMillis()

        dao.updateUserLocation(
            userId = userId,
            lat = lat,
            lng = lng,
            speed = speed,
            battery = battery,
            timestamp = timestamp
        )

        val user =
            dao.getUserByIdSync(userId)

        dao.insertLocationPoint(
            LocationHistoryEntity(
                userId = userId,
                userName = user?.name ?: "فرد العائلة",
                latitude = lat,
                longitude = lng,
                speedKmh = speed,
                batteryLevel = battery,
                placeName = placeName,
                timestamp = timestamp
            )
        )
    }

    suspend fun toggleLocationSharing(
        userId: String,
        enabled: Boolean
    ) {

        dao.updateLocationSharing(
            userId = userId,
            enabled = enabled
        )
    }

    // ============================================================
    // SOS
    // ============================================================

    suspend fun sendSosAlert(
        userId: String,
        userName: String,
        lat: Double,
        lng: Double,
        battery: Int
    ) {

        val mapsUrl =
            "https://maps.google.com/?q=$lat,$lng"

        val log =
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = userId,
                userName = userName,
                eventType = "SOS_ALERT",
                title = "🚨 طوارئ SOS من $userName!",
                description =
                    "تم إرسال نداء استغاثة طوارئ. " +
                    "الموقع: $lat, $lng | " +
                    "البطارية: $battery% | " +
                    "الرابط: $mapsUrl",
                severity = EventSeverity.EMERGENCY
            )

        dao.insertEventLog(log)
    }

    // ============================================================
    // PLACES
    // ============================================================

    suspend fun insertPlace(
        place: PlaceEntity
    ) {
        dao.insertPlace(place)
    }

    suspend fun deletePlace(
        place: PlaceEntity
    ) {
        dao.deletePlace(place)
    }

    // ============================================================
    // EVENT LOGS
    // ============================================================

    suspend fun insertEventLog(
        log: EventLogEntity
    ) {
        dao.insertEventLog(log)
    }

    suspend fun clearEventLogs(
        familyId: String = defaultFamilyId
    ) {
        dao.clearEventLogs(familyId)
    }

    // ============================================================
    // TRIPS
    // ============================================================

    suspend fun insertTrip(
        trip: TripEntity
    ) {
        dao.insertTrip(trip)
    }

    suspend fun updateTripProgress(
        tripId: String,
        lat: Double,
        lng: Double,
        progress: Int,
        eta: Int
    ) {

        dao.updateTripProgress(
            tripId = tripId,
            lat = lat,
            lng = lng,
            progress = progress,
            eta = eta
        )
    }

    suspend fun completeTrip(
        tripId: String
    ) {
        dao.completeTrip(tripId)
    }

    // ============================================================
    // USER SETTINGS
    // ============================================================

    suspend fun updateUserSettings(
        settings: UserSettingsEntity
    ) {
        dao.insertUserSettings(settings)
    }

    // ============================================================
    // DATABASE SEED
    // ============================================================

    /**
     * إنشاء البيانات الأولية للتطبيق عند أول تشغيل فقط.
     *
     * مهم:
     * إذا كانت العائلة موجودة فلن نقوم بإعادة إنشاء
     * الأشخاص الافتراضيين.
     */
    suspend fun seedDatabaseIfEmpty() {

        val existingFamily =
            dao.getFamilySync(defaultFamilyId)

        if (existingFamily != null) {
            return
        }

        // --------------------------------------------------------
        // إنشاء العائلة
        // --------------------------------------------------------

        val family =
            FamilyEntity(
                id = defaultFamilyId,
                name = defaultFamilyName,
                inviteCode =
                    "FG-" +
                    UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .take(6)
                        .uppercase(),
                qrCodeData =
                    "FAMILY_GUARDIAN_JOIN_" +
                    UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .take(8),
                adminUserId = currentUserId
            )

        dao.insertFamily(family)

        // --------------------------------------------------------
        // إنشاء مدير العائلة فقط
        // --------------------------------------------------------

        val admin =
            UserEntity(
                id = currentUserId,
                name = "مدير العائلة",
                phone = "",
                email = "",
                role = FamilyRole.FATHER,
                familyId = defaultFamilyId,
                batteryLevel = 100,
                isCharging = false,
                isInsideHome = true,
                currentPlaceName = "المنزل",
                isLocationSharingEnabled = true,
                speedKmh = 0f,
                movementDirection = "شمال",
                latitude = 24.7136,
                longitude = 46.6753,
                isOnline = true
            )

        dao.insertUser(admin)

        // --------------------------------------------------------
        // الإعدادات الافتراضية
        // --------------------------------------------------------

        val settings =
            UserSettingsEntity(
                userId = "current_user",
                isDarkMode = true,
                language = "ar",
                updateIntervalSeconds = 5,
                showTraffic = true,
                batterySaverEnabled = false,
                sosAlertsEnabled = true,
                autoBackupEnabled = true
            )

        dao.insertUserSettings(settings)

        // --------------------------------------------------------
        // تسجيل إنشاء العائلة
        // --------------------------------------------------------

        dao.insertEventLog(
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = currentUserId,
                userName = admin.name,
                eventType = "FAMILY_CREATED",
                title = "إنشاء العائلة",
                description =
                    "تم إنشاء عائلة جديدة في Family Guardian.",
                severity = EventSeverity.INFO
            )
        )
    }
}
