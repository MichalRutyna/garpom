package mm.zamiec.garpom.data.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import mm.zamiec.garpom.data.dto.AlarmConditionDto
import mm.zamiec.garpom.domain.model.AlarmCondition
import mm.zamiec.garpom.domain.model.Parameter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmConditionRepository @Inject constructor() {
    private val db = Firebase.firestore

    private fun dtoMapper(doc: DocumentSnapshot): AlarmConditionDto? {
        val parameter: String = doc.getString("parameter") ?: return null
        val triggerLevel: Double = doc.getDouble("trigger_level") ?: return null
        val triggerOnHigher: Boolean = doc.getBoolean("trigger_on_higher") ?: return null

        return AlarmConditionDto(
            doc.id,
            parameter,
            triggerLevel,
            triggerOnHigher
        )
    }

    private fun domainMapper(dto: AlarmConditionDto): AlarmCondition? {
        val type = Parameter.entries.find { it.dbName == dto.parameter } ?: return null

        return AlarmCondition(
            id = dto.id,
            parameter = type,
            triggerLevel = dto.triggerLevel,
            triggerOnHigher = dto.triggerOnHigher,
        )
    }

    // TODO?
    fun getConditionById(conditionId: String, alarmId: String): Flow<AlarmCondition?> =
        callbackFlow {
            val listener: ListenerRegistration = db.collection("alarms")
                .document(alarmId)
                .collection("conditions")
                .document(conditionId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val item = snapshot?.let {
                        dtoMapper(it)
                    }
                        ?.let {
                            domainMapper(it)
                        }

                    trySend(item).isSuccess
                }
            awaitClose { listener.remove() }
        }

    fun getConditionsByAlarm(alarmId: String): Flow<List<AlarmCondition>> =
        callbackFlow {
            val listener: ListenerRegistration = db.collection("alarms")
                .document(alarmId)
                .collection("conditions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val items = snapshot?.documents
                        ?.mapNotNull { dtoMapper(it) }
                        ?.mapNotNull { domainMapper(it) }
                        .orEmpty()

                    trySend(items).isSuccess
                }
            awaitClose { listener.remove() }
        }
}
