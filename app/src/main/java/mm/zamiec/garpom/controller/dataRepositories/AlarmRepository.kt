package mm.zamiec.garpom.controller.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.controller.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.controller.firebase.collectionByIdsAsFlow
import mm.zamiec.garpom.controller.firebase.documentAsFlow
import mm.zamiec.garpom.domain.model.dto.AlarmDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor() {
    private val db = Firebase.firestore

    fun mapper(doc: DocumentSnapshot): AlarmDto? {
        val stationId = doc.getString("station_id") ?: return null
        val active = doc.getBoolean("active") ?: return null

        return AlarmDto(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed alarm",
            description = doc.getString("description") ?: "",
            stationId = stationId,
            active = active,
            conditions = null // TODO?
        )
    }

    fun getAlarmById(id: String): Flow<AlarmDto?> =
        db.documentAsFlow("alarms", id, ::mapper)

    fun getAlarmsByIdList(ids: List<String>): Flow<List<AlarmDto>> =
        db.collectionByIdsAsFlow("alarms", ids, ::mapper)

    fun getAlarmsByStation(stationId: String): Flow<List<AlarmDto>> =
        db.filteredCollectionAsFlow("alarms", "station_id", stationId, ::mapper)

}