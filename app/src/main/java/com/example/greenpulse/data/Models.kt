package com.example.greenpulse.data

import java.util.UUID

enum class IntakeType {
    BEFORE_FOOD, AFTER_FOOD, NONE
}

enum class SlotID {
    S1, S2, S3, S4, S5, S6
}

data class Medicine(
    val id: String = "", // Matches {userId}_med_X
    val tabletName: String = "", // "Tablet 1" to "Tablet 6"
    val userMedicineName: String = "", // User entered name (e.g. "Aspirin")
    val time: String = "", // "HH:mm"
    val slot: SlotID = SlotID.S1,
    val userId: String = "simulated_user_123",
    val createdAt: Long = System.currentTimeMillis()
)

data class DoseRecord(
    val id: String = UUID.randomUUID().toString(),
    val medicineId: String = "",
    val tabletName: String = "", // Grouping key for reports
    val medicineName: String = "",
    val slot: SlotID = SlotID.S1,
    val scheduledTime: String = "",
    val actualTime: Long? = null,
    val status: DoseStatus = DoseStatus.PENDING,
    val sensorDetected: Boolean = true
)

enum class DoseStatus {
    PENDING, DISPENSED, TAKEN, MISSED
}

data class SMSLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: SMSLogType = SMSLogType.REMINDER,
    val message: String = ""
)

enum class SMSLogType {
    REMINDER, CONFIRMATION, WARNING
}

data class PatientProfile(
    val name: String = "John Doe",
    val age: String = "65",
    val gender: String = "Male",
    val bloodGroup: String = "O+",
    val weight: String = "70 kg",
    val healthCondition: String = "Hypertension"
)
