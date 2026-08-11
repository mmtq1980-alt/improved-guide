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

    // ============================================================
    // FAMILY MEMBERS
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

    // ============================================================
    // PLACES
    // ============================================================

    fun getPlaces(
        familyId: String = defaultFamilyId
    ): Flow<List<PlaceEntity>> =
        dao.getPlaces(familyId)

    // ============================================================
    // EVENTS
    // ============================================================

    fun getEventLogs(
        familyId: String = defaultFamilyId
    ): Flow<List<EventLogEntity>> =
        dao.getEventLogs(familyId)

    // ============================================================
    // LOCATION HISTORY
    // ============================================================

    fun getLocationHistory(
        userId: String
    ): Flow<List<LocationHistoryEntity>> =
        dao.getLocationHistoryForUser(userId)

    // ============================================================
    // TRIPS
    // ============================================================

    fun getActiveTrip(
        userId: String = currentUserId
    ): Flow<TripEntity?> =
        dao.getActiveTripForUser(userId)

    fun getAllTrips(): Flow<List<TripEntity>> =
        dao.getAllTrips()

    // ============================================================
    // SETTINGS
    // ============================================================

    fun getUserSettings(
        userId: String = "current_user"
    ): Flow<UserSettingsEntity?> =
        dao.getUserSettings(userId)

    // ============================================================
    // ADD FAMILY MEMBER
    // ============================================================

    /**
     * إضافة فرد جديد للعائلة.
     *
     * النتيجة:
     * true  = تمت الإضافة
     * false = لم تتم الإضافة
     *
     * يمنع:
     * - تكرار رقم الهاتف
     * - تكرار البريد الإلكتروني
     * - إضافة أكثر من 3 أصدقاء مقربين
     */
    suspend fun addFamilyMember(
        name: String,
        phone: String,
        email: String,
        role: FamilyRole,
        avatarUrl: String = "",
        familyId: String = defaultFamilyId
    ): Boolean {

        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        val cleanEmail = email.trim()

        // --------------------------------------------
        // التحقق من البيانات الأساسية
        // --------------------------------------------

        if (cleanName.isBlank()) {
            return false
        }

        // --------------------------------------------
        // منع تكرار العضو
        // --------------------------------------------

        val alreadyExists = dao.memberAlreadyExists(
            familyId = familyId,
            phone = cleanPhone,
            email = cleanEmail
        )

        if (alreadyExists > 0) {
            return false
        }

        // --------------------------------------------
        // الحد الأقصى للأصدقاء المقربين = 3
        // --------------------------------------------

        if (role == FamilyRole.CLOSE_FRIEND) {

            val friendsCount =
                dao.getCloseFriendsCount(familyId)

            if (friendsCount >= 3) {
                return false
            }
        }

        // --------------------------------------------
        // إنشاء معرف جديد
        // --------------------------------------------

        val newUserId =
            "usr_${UUID.randomUUID().toString().take(8)}"

        // --------------------------------------------
        // إنشاء العضو
        // --------------------------------------------

        val newMember = UserEntity(
            id = newUserId,
            name = cleanName,
            phone = cleanPhone,
            email = cleanEmail,
            role = role,
            avatarUrl = avatarUrl,
            familyId = familyId,

            batteryLevel = 100,
            isCharging = false,

            isInsideHome = true,
            currentPlaceName = "غير محدد",

            isLocationSharingEnabled = false,

            speedKmh = 0f,
            movementDirection = "غير محدد",

            latitude = 24.7136,
            longitude = 46.6753,

            lastUpdated = System.currentTimeMillis(),

            isOnline = false
        )

        dao.insertUser(newMember)

        // --------------------------------------------
        // تسجيل العملية
        // --------------------------------------------

        dao.insertEventLog(
            EventLogEntity(
                familyId = familyId,
                userId = newUserId,
                userName = cleanName,
                eventType = "MEMBER_ADDED",
                title = "إضافة فرد جديد",
                description =
                    "تمت إضافة $cleanName إلى العائلة " +
                    "بصفة ${role.labelAr}.",
                severity = EventSeverity.INFO
            )
        )

        return true
    }

    // ============================================================
    // DELETE FAMILY MEMBER
    // ============================================================

    /**
     * حذف فرد من العائلة.
     *
     * يمنع حذف مدير العائلة.
     *
     * كما يقوم بتنظيف:
     * - سجل المواقع
     * - سجل الأحداث
     * - الرحلات
     */
    suspend fun deleteFamilyMember(
        userId: String,
        familyId: String = defaultFamilyId
    ): Boolean {

        // --------------------------------------------
        // التأكد من أن العضو موجود
        // --------------------------------------------

        val user =
            dao.getUserByIdSync(userId)
                ?: return false

        // --------------------------------------------
        // حماية مدير العائلة
        // --------------------------------------------

        val adminId =
            dao.getFamilyAdminId(familyId)

        if (userId == adminId) {
            return false
        }

        // --------------------------------------------
        // حذف بيانات العضو المرتبطة به
        // --------------------------------------------

        dao.deleteLocationHistoryForUser(userId)

        dao.deleteEventLogsForUser(userId)

        dao.deleteTripsForUser(userId)

        // --------------------------------------------
        // حذف العضو نفسه
        // --------------------------------------------

        val deletedRows =
            dao.deleteUserSafely(
                userId = userId,
                familyId = familyId
            )

        if (deletedRows <= 0) {
            return false
        }

        // --------------------------------------------
        // تسجيل عملية الحذف
        // --------------------------------------------

        dao.insertEventLog(
            EventLogEntity(
                familyId = familyId,
                userId = userId,
                userName = user.name,
                eventType = "MEMBER_REMOVED",
                title = "حذف فرد من العائلة",
                description =
                    "تم حذف ${user.name} من أفراد العائلة.",
                severity = EventSeverity.WARNING
            )
        )

        return true
    }

    // ============================================================
    // BASIC DATABASE OPERATIONS
    // ============================================================

    suspend fun insertUser(
        user: UserEntity
    ) =
        dao.insertUser(user)

    suspend fun insertFamily(
        family: FamilyEntity
    ) =
        dao.insertFamily(family)

    // ============================================================
    // PLACES
    // ============================================================

    suspend fun insertPlace(
        place: PlaceEntity
    ) =
        dao.insertPlace(place)

    suspend fun deletePlace(
        place: PlaceEntity
    ) =
        dao.deletePlace(place)

    // ============================================================
    // EVENTS
    // ============================================================

    suspend fun insertEventLog(
        log: EventLogEntity
    ) =
        dao.insertEventLog(log)

    suspend fun clearEventLogs(
        familyId: String = defaultFamilyId
    ) =
        dao.clearEventLogs(familyId)

    // ============================================================
    // TRIPS
    // ============================================================

    suspend fun insertTrip(
        trip: TripEntity
    ) =
        dao.insertTrip(trip)

    suspend fun updateTripProgress(
        tripId: String,
        lat: Double,
        lng: Double,
        progress: Int,
        eta: Int
    ) =
        dao.updateTripProgress(
            tripId = tripId,
            lat = lat,
            lng = lng,
            progress = progress,
            eta = eta
        )

    suspend fun completeTrip(
        tripId: String
    ) =
        dao.completeTrip(tripId)

    // ============================================================
    // USER SETTINGS
    // ============================================================

    suspend fun updateUserSettings(
        settings: UserSettingsEntity
    ) =
        dao.insertUserSettings(settings)

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

        // تحديث آخر موقع
        dao.updateUserLocation(
            userId = userId,
            lat = lat,
            lng = lng,
            speed = speed,
            battery = battery,
            timestamp = timestamp
        )

        // جلب بيانات العضو
        val user =
            dao.getUserByIdSync(userId)

        // إضافة نقطة إلى سجل الحركة
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

    // ============================================================
    // LOCATION SHARING
    // ============================================================

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
    // SEED DATABASE
    // ============================================================

    /**
     * ملاحظة مهمة:
     *
     * هذه الدالة تحتفظ بالبيانات التجريبية الحالية
     * حتى لا نكسر التطبيق الحالي.
     *
     * لاحقاً يمكن إزالة بيانات Demo بالكامل بعد
     * إضافة شاشة إنشاء العائلة الأولى.
     */
    suspend fun seedDatabaseIfEmpty() {

        val existingMembers =
            dao.getFamilyMembers(defaultFamilyId)

        /*
         * لا يمكن قراءة Flow مباشرة هنا بطريقة بسيطة،
         * لذلك نعتمد على وجود المستخدم الحالي.
         */
        val currentUser =
            dao.getUserByIdSync(currentUserId)

        if (currentUser != null) {
            return
        }

        // ========================================================
        // DEFAULT FAMILY
        // ========================================================

        val baseLat = 24.7136
        val baseLng = 46.6753

        val family =
            FamilyEntity(
                id = defaultFamilyId,
                name = "عائلتي",
                inviteCode =
                    "FG-${UUID.randomUUID().toString().take(6).uppercase()}",
                qrCodeData =
                    "FAMILY_GUARDIAN_JOIN_$defaultFamilyId",
                adminUserId = currentUserId
            )

        dao.insertFamily(family)

        // ========================================================
        // DEFAULT ADMIN
        // ========================================================

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

                latitude = baseLat,
                longitude = baseLng,

                isOnline = true
            )

        dao.insertUser(admin)

        // ========================================================
        // DEFAULT SETTINGS
        // ========================================================

        dao.insertUserSettings(
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
        )
    }
}
