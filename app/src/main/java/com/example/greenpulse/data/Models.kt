package com.example.greenpulse.data

import java.util.UUID

enum class IntakeType {
    BEFORE_FOOD, AFTER_FOOD, NONE
}

enum class SlotID {
    S1, S2, S3, S4, S5, S6
}

enum class ScheduleType {
    EVERY_DAY, EVERY_OTHER_DAY, SPECIFIC_DAYS
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

data class Medicine(
    val id: String = "", 
    val tabletName: String = "", 
    val userMedicineName: String = "", 
    val times: List<String> = emptyList(), // Up to 4 times: ["08:00", "14:00", ...]
    val slot: SlotID = SlotID.S1,
    val userId: String = "",
    val scheduleType: ScheduleType = ScheduleType.EVERY_DAY,
    val selectedDays: List<DayOfWeek> = emptyList(),
    val isActive: Boolean = true, // For cancelling medication
    val createdAt: Long = System.currentTimeMillis()
)

data class DoseRecord(
    val id: String = UUID.randomUUID().toString(),
    val medicineId: String = "",
    val tabletName: String = "", 
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
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val bloodGroup: String = "",
    val weight: String = "",
    val healthCondition: String = "",
    val isSetupComplete: Boolean = false // Track if first-time setup is done
)
