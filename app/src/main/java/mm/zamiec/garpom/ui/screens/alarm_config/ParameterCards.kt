package mm.zamiec.garpom.ui.screens.alarm_config

import mm.zamiec.garpom.domain.model.Parameter

class ParameterRangeCard (
    val title: String,
    val descriptionParameterName: String,
    val unit: String,

    val minValue: Double,
    val maxValue: Double,

    val startValue: Double,
    val endValue: Double,
)

class ParameterCardFactory {
    companion object {
        fun create(
            type: Parameter,
            startValue: Double,
            endValue: Double,
        ): ParameterRangeCard {
            return ParameterRangeCard(
                title = type.title,
                descriptionParameterName = type.descriptionText,
                unit = type.unit,
                minValue = type.minValue,
                maxValue = type.maxValue,
                startValue = startValue,
                endValue = endValue,
            )
        }
    }
}