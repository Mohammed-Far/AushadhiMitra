package com.example.greenpulse

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenpulse.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val medicines = mutableStateListOf<Medicine>()
    val doseRecords = mutableStateListOf<DoseRecord>()
    val smsLogs = mutableStateListOf<SMSLog>()
    
    val isBuzzerActive = mutableStateOf(value = false)
    val currentAlertMedicine = mutableStateOf<Medicine?>(value = null)
    val activeDoseId = mutableStateOf<String?>(value = null)
    
    var patientProfile = mutableStateOf(value = PatientProfile())

    init {
        // Initialize exactly 6 tablets (Tablet 1 to Tablet 6)
        SlotID.entries.forEachIndexed { index, slot ->
            medicines.add(
                Medicine(
                    id = "user123_med_${index + 1}",
                    tabletName = "Tablet ${index + 1}",
                    slot = slot,
                    userMedicineName = "",
                    time = ""
                )
            )
        }
    }

    fun updateMedicine(slot: SlotID, userName: String, time: String) {
        val index = medicines.indexOfFirst { it.slot == slot }
        if (index != -1) {
            val updatedMed = medicines[index].copy(
                userMedicineName = userName, 
                time = time, 
                createdAt = System.currentTimeMillis()
            )
            medicines[index] = updatedMed
            
            // Sync Daily Schedule
            doseRecords.removeAll { it.slot == slot }
            if (userName.isNotEmpty() && time.isNotEmpty()) {
                doseRecords.add(
                    DoseRecord(
                        medicineId = updatedMed.id,
                        tabletName = updatedMed.tabletName,
                        medicineName = userName,
                        scheduledTime = time,
                        status = DoseStatus.PENDING,
                        slot = slot
                    )
                )
            }
            sortDoseRecords()
            addSMSLog(SMSLogType.REMINDER, "Configured ${updatedMed.tabletName} ($userName) for $time")
        }
    }

    fun clearSlot(slot: SlotID) {
        val index = medicines.indexOfFirst { it.slot == slot }
        if (index != -1) {
            val tabletName = medicines[index].tabletName
            medicines[index] = medicines[index].copy(userMedicineName = "", time = "")
            doseRecords.removeAll { it.slot == slot }
            sortDoseRecords()
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
            delay(5000) 
            checkSensor(recordId)
        }
    }

    private fun checkSensor(recordId: String) {
        val index = doseRecords.indexOfFirst { it.id == recordId }
        if (index != -1) {
            val record = doseRecords[index]
            if (record.sensorDetected) {
                updateRecordStatus(recordId, DoseStatus.MISSED)
                addSMSLog(SMSLogType.WARNING, "CRITICAL: ${record.tabletName} was NOT taken!")
            } else {
                updateRecordStatus(recordId, DoseStatus.TAKEN)
                addSMSLog(SMSLogType.CONFIRMATION, "CONFIRMED: ${record.tabletName} taken.")
            }
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
                actualTime = if (status == DoseStatus.TAKEN) System.currentTimeMillis() else doseRecords[index].actualTime
            )
        }
    }

    fun updatePatientProfile(updated: PatientProfile) {
        patientProfile.value = updated
    }

    private fun addSMSLog(type: SMSLogType, message: String) {
        smsLogs.add(0, SMSLog(type = type, message = message))
    }
}
