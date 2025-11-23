package mm.zamiec.garpom.data.processed

import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.station.PeriodSelection
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphDataRepository @Inject constructor(
) {
    fun getValuesForParameterInTimeRangeAndPeriodSelection(parameter: Parameter, periodSelection: PeriodSelection, start: LocalDateTime, end: LocalDateTime): List<Double> {
        return when (periodSelection) {
            PeriodSelection.AllTime -> getWeeklyValuesForParameterInTimeRange(parameter, start, end)
            PeriodSelection.LastWeek -> getDailyValuesForParameterInTimeRange(parameter, start, end)
            PeriodSelection.Last3Days -> getHourlyValuesForParameterInTimeRange(parameter, start, end)
            PeriodSelection.Last24 -> getHourlyValuesForParameterInTimeRange(parameter, start, end)
        }
    }

    fun getHourlyValuesForParameterInTimeRange(parameter: Parameter, start: LocalDateTime, end: LocalDateTime): List<Double> {
        if (parameter == Parameter.TEMPERATURE)
            return listOf(11.2, 23.4, 23.4, 34.5)
        else
            return listOf(65.4, 87.4, 34.6, 38.3)
    }

    fun getDailyValuesForParameterInTimeRange(parameter: Parameter, start: LocalDateTime, end: LocalDateTime): List<Double> {
        return emptyList()
    }

    fun getWeeklyValuesForParameterInTimeRange(parameter: Parameter, start: LocalDateTime, end: LocalDateTime): List<Double> {
        return emptyList()
    }

    fun getLabelsForPeriodSelectionInTimeRange(periodSelection: PeriodSelection, start: LocalDateTime, end: LocalDateTime): List<String> {
        return listOf("12.04", "12.05", "12.06", "12.07")
    }

    fun getTimeUnitCountForPeriodSelection(periodSelection: PeriodSelection): Int {
        return 4
    }
}