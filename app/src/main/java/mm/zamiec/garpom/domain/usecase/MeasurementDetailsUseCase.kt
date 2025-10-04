package mm.zamiec.garpom.domain.usecase


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.controller.dataRepositories.AlarmConditionRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmOccuranceRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmRepository
import mm.zamiec.garpom.controller.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.model.state.TriggeredAlarm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementDetailsUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val occurrencesRepository: AlarmOccuranceRepository,
    private val alarmRepository: AlarmRepository,
    private val conditionRepository: AlarmConditionRepository,
) {

    suspend fun measurementDetailsSnapshot(measurementId: String): MeasurementScreenState {
        return try {
            val measurement = measurementRepository.getMeasurementById(measurementId).firstOrNull()
                ?: return MeasurementScreenState.Error("Measurement not found!")

            val occurrences = occurrencesRepository
                .getOccurrencesForMeasurement(measurementId)
                .firstOrNull()
                .orEmpty()

            val triggeredAlarms = occurrences.mapNotNull { occurrence ->
                val alarm =
                    alarmRepository
                        .getAlarmById(occurrence.alarmId)
                        .firstOrNull()
                val condition =
                    conditionRepository
                        .getConditionById(occurrence.conditionId, occurrence.alarmId)
                        .firstOrNull()
                if (alarm != null && condition != null) {
                    TriggeredAlarm(alarm.id, alarm.name, condition.parameter)
                } else null
            }

            return MeasurementScreenState.MeasurementData(
                measurement.date,
                measurement.co,
                measurement.humidity,
                measurement.light,
                measurement.pressure,
                measurement.temperature,
                triggeredAlarms
            )
        } catch (e: Exception) {
            MeasurementScreenState.Error(e.message ?: "Unknown error")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun measurementDetails(measurementId: String): Flow<MeasurementScreenState> {
        val measurementFlow = measurementRepository.getMeasurementById(measurementId)
        val occurrencesFlow = occurrencesRepository.getOccurrencesForMeasurement(measurementId)

        return combineTransform(measurementFlow, occurrencesFlow) { measurement, occurrences ->
            if (measurement == null) {
                emit(MeasurementScreenState.Error("Not found"))
                return@combineTransform
            }

            val alarmFlows: List<Flow<TriggeredAlarm?>> = occurrences.map { occurrence ->
                combine(
                    alarmRepository.getAlarmById(occurrence.alarmId),
                    conditionRepository.getConditionById(occurrence.conditionId, occurrence.alarmId)
                ) { alarm, condition ->
                    if (alarm != null && condition != null) {
                        TriggeredAlarm(
                            alarm.id,
                            alarm.name,
                            condition.parameter
                        )
                    } else null
                }
            }

            // Combine them into a list and emit directly
            emitAll(
                combineIntoListOrEmpty(alarmFlows).map { triggeredList ->
                    val triggeredAlarms = triggeredList.filterNotNull()
                    MeasurementScreenState.MeasurementData(
                        measurement.date,
                        measurement.co,
                        measurement.humidity,
                        measurement.light,
                        measurement.pressure,
                        measurement.temperature,
                        triggeredAlarms
                    )
                }
            )
        }
    }

    inline fun <reified T> combineIntoListOrEmpty(flows: List<Flow<T>>): Flow<List<T>> =
        if (flows.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(flows) { it.toList() }
        }
}