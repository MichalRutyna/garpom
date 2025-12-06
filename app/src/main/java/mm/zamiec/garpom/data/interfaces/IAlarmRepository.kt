package mm.zamiec.garpom.data.interfaces

import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.domain.model.Alarm

interface IAlarmRepository {

    fun getAlarmById(id: String): Flow<Alarm?>
    fun getAlarmsByIdList(ids: List<String>): Flow<List<Alarm>>
    fun getAlarmsByStation(stationId: String): Flow<List<Alarm>>
    suspend fun saveAlarm(alarm: Alarm, userId: String)
}
