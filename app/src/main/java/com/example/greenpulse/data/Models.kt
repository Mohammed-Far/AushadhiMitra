package com.example.greenpulse.data

import java.util.UUID

enum class IntakeType {
    BEFORE_FOOD, AFTER_FOOD, NONE
}

enum class SlotID {
    S1, S2, S3, S4
}

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val slot: SlotID = SlotID.S1,
    val intakeType: IntakeType = IntakeType.NONE,
    val reminderTimes: List<String> = emptyList(), // Store as "HH:mm"
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Sun, 7=Sat
    val dosage: String = "1 pill"
)

data class DoseRecord(
    val id: String = UUID.randomUUID().toString(),
    val medicineId: String = "",
    val medicineName: String = "",
    val slot: SlotID = SlotID.S1,
    val scheduledTime: String = "",
    val actualTime: Long? = null, // null if missed
    val status: DoseStatus = DoseStatus.PENDING
)

enum class DoseStatus {
    PENDING, TAKEN, MISSED
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
    val name: String = "",
    val age: Int = 0,
    val healthNotes: String = ""
)
