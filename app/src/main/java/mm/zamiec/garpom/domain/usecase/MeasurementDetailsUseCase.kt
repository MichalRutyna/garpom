package mm.zamiec.garpom.domain.usecase


import kotlinx.coroutines.flow.firstOrNull
import mm.zamiec.garpom.controller.dataRepositories.AlarmConditionRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmRepository
import mm.zamiec.garpom.controller.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.controller.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.state.FireCard
import mm.zamiec.garpom.domain.model.state.MeasurementCardFactory
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.model.state.MeasurementType
import mm.zamiec.garpom.domain.model.state.TriggeredAlarm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementDetailsUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val occurrencesRepository: AlarmOccurrenceRepository,
    private val alarmRepository: AlarmRepository,
    private val conditionRepository: AlarmConditionRepository,
    private val stationRepository: StationRepository,
) {

    suspend fun measurementDetailsSnapshot(measurementId: String): MeasurementScreenState {
        return try {
            val measurement = measurementRepository
                .getMeasurementById(measurementId)
                .firstOrNull()
                ?: return MeasurementScreenState.Error("Measurement not found!")

            val occurrences = occurrencesRepository
                .getOccurrencesForMeasurement(measurementId)
                .firstOrNull()
                .orEmpty()

            val station = stationRepository
                .getStationById(measurement.stationId)
                .firstOrNull()
                ?: return MeasurementScreenState.Error("Measurement without station!")
            val occurrenceMap = occurrences.mapNotNull { occurrence ->
                val alarm =
                    alarmRepository
                        .getAlarmById(occurrence.alarmId)
                        .firstOrNull()
                val condition =
                    conditionRepository
                        .getConditionById(occurrence.conditionId, occurrence.alarmId)
                        .firstOrNull()
                if (alarm != null && condition != null) {
                    condition.parameter to TriggeredAlarm(
                        alarm.id,
                        alarm.name
                    )
                } else null
            }.groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )

            return MeasurementScreenState.MeasurementData(
                station.name,
                measurement.stationId,
                measurement.date,
                cards = listOf(
                    MeasurementType.TEMPERATURE to measurement.temperature,
                    MeasurementType.AIR_HUMIDITY to measurement.airHumidity,
                    MeasurementType.CO to measurement.co,
                    MeasurementType.GROUND_HUMIDITY to measurement.groundHumidity,
                    MeasurementType.LIGHT to measurement.light,
                    MeasurementType.PH to measurement.ph,
                    MeasurementType.PRESSURE to measurement.pressure
                ).map { (type, level) ->
                    MeasurementCardFactory.create(type, level, occurrenceMap[type.dbName] ?: emptyList())
                },
                FireCard(measurement.fire)
            )
        } catch (e: Exception) {
            MeasurementScreenState.Error(e.message ?: "Unknown error")
        }
    }
}