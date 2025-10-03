package mm.zamiec.garpom.controller.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import mm.zamiec.garpom.domain.model.StationSummary
import mm.zamiec.garpom.domain.model.dto.AlarmConditionDto
import mm.zamiec.garpom.domain.model.dto.AlarmDto
import mm.zamiec.garpom.domain.model.dto.AlarmOccurrenceDto
import mm.zamiec.garpom.domain.model.dto.StationDto
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirestoreRepository @Inject constructor() {

    private val db = Firebase.firestore

    fun getStationsForUser(userId: String): Flow<List<StationDto>> =
        callbackFlow {
            val listener = db.collection("stations")
                .whereEqualTo("owner_id", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val stations = snapshot?.documents?.map { doc ->
                        StationDto(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unnamed station",
                            ownerId = doc.getString("owner_id")!! // we query by owner_id, fatal error
                        )
                    }.orEmpty()
                    trySend (stations)
                }
            awaitClose { listener.remove() }
        }

    fun getRecentAlarmOccurrences(userId: String): Flow<List<AlarmOccurrenceDto>> =
        callbackFlow {
            val listener = db.collection("alarm_occurrences")
                .whereEqualTo("user_id", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val occurrences = snapshot?.documents?.mapNotNull { doc ->
                        val alarmId = doc.getString("alarm_id") ?: return@mapNotNull null
                        val measurementId =
                            doc.getString("measurement_id") ?: return@mapNotNull null
                        val stationId = doc.getString("station_id") ?: return@mapNotNull null
                        val userId = doc.getString("user_id") ?: return@mapNotNull null
                        val date = doc.getTimestamp("date") ?: return@mapNotNull null
                        AlarmOccurrenceDto(doc.id, alarmId, measurementId, stationId, userId, date)
                    }.orEmpty()

                    trySend(occurrences)
                }

            awaitClose { listener.remove() }
        }

    fun getAlarmsByIds(ids: List<String>): Flow<List<AlarmDto>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("alarms")
            .whereIn(FieldPath.documentId(), ids.take(10))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alarms = snapshot?.documents?.mapNotNull { doc ->
                    val stationId = doc.getString("station_id") ?: return@mapNotNull null
                    val active = doc.getBoolean("active") ?: return@mapNotNull null

                    AlarmDto(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed alarm",
                        description = doc.getString("description") ?: "",
                        stationId = stationId,
                        active = active,
                        conditions = null // populated later
                    )
                }.orEmpty()

                trySend(alarms)
            }

        awaitClose { listener.remove() }
    }

    fun getConditionsForAlarm(alarmId: String): Flow<List<AlarmConditionDto>> = callbackFlow {
        val listener = db.collection("alarms")
            .document(alarmId)
            .collection("conditions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conditions = snapshot?.documents?.mapNotNull { doc ->
                    val parameter = doc.getString("parameter") ?: return@mapNotNull null
                    val triggerLevel =
                        doc.getDouble("triggerLevel")?.toFloat() ?: return@mapNotNull null
                    val triggerOnHigher =
                        doc.getBoolean("triggerOnHigher") ?: return@mapNotNull null
                    AlarmConditionDto(doc.id, parameter, triggerLevel, triggerOnHigher)
                }.orEmpty()

                trySend(conditions)
            }

        awaitClose { listener.remove() }
    }
}
