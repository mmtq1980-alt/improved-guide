package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FamilyGuardianRepository(private val dao: FamilyGuardianDao) {

    val defaultFamilyId = "fam_1001"
    val currentUserId = "usr_001"

    fun getFamilyMembers(familyId: String = defaultFamilyId): Flow<List<UserEntity>> = dao.getFamilyMembers(familyId)
    fun getUser(userId: String = currentUserId): Flow<UserEntity?> = dao.getUserById(userId)
    fun getFamily(familyId: String = defaultFamilyId): Flow<FamilyEntity?> = dao.getFamily(familyId)
    fun getPlaces(familyId: String = defaultFamilyId): Flow<List<PlaceEntity>> = dao.getPlaces(familyId)
    fun getEventLogs(familyId: String = defaultFamilyId): Flow<List<EventLogEntity>> = dao.getEventLogs(familyId)
    fun getLocationHistory(userId: String): Flow<List<LocationHistoryEntity>> = dao.getLocationHistoryForUser(userId)
    fun getActiveTrip(userId: String = currentUserId): Flow<TripEntity?> = dao.getActiveTripForUser(userId)
    fun getAllTrips(): Flow<List<TripEntity>> = dao.getAllTrips()
    fun getUserSettings(userId: String = "current_user"): Flow<UserSettingsEntity?> = dao.getUserSettings(userId)

    suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
    suspend fun insertFamily(family: FamilyEntity) = dao.insertFamily(family)
    suspend fun insertPlace(place: PlaceEntity) = dao.insertPlace(place)
    suspend fun deletePlace(place: PlaceEntity) = dao.deletePlace(place)
    suspend fun insertEventLog(log: EventLogEntity) = dao.insertEventLog(log)
    suspend fun insertTrip(trip: TripEntity) = dao.insertTrip(trip)
    suspend fun updateTripProgress(tripId: String, lat: Double, lng: Double, progress: Int, eta: Int) =
        dao.updateTripProgress(tripId, lat, lng, progress, eta)
    suspend fun completeTrip(tripId: String) = dao.completeTrip(tripId)
    suspend fun updateUserSettings(settings: UserSettingsEntity) = dao.insertUserSettings(settings)

    suspend fun updateMemberLocation(userId: String, lat: Double, lng: Double, speed: Float, battery: Int, placeName: String) {
        val timestamp = System.currentTimeMillis()
        dao.updateUserLocation(userId, lat, lng, speed, battery, timestamp)
        val user = dao.getUserByIdSync(userId)
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

    suspend fun toggleLocationSharing(userId: String, enabled: Boolean) {
        dao.updateLocationSharing(userId, enabled)
    }

    suspend fun sendSosAlert(userId: String, userName: String, lat: Double, lng: Double, battery: Int) {
        val mapsUrl = "https://maps.google.com/?q=$lat,$lng"
        val log = EventLogEntity(
            familyId = defaultFamilyId,
            userId = userId,
            userName = userName,
            eventType = "SOS_ALERT",
            title = "🚨 طوارئ SOS من $userName!",
            description = "تم إرسال نداء استغاثة طوارئ. الموقع: $lat, $lng | البطارية: $battery% | الرابط: $mapsUrl",
            severity = EventSeverity.EMERGENCY
        )
        dao.insertEventLog(log)
    }

    suspend fun seedDatabaseIfEmpty() {
        // Seed Family
        val family = FamilyEntity(
            id = defaultFamilyId,
            name = "عائلة الأحمد",
            inviteCode = "FG-8942",
            qrCodeData = "FAMILY_GUARDIAN_JOIN_FG-8942",
            adminUserId = currentUserId
        )
        dao.insertFamily(family)

        // Seed Users
        val baseLat = 24.7136
        val baseLng = 46.6753
        val users = listOf(
            UserEntity(
                id = currentUserId,
                name = "أحمد السعيد",
                phone = "+966501234567",
                email = "ahmed@familyguardian.com",
                role = FamilyRole.FATHER,
                familyId = defaultFamilyId,
                batteryLevel = 92,
                isCharging = false,
                isInsideHome = true,
                currentPlaceName = "المنزل الرئيسي",
                isLocationSharingEnabled = true,
                speedKmh = 0f,
                latitude = baseLat,
                longitude = baseLng,
                isOnline = true
            ),
            UserEntity(
                id = "usr_002",
                name = "منيرة الخالد",
                phone = "+966507654321",
                email = "mona@familyguardian.com",
                role = FamilyRole.MOTHER,
                familyId = defaultFamilyId,
                batteryLevel = 78,
                isCharging = true,
                isInsideHome = true,
                currentPlaceName = "المنزل الرئيسي",
                isLocationSharingEnabled = true,
                speedKmh = 0f,
                latitude = baseLat + 0.0005,
                longitude = baseLng - 0.0003,
                isOnline = true
            ),
            UserEntity(
                id = "usr_003",
                name = "طارق الأحمد",
                phone = "+966509876543",
                email = "tariq@familyguardian.com",
                role = FamilyRole.SON,
                familyId = defaultFamilyId,
                batteryLevel = 45,
                isCharging = false,
                isInsideHome = false,
                currentPlaceName = "جامعة الملك سعود",
                isLocationSharingEnabled = true,
                speedKmh = 42f,
                movementDirection = "شمال شرق",
                latitude = baseLat + 0.015,
                longitude = baseLng + 0.012,
                isOnline = true
            ),
            UserEntity(
                id = "usr_004",
                name = "سارة الأحمد",
                phone = "+966503456789",
                email = "sara@familyguardian.com",
                role = FamilyRole.DAUGHTER,
                familyId = defaultFamilyId,
                batteryLevel = 18,
                isCharging = false,
                isInsideHome = false,
                currentPlaceName = "مدرسة النموذجية",
                isLocationSharingEnabled = true,
                speedKmh = 0f,
                latitude = baseLat - 0.008,
                longitude = baseLng + 0.005,
                isOnline = true
            )
        )
        dao.insertUsers(users)

        // Seed Places
        val places = listOf(
            PlaceEntity(
                id = "place_1",
                familyId = defaultFamilyId,
                name = "المنزل الرئيسي",
                placeType = PlaceType.HOME,
                latitude = baseLat,
                longitude = baseLng,
                radiusMeters = 200,
                enterNotify = true,
                exitNotify = true
            ),
            PlaceEntity(
                id = "place_2",
                familyId = defaultFamilyId,
                name = "مدرسة النموذجية",
                placeType = PlaceType.SCHOOL,
                latitude = baseLat - 0.008,
                longitude = baseLng + 0.005,
                radiusMeters = 150,
                enterNotify = true,
                exitNotify = true
            ),
            PlaceEntity(
                id = "place_3",
                familyId = defaultFamilyId,
                name = "جامعة الملك سعود",
                placeType = PlaceType.UNIVERSITY,
                latitude = baseLat + 0.015,
                longitude = baseLng + 0.012,
                radiusMeters = 300,
                enterNotify = true,
                exitNotify = true
            ),
            PlaceEntity(
                id = "place_4",
                familyId = defaultFamilyId,
                name = "مقر العمل",
                placeType = PlaceType.WORK,
                latitude = baseLat + 0.022,
                longitude = baseLng - 0.018,
                radiusMeters = 250,
                enterNotify = true,
                exitNotify = true
            )
        )
        places.forEach { dao.insertPlace(it) }

        // Seed History Points
        val now = System.currentTimeMillis()
        val historyPoints = mutableListOf<LocationHistoryEntity>()
        for (i in 0..12) {
            val timeOffset = now - (12 - i) * 15 * 60 * 1000L
            val stepLat = baseLat + (i * 0.0012)
            val stepLng = baseLng + (i * 0.0009)
            historyPoints.add(
                LocationHistoryEntity(
                    userId = "usr_003",
                    userName = "طارق الأحمد",
                    latitude = stepLat,
                    longitude = stepLng,
                    speedKmh = if (i % 2 == 0) 35f else 0f,
                    batteryLevel = 60 - (i * 2),
                    placeName = if (i == 12) "جامعة الملك سعود" else "طريق الملك فهد",
                    timestamp = timeOffset
                )
            )
        }
        dao.insertLocationPoints(historyPoints)

        // Seed Event Logs
        val logs = listOf(
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = "usr_003",
                userName = "طارق الأحمد",
                eventType = "GEOFENCE_EXIT",
                title = "مغادرة منطقة",
                description = "غادر طارق المنزل الرئيسي متجهاً إلى الجامعة.",
                severity = EventSeverity.INFO,
                timestamp = now - 45 * 60 * 1000L
            ),
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = "usr_004",
                userName = "سارة الأحمد",
                eventType = "GEOFENCE_ENTER",
                title = "وصول آمن",
                description = "وصلت سارة إلى مدرسة النموذجية بنجاح.",
                severity = EventSeverity.INFO,
                timestamp = now - 30 * 60 * 1000L
            ),
            EventLogEntity(
                familyId = defaultFamilyId,
                userId = "usr_004",
                userName = "سارة الأحمد",
                eventType = "LOW_BATTERY",
                title = "تنبيه انخفاض البطارية ⚠️",
                description = "بطارية هاتف سارة أصبحت 18%. يرجى التذكير بالشحن.",
                severity = EventSeverity.WARNING,
                timestamp = now - 10 * 60 * 1000L
            )
        )
        logs.forEach { dao.insertEventLog(it) }

        // Seed Active Trip
        val trip = TripEntity(
            id = "trip_501",
            userId = "usr_003",
            userName = "طارق الأحمد",
            startLocationName = "المنزل الرئيسي",
            destinationName = "جامعة الملك سعود",
            startLatitude = baseLat,
            startLongitude = baseLng,
            destLatitude = baseLat + 0.015,
            destLongitude = baseLng + 0.012,
            currentLatitude = baseLat + 0.011,
            currentLongitude = baseLng + 0.009,
            etaMinutes = 8,
            progressPercent = 75,
            isCompleted = false,
            startTime = now - 20 * 60 * 1000L
        )
        dao.insertTrip(trip)

        // Seed Default Settings
        val settings = UserSettingsEntity(
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
    }
}
