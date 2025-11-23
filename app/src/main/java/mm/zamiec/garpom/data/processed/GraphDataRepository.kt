package mm.zamiec.garpom.data.processed

import android.util.Log
import mm.zamiec.garpom.domain.model.Average
import mm.zamiec.garpom.domain.model.Measurement
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.station.PeriodSelection
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class GraphDataRepository @Inject constructor(
    val measurementsFilteredRepository: MeasurementsFilteredRepository
) {
    val dayFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
    val hourFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.getDefault())

    suspend fun getResponseForParameterInTimeRangeAndPeriodSelection(stationId: String, periodSelection: PeriodSelection, start: LocalDateTime, end: LocalDateTime): RepositoryResponse {
        return when (periodSelection) {
            PeriodSelection.AllTime -> getWeeklyResponseForParameterInTimeRange(stationId, start, end)
            PeriodSelection.LastWeek -> getDailyResponseForParameterInTimeRange(stationId, start, end)
            PeriodSelection.Last3Days -> getHourlyResponseForParameterInTimeRange(stationId, start, end)
            PeriodSelection.Last24 -> getHourlyResponseForParameterInTimeRange(stationId, start, end)
        }
    }

    private suspend fun getHourlyResponseForParameterInTimeRange(stationId: String, start: LocalDateTime, end: LocalDateTime): RepositoryResponse {
        val measurements: List<Measurement> =
            measurementsFilteredRepository
                .getHourlyMeasurements(stationId, start, end)
        val dates = measurements.map {it.date}
        val newValues: Map<Parameter, List<Double>> =
            Parameter.entries.associateWith { parameter ->
                measurements.mapNotNull { measurement ->
                    when (parameter) {
                        Parameter.TEMPERATURE -> measurement.temperature
                        Parameter.AIR_HUMIDITY -> measurement.airHumidity
                        Parameter.CO -> measurement.co
                        Parameter.GROUND_HUMIDITY -> measurement.groundHumidity
                        Parameter.LIGHT -> measurement.light
                        Parameter.PH -> measurement.ph
                        Parameter.PRESSURE -> measurement.pressure
                    }
                }
            }

        val n = measurements.size
        val labelCount = min(n, 6)
        val labels =
            rangePoints(n, labelCount)
                .map {
                    dates.elementAt(it)
                        .format(hourFormatter)
                }

        val response = RepositoryResponse(
            values = newValues,
            labels = labels,
            count = n
        )
        return response
    }

    private suspend fun getDailyResponseForParameterInTimeRange(stationId: String, start: LocalDateTime, end: LocalDateTime): RepositoryResponse {
        val measurements: Map<LocalDate, Average> =
            measurementsFilteredRepository
                .getDailyMeasurements(stationId, start, end)
        val dates = measurements.keys.toList()
        val newValues: Map<Parameter, List<Double>> =
            Parameter.entries.associateWith { parameter ->
                measurements.mapNotNull { (date, average) ->
                    when (parameter) {
                        Parameter.TEMPERATURE -> average.temperature
                        Parameter.AIR_HUMIDITY -> average.airHumidity
                        Parameter.CO -> average.co
                        Parameter.GROUND_HUMIDITY -> average.groundHumidity
                        Parameter.LIGHT -> average.light
                        Parameter.PH -> average.ph
                        Parameter.PRESSURE -> average.pressure
                    }
                }
            }

        Log.d("GraphDataRepository", dates.toString())
        Log.d("GraphDataRepository", measurements.toString())
        val n = measurements.size
        val labelCount = min(n, 6)
        Log.d("GraphDataRepository", labelCount.toString())
        val labels =
            rangePoints(n, labelCount)
                .map {
                    dates.elementAt(it)
                        .format(dayFormatter)
                }

        val response = RepositoryResponse(
            values = newValues,
            labels = labels,
            count = n
        )
        return response
    }

    private suspend fun getWeeklyResponseForParameterInTimeRange(stationId: String, start: LocalDateTime, end: LocalDateTime):RepositoryResponse {
        val measurements: Map<LocalDate, Average> =
            measurementsFilteredRepository
                .getWeeklyMeasurements(stationId, start, end)
        val dates = measurements.keys.toList()
        val newValues: Map<Parameter, List<Double>> =
            Parameter.entries.associateWith { parameter ->
                measurements.mapNotNull { (date, average) ->
                    when (parameter) {
                        Parameter.TEMPERATURE -> average.temperature
                        Parameter.AIR_HUMIDITY -> average.airHumidity
                        Parameter.CO -> average.co
                        Parameter.GROUND_HUMIDITY -> average.groundHumidity
                        Parameter.LIGHT -> average.light
                        Parameter.PH -> average.ph
                        Parameter.PRESSURE -> average.pressure
                    }
                }
            }

        val n = measurements.size
        val labelCount = min(n, 6)
        val labels =
            rangePoints(n, labelCount)
                .map {
                    dates.elementAt(it)
                        .format(dayFormatter)
                }

        val response = RepositoryResponse(
            values = newValues,
            labels = labels,
            count = n
        )
        return response
    }

    fun rangePoints(total: Int, parts: Int): List<Int> {
        if (parts == 0) {
            return emptyList()
        }
        val step = total.toDouble() / parts
        return (0..< parts).map { (it * step).toInt() }
    }
}

data class RepositoryResponse(
    val values: Map<Parameter, List<Double>>,
    val labels: List<String>,
    val count: Int, // only used in initial graph
)