package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EventEntity
import com.example.data.EventSeverity
import com.example.data.EventLogEntity
import com.example.data.FamilyGuardianDatabase
import com.example.data.FamilyGuardianRepository
import com.example.data.FamilyRole
import com.example.data.PlaceEntity
import com.example.data.PlaceType
import com.example.data.TripEntity
import com.example.data.UserEntity
import com.example.data.UserSettingsEntity
import com.example.data.LocationHistoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyGuardianViewModel(
    application: Application
) : AndroidViewModel(application) {

    // ============================================================
    // DATABASE
    // ============================================================

    private val db =
        FamilyGuardianDatabase.getDatabase(application)

    private val repository =
        FamilyGuardianRepository(db.dao())

    // ============================================================
    // FAMILY
    // ============================================================

    val familyMembers: StateFlow<List<UserEntity>> =
        repository
            .getFamilyMembers()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val currentFamily: StateFlow<com.example.data.FamilyEntity?> =
        repository
            .getFamily()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val currentUser: StateFlow<UserEntity?> =
        repository
            .getUser()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    // ============================================================
    // PLACES
    // ============================================================

    val places: StateFlow<List<PlaceEntity>> =
        repository
            .getPlaces()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // ============================================================
    // EVENTS
    // ============================================================

    val eventLogs: StateFlow<List<EventLogEntity>> =
        repository
            .getEventLogs()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // ============================================================
    // TRIPS
    // ============================================================

    val activeTrip: StateFlow<TripEntity?> =
        repository
            .getActiveTrip()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val allTrips: StateFlow<List<TripEntity>> =
        repository
            .getAllTrips()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // ============================================================
    // SETTINGS
    // ============================================================

    val userSettings: StateFlow<UserSettingsEntity?> =
        repository
            .getUserSettings()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    // ============================================================
    // SELECTED FAMILY MEMBER
    // ============================================================

    /**
     * لا نحدد usr_003 تلقائياً بعد الآن.
     *
     * المستخدم سيختار فرد العائلة بنفسه.
     */
    private val _selectedMemberId =
        MutableStateFlow<String?>(null)

    val selectedMemberId: StateFlow<String?> =
        _selectedMemberId.asStateFlow()

    val selectedMemberHistory:
        StateFlow<List<LocationHistoryEntity>> =
        _selectedMemberId
            .flatMapLatest { id ->
                if (id != null) {
                    repository.getLocationHistory(id)
                } else {
                    flowOf(emptyList())
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // ============================================================
    // TABS
    // ============================================================

    private val _selectedTab =
        MutableStateFlow(0)

    val selectedTab: StateFlow<Int> =
        _selectedTab.asStateFlow()

    // ============================================================
    // SOS
    // ============================================================

    private val _isSosActive =
        MutableStateFlow(false)

    val isSosActive: StateFlow<Boolean> =
        _isSosActive.asStateFlow()

    private val _showSosDialog =
        MutableStateFlow(false)

    val showSosDialog: StateFlow<Boolean> =
        _showSosDialog.asStateFlow()

    // ============================================================
    // QR
    // ============================================================

    private val _showQrDialog =
        MutableStateFlow(false)

    val showQrDialog: StateFlow<Boolean> =
        _showQrDialog.asStateFlow()

    // ============================================================
    // ADD PLACE
    // ============================================================

    private val _showAddPlaceDialog =
        MutableStateFlow(false)

    val showAddPlaceDialog: StateFlow<Boolean> =
        _showAddPlaceDialog.asStateFlow()

    // ============================================================
    // START TRIP
    // ============================================================

    private val _showStartTripDialog =
        MutableStateFlow(false)

    val showStartTripDialog: StateFlow<Boolean> =
        _showStartTripDialog.asStateFlow()

    // ============================================================
    // ADD MEMBER
    // ============================================================

    private val _showAddMemberDialog =
        MutableStateFlow(false)

    val showAddMemberDialog: StateFlow<Boolean> =
        _showAddMemberDialog.asStateFlow()

    // ============================================================
    // DELETE MEMBER
    // ============================================================

    private val _showDeleteMemberDialog =
        MutableStateFlow(false)

    val showDeleteMemberDialog: StateFlow<Boolean> =
        _showDeleteMemberDialog.asStateFlow()

    private val _memberToDelete =
        MutableStateFlow<UserEntity?>(null)

    val memberToDelete: StateFlow<UserEntity?> =
        _memberToDelete.asStateFlow()

    // ============================================================
    // OPERATION MESSAGE
    // ============================================================

    private val _operationMessage =
        MutableStateFlow<String?>(null)

    val operationMessage: StateFlow<String?> =
        _operationMessage.asStateFlow()

    // ============================================================
    // HISTORY FILTER
    // ============================================================

    private val _selectedHistoryFilter =
        MutableStateFlow("DAY")

    val selectedHistoryFilter: StateFlow<String> =
        _selectedHistoryFilter.asStateFlow()

    // ============================================================
    // LIVE SIMULATION
    // ============================================================

    private val _isLiveSimulationRunning =
        MutableStateFlow(false)

    val isLiveSimulationRunning:
        StateFlow<Boolean> =
        _isLiveSimulationRunning.asStateFlow()

    private var simulationJob: Job? = null

    // ============================================================
    // INITIALIZATION
    // ============================================================

    init {

        viewModelScope.launch {

            repository.seedDatabaseIfEmpty()

            /*
             * لا نشغل محاكاة الموقع تلقائياً.
             *
             * لأن التطبيق أصبح مخصصاً لإضافة أفراد حقيقيين
             * بدلاً من الأشخاص التجريبيين.
             */
            _isLiveSimulationRunning.value = false
        }
    }

    // ============================================================
    // TAB
    // ============================================================

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    // ============================================================
    // MEMBER SELECTION
    // ============================================================

    fun selectMember(memberId: String?) {
        _selectedMemberId.value = memberId
    }

    // ============================================================
    // HISTORY
    // ============================================================

    fun setHistoryFilter(filter: String) {
        _selectedHistoryFilter.value = filter
    }

    // ============================================================
    // SOS DIALOG
    // ============================================================

    fun toggleSosDialog(show: Boolean) {
        _showSosDialog.value = show
    }

    // ============================================================
    // QR DIALOG
    // ============================================================

    fun toggleQrDialog(show: Boolean) {
        _showQrDialog.value = show
    }

    // ============================================================
    // ADD PLACE DIALOG
    // ============================================================

    fun toggleAddPlaceDialog(show: Boolean) {
        _showAddPlaceDialog.value = show
    }

    // ============================================================
    // START TRIP DIALOG
    // ============================================================

    fun toggleStartTripDialog(show: Boolean) {
        _showStartTripDialog.value = show
    }

    // ============================================================
    // ADD MEMBER DIALOG
    // ============================================================

    fun toggleAddMemberDialog(show: Boolean) {
        _showAddMemberDialog.value = show
    }

    // ============================================================
    // DELETE MEMBER DIALOG
    // ============================================================

    fun showDeleteMemberDialog(member: UserEntity) {

        _memberToDelete.value = member

        _showDeleteMemberDialog.value = true
    }

    fun hideDeleteMemberDialog() {

        _memberToDelete.value = null

        _showDeleteMemberDialog.value = false
    }

    // ============================================================
    // CLEAR MESSAGE
    // ============================================================

    fun clearOperationMessage() {
        _operationMessage.value = null
    }

    // ============================================================
    // ADD FAMILY MEMBER
    // ============================================================

    fun addFamilyMember(
        name: String,
        phone: String,
        email: String,
        role: FamilyRole
    ) {

        viewModelScope.launch {

            val cleanName =
                name.trim()

            val cleanPhone =
                phone.trim()

            val cleanEmail =
                email.trim()

            if (cleanName.isBlank()) {

                _operationMessage.value =
                    "يرجى إدخال اسم الفرد."

                return@launch
            }

            /*
             * السماح للهاتف والبريد الإلكتروني
             * بأن يكونا فارغين، لكن عند إدخالهما
             * سيتم التحقق من عدم التكرار داخل Repository.
             */

            val success =
                repository.addFamilyMember(
                    name = cleanName,
                    phone = cleanPhone,
                    email = cleanEmail,
                    role = role
                )

            if (success) {

                _operationMessage.value =
                    "تمت إضافة $cleanName إلى العائلة بنجاح."

                _showAddMemberDialog.value = false

            } else {

                _operationMessage.value =
                    when (role) {

                        FamilyRole.CLOSE_FRIEND ->
                            "تعذر الإضافة. الحد الأقصى للأصدقاء المقربين هو 3."

                        else ->
                            "تعذر إضافة الفرد. قد يكون رقم الهاتف أو البريد الإلكتروني مستخدماً بالفعل."
                    }
            }
        }
    }

    // ============================================================
    // DELETE FAMILY MEMBER
    // ============================================================

    fun deleteFamilyMember(
        member: UserEntity
    ) {

        viewModelScope.launch {

            /*
             * منع حذف المستخدم الحالي من الواجهة.
             */
            if (member.id == repository.currentUserId) {

                _operationMessage.value =
                    "لا يمكن حذف مدير العائلة."

                hideDeleteMemberDialog()

                return@launch
            }

            val success =
                repository.deleteFamilyMember(
                    userId = member.id
                )

            if (success) {

                if (_selectedMemberId.value == member.id) {
                    _selectedMemberId.value = null
                }

                _operationMessage.value =
                    "تم حذف ${member.name} من العائلة."

            } else {

                _operationMessage.value =
                    "تعذر حذف ${member.name}. قد يكون مدير العائلة."

            }

            hideDeleteMemberDialog()
        }
    }

    // ============================================================
    // DELETE SELECTED MEMBER
    // ============================================================

    fun deleteSelectedMember() {

        val member =
            _memberToDelete.value
                ?: return

        deleteFamilyMember(member)
    }

    // ============================================================
    // SOS
    // ============================================================

    fun triggerSosAlert() {

        viewModelScope.launch {

            _isSosActive.value = true

            _showSosDialog.value = true

            val user =
                currentUser.value
                    ?: return@launch

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

    // ============================================================
    // LOCATION SHARING
    // ============================================================

    fun toggleLocationSharing(
        enabled: Boolean
    ) {

        viewModelScope.launch {

            val user =
                currentUser.value
                    ?: return@launch

            repository.toggleLocationSharing(
                user.id,
                enabled
            )

            repository.insertEventLog(
                EventLogEntity(
                    familyId =
                        repository.defaultFamilyId,

                    userId =
                        user.id,

                    userName =
                        user.name,

                    eventType =
                        "PRIVACY_UPDATE",

                    title =
                        if (enabled)
                            "تفعيل مشاركة الموقع"
                        else
                            "إيقاف مشاركة الموقع",

                    description =
                        if (enabled)
                            "قام ${user.name} بتفعيل مشاركة الموقع."
                        else
                            "قام ${user.name} بإيقاف مشاركة الموقع.",

                    severity =
                        EventSeverity.WARNING
                )
            )
        }
    }

    // ============================================================
    // DARK MODE
    // ============================================================

    fun toggleDarkMode(
        enabled: Boolean
    ) {

        viewModelScope.launch {

            val current =
                userSettings.value
                    ?: UserSettingsEntity()

            repository.updateUserSettings(
                current.copy(
                    isDarkMode = enabled
                )
            )
        }
    }

    // ============================================================
    // LANGUAGE
    // ============================================================

    fun toggleLanguage(
        lang: String
    ) {

        viewModelScope.launch {

            val current =
                userSettings.value
                    ?: UserSettingsEntity()

            repository.updateUserSettings(
                current.copy(
                    language = lang
                )
            )
        }
    }

    // ============================================================
    // UPDATE INTERVAL
    // ============================================================

    fun updateUpdateInterval(
        seconds: Int
    ) {

        viewModelScope.launch {

            val current =
                userSettings.value
                    ?: UserSettingsEntity()

            repository.updateUserSettings(
                current.copy(
                    updateIntervalSeconds =
                        seconds.coerceAtLeast(1)
                )
            )
        }
    }

    // ============================================================
    // ADD PLACE
    // ============================================================

    fun addPlace(
        name: String,
        type: PlaceType,
        lat: Double,
        lng: Double,
        radius: Int
    ) {

        viewModelScope.launch {

            val place =
                PlaceEntity(
                    id =
                        "place_${UUID.randomUUID()}",

                    familyId =
                        repository.defaultFamilyId,

                    name =
                        name.trim(),

                    placeType =
                        type,

                    latitude =
                        lat,

                    longitude =
                        lng,

                    radiusMeters =
                        radius.coerceAtLeast(50)
                )

            repository.insertPlace(place)

            repository.insertEventLog(
                EventLogEntity(
                    familyId =
                        repository.defaultFamilyId,

                    userId =
                        repository.currentUserId,

                    userName =
                        currentUser.value?.name
                            ?: "مدير العائلة",

                    eventType =
                        "PLACE_ADDED",

                    title =
                        "إضافة مكان جديد",

                    description =
                        "تمت إضافة المكان ($name) إلى قائمة الأماكن المهمة.",

                    severity =
                        EventSeverity.INFO
                )
            )

            _showAddPlaceDialog.value = false
        }
    }

    // ============================================================
    // DELETE PLACE
    // ============================================================

    fun deletePlace(
        place: PlaceEntity
    ) {

        viewModelScope.launch {

            repository.deletePlace(place)

            _operationMessage.value =
                "تم حذف المكان ${place.name}."
        }
    }

    // ============================================================
    // START TRIP
    // ============================================================

    fun startTrip(
        destName: String,
        destLat: Double,
        destLng: Double
    ) {

        viewModelScope.launch {

            val user =
                currentUser.value
                    ?: return@launch

            val trip =
                TripEntity(
                    id =
                        "trip_${UUID.randomUUID()}",

                    userId =
                        user.id,

                    userName =
                        user.name,

                    startLocationName =
                        user.currentPlaceName,

                    destinationName =
                        destName,

                    startLatitude =
                        user.latitude,

                    startLongitude =
                        user.longitude,

                    destLatitude =
                        destLat,

                    destLongitude =
                        destLng,

                    currentLatitude =
                        user.latitude,

                    currentLongitude =
                        user.longitude,

                    etaMinutes =
                        15,

                    progressPercent =
                        0,

                    isCompleted =
                        false
                )

            repository.insertTrip(trip)

            repository.insertEventLog(
                EventLogEntity(
                    familyId =
                        repository.defaultFamilyId,

                    userId =
                        user.id,

                    userName =
                        user.name,

                    eventType =
                        "TRIP_STARTED",

                    title =
                        "بدء رحلة جديدة",

                    description =
                        "بدأ ${user.name} رحلة مباشرة متجهاً إلى $destName.",

                    severity =
                        EventSeverity.INFO
                )
            )

            _showStartTripDialog.value = false
        }
    }

    // ============================================================
    // LIVE SIMULATION
    // ============================================================

    fun toggleLiveSimulation() {

        _isLiveSimulationRunning.value =
            !_isLiveSimulationRunning.value

        if (_isLiveSimulationRunning.value) {

            startLiveLocationSimulation()

        } else {

            simulationJob?.cancel()
        }
    }

    private fun startLiveLocationSimulation() {

        simulationJob?.cancel()

        simulationJob =
            viewModelScope.launch {

                var step = 0

                while (_isLiveSimulationRunning.value) {

                    val interval =
                        userSettings.value
                            ?.updateIntervalSeconds
                            ?: 5

                    delay(
                        interval * 1000L
                    )

                    step++

                    /*
                     * المحاكاة أصبحت اختيارية.
                     *
                     * لا يتم إنشاء فرد افتراضي.
                     *
                     * إذا أراد المطور اختبار المحاكاة،
                     * يتم تطبيقها فقط على عضو حقيقي
                     * تم اختياره من القائمة.
                     */

                    val selectedId =
                        _selectedMemberId.value

                    if (selectedId != null) {

                        val member =
                            familyMembers.value
                                .firstOrNull {
                                    it.id == selectedId
                                }

                        if (member != null) {

                            val baseLat =
                                member.latitude

                            val baseLng =
                                member.longitude

                            val newLat =
                                baseLat +
                                    (
                                        kotlin.math.sin(
                                            step * 0.2
                                        ) * 0.0005
                                    )

                            val newLng =
                                baseLng +
                                    (
                                        kotlin.math.cos(
                                            step * 0.2
                                        ) * 0.0005
                                    )

                            val speed =
                                if (step % 4 == 0)
                                    0f
                                else
                                    20f +
                                        (step % 15)

                            val battery =
                                (
                                    member.batteryLevel - 1
                                ).coerceAtLeast(5)

                            repository.updateMemberLocation(
                                userId =
                                    member.id,

                                lat =
                                    newLat,

                                lng =
                                    newLng,

                                speed =
                                    speed,

                                battery =
                                    battery,

                                placeName =
                                    member.currentPlaceName
                            )
                        }
                    }
                }
            }
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    override fun onCleared() {

        simulationJob?.cancel()

        super.onCleared()
    }
}
