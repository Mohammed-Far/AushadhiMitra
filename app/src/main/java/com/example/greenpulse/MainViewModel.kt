package com.example.greenpulse

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenpulse.data.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    val medicines = mutableStateListOf<Medicine>()
    val doseRecords = mutableStateListOf<DoseRecord>()
    val smsLogs = mutableStateListOf<SMSLog>()

    val isBuzzerActive = mutableStateOf(false)
    val currentAlertMedicine = mutableStateOf<Medicine?>(null)
    val activeDoseId = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    var patientProfile = mutableStateOf(PatientProfile())

    init {
        initializeMedicineSlots()
        // ✅ loadData() removed from here — called only after auth
    }

    // ✅ Called from AppNavigator after AuthState.Authenticated
    fun onUserAuthenticated() {
        loadData()
    }

    // Initialize 6 empty slots locally
    private fun initializeMedicineSlots() {
        SlotID.entries.forEachIndexed { index, slot ->
            medicines.add(
                Medicine(
                    id = "user_${slot.name}",
                    tabletName = "Tablet ${index + 1}",
                    slot = slot,
                    userMedicineName = "",
                    time = ""
                )
            )
        }
    }

    // Load all data from Firestore
    fun loadData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Load medicines
                val loadedMedicines = repository.loadMedicines()
                loadedMedicines.forEach { loaded ->
                    val index = medicines.indexOfFirst { it.slot == loaded.slot }
                    if (index != -1 && loaded.userMedicineName.isNotEmpty()) {
                        medicines[index] = loaded
                    }
                }

                // Load today's dose records
                val loadedRecords = repository.loadTodayDoseRecords()
                doseRecords.clear()
                doseRecords.addAll(loadedRecords)
                sortDoseRecords()

                // Load profile
                val profile = repository.loadProfile()
                if (profile != null) patientProfile.value = profile

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    // Update medicine and save to Firestore
    fun updateMedicine(slot: SlotID, userName: String, time: String) {
        val index = medicines.indexOfFirst { it.slot == slot }
        if (index != -1) {
            val updatedMed = medicines[index].copy(
                userMedicineName = userName,
                time = time,
                createdAt = System.currentTimeMillis()
            )
            medicines[index] = updatedMed

            // Save to Firestore
            viewModelScope.launch {
                try {
                    repository.saveMedicine(updatedMed)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Sync Daily Schedule
            doseRecords.removeAll { it.slot == slot }
            if (userName.isNotEmpty() && time.isNotEmpty()) {
                val record = DoseRecord(
                    medicineId = updatedMed.id,
                    tabletName = updatedMed.tabletName,
                    medicineName = userName,
                    scheduledTime = time,
                    status = DoseStatus.PENDING,
                    slot = slot
                )
                doseRecords.add(record)

                // Save dose record to Firestore
                viewModelScope.launch {
                    try {
                        repository.saveDoseRecord(record)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            sortDoseRecords()
            addSMSLog(SMSLogType.REMINDER, "Configured ${updatedMed.tabletName} ($userName) for $time")
        }
    }

    // Clear slot and update Firestore
    fun clearSlot(slot: SlotID) {
        val index = medicines.indexOfFirst { it.slot == slot }
        if (index != -1) {
            val tabletName = medicines[index].tabletName
            medicines[index] = medicines[index].copy(
                userMedicineName = "",
                time = ""
            )
            doseRecords.removeAll { it.slot == slot }
            sortDoseRecords()

            viewModelScope.launch {
                try {
                    repository.clearMedicine(slot)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            addSMSLog(SMSLogType.REMINDER, "Cleared configuration for $tabletName")
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