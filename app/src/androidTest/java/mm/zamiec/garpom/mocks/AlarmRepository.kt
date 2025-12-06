package mm.zamiec.garpom.mocks

import kotlinx.coroutines.flow.MutableStateFlow
import mm.zamiec.garpom.data.interfaces.IAlarmRepository
import mm.zamiec.garpom.domain.model.Alarm
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FakeAlarmRepository @Inject constructor() : IAlarmRepository {
    private val alarms = mutableListOf<Alarm>()
    override fun getAlarmById(id: String) = MutableStateFlow(alarms.find { it.id == id })
    override fun getAlarmsByIdList(ids: List<String>) = MutableStateFlow(alarms.filter { it.id in ids })
    override fun getAlarmsByStation(stationId: String) = MutableStateFlow(alarms.filter { it.stations.contains(stationId) })
    override suspend fun saveAlarm(alarm: Alarm, userId: String) {
        alarms.removeAll { it.id == alarm.id }
        alarms.add(alarm)
    }
}