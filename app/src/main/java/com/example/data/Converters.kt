package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFamilyRole(value: FamilyRole): String = value.name

    @TypeConverter
    fun toFamilyRole(value: String): FamilyRole = runCatching { FamilyRole.valueOf(value) }.getOrDefault(FamilyRole.GUEST)

    @TypeConverter
    fun fromPlaceType(value: PlaceType): String = value.name

    @TypeConverter
    fun toPlaceType(value: String): PlaceType = runCatching { PlaceType.valueOf(value) }.getOrDefault(PlaceType.CUSTOM)

    @TypeConverter
    fun fromEventSeverity(value: EventSeverity): String = value.name

    @TypeConverter
    fun toEventSeverity(value: String): EventSeverity = runCatching { EventSeverity.valueOf(value) }.getOrDefault(EventSeverity.INFO)
}
