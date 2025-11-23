package mm.zamiec.garpom.data.processed

import kotlinx.coroutines.flow.first
import mm.zamiec.garpom.data.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.domain.model.Average
import mm.zamiec.garpom.domain.model.Measurement
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class MeasurementsFilteredRepository @Inject constructor(
    val measurementRepository: MeasurementRepository
) {
    suspend fun getHourlyMeasurements(stationId: String, start: LocalDateTime, end: LocalDateTime): List<Measurement> {
        return measurementRepository
            .getMeasurementsByStationBetweenDates(stationId, start, end)
            .first()
    }

    suspend fun getDailyMeasurements(stationId: String, start: LocalDateTime, end: LocalDateTime): Map<LocalDate, Average> {
        val dailyAverages: Map<LocalDate, Average> = measurementRepository
            .getMeasurementsByStationBetweenDates(stationId, start, end)
            .first()
            .filter { it.date.hour > 8 && it.date.hour < 18 } // only day values
            .groupBy { it.date.toLocalDate() }
            .mapValues { (_, list) ->
                Average(
                    temperature = list.mapNotNull { it.temperature }.average(),
                    co = list.mapNotNull { it.co }.average(),
                    airHumidity = list.mapNotNull { it.airHumidity }.average(),
                    groundHumidity = list.mapNotNull { it.groundHumidity }.average(),
                    light = list.mapNotNull { it.light }.average(),
                    pressure = list.mapNotNull { it.pressure }.average(),
                    ph = list.mapNotNull { it.ph }.average(),
                )
            }
        return dailyAverages
    }

    suspend fun getWeeklyMeasurements(
        stationId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): Map<LocalDate, Average> {

        return measurementRepository
            .getMeasurementsByStationBetweenDates(stationId, start, end)
            .first()
            .filter { it.date.hour > 8 && it.date.hour < 18 } // only day values
            .groupBy { measurement ->
                measurement.date.toLocalDate()
                    .with(DayOfWeek.MONDAY)
            }
            .mapValues { (_, list) ->
                Average(
                    temperature = list.mapNotNull { it.temperature }.average(),
                    co = list.mapNotNull { it.co }.average(),
                    airHumidity = list.mapNotNull { it.airHumidity }.average(),
                    groundHumidity = list.mapNotNull { it.groundHumidity }.average(),
                    light = list.mapNotNull { it.light }.average(),
                    pressure = list.mapNotNull { it.pressure }.average(),
                    ph = list.mapNotNull { it.ph }.average(),
                )
            }
    }
}