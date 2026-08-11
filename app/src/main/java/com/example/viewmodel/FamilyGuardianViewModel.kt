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
class FamilyGuardianViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = FamilyGuardianDatabase.getDatabase(application)
    private val repository = FamilyGuardianRepository(db.dao())

    // ============================================================
    // FAMILY
    // ============================================================

    val familyMembers: StateFlow<List<UserEntity>> =
        repository.getFamilyMembers()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val currentFamily: StateFlow<FamilyEntity?> =
        repository.getFamily()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val currentUser: StateFlow<UserEntity?> =
        repository.getUser()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    // ============================================================
    // MEMBER SELECTION
    // ============================================================

    private val _selectedMemberId =
        MutableStateFlow<String?>(null)

    val selectedMemberId: StateFlow<String?> =
        _selectedMemberId.asStateFlow()

    val selectedMemberHistory: StateFlow<List<LocationHistoryEntity>> =
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
    // NAVIGATION
    // ============================================================

    private val _selectedTab =
        MutableStateFlow(0)

    val selectedTab: StateFlow<Int> =
        _selectedTab.asStateFlow()

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectMember(memberId: String) {
        _selectedMemberId.value = memberId
    }

    // ============================================================
    // ADD MEMBER
    // ============================================================

    private val _showAddMemberDialog =
        MutableStateFlow(false)

    val showAddMemberDialog: StateFlow<Boolean> =
        _showAddMemberDialog.asStateFlow()

    fun toggleAddMemberDialog(show: Boolean) {
        _showAddMemberDialog.value = show
    }

    /**
     * إضافة فرد جديد للعائلة.
     *
     * يمكن استخدام هذه الدالة من شاشة إضافة أفراد العائلة.
     *
     * friendCount:
     * عدد الأصدقاء المقربين الموجودين حاليًا.
     *
     * يسمح النظام بحد أقصى 3 أصدقاء مقربين.
     */
    fun addFamilyMember(
        name: String,
        phone: String,
        email: String,
        role: FamilyRole
    ) {
        if (name.trim().isEmpty()) return

        viewModelScope.launch {

            // ----------------------------------------------------
            // منع إضافة أكثر من 3 أصدقاء مقربين
            // ----------------------------------------------------

            if (role == FamilyRole.CLOSE_FRIEND) {

                val currentFriends =
                    familyMembers.value.count {
                        it.role == FamilyRole.CLOSE_FRIEND
                    }

                if (currentFriends >= 3) {
                    return@launch
                }
            }

            val familyId =
                currentFamily.value?.id
                    ?: repository.defaultFamilyId

            val newMember =
                UserEntity(
                    id = "usr_${UUID.randomUUID()}",
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    role = role,
                    familyId = familyId,

                    batteryLevel = 100,
                    isCharging = false,
                    isInsideHome = true,
                    currentPlaceName = "غير محدد",

                    isLocationSharingEnabled = true,

                    speedKmh = 0f,
                    movementDirection = "شمال",

                    latitude = 24.7136,
                    longitude = 46.6753,

                    isOnline = false
                )

            repository.insertUser(newMember)

            repository.insertEventLog(
                EventLogEntity(
                    familyId = familyId,
                    userId = repository.currentUserId,
                    userName =
                        currentUser.value?.name ?: "مدير العائلة",
                    eventType = "MEMBER_ADDED",
                    title = "إضافة فرد جديد",
                    description =
                        "تمت إضافة ${newMember.name} إلى العائلة بصفة ${role.labelAr}.",
                    severity = EventSeverity.INFO
                )
            )

            _showAddMemberDialog.value = false
        }
    }

    // ============================================================
    // DELETE MEMBER
    // ============================================================

    private val _showDeleteMemberDialog =
        MutableStateFlow(false)

    val showDeleteMemberDialog: StateFlow<Boolean> =
        _showDeleteMemberDialog.asStateFlow()

    private val _memberPendingDeletion =
        MutableStateFlow<UserEntity?>(null)

    val memberPendingDeletion: StateFlow<UserEntity?> =
        _memberPendingDeletion.asStateFlow()

    /**
     * فتح نافذة تأكيد حذف العضو.
     *
     * لا يتم الحذف مباشرة حتى لا يتم حذف فرد بالخطأ.
     */
    fun requestDeleteMember(member: UserEntity) {

        // لا يسمح بحذف مالك/مدير العائلة
        if (member.id == repository.currentUserId) {
            return
        }

        _memberPendingDeletion.value = member
        _showDeleteMemberDialog.value = true
    }

    fun cancelDeleteMember() {
        _memberPendingDeletion.value = null
        _showDeleteMemberDialog.value = false
    }

    /**
     * تنفيذ الحذف بعد تأكيد المستخدم.
     */
    fun confirmDeleteMember() {

        viewModelScope.launch {

            val member =
                _memberPendingDeletion.value
                    ?: return@launch

            // حماية إضافية
            if (member.id == repository.currentUserId) {
                cancelDeleteMember()
                return@launch
            }

            repository.deleteUser(member.id)

            repository.insertEventLog(
                EventLogEntity(
                    familyId = member.familyId,
                    userId = repository.currentUserId,
                    userName =
                        currentUser.value?.name ?: "مدير العائلة",
                    eventType = "MEMBER_REMOVED",
                    title = "حذف فرد من العائلة",
                    description =
                        "تم حذف ${member.name} (${member.role.labelAr}) من العائلة.",
                    severity = EventSeverity.WARNING
                )
            )

            if (_selectedMemberId.value == member.id) {
                _selectedMemberId.value = null
            }

            _memberPendingDeletion.value = null
            _showDeleteMemberDialog.value = false
        }
    }

    // ============================================================
    // LOCATION / HISTORY
    // ============================================================

    fun toggleLocationSharing(enabled: Boolean) {

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
                    familyId = repository.defaultFamilyId,
                    userId = user.id,
                    userName = user.name,
                    eventType = "PRIVACY_UPDATE",
                    title =
                        if (enabled)
                            "تفعيل مشاركة الموقع"
                        else
                            "إيقاف مشاركة الموقع",
                    description =
                        if (enabled)
                            "قام ${user.name} بتفعيل مشاركة الموقع."
                        else
                            "قام ${user.name} بإيقاف مشاركة الموقع مؤقتاً.",
                    severity = EventSeverity.WARNING
                )
            )
        }
    }

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

    fun toggleSosDialog(show: Boolean) {
        _showSosDialog.value = show
    }

    fun triggerSosAlert() {

        viewModelScope.launch {

            val user =
                currentUser.value
                    ?: return@launch

            _isSosActive.value = true
            _showSosDialog.value = true

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
    // QR / INVITATION
    // ============================================================

    private val _showQrDialog =
        MutableStateFlow(false)

    val showQrDialog: StateFlow<Boolean> =
        _showQrDialog.asStateFlow()

    fun toggleQrDialog(show: Boolean) {
        _showQrDialog.value = show
    }

    /**
     * إنشاء رمز دعوة جديد للعائلة.
     *
     * لا يغير هوية العائلة.
     */
    fun generateFamilyInviteCode(): String {

        val code =
            "FG-" +
                    UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .take(6)
                        .uppercase()

        return code
    }

    // ============================================================
    // PLACES
    // ============================================================

    val places: StateFlow<List<PlaceEntity>> =
        repository.getPlaces()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val _showAddPlaceDialog =
        MutableStateFlow(false)

    val showAddPlaceDialog: StateFlow<Boolean> =
        _showAddPlaceDialog.asStateFlow()

    fun toggleAddPlaceDialog(show: Boolean) {
        _showAddPlaceDialog.value = show
    }

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
                        "place_${UUID.randomUUID()}"
                            .take(18),

                    familyId =
                        repository.defaultFamilyId,

                    name = name,

                    placeType = type,

                    latitude = lat,
                    longitude = lng,

                    radiusMeters = radius,

                    enterNotify = true,
                    exitNotify = true
                )

            repository.insertPlace(place)

            repository.insertEventLog(
                EventLogEntity(
                    familyId = repository.defaultFamilyId,
                    userId = repository.currentUserId,
                    userName =
                        currentUser.value?.name
                            ?: "مدير العائلة",
                    eventType = "PLACE_ADDED",
                    title = "إضافة مكان جديد",
                    description =
                        "تمت إضافة المكان: $name.",
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

    // ============================================================
    // TRIPS
    // ============================================================

    val activeTrip: StateFlow<TripEntity?> =
        repository.getActiveTrip()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val allTrips: StateFlow<List<TripEntity>> =
        repository.getAllTrips()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val _showStartTripDialog =
        MutableStateFlow(false)

    val showStartTripDialog: StateFlow<Boolean> =
        _showStartTripDialog.asStateFlow()

    fun toggleStartTripDialog(show: Boolean) {
        _showStartTripDialog.value = show
    }

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

                    userId = user.id,
                    userName = user.name,

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

                    etaMinutes = 15,
                    progressPercent = 0,
                    isCompleted = false
                )

            repository.insertTrip(trip)

            repository.insertEventLog(
                EventLogEntity(
                    familyId =
                        repository.defaultFamilyId,

                    userId = user.id,

                    userName = user.name,

                    eventType =
                        "TRIP_STARTED",

                    title =
                        "بدء رحلة جديدة",

                    description =
                        "بدأ ${user.name} رحلة إلى $destName.",

                    severity =
                        EventSeverity.INFO
                )
            )

            _showStartTripDialog.value = false
        }
    }

    // ============================================================
    // SETTINGS
    // ============================================================

    val userSettings:
            StateFlow<UserSettingsEntity?> =
        repository.getUserSettings()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    fun toggleDarkMode(enabled: Boolean) {

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

    fun toggleLanguage(lang: String) {

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

    fun updateUpdateInterval(seconds: Int) {

        viewModelScope.launch {

            val current =
                userSettings.value
                    ?: UserSettingsEntity()

            repository.updateUserSettings(
                current.copy(
                    updateIntervalSeconds =
                        seconds
                )
            )
        }
    }

    // ============================================================
    // HISTORY
    // ============================================================

    private val _selectedHistoryFilter =
        MutableStateFlow("DAY")

    val selectedHistoryFilter:
            StateFlow<String> =
        _selectedHistoryFilter.asStateFlow()

    fun setHistoryFilter(filter: String) {
        _selectedHistoryFilter.value = filter
    }

    // ============================================================
    // SHARE APPLICATION
    // ============================================================

    /**
     * الرابط الذي سيتم استخدامه لاحقاً
     * في زر مشاركة التطبيق.
     *
     * غيّره لاحقاً إلى رابط Google Play الحقيقي.
     */
    val applicationShareUrl:
            String =
        "https://play.google.com/store/apps/details?id=com.example.familyguardian"

    /**
     * النص الجاهز للمشاركة.
     */
    val applicationShareText:
            String
        get() =
            """
            🏠 Family Guardian
            
            تطبيق لمتابعة أفراد العائلة ومشاركة الموقع
            وتنبيهات الأمان والأماكن المهمة.
            
            حمّل التطبيق من الرابط:
            $applicationShareUrl
            """.trimIndent()

    // ============================================================
    // SUBSCRIPTION FOUNDATION
    // ============================================================

    /**
     * هذه مجرد بنية أولية للاشتراكات.
     *
     * الدفع الفعلي سيتم ربطه لاحقاً بـ:
     * Google Play Billing
     * أو نظام دفع خارجي حسب السوق المستهدف.
     */

    enum class SubscriptionPlan {
        FREE,
        MONTHLY,
        YEARLY
    }

    private val _subscriptionPlan =
        MutableStateFlow(
            SubscriptionPlan.FREE
        )

    val subscriptionPlan:
            StateFlow<SubscriptionPlan> =
        _subscriptionPlan.asStateFlow()

    private val _subscriptionActive =
        MutableStateFlow(false)

    val subscriptionActive:
            StateFlow<Boolean> =
        _subscriptionActive.asStateFlow()

    fun setSubscriptionPlan(
        plan: SubscriptionPlan
    ) {

        _subscriptionPlan.value = plan

        _subscriptionActive.value =
            plan != SubscriptionPlan.FREE
    }

    fun isPremiumFeatureAvailable():
            Boolean {

        return subscriptionActive.value
    }

    // ============================================================
    // LIVE SIMULATION
    // ============================================================

    private val _isLiveSimulationRunning =
        MutableStateFlow(true)

    val isLiveSimulationRunning:
            StateFlow<Boolean> =
        _isLiveSimulationRunning.asStateFlow()

    private var simulationJob:
            Job? = null

    // ============================================================
    // INITIALIZATION
    // ============================================================

    init {

        viewModelScope.launch {

            repository.seedDatabaseIfEmpty()

            startLiveLocationSimulation()
        }
    }

    // ============================================================
    // LIVE LOCATION SIMULATION
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

                while (
                    _isLiveSimulationRunning.value
                ) {

                    val interval =
                        userSettings.value
                            ?.updateIntervalSeconds
                            ?: 5

                    delay(
                        interval * 1000L
                    )

                    step++

                    // ------------------------------------------------
                    // المحاكاة الآن تعتمد على أول ابن موجود
                    // بدلاً من الاعتماد الدائم على usr_003
                    // ------------------------------------------------

                    val simulatedMember =
                        familyMembers.value
                            .firstOrNull {
                                it.role ==
                                        FamilyRole.SON
                            }
                            ?: familyMembers.value
                                .firstOrNull()

                    if (simulatedMember == null) {
                        continue
                    }

                    val baseLat =
                        24.7136

                    val baseLng =
                        46.6753

                    val memberLat =
                        baseLat +
                                0.015 +
                                (
                                    Math.sin(
                                        step * 0.2
                                    ) * 0.003
                                )

                    val memberLng =
                        baseLng +
                                0.012 +
                                (
                                    Math.cos(
                                        step * 0.2
                                    ) * 0.003
                                )

                    val memberSpeed =
                        if (step % 4 == 0) {
                            0f
                        } else {
                            30f +
                                    (
                                        step % 20
                                    )
                    }

                    val memberBattery =
                        (
                            simulatedMember
                                .batteryLevel -
                                    (step / 10)
                        )
                            .coerceAtLeast(10)

                    repository.updateMemberLocation(

                        userId =
                            simulatedMember.id,

                        lat =
                            memberLat,

                        lng =
                            memberLng,

                        speed =
                            memberSpeed,

                        battery =
                            memberBattery,

                        placeName =
                            if (
                                memberSpeed == 0f
                            ) {
                                "الموقع الحالي"
                            } else {
                                "في الطريق"
                            }
                    )
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
