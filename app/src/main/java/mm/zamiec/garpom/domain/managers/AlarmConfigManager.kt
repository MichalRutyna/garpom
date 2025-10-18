package mm.zamiec.garpom.domain.managers


import androidx.compose.runtime.toMutableStateMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmConditionRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.data.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.data.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.alarm_config.AlarmConfigUiState
import mm.zamiec.garpom.ui.screens.alarm_config.ParameterCardFactory
import mm.zamiec.garpom.ui.screens.alarm_config.ParameterRangeCard
import mm.zamiec.garpom.ui.screens.alarm_config.StationChoice
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmConfigManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val measurementRepository: MeasurementRepository,
    private val occurrencesRepository: AlarmOccurrenceRepository,
    private val alarmRepository: AlarmRepository,
    private val conditionRepository: AlarmConditionRepository,
    private val stationRepository: StationRepository,
) {

    suspend fun alarmDetails(alarmId: String): AlarmConfigUiState {
        return try {
            val alarm = alarmRepository
                .getAlarmById(alarmId)
                .firstOrNull()
                ?: return AlarmConfigUiState.Error("Alarm not found!")

            val rangesMap = listOf(
                Parameter.TEMPERATURE to (Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY),
                    Parameter.AIR_HUMIDITY to (Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY),
                    Parameter.CO to (Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY),
                    Parameter.GROUND_HUMIDITY to (Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY),
                    Parameter.LIGHT to (Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY),
                    Parameter.PH to (Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY),
                    Parameter.PRESSURE to (Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY),
            ).toMap().toMutableMap()

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


            return AlarmConfigUiState.ConfigData(
                alarmId = alarm.id,
                createAlarm = alarmId == "",
                alarmName = alarm.name,
                alarmActive = alarm.active,
                userStations = stationChoices,
                alarmStart = Date.from(Instant.now()), // TODO()
                alarmEnd = Date.from(Instant.now()), // TODO()
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
}