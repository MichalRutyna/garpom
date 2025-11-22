package mm.zamiec.garpom.domain.managers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.data.dataRepositories.StationRepository
import mm.zamiec.garpom.data.processed.GraphDataRepository
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.station.GraphData
import mm.zamiec.garpom.ui.screens.station.PeriodSelection
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.mutableListOf

@Singleton
class GraphDataManager @Inject constructor(
    private val graphDataRepository: GraphDataRepository
) {


    fun initialGraph(period: PeriodSelection): GraphData {

    }

    fun updateLines(graphData: GraphData): GraphData {
        // period selector changed
        if (graphData.selectedPeriod != graphData.storedPeriod)
            return initialGraph(graphData.selectedPeriod)

        when(graphData.selectedPeriod) {
            PeriodSelection.AllTime -> {

            }
            PeriodSelection.Last3Days -> TODO()
            PeriodSelection.Last24 -> TODO()

            else -> { // PeriodSelection.LastWeek

            }
        }
    }

    private fun updateForLastWeek(graphData: GraphData): GraphData {
        val lastWeekStart = LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0)
        val start = lastWeekStart.plusDays(graphData.graphActiveTimeRange.start.toLong())
        val end = lastWeekStart.plusDays(graphData.graphActiveTimeRange.endInclusive.toLong())

        val newLines = mutableListOf<Line>()
        newLines.addAll(
            graphData.graphChips
                .filter { it.enabled } // parameter selection might have changed
                .map {
                    getAnimatedRandomlyColoredLine()
                        .copy(
                            values = graphDataRepository
                                .getValuesForParameterInTimeRange(
                                    it.parameter
                                    , start, end
                                ), // slider changed
                            label = it.parameter.title,
                            strokeProgress = Animatable(0f), gradientProgress = Animatable(0f) // reset line animation
                        )
                }
        )

        val newLabels =
            graphDataRepository
                .getLabelsForPeriodSelectionInTimeRange(
                    graphData.selectedPeriod,
                    start, end
                )
        return graphData.copy(
            lines = newLines,
            graphTimeLabels = newLabels,

        )
    }


    private val predeterminedColors = listOf(
        Color(0xFF4E79A7),
        Color(0xFFF28E2B),
        Color(0xFFE15759),
        Color(0xFF76B7B2),
        Color(0xFF59A14F),
        Color(0xFFEDC948),
        Color(0xFFB07AA1))
    companion object Counter {
        var counter = 0
    }
    private fun getAnimatedRandomlyColoredLine(): Line {
        val color = predeterminedColors[counter]
        counter = (counter + 1) % predeterminedColors.size
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