package mm.zamiec.garpom.ui.screens.measurement

import mm.zamiec.garpom.domain.model.Parameter


open class MeasurementCard(
    val title: String,
    val value: Double,
    val unit: String,
    val triggeredAlarms: List<TriggeredAlarm>
)

class MeasurementCardFactory {
    companion object {
        fun create(
            type: Parameter,
            level: Double?,
            triggeredAlarms: List<TriggeredAlarm>
        ): MeasurementCard {
            return MeasurementCard(
                title = type.title,
                unit = type.unit,
                value = level ?: 0.0,
                triggeredAlarms = triggeredAlarms
            )
        }
    }
}

data class FireCard(
    val value: Boolean
) {
    constructor(value: Boolean?) : this(value ?: false)
}