package mm.zamiec.garpom.domain.managers


import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmConditionRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.data.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.data.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.Alarm
import mm.zamiec.garpom.domain.model.AlarmCondition
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.alarm_config.AlarmConfigUiState
import mm.zamiec.garpom.ui.screens.alarm_config.ParameterCardFactory
import mm.zamiec.garpom.ui.screens.alarm_config.ParameterRangeCard
import mm.zamiec.garpom.ui.screens.alarm_config.StationChoice
import mm.zamiec.garpom.ui.screens.alarm_config.getInitialRangesMutableMap
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmConfigManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val alarmRepository: AlarmRepository,
    private val stationRepository: StationRepository,
) {

    suspend fun alarmDetails(alarmId: String): AlarmConfigUiState {
        return try {
            val alarm = alarmRepository
                .getAlarmById(alarmId)
                .firstOrNull()
                ?: return AlarmConfigUiState.Error("Alarm not found!")

            val rangesMap = getInitialRangesMutableMap()

            alarm.conditions.forEach { condition ->
                if (condition.triggerOnHigher) {
                    rangesMap[condition.parameter]?.let {
                        rangesMap[condition.parameter] =
                            it.copy(second = condition.triggerLevel)
                    }
                } else {
                    rangesMap[condition.parameter]?.let {
                        rangesMap[condition.parameter] =
                            it.copy(first = condition.triggerLevel)
                    }
                }
            }

            val stationChoices = stationRepository
                .getStationsByOwner(authRepository.currentUser.value.id)
                .first()
                .map { station ->
                    StationChoice(
                        station.id,
                        station.name,
                        hasThisAlarm = alarm.stations.contains(station.id)
                    )
                }

            Log.d("ConfigMenager", rangesMap.toString())
            return AlarmConfigUiState.ConfigData(
                alarmId = alarm.id,
                createAlarm = alarmId == "",
                alarmName = alarm.name,
                alarmDescription = alarm.description,
                alarmEnabled = alarm.active,
                userStations = stationChoices,
                alarmStart = alarm.startTime,
                alarmEnd = alarm.endTime,
                cards = rangesMap.map{ (parameter, values) ->
                    ParameterCardFactory.create(parameter,
                        values.first,
                        values.second)
                }
            )
        } catch (e: Exception) {
            AlarmConfigUiState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun saveUiState(state: AlarmConfigUiState.ConfigData) {
        val stationIds = state.userStations.mapNotNull { if (it.hasThisAlarm) it.stationId else null }
        val conditions = mutableListOf<AlarmCondition>()
        state.cards.forEach { card: ParameterRangeCard ->
            conditions.add(AlarmCondition(
                parameter = Parameter.entries.find { it.title == card.title } ?: return,
                triggerLevel = card.startValue,
                triggerOnHigher = false,
            ))
            conditions.add(AlarmCondition(
                parameter = Parameter.entries.find { it.title == card.title } ?: return,
                triggerLevel = card.endValue,
                triggerOnHigher = true,
            ))
        }

        val alarm = Alarm(
            id = state.alarmId,
            name = state.alarmName,
            description = state.alarmDescription,
            active = state.alarmEnabled,
            conditions = conditions,
            stations = stationIds,
            startTime = state.alarmStart,
            endTime = state.alarmEnd,
        )
        alarmRepository.saveAlarm(alarm, authRepository.currentUser.value.id)
    }

}