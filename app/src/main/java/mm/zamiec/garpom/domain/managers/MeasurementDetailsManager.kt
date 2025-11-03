package mm.zamiec.garpom.domain.managers


import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import mm.zamiec.garpom.data.dataRepositories.AlarmConditionRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.data.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.data.dataRepositories.StationRepository
import mm.zamiec.garpom.ui.screens.measurement.components.FireCard
import mm.zamiec.garpom.ui.screens.measurement.components.MeasurementCardFactory
import mm.zamiec.garpom.ui.screens.measurement.MeasurementScreenState
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.measurement.TriggeredAlarm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementDetailsManager @Inject constructor(
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
                    Log.d("HERE", "alarm par: "+condition.parameter)
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
                    Parameter.TEMPERATURE to measurement.temperature,
                    Parameter.AIR_HUMIDITY to measurement.airHumidity,
                    Parameter.CO to measurement.co,
                    Parameter.GROUND_HUMIDITY to measurement.groundHumidity,
                    Parameter.LIGHT to measurement.light,
                    Parameter.PH to measurement.ph,
                    Parameter.PRESSURE to measurement.pressure
                ).map { (type, level) ->
                    MeasurementCardFactory.create(type, level, occurrenceMap[type] ?: emptyList())
                },
                FireCard(measurement.fire)
            )
        } catch (e: Exception) {
            MeasurementScreenState.Error(e.message ?: "Unknown error")
        }
    }
}