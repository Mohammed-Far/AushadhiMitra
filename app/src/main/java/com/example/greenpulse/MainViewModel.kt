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
    
    var isBuzzerActive = mutableStateOf(false)
    var currentAlertMedicine = mutableStateOf<Medicine?>(null)
    var activeDoseId = mutableStateOf<String?>(null)

    init {
        // Initialize exactly 6 tablets
        SlotID.entries.forEachIndexed { index, slot ->
            medicines.add(
                Medicine(
                    name = "Tablet ${index + 1}",
                    slot = slot,
                    intakeType = IntakeType.NONE,
                    reminderTimes = emptyList(),
                )
            )
        }
        
        addSMSLog(SMSLogType.REMINDER, "System Initialized: 6 Tablet Slots Ready.")
    }

    fun updateTabletTimings(medicineId: String, newTimes: List<String>) {
        val index = medicines.indexOfFirst { it.id == medicineId }
        if (index != -1) {
            val updatedMed = medicines[index].copy(reminderTimes = newTimes)
            medicines[index] = updatedMed
            
            // Refresh dose records for this tablet
            doseRecords.removeAll { it.medicineName == updatedMed.name }
            newTimes.forEach { time ->
                doseRecords.add(DoseRecord(
                    medicineName = updatedMed.name,
                    scheduledTime = time,
                    status = DoseStatus.PENDING,
                    slot = updatedMed.slot
                ))
            }
            // Sort by time
            val sorted = doseRecords.sortedBy { it.scheduledTime }
            doseRecords.clear()
            doseRecords.addAll(sorted)
        }
    }

    fun triggerAlert(medicine: Medicine, recordId: String) {
        currentAlertMedicine.value = medicine
        activeDoseId.value = recordId
        isBuzzerActive.value = true
        addSMSLog(SMSLogType.REMINDER, "TIME TRIGGERED: Please take ${medicine.name} from Slot ${medicine.slot.name}")
    }

    fun dispenseMedicine() {
        val recordId = activeDoseId.value ?: return
        val medicine = currentAlertMedicine.value ?: return
        
        updateRecordStatus(recordId, DoseStatus.DISPENSED)
        isBuzzerActive.value = false
        
        addSMSLog(SMSLogType.CONFIRMATION, "${medicine.name} dispensed. Waiting for pickup...")

        viewModelScope.launch {
            delay(5000) // 5 second simulation for 5 minutes
            checkSensor(recordId)
        }
    }

    private fun checkSensor(recordId: String) {
        val index = doseRecords.indexOfFirst { it.id == recordId }
        if (index != -1) {
            val record = doseRecords[index]
            if (record.sensorDetected) {
                updateRecordStatus(recordId, DoseStatus.MISSED)
                addSMSLog(SMSLogType.WARNING, "ALERT: ${record.medicineName} was NOT removed from the plate!")
            } else {
                updateRecordStatus(recordId, DoseStatus.TAKEN)
                addSMSLog(SMSLogType.CONFIRMATION, "${record.medicineName} successfully taken.")
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

    private fun addSMSLog(type: SMSLogType, message: String) {
        smsLogs.add(0, SMSLog(type = type, message = message))
    }
}
