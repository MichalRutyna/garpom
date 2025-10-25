package mm.zamiec.garpom.data.dataRepositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import mm.zamiec.garpom.data.dto.AlarmConditionDto
import mm.zamiec.garpom.data.firebase.collectionByIdsAsFlow
import mm.zamiec.garpom.data.firebase.documentAsFlow
import mm.zamiec.garpom.data.dto.AlarmDto
import mm.zamiec.garpom.data.firebase.filteredArrayContainsCollectionAsFlow
import mm.zamiec.garpom.domain.model.Alarm
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmConditionRepository: AlarmConditionRepository
) {
    companion object {
        private val TAG = "AlarmRepository"
    }
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
            user_id = doc.getString("user_id") ?: "",
            start_hour = doc.getLong("start_hour") ?: 0,
            start_minute = doc.getLong("start_minute") ?: 0,
            end_hour = doc.getLong("end_hour") ?: 24,
            end_minute = doc.getLong("end_minute") ?: 0,
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
            startTime = Calendar.Builder()
                .setTimeOfDay(dto.start_hour.toInt(), dto.start_minute.toInt(), 0)
                .build(),
            endTime = Calendar.Builder()
                .setTimeOfDay(dto.end_hour.toInt(), dto.end_minute.toInt(), 0)
                .build(),
        )
    }

    private fun domainToDto(alarm: Alarm, userId: String) : AlarmDto {
        return AlarmDto(
            id = alarm.id,
            active = alarm.active,
            description = alarm.description,
            name = alarm.name,
            stations = alarm.stations,
            user_id = userId,
            start_hour = alarm.startTime.get(Calendar.HOUR_OF_DAY).toLong(),
            start_minute = alarm.startTime.get(Calendar.MINUTE).toLong(),
            end_hour = alarm.endTime.get(Calendar.HOUR_OF_DAY).toLong(),
            end_minute = alarm.endTime.get(Calendar.MINUTE).toLong(),
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

    suspend fun saveAlarm(alarm: Alarm, userId: String) {
        val conditions = alarm.conditions
        val alarmDto = domainToDto(alarm, userId)

        val alarmRef: DocumentReference
        if (alarmDto.id == "") {
            alarmRef = db.collection("alarms").document()
        } else {
            alarmRef = db.collection("alarms").document(alarmDto.id)
        }

        alarmRef.set(alarmDto)
            .addOnSuccessListener {
                Log.d(TAG, "Added alarm docuemnt")
            }

        for (doc in alarmRef.collection("conditions").get().await().documents) {
            doc.reference.delete()
        }
        val conditionsRef = alarmRef.collection("conditions")
        val conditionsDtos = conditions.map { alarmConditionRepository.conditionDomainToDto(it) }
        conditionsDtos.forEach { conditionsRef.add(it) }
    }

}