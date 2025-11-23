package mm.zamiec.garpom.domain.managers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import mm.zamiec.garpom.data.processed.GraphDataRepository
import mm.zamiec.garpom.data.processed.RepositoryResponse
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.station.GraphData
import mm.zamiec.garpom.ui.screens.station.PeriodSelection
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.mutableListOf
import kotlin.math.max

@Singleton
class GraphDataManager @Inject constructor(
    private val graphDataRepository: GraphDataRepository
) {
    final val LOCAL_EPOCH = LocalDateTime.of(2025, 1, 1, 0, 0)
    suspend fun initialGraph(stationId: String, period: PeriodSelection, enabledParameters: Set<Parameter>): GraphData {
        val rangeStart: LocalDateTime =
            getInitialTimeRangeStartInPeriodSelection(period)
        val rangeEnd: LocalDateTime =
            LocalDateTime.now()

        val repositoryResponse = graphDataRepository
            .getResponseForParameterInTimeRangeAndPeriodSelection(
                stationId, period, rangeStart, rangeEnd
            )
        val timeUnitCount: Int =
            repositoryResponse.count
        val initialTimeRangeStart: Int =
            getStartValueOfInitialTimeRangeInPeriodSelection(period, timeUnitCount)

        val x = when (period) {
            PeriodSelection.AllTime -> ChronoUnit.WEEKS.between(LOCAL_EPOCH, LocalDateTime.now()).toInt()
            PeriodSelection.LastWeek -> 7
            PeriodSelection.Last3Days -> 56
            PeriodSelection.Last24 -> 24
        }

        val data = GraphData(
            selectedPeriod = period,
            storedPeriod = period,
            enabledParameters = enabledParameters,
            graphTimeRange = 0f..x.toFloat(), // 0-indexed
            graphActiveTimeRange = initialTimeRangeStart.toFloat()..x.toFloat(),
            timeRangeSteps = x-1, // allows x values evenly distributed
        )
        return updateLinesAndLabels(stationId,data, repositoryResponse)
    }

    suspend fun updateData(stationId: String, graphData: GraphData): GraphData {
        // period selector changed
        if (graphData.selectedPeriod != graphData.storedPeriod)
            return initialGraph(stationId, graphData.selectedPeriod, graphData.enabledParameters)

        return updateLinesAndLabels(stationId, graphData)
    }

    private suspend fun updateLinesAndLabels(stationId: String, graphData: GraphData, repositoryResponse: RepositoryResponse? = null): GraphData {
        val (rangeStart: LocalDateTime, rangeEnd: LocalDateTime) =
            getTimeRangeFromTimeSliderInPeriodSelection(graphData.selectedPeriod, graphData.graphActiveTimeRange)

        val data = repositoryResponse
            ?: graphDataRepository // if not called from initialGraph
            .getResponseForParameterInTimeRangeAndPeriodSelection(
                stationId, graphData.selectedPeriod, rangeStart, rangeEnd
            )

        val newLines = mutableListOf<Line>()
        newLines.addAll(
            graphData.enabledParameters
                .map {
                    getLineForParameter(it)
                        .copy(
                            values = data.values[it] ?: emptyList(),
                            label = it.title,
                            strokeProgress = Animatable(0f), gradientProgress = Animatable(0f) // reset line animation
                        )
                }
        )

        return graphData.copy(
            lines = newLines,
            graphTimeLabels = data.labels,
        )
    }

    private fun getTimeRangeFromTimeSliderInPeriodSelection(periodSelection: PeriodSelection, timeRange: ClosedFloatingPointRange<Float>): Pair<LocalDateTime, LocalDateTime> {
        val firstDate: LocalDateTime
        val rangeStart: LocalDateTime
        val rangeEnd: LocalDateTime

        when (periodSelection) {
            PeriodSelection.AllTime -> {
                firstDate = LOCAL_EPOCH
                rangeStart = firstDate.plusWeeks(timeRange.start.toLong())
                rangeEnd = firstDate.plusWeeks(timeRange.endInclusive.toLong()+1) // include this week
            }

            PeriodSelection.LastWeek -> {
                firstDate = LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0)
                rangeStart = firstDate.plusDays(timeRange.start.toLong())
                rangeEnd = firstDate.plusDays(timeRange.endInclusive.toLong())
            }

            PeriodSelection.Last3Days -> {
                firstDate = LocalDateTime.now().minusDays(3).withHour(0).withMinute(0)
                rangeStart = firstDate.plusHours(timeRange.start.toLong())
                rangeEnd = firstDate.plusHours(timeRange.endInclusive.toLong())
            }

            PeriodSelection.Last24 -> {
                firstDate = LocalDateTime.now().minusHours(24).withMinute(0)
                rangeStart = firstDate.plusHours(timeRange.start.toLong())
                rangeEnd = firstDate.plusHours(timeRange.endInclusive.toLong())
            }
        }
        return Pair(rangeStart, rangeEnd)
    }

    private fun getInitialTimeRangeStartInPeriodSelection(periodSelection: PeriodSelection): LocalDateTime {
        return when (periodSelection) {
            PeriodSelection.AllTime ->
                LocalDateTime.now().minusYears(1)
            PeriodSelection.LastWeek ->
                LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0)
            PeriodSelection.Last3Days ->
                LocalDateTime.now().minusDays(3).withHour(0).withMinute(0)
            PeriodSelection.Last24 ->
                LocalDateTime.now().minusHours(24).withMinute(0)
        }
    }

    private fun getStartValueOfInitialTimeRangeInPeriodSelection(periodSelection: PeriodSelection, timeUnitCount: Int): Int {
        val initialDateTime: LocalDateTime =
            getInitialTimeRangeStartInPeriodSelection(periodSelection)
        val now = LocalDateTime.now()
        val timeUnitDifference =
            when (periodSelection) {
                PeriodSelection.AllTime ->
                    ChronoUnit.WEEKS.between(initialDateTime, now)
                PeriodSelection.LastWeek ->
                    ChronoUnit.DAYS.between(initialDateTime, now)
                PeriodSelection.Last3Days ->
                    ChronoUnit.HOURS.between(initialDateTime, now)
                PeriodSelection.Last24 ->
                    ChronoUnit.HOURS.between(initialDateTime, now)
            }

        return max(
            timeUnitCount - timeUnitDifference.toInt(),
            0
        )
    }

    private fun getLineForParameter(parameter: Parameter): Line {
        val color: Color =
            when(parameter) {
                Parameter.TEMPERATURE -> Color(0xFFE15759)
                Parameter.AIR_HUMIDITY -> Color(0xFF76B7B2)
                Parameter.CO -> Color(0xFFB07AA1)
                Parameter.GROUND_HUMIDITY -> Color(0xFF4E79A7)
                Parameter.LIGHT -> Color(0xFFEDC948)
                Parameter.PH -> Color(0xFFF28E2B)
                Parameter.PRESSURE -> Color(0xFF59A14F)
            }
        return Line(
            color = SolidColor(color),
            firstGradientFillColor = color.copy(alpha = .5f),
            secondGradientFillColor = Color.Transparent,
            strokeAnimationSpec = tween(700, easing = EaseInOutCubic),
            gradientAnimationDelay = 200,
            drawStyle = DrawStyle.Stroke(width = 2.dp),
            values = emptyList(),
        )
    }
}