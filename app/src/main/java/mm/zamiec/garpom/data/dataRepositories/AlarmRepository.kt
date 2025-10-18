package mm.zamiec.garpom.data.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mm.zamiec.garpom.data.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.data.firebase.collectionByIdsAsFlow
import mm.zamiec.garpom.data.firebase.documentAsFlow
import mm.zamiec.garpom.data.dto.AlarmDto
import mm.zamiec.garpom.data.firebase.filteredArrayContainsCollectionAsFlow
import mm.zamiec.garpom.domain.model.Alarm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmConditionRepository: AlarmConditionRepository
) {
    private val db = Firebase.firestore

    private fun dtoMapper(doc: DocumentSnapshot): AlarmDto? {
        val stationsAny = doc.get("stations") as? List<*>
        val stations: List<String> = stationsAny?.mapNotNull { it as? String } ?: emptyList()
        val active = doc.getBoolean("active") ?: return null

        return AlarmDto(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed alarm",
            description = doc.getString("description") ?: "",
            stations = stations,
            active = active,
        )
    }

    private fun domainMapper(dto: AlarmDto): Alarm? {
        return Alarm(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            active = dto.active,
            conditions = emptyList(),
            stations = dto.stations,
        )
    }

    fun getAlarmById(id: String): Flow<Alarm?> {
        val alarms = db.documentAsFlow("alarms", id, ::dtoMapper, ::domainMapper)
        val conditionsFlow = alarmConditionRepository.getConditionsByAlarm(id)
        return combine(alarms, conditionsFlow) { alarm, conditions ->
            alarm?.copy(conditions = conditions)
        }
    }

    fun getAlarmsByIdList(ids: List<String>): Flow<List<Alarm>> {
       return db.collectionByIdsAsFlow("alarms", ids, ::dtoMapper, ::domainMapper)
            .map { alarms ->
                 alarms.map { alarm ->
                     val condition = alarmConditionRepository.getConditionsByAlarm(alarm.id).firstOrNull().orEmpty()
                    alarm.copy(conditions = condition)
                }
            }
    }


    fun getAlarmsByStation(stationId: String): Flow<List<Alarm>> {
        return db.filteredArrayContainsCollectionAsFlow("alarms", "stations", stationId, ::dtoMapper, ::domainMapper)
            .map { alarms ->
                alarms.map { alarm ->
                    val condition = alarmConditionRepository.getConditionsByAlarm(alarm.id).firstOrNull().orEmpty()
                    alarm.copy(conditions = condition)
                }
            }
    }

}