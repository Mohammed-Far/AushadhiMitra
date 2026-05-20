package com.example.greenpulse

import com.example.greenpulse.data.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val userId get() = auth.currentUser?.uid ?: "unknown"

    private fun userMedicines() = db.collection("users").document(userId).collection("medicines")

    // ─── MEDICINES ───────────────────────────────────────────

    suspend fun saveMedicine(medicine: Medicine) {
        if (userId == "unknown") return
        userMedicines().document(medicine.slot.name)
            .set(mapOf(
                "tabletName" to medicine.tabletName,
                "userMedicineName" to medicine.userMedicineName,
                "times" to medicine.times,
                "slot" to medicine.slot.name,
                "scheduleType" to medicine.scheduleType.name,
                "selectedDays" to medicine.selectedDays.map { it.name },
                "isActive" to medicine.isActive,
                "createdAt" to medicine.createdAt
            )).await()
    }

    suspend fun loadMedicines(): List<Medicine> {
        if (userId == "unknown") return emptyList()
        val snapshot = userMedicines().get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                Medicine(
                    id = doc.id,
                    tabletName = doc.getString("tabletName") ?: "",
                    userMedicineName = doc.getString("userMedicineName") ?: "",
                    times = (doc.get("times") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    slot = SlotID.valueOf(doc.getString("slot") ?: "S1"),
                    userId = userId,
                    scheduleType = ScheduleType.valueOf(doc.getString("scheduleType") ?: "EVERY_DAY"),
                    selectedDays = (doc.get("selectedDays") as? List<*>)?.mapNotNull {
                        try { DayOfWeek.valueOf(it as String) } catch (e: Exception) { null }
                    } ?: emptyList(),
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } catch (e: Exception) { null }
        }
    }

    suspend fun clearMedicine(slot: SlotID) {
        if (userId == "unknown") return
        userMedicines().document(slot.name)
            .update(mapOf(
                "userMedicineName" to "",
                "times" to emptyList<String>(),
                "isActive" to false
            )).await()
    }

    fun listenForDispensed(slot: SlotID, onDispensed: () -> Unit) {
        if (userId == "unknown") return
        userMedicines().document(slot.name)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val taken = snapshot.getBoolean("taken") ?: false
                    if (taken) onDispensed()
                }
            }
    }

    // ─── DOSE RECORDS (ADHERENCE) ─────────────────────────────

    suspend fun saveDoseRecord(record: DoseRecord) {
        if (userId == "unknown") return
        db.collection("users").document(userId).collection("adherence")
            .document(record.id)
            .set(mapOf(
                "id" to record.id,
                "medicineId" to record.medicineId,
                "tabletName" to record.tabletName,
                "medicineName" to record.medicineName,
                "slot" to record.slot.name,
                "scheduledTime" to record.scheduledTime,
                "actualTime" to record.actualTime,
                "status" to record.status.name,
                "sensorDetected" to record.sensorDetected,
                "date" to java.time.LocalDate.now().toString()
            )).await()
    }

    suspend fun updateDoseStatus(recordId: String, status: DoseStatus) {
        if (userId == "unknown") return
        db.collection("users").document(userId).collection("adherence")
            .document(recordId)
            .update(mapOf(
                "status" to status.name,
                "actualTime" to if (status == DoseStatus.TAKEN)
                    System.currentTimeMillis() else null
            )).await()
    }

    suspend fun loadTodayDoseRecords(): List<DoseRecord> {
        if (userId == "unknown") return emptyList()
        val today = java.time.LocalDate.now().toString()
        val snapshot = db.collection("users").document(userId).collection("adherence")
            .whereEqualTo("date", today)
            .get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                DoseRecord(
                    id = doc.getString("id") ?: "",
                    medicineId = doc.getString("medicineId") ?: "",
                    tabletName = doc.getString("tabletName") ?: "",
                    medicineName = doc.getString("medicineName") ?: "",
                    slot = SlotID.valueOf(doc.getString("slot") ?: "S1"),
                    scheduledTime = doc.getString("scheduledTime") ?: "",
                    actualTime = doc.getLong("actualTime"),
                    status = DoseStatus.valueOf(doc.getString("status") ?: "PENDING"),
                    sensorDetected = doc.getBoolean("sensorDetected") ?: true
                )
            } catch (e: Exception) { null }
        }
    }

    // ─── PROFILE ─────────────────────────────────────────────

    suspend fun loadProfile(): PatientProfile? {
        if (userId == "unknown") return null
        val snapshot = db.collection("users").document(userId).get().await()
        return if (snapshot.exists()) {
            PatientProfile(
                name = snapshot.getString("name") ?: "",
                age = snapshot.getString("age") ?: "",
                gender = snapshot.getString("gender") ?: "",
                bloodGroup = snapshot.getString("bloodGroup") ?: "",
                weight = snapshot.getString("weight") ?: "",
                healthCondition = snapshot.getString("healthCondition") ?: "",
                // ✅ If doc exists and name is filled, treat as setup complete
                isSetupComplete = snapshot.getBoolean("isSetupComplete")
                    ?: (snapshot.getString("name")?.isNotBlank() == true)
            )
        } else null
    }

    suspend fun saveProfile(profile: PatientProfile) {
        if (userId == "unknown") return
        // ✅ Use merge so we don't overwrite uid, email, fcmToken etc
        db.collection("users").document(userId)
            .set(
                mapOf(
                    "name" to profile.name,
                    "age" to profile.age,
                    "gender" to profile.gender,
                    "bloodGroup" to profile.bloodGroup,
                    "weight" to profile.weight,
                    "healthCondition" to profile.healthCondition,
                    "isSetupComplete" to true  // ✅ Always true when saving profile
                ),
                com.google.firebase.firestore.SetOptions.merge() // ✅ Merge not overwrite
            ).await()
    }

    // ─── FCM TOKEN ───────────────────────────────────────────

    suspend fun saveFCMToken(token: String) {
        if (userId != "unknown") {
            db.collection("users").document(userId)
                .update("fcmToken", token).await()
        }
    }
}