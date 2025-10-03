package mm.zamiec.garpom.controller.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.controller.firebase.documentAsFlow
import mm.zamiec.garpom.controller.firebase.queryAsFlow
import mm.zamiec.garpom.domain.model.dto.AlarmDto
import mm.zamiec.garpom.domain.model.dto.AlarmOccurrenceDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmOccuranceRepository @Inject constructor() {
    private val db = Firebase.firestore

    fun mapper(doc: DocumentSnapshot): AlarmOccurrenceDto? {
        val alarmId = doc.getString("alarm_id") ?: return null
        val conditionId = doc.getString("condition_id") ?: return null
        val measurementId = doc.getString("measurement_id") ?: return null
        val stationId = doc.getString("station_id") ?: return null
        val userId = doc.getString("user_id") ?: return null
        val date = doc.getTimestamp("date") ?: return null

        return AlarmOccurrenceDto(
            id = doc.id,
            alarmId,
            conditionId,
            measurementId,
            stationId,
            userId,
            date,
        )
    }

    fun getAlarmOccurrenceById(id: String): Flow<AlarmOccurrenceDto?> =
        db.documentAsFlow("alarm_occurrences", id) {doc ->
            mapper(doc)
        }
    fun getRecentAlarmOccurances(userId: String): Flow<List<AlarmOccurrenceDto>> =
        db.queryAsFlow(
            db.collection("alarm_occurrences")
                .whereEqualTo("user_id", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5)
        ) {doc ->
            mapper(doc)
        }
}