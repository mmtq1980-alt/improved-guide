package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============================================================
// FAMILY ROLES
// ============================================================

enum class FamilyRole(
    val labelAr: String,
    val labelEn: String
) {
    FATHER("الأب", "Father"),
    MOTHER("الأم", "Mother"),

    GRANDFATHER("الجد", "Grandfather"),
    GRANDMOTHER("الجدة", "Grandmother"),

    BROTHER("الأخ", "Brother"),
    SISTER("الأخت", "Sister"),

    SON("الابن", "Son"),
    DAUGHTER("البنت", "Daughter"),

    GRANDSON("الحفيد", "Grandson"),
    GRANDDAUGHTER("الحفيدة", "Granddaughter"),

    COUSIN_PATERNAL("ابن العم", "Paternal Cousin"),
    COUSIN_MATERNAL("ابن الخال", "Maternal Cousin"),

    CLOSE_FRIEND("صديق مقرب", "Close Friend"),

    GUARDIAN("ولي الأمر", "Guardian"),
    GUEST("ضيف", "Guest")
}

// ============================================================
// PLACE TYPES
// ============================================================

enum class PlaceType(
    val labelAr: String,
    val labelEn: String,
    val iconName: String
) {
    HOME(
        "المنزل",
        "Home",
        "home"
    ),

    SCHOOL(
        "المدرسة",
        "School",
        "school"
    ),

    UNIVERSITY(
        "الجامعة",
        "University",
        "university"
    ),

    WORK(
        "العمل",
        "Work",
        "work"
    ),

    CLUB(
        "النادي",
        "Club",
        "fitness"
    ),

    HOSPITAL(
        "المستشفى",
        "Hospital",
        "hospital"
    ),

    CUSTOM(
        "مكان مخصص",
        "Custom Place",
        "place"
    )
}

// ============================================================
// EVENT SEVERITY
// ============================================================

enum class EventSeverity {
    INFO,
    WARNING,
    ALERT,
    EMERGENCY
}

// ============================================================
// USERS
// ============================================================

@Entity(tableName = "users")
data class UserEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val phone: String,

    val email: String,

    val role: FamilyRole,

    val avatarUrl: String = "",

    val familyId: String,

    val batteryLevel: Int = 100,

    val isCharging: Boolean = false,

    val isInsideHome: Boolean = true,

    val currentPlaceName: String = "المنزل",

    val isLocationSharingEnabled: Boolean = true,

    val speedKmh: Float = 0f,

    val movementDirection: String = "شمال",

    val latitude: Double = 24.7136,

    val longitude: Double = 46.6753,

    val lastUpdated: Long = System.currentTimeMillis(),

    val isOnline: Boolean = true
)

// ============================================================
// FAMILY
// ============================================================

@Entity(tableName = "family")
data class FamilyEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val inviteCode: String,

    val qrCodeData: String,

    val adminUserId: String,

    val createdAt: Long = System.currentTimeMillis()
)

// ============================================================
// PLACES / GEOFENCES
// ============================================================

@Entity(tableName = "places")
data class PlaceEntity(

    @PrimaryKey
    val id: String,

    val familyId: String,

    val name: String,

    val placeType: PlaceType,

    val latitude: Double,

    val longitude: Double,

    val radiusMeters: Int = 150,

    val enterNotify: Boolean = true,

    val exitNotify: Boolean = true,

    val createdAt: Long = System.currentTimeMillis()
)

// ============================================================
// LOCATION HISTORY
// ============================================================

@Entity(tableName = "location_history")
data class LocationHistoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,

    val userName: String,

    val latitude: Double,

    val longitude: Double,

    val speedKmh: Float,

    val batteryLevel: Int,

    val placeName: String = "",

    val timestamp: Long = System.currentTimeMillis()
)

// ============================================================
// EVENT LOGS
// ============================================================

@Entity(tableName = "event_logs")
data class EventLogEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val familyId: String,

    val userId: String,

    val userName: String,

    val eventType: String,

    val title: String,

    val description: String,

    val severity: EventSeverity = EventSeverity.INFO,

    val timestamp: Long = System.currentTimeMillis()
)

// ============================================================
// TRIPS
// ============================================================

@Entity(tableName = "trips")
data class TripEntity(

    @PrimaryKey
    val id: String,

    val userId: String,

    val userName: String,

    val startLocationName: String,

    val destinationName: String,

    val startLatitude: Double,

    val startLongitude: Double,

    val destLatitude: Double,

    val destLongitude: Double,

    val currentLatitude: Double,

    val currentLongitude: Double,

    val etaMinutes: Int,

    val progressPercent: Int,

    val isCompleted: Boolean = false,

    val startTime: Long = System.currentTimeMillis()
)

// ============================================================
// USER SETTINGS
// ============================================================

@Entity(tableName = "user_settings")
data class UserSettingsEntity(

    @PrimaryKey
    val userId: String = "current_user",

    val isDarkMode: Boolean = true,

    val language: String = "ar",

    val updateIntervalSeconds: Int = 5,

    val showTraffic: Boolean = true,

    val batterySaverEnabled: Boolean = false,

    val sosAlertsEnabled: Boolean = true,

    val autoBackupEnabled: Boolean = true
)
