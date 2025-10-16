package mm.zamiec.garpom.data.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.data.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.data.firebase.collectionByIdsAsFlow
import mm.zamiec.garpom.data.firebase.documentAsFlow
import mm.zamiec.garpom.data.dto.AlarmDto
import mm.zamiec.garpom.domain.model.Alarm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor() {
    private val db = Firebase.firestore

    private fun dtoMapper(doc: DocumentSnapshot): AlarmDto? {
        val stationId = doc.getString("station_id") ?: return null
        val active = doc.getBoolean("active") ?: return null

        return AlarmDto(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed alarm",
            description = doc.getString("description") ?: "",
            stationId = stationId,
            active = active,
            conditions = emptyList() // TODO?
        )
    }

    private fun domainMapper(dto: AlarmDto): Alarm? {
        return Alarm(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            stationId = dto.stationId,
            active = dto.active,
            conditions = emptyList() // TODO
        )
    }

    fun getAlarmById(id: String): Flow<Alarm?> =
        db.documentAsFlow("alarms", id, ::dtoMapper, ::domainMapper)

    fun getAlarmsByIdList(ids: List<String>): Flow<List<Alarm>> =
        db.collectionByIdsAsFlow("alarms", ids, ::dtoMapper, ::domainMapper)

    fun getAlarmsByStation(stationId: String): Flow<List<Alarm>> =
        db.filteredCollectionAsFlow("alarms", "station_id", stationId, ::dtoMapper, ::domainMapper)

}