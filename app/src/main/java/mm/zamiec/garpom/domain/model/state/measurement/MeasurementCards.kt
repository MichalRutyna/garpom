package mm.zamiec.garpom.domain.model.state.measurement

enum class MeasurementType(val title: String, val unit: String, val dbName: String) {
    TEMPERATURE("Temperature", "°C", "temperature"),
    AIR_HUMIDITY("Air humidity", "%", "air_humidity"),
    CO("CO", "ppm", "co"),
    GROUND_HUMIDITY("Ground humidity", "%", "ground_humidity"),
    LIGHT("Light level", " lumen", "light"),
    PH("Ground pH", "pH", "ph"),
    PRESSURE("Air pressure", "bar", "pressure")
}

class MeasurementCardFactory {
    companion object {
        fun create(
            type: MeasurementType,
            level: Double?,
            triggeredAlarms: List<TriggeredAlarm>? = emptyList()
        ): MeasurementCard {
            return MeasurementCard(
                title = type.title,
                unit = type.unit,
                value = level ?: 0.0,
                triggeredAlarms = triggeredAlarms ?: emptyList()
            )
        }
    }
}

data class FireCard(
    val value: Boolean
) {
    constructor(value: Boolean?) : this(value ?: false)
}