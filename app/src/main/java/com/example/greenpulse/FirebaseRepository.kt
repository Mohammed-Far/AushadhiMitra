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

    // ─── MEDICINES ───────────────────────────────────────────

    suspend fun saveMedicine(medicine: Medicine) {
        val docId = "${userId}_${medicine.slot.name}"
        db.collection("medicines").document(docId)
            .set(mapOf(
                "id" to docId,
                "tabletName" to medicine.tabletName,
                "userMedicineName" to medicine.userMedicineName,
                "time" to medicine.time,
                "slot" to medicine.slot.name,
                "userId" to userId,
                "createdAt" to medicine.createdAt,
                "taken" to false
            )).await()
    }

    suspend fun loadMedicines(): List<Medicine> {
        val snapshot = db.collection("medicines")
            .whereEqualTo("userId", userId)
            .get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                Medicine(
                    id = doc.getString("id") ?: "",
                    tabletName = doc.getString("tabletName") ?: "",
                    userMedicineName = doc.getString("userMedicineName") ?: "",
                    time = doc.getString("time") ?: "",
                    slot = SlotID.valueOf(doc.getString("slot") ?: "S1"),
                    userId = doc.getString("userId") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } catch (e: Exception) { null }
        }
    }

    suspend fun clearMedicine(slot: SlotID) {
        val docId = "${userId}_${slot.name}"
        db.collection("medicines").document(docId)
            .update(mapOf(
                "userMedicineName" to "",
                "time" to "",
                "taken" to false
            )).await()
    }

    // Listen for hardware dispensing in real time
    fun listenForDispensed(
        slot: SlotID,
        onDispensed: () -> Unit
    ) {
        val docId = "${userId}_${slot.name}"
        db.collection("medicines").document(docId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val taken = snapshot.getBoolean("taken") ?: false
                    if (taken) onDispensed()
                }
            }
    }

    // ─── DOSE RECORDS (ADHERENCE) ─────────────────────────────

    suspend fun saveDoseRecord(record: DoseRecord) {
        db.collection("adherence").document(record.id)
            .set(mapOf(
                "id" to record.id,
                "medicineId" to record.medicineId,
                "tabletName" to record.tabletName,
                "medicineName" to record.medicineName,
                "slot" to record.slot.name,
                "scheduledTime" to record.scheduledTime,
                "actualTime" to record.actualTime,
                "status" to record.status.name,
                "userId" to userId,
                "date" to java.time.LocalDate.now().toString()
            )).await()
    }

    suspend fun updateDoseStatus(recordId: String, status: DoseStatus) {
        db.collection("adherence").document(recordId)
            .update(mapOf(
                "status" to status.name,
                "actualTime" to if (status == DoseStatus.TAKEN)
                    System.currentTimeMillis() else null
            )).await()
    }

    suspend fun loadTodayDoseRecords(): List<DoseRecord> {
        val today = java.time.LocalDate.now().toString()
        val snapshot = db.collection("adherence")
            .whereEqualTo("userId", userId)
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