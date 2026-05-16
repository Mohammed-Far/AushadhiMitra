package com.example.greenpulse

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.greenpulse.data.*
import java.util.*

class MainViewModel : ViewModel() {
    val medicines = mutableStateListOf<Medicine>()
    val doseRecords = mutableStateListOf<DoseRecord>()
    val smsLogs = mutableStateListOf<SMSLog>()
    
    var isBuzzerActive = mutableStateOf(false)
    var currentAlertMedicine = mutableStateOf<Medicine?>(null)

    init {
        // Mock initial data
        val m1 = Medicine(name = "Aspirin", slot = SlotID.S1, intakeType = IntakeType.AFTER_FOOD)
        val m2 = Medicine(name = "Vitamin C", slot = SlotID.S2, intakeType = IntakeType.BEFORE_FOOD)
        medicines.add(m1)
        medicines.add(m2)
        
        doseRecords.add(DoseRecord(medicineName = m1.name, scheduledTime = "08:00", status = DoseStatus.TAKEN, slot = m1.slot))
        doseRecords.add(DoseRecord(medicineName = m2.name, scheduledTime = "12:00", status = DoseStatus.PENDING, slot = m2.slot))
        
        smsLogs.add(SMSLog(type = SMSLogType.CONFIRMATION, message = "Aspirin taken successfully at 08:05 AM."))
    }

    fun addMedicine(name: String, slot: SlotID, intakeType: IntakeType, dosage: String) {
        val newMed = Medicine(name = name, slot = slot, intakeType = intakeType, dosage = dosage)
        medicines.add(newMed)
        // Simulate adding a dose record for today
        doseRecords.add(DoseRecord(medicineName = name, scheduledTime = "14:00", status = DoseStatus.PENDING, slot = slot))
        
        addSMSLog(SMSLogType.REMINDER, "New medicine added: $name assigned to Slot ${slot.name}")
    }

    fun markAsTaken(recordId: String) {
        val index = doseRecords.indexOfFirst { it.id == recordId }
        if (index != -1) {
            val record = doseRecords[index]
            doseRecords[index] = record.copy(status = DoseStatus.TAKEN, actualTime = System.currentTimeMillis())
            addSMSLog(SMSLogType.CONFIRMATION, "${record.medicineName} taken successfully.")
            
            // If this was the alerting med, stop buzzer
            if (currentAlertMedicine.value?.name == record.medicineName) {
                stopAlert()
            }
        }
    }

    fun triggerAlert(medicine: Medicine) {
        currentAlertMedicine.value = medicine
        isBuzzerActive.value = true
        addSMSLog(SMSLogType.REMINDER, "Medicine due: ${medicine.name} in Slot ${medicine.slot.name}")
    }

    fun stopAlert() {
        isBuzzerActive.value = false
        currentAlertMedicine.value = null
    }

    private fun addSMSLog(type: SMSLogType, message: String) {
        smsLogs.add(0, SMSLog(type = type, message = message))
    }
}
