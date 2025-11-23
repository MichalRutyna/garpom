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


    fun initialGraph(period: PeriodSelection, enabledParameters: Set<Parameter>): GraphData {
        val timeUnitCount: Int =
            graphDataRepository.getTimeUnitCountForPeriodSelection(period)
        val initialTimeRangeStart: Int =
            getInitialTimeRangeStartInPeriodSelection(period, timeUnitCount)


        val data = GraphData(
            selectedPeriod = period,
            storedPeriod = period,
            enabledParameters = enabledParameters,
            graphTimeRange = 0f..timeUnitCount.toFloat(), // 0-indexed
            graphActiveTimeRange = initialTimeRangeStart.toFloat()..timeUnitCount.toFloat(),
            timeRangeSteps = timeUnitCount-1, // allows x values evenly distributed
        )
        return updateLinesAndLabels(data)
    }

    fun updateData(graphData: GraphData): GraphData {
        // period selector changed
        if (graphData.selectedPeriod != graphData.storedPeriod)
            return initialGraph(graphData.selectedPeriod, graphData.enabledParameters)

        return updateLinesAndLabels(graphData)
    }

    private fun updateLinesAndLabels(graphData: GraphData): GraphData {
        val (rangeStart: LocalDateTime, rangeEnd: LocalDateTime) =
            getTimeRangeFromTimeSliderInPeriodSelection(graphData)
        val newLines = mutableListOf<Line>()
        newLines.addAll(
            graphData.enabledParameters
                .map {
                    getLineForParameter(it)
                        .copy(
                            values = graphDataRepository
                                .getValuesForParameterInTimeRangeAndPeriodSelection(
                                    it,
                                    graphData.selectedPeriod,
                                    rangeStart, rangeEnd
                                ), // slider changed
                            label = it.title,
                            strokeProgress = Animatable(0f), gradientProgress = Animatable(0f) // reset line animation
                        )
                }
        )
        val newLabels =
            graphDataRepository
                .getLabelsForPeriodSelectionInTimeRange(
                    graphData.selectedPeriod,
                    rangeStart, rangeEnd
                )

        return graphData.copy(
            lines = newLines,
            graphTimeLabels = newLabels,
        )
    }

    private fun getTimeRangeFromTimeSliderInPeriodSelection(graphData: GraphData): Pair<LocalDateTime, LocalDateTime> {
        val firstDate: LocalDateTime
        val rangeStart: LocalDateTime
        val rangeEnd: LocalDateTime

        when (graphData.selectedPeriod) {
            PeriodSelection.AllTime -> {
                firstDate = LocalDateTime.now().minusYears(9999)
                rangeStart = firstDate.plusWeeks(graphData.graphActiveTimeRange.start.toLong())
                rangeEnd = firstDate.plusWeeks(graphData.graphActiveTimeRange.endInclusive.toLong())
            }

            PeriodSelection.LastWeek -> {
                firstDate = LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0)
                rangeStart = firstDate.plusDays(graphData.graphActiveTimeRange.start.toLong())
                rangeEnd = firstDate.plusDays(graphData.graphActiveTimeRange.endInclusive.toLong())
            }

            PeriodSelection.Last3Days -> {
                firstDate = LocalDateTime.now().minusDays(3).withHour(0).withMinute(0)
                rangeStart = firstDate.plusHours(graphData.graphActiveTimeRange.start.toLong())
                rangeEnd = firstDate.plusHours(graphData.graphActiveTimeRange.endInclusive.toLong())
            }

            PeriodSelection.Last24 -> {
                firstDate = LocalDateTime.now().minusHours(24).withMinute(0)
                rangeStart = firstDate.plusHours(graphData.graphActiveTimeRange.start.toLong())
                rangeEnd = firstDate.plusHours(graphData.graphActiveTimeRange.endInclusive.toLong())
            }
        }
        return Pair(rangeStart, rangeEnd)
    }

    private fun getInitialTimeRangeStartInPeriodSelection(periodSelection: PeriodSelection, timeUnitCount: Int): Int {
        val initialDateTime: LocalDateTime =
            when (periodSelection) {
                PeriodSelection.AllTime ->
                    LocalDateTime.now().minusYears(1)
                PeriodSelection.LastWeek ->
                    LocalDateTime.now().minusWeeks(1)
                PeriodSelection.Last3Days ->
                    LocalDateTime.now().minusDays(3)
                PeriodSelection.Last24 ->
                    LocalDateTime.now().minusHours(24)
            }
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