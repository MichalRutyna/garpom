package mm.zamiec.garpom.controller.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import mm.zamiec.garpom.controller.firebase.documentAsFlow
import mm.zamiec.garpom.controller.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.domain.model.dto.AlarmConditionDto
import mm.zamiec.garpom.domain.model.dto.StationDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.orEmpty

@Singleton
class AlarmConditionRepository @Inject constructor() {
    private val db = Firebase.firestore

    fun mapper(doc: DocumentSnapshot): AlarmConditionDto? {
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

    // TODO?
    fun getConditionById(conditionId: String, alarmId: String): Flow<AlarmConditionDto?> =
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
                        mapper(it)
                    }

                    trySend(item).isSuccess
                }
            awaitClose { listener.remove() }
        }
}
