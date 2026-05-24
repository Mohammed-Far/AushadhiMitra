package com.example.greenpulse

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenpulse.data.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    val medicines = mutableStateListOf<Medicine>()
    val doseRecords = mutableStateListOf<DoseRecord>()
    val smsLogs = mutableStateListOf<SMSLog>()

    val isBuzzerActive = mutableStateOf(false)
    val currentAlertMedicine = mutableStateOf<Medicine?>(null)
    val activeDoseId = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)
    val isProfileLoaded = mutableStateOf(false)

    var patientProfile = mutableStateOf(PatientProfile())

    init {
        initializeMedicineSlots()
    }

    fun onUserAuthenticated() {
        loadData()
    }

    fun clearData() {
        medicines.clear()
        doseRecords.clear()
        smsLogs.clear()
        patientProfile.value = PatientProfile()
        isProfileLoaded.value = false
        initializeMedicineSlots()
    }

    private fun initializeMedicineSlots() {
        medicines.clear()
        SlotID.entries.forEachIndexed { index, slot ->
            medicines.add(
                Medicine(
                    id = slot.name,
                    tabletName = "Tablet ${index + 1}",
                    slot = slot,
                    userMedicineName = "",
                    time = "08:00"
                )
            )
        }
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading.value = true

            try {
                // 1. Load Profile First
                val profile = try {
                    repository.loadProfile()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                if (profile != null) patientProfile.value = profile
                isProfileLoaded.value = true // ✅ Profile is now verified

                // 2. Load Medicines
                try {
                    val loadedMedicines = repository.loadMedicines()
                    loadedMedicines.forEach { loaded ->
                        val index = medicines.indexOfFirst { it.slot == loaded.slot }
                        if (index != -1 && loaded.userMedicineName.isNotEmpty()) {
                            medicines[index] = loaded
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }

                // 3. Load Doses
                try {
                    val loadedRecords = repository.loadTodayDoseRecords()
                    doseRecords.clear()
                    doseRecords.addAll(loadedRecords)
                    generateDoseRecordsForToday()
                    sortDoseRecords()
                } catch (e: Exception) { e.printStackTrace() }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
                isProfileLoaded.value = true
            }
        }
    }

    fun updateMedicine(
        slot: SlotID,
        userName: String,
        time: String,
        scheduleType: ScheduleType,
        selectedDays: List<DayOfWeek>
    ) {
        val index = medicines.indexOfFirst { it.slot == slot }
        if (index != -1) {
            val updatedMed = medicines[index].copy(
                userMedicineName = userName,
                time = time,
                scheduleType = scheduleType,
                selectedDays = selectedDays,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            medicines[index] = updatedMed

            // ✅ Remove old records for this slot first
            doseRecords.removeAll { it.slot == slot }

            // ✅ Generate immediately so UI updates instantly
            generateDoseRecordsForToday()

            // ✅ Save to Firestore in background
            viewModelScope.launch {
                try {
                    repository.saveMedicine(updatedMed)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun cancelMedication(slot: SlotID) {
        val index = medicines.indexOfFirst { it.slot == slot }
        if (index != -1) {
            val updatedMed = medicines[index].copy(isActive = false)
            medicines[index] = updatedMed
            doseRecords.removeAll { it.slot == slot }
            sortDoseRecords()
            viewModelScope.launch {
                repository.saveMedicine(updatedMed)
            }
        }
    }

    private fun generateDoseRecordsForToday() {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val todayDay = DayOfWeek.valueOf(today.dayOfWeek.name)
        val isEvenDay = today.dayOfYear % 2 == 0

        val currentDoseIds = doseRecords.map { it.id }.toSet()
        val newRecords = mutableListOf<DoseRecord>()

        medicines.filter { it.isActive && it.userMedicineName.isNotEmpty() }.forEach { med ->
            val shouldSchedule = when (med.scheduleType) {
                ScheduleType.EVERY_DAY -> true
                ScheduleType.EVERY_OTHER_DAY -> isEvenDay
                ScheduleType.SPECIFIC_DAYS -> med.selectedDays.contains(todayDay)
            }

            if (shouldSchedule) {
                val time = med.time
                val recordId = "${med.id}_${todayStr}_${time.replace(":", "")}"
                if (!currentDoseIds.contains(recordId)) {
                    newRecords.add(
                        DoseRecord(
                            id = recordId,
                            medicineId = med.id,
                            tabletName = med.tabletName,
                            medicineName = med.userMedicineName,
                            scheduledTime = time,
                            status = DoseStatus.PENDING,
                            slot = med.slot
                        )
                    )
                }
            }
        }

        if (newRecords.isNotEmpty()) {
            doseRecords.addAll(newRecords)
            sortDoseRecords()
            viewModelScope.launch {
                newRecords.forEach {
                    try { repository.saveDoseRecord(it) } catch (e: Exception) { e.printStackTrace() }
                }
            }
        } else {
            sortDoseRecords()
        }
    }

    fun clearSlot(slot: SlotID) {
        val index = medicines.indexOfFirst { it.slot == slot }
        if (index != -1) {
            medicines[index] = medicines[index].copy(
                userMedicineName = "",
                time = "08:00",
                isActive = false
            )
            // ✅ Remove from UI immediately
            doseRecords.removeAll { it.slot == slot }
            sortDoseRecords()

            viewModelScope.launch {
                try { repository.clearMedicine(slot) } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private fun sortDoseRecords() {
        val sorted = doseRecords.sortedBy { it.scheduledTime }
        doseRecords.clear()
        doseRecords.addAll(sorted)
    }

    fun triggerAlert(medicine: Medicine, recordId: String) {
        currentAlertMedicine.value = medicine
        activeDoseId.value = recordId
        isBuzzerActive.value = true
        addSMSLog(SMSLogType.REMINDER, "REMINDER: Time for ${medicine.userMedicineName} (${medicine.tabletName})")
    }

    fun dispenseMedicine() {
        val recordId = activeDoseId.value ?: return
        val medicine = currentAlertMedicine.value ?: return

        updateRecordStatus(recordId, DoseStatus.DISPENSED)
        isBuzzerActive.value = false

        addSMSLog(SMSLogType.CONFIRMATION, "${medicine.tabletName} dispensed. Waiting for pickup...")

        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            checkSensor(recordId)
        }
    }

    private fun checkSensor(recordId: String) {
        val index = doseRecords.indexOfFirst { it.id == recordId }
        if (index != -1) {
            val record = doseRecords[index]
            val newStatus = if (record.sensorDetected) DoseStatus.MISSED else DoseStatus.TAKEN
            updateRecordStatus(recordId, newStatus)

            viewModelScope.launch {
                try {
                    repository.updateDoseStatus(recordId, newStatus)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val logMessage = if (record.sensorDetected)
                "CRITICAL: ${record.tabletName} was NOT taken!"
            else
                "CONFIRMED: ${record.tabletName} taken."

            addSMSLog(
                if (record.sensorDetected) SMSLogType.WARNING else SMSLogType.CONFIRMATION,
                logMessage
            )
        }
        currentAlertMedicine.value = null
        activeDoseId.value = null
    }

    fun simulatePickup(recordId: String) {
        val index = doseRecords.indexOfFirst { it.id == recordId }
        if (index != -1) {
            doseRecords[index] = doseRecords[index].copy(sensorDetected = false)
        }
    }

    private fun updateRecordStatus(recordId: String, status: DoseStatus) {
        val index = doseRecords.indexOfFirst { it.id == recordId }
        if (index != -1) {
            doseRecords[index] = doseRecords[index].copy(
                status = status,
                actualTime = if (status == DoseStatus.TAKEN)
                    System.currentTimeMillis() else doseRecords[index].actualTime
            )
        }
    }

    fun updatePatientProfile(updated: PatientProfile) {
        patientProfile.value = updated
        viewModelScope.launch {
            try {
                repository.saveProfile(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addSMSLog(type: SMSLogType, message: String) {
        smsLogs.add(0, SMSLog(type = type, message = message))
    }
}
