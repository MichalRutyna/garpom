package mm.zamiec.garpom.data.processed

import com.google.type.DateTime
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.data.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.station.PeriodSelection
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphDataRepository @Inject constructor(
) {

    fun getValuesForParameterInTimeRange(parameter: Parameter, start: LocalDateTime, end: LocalDateTime): List<Double> {
        return emptyList()
    }

    fun getLabelsForPeriodSelectionInTimeRange(periodSelection: PeriodSelection, start: LocalDateTime, end: LocalDateTime): List<String> {
        return emptyList()
    }
}