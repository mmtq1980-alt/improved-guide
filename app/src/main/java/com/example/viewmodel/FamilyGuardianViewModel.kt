package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyGuardianViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FamilyGuardianDatabase.getDatabase(application)
    private val repository = FamilyGuardianRepository(db.dao())

    // UI state
    val familyMembers: StateFlow<List<UserEntity>> = repository.getFamilyMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFamily: StateFlow<FamilyEntity?> = repository.getFamily()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val places: StateFlow<List<PlaceEntity>> = repository.getPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventLogs: StateFlow<List<EventLogEntity>> = repository.getEventLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTrip: StateFlow<TripEntity?> = repository.getActiveTrip()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTrips: StateFlow<List<TripEntity>> = repository.getAllTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity?> = repository.getUserSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedMemberId = MutableStateFlow<String?>("usr_003")
    val selectedMemberId: StateFlow<String?> = _selectedMemberId.asStateFlow()

    val selectedMemberHistory: StateFlow<List<LocationHistoryEntity>> = _selectedMemberId.flatMapLatest { id ->
        if (id != null) repository.getLocationHistory(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Map, 2: Places, 3: History, 4: Reports/Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _showSosDialog = MutableStateFlow(false)
    val showSosDialog: StateFlow<Boolean> = _showSosDialog.asStateFlow()

    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog: StateFlow<Boolean> = _showQrDialog.asStateFlow()

    private val _showAddPlaceDialog = MutableStateFlow(false)
    val showAddPlaceDialog: StateFlow<Boolean> = _showAddPlaceDialog.asStateFlow()

    private val _showStartTripDialog = MutableStateFlow(false)
    val showStartTripDialog: StateFlow<Boolean> = _showStartTripDialog.asStateFlow()

    private val _selectedHistoryFilter = MutableStateFlow("DAY") // "DAY", "WEEK", "MONTH"
    val selectedHistoryFilter: StateFlow<String> = _selectedHistoryFilter.asStateFlow()

    private val _isLiveSimulationRunning = MutableStateFlow(true)
    val isLiveSimulationRunning: StateFlow<Boolean> = _isLiveSimulationRunning.asStateFlow()

    private var simulationJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            startLiveLocationSimulation()
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectMember(memberId: String) {
        _selectedMemberId.value = memberId
    }

    fun setHistoryFilter(filter: String) {
        _selectedHistoryFilter.value = filter
    }

    fun toggleSosDialog(show: Boolean) {
        _showSosDialog.value = show
    }

    fun toggleQrDialog(show: Boolean) {
        _showQrDialog.value = show
    }

    fun toggleAddPlaceDialog(show: Boolean) {
        _showAddPlaceDialog.value = show
    }

    fun toggleStartTripDialog(show: Boolean) {
        _showStartTripDialog.value = show
    }

    fun triggerSosAlert() {
        viewModelScope.launch {
            _isSosActive.value = true
            _showSosDialog.value = true
            val user = currentUser.value ?: return@launch
            repository.sendSosAlert(
                userId = user.id,
                userName = user.name,
                lat = user.latitude,
                lng = user.longitude,
                battery = user.batteryLevel
            )
        }
    }

    fun cancelSosAlert() {
        _isSosActive.value = false
        _showSosDialog.value = false
    }

    fun toggleLocationSharing(enabled: Boolean) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.toggleLocationSharing(user.id, enabled)
            repository.insertEventLog(
                EventLogEntity(
                    familyId = repository.defaultFamilyId,
                    userId = user.id,
                    userName = user.name,
                    eventType = "PRIVACY_UPDATE",
                    title = if (enabled) "تفعيل مشاركة الموقع" else "إيقاف مشاركة الموقع",
                    description = if (enabled) "قام $user.name بتفعيل مشاركة الموقع للجماعة." else "قام $user.name بإيقاف مشاركة الموقع مؤقتاً.",
                    severity = EventSeverity.WARNING
                )
            )
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(isDarkMode = enabled))
        }
    }

    fun toggleLanguage(lang: String) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(language = lang))
        }
    }

    fun updateUpdateInterval(seconds: Int) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(updateIntervalSeconds = seconds))
        }
    }

    fun addPlace(name: String, type: PlaceType, lat: Double, lng: Double, radius: Int) {
        viewModelScope.launch {
            val place = PlaceEntity(
                id = "place_${UUID.randomUUID().toString().take(6)}",
                familyId = repository.defaultFamilyId,
                name = name,
                placeType = type,
                latitude = lat,
                longitude = lng,
                radiusMeters = radius
            )
            repository.insertPlace(place)
            repository.insertEventLog(
                EventLogEntity(
                    familyId = repository.defaultFamilyId,
                    userId = repository.currentUserId,
                    userName = currentUser.value?.name ?: "المشرف",
                    eventType = "PLACE_ADDED",
                    title = "إضافة مكان جديد",
                    description = "تمت إضافة مكان ($name) إلى قائمة الأماكن المهمة.",
                    severity = EventSeverity.INFO
                )
            )
            _showAddPlaceDialog.value = false
        }
    }

    fun deletePlace(place: PlaceEntity) {
        viewModelScope.launch {
            repository.deletePlace(place)
        }
    }

    fun startTrip(destName: String, destLat: Double, destLng: Double) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val trip = TripEntity(
                id = "trip_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                userName = user.name,
                startLocationName = user.currentPlaceName,
                destinationName = destName,
                startLatitude = user.latitude,
                startLongitude = user.longitude,
                destLatitude = destLat,
                destLongitude = destLng,
                currentLatitude = user.latitude,
                currentLongitude = user.longitude,
                etaMinutes = 15,
                progressPercent = 0,
                isCompleted = false
            )
            repository.insertTrip(trip)
            repository.insertEventLog(
                EventLogEntity(
                    familyId = repository.defaultFamilyId,
                    userId = user.id,
                    userName = user.name,
                    eventType = "TRIP_STARTED",
                    title = "بدء رحلة جديدة",
                    description = "بدأ $user.name رحلة مباشرة متجهاً إلى $destName.",
                    severity = EventSeverity.INFO
                )
            )
            _showStartTripDialog.value = false
        }
    }

    fun toggleLiveSimulation() {
        _isLiveSimulationRunning.value = !_isLiveSimulationRunning.value
        if (_isLiveSimulationRunning.value) {
            startLiveLocationSimulation()
        } else {
            simulationJob?.cancel()
        }
    }

    private fun startLiveLocationSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            var step = 0
            while (_isLiveSimulationRunning.value) {
                val interval = userSettings.value?.updateIntervalSeconds ?: 5
                delay(interval * 1000L)
                step++

                // Simulate movement for Son (Tariq - usr_003)
                val baseLat = 24.7136
                val baseLng = 46.6753
                val sonLat = baseLat + 0.015 + (Math.sin(step * 0.2) * 0.003)
                val sonLng = baseLng + 0.012 + (Math.cos(step * 0.2) * 0.003)
                val sonSpeed = if (step % 4 == 0) 0f else (30f + (step % 20))
                val sonBattery = (45 - (step / 10)).coerceAtLeast(10)

                repository.updateMemberLocation(
                    userId = "usr_003",
                    lat = sonLat,
                    lng = sonLng,
                    speed = sonSpeed,
                    battery = sonBattery,
                    placeName = if (sonSpeed == 0f) "جامعة الملك سعود" else "طريق الجامعة"
                )

                // Simulate progress for active trip if exists
                val trip = activeTrip.value
                if (trip != null && !trip.isCompleted) {
                    val nextProgress = (trip.progressPercent + 5).coerceAtMost(100)
                    val nextEta = ((100 - nextProgress) / 5).coerceAtLeast(1)
                    if (nextProgress >= 100) {
                        repository.completeTrip(trip.id)
                        repository.insertEventLog(
                            EventLogEntity(
                                familyId = repository.defaultFamilyId,
                                userId = trip.userId,
                                userName = trip.userName,
                                eventType = "TRIP_COMPLETED",
                                title = "وصول الرحلة 🏁",
                                description = "وصل ${trip.userName} إلى وجهته (${trip.destinationName}) بسلام.",
                                severity = EventSeverity.INFO
                            )
                        )
                    } else {
                        repository.updateTripProgress(
                            tripId = trip.id,
                            lat = sonLat,
                            lng = sonLng,
                            progress = nextProgress,
                            eta = nextEta
                        )
                    }
                }
            }
        }
    }
}
