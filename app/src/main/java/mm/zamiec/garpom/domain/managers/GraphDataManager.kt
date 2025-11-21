package mm.zamiec.garpom.domain.managers

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
import mm.zamiec.garpom.domain.model.Parameter
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphDataManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val alarmRepository: AlarmRepository,
    private val stationRepository: StationRepository,
) {
    fun getTemperatureGraphLine(): Line {
        return getAnimatedRandomlyColoredLine().copy(
            values = listOf(20.2, 33.2, 34.3, 34.5, 27.2, 12.3, -17.2),
            label = Parameter.TEMPERATURE.title,
        )
    }
    fun getAirHumidityLine(): Line {
        return getAnimatedRandomlyColoredLine().copy(
            values = listOf(87.2, 85.2, 98.3, 76.5, 65.4, 66.1, 97.1),
            label = Parameter.AIR_HUMIDITY.title,
        )
    }

    fun getAllDates(): List<LocalDate> {
        return listOf(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 2, 1),
            LocalDate.of(2023, 3, 1),
            LocalDate.of(2023, 4, 1),
            LocalDate.of(2023, 5, 1),
            LocalDate.of(2023, 6, 1),
            LocalDate.of(2023, 12, 31)
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