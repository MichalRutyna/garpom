package mm.zamiec.garpom.ui.state.measurement

enum class Parameter(val title: String, val unit: String, val dbName: String) {
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