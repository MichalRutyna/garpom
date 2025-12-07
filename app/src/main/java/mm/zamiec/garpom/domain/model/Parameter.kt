package mm.zamiec.garpom.domain.model

enum class Parameter(
    val title: String,
    val unit: String,
    val dbName: String,
    val descriptionText: String,
    val minValue: Double,
    val maxValue: Double,
    val icon: IconType
) {
    TEMPERATURE("Temperature", "°C", "temperatureAir",
        "temperatures", -30.0, 50.0,
        IconType.Dataset),
    AIR_HUMIDITY("Air humidity", "%", "humidityAir",
        "air humidity levels", 0.0, 100.0,
        IconType.Dataset),
    GROUND_HUMIDITY("Ground humidity", "%", "humiditySoil",
        "ground humidity levels", 0.0, 100.0,
        IconType.Dataset),
    LIGHT("Light level", " lux", "sunlight",
        "light levels", 1.0, 350.0,
        IconType.Dataset),
    PRESSURE("Air pressure", " bar", "pressureAir",
        "pressure levels", 900.0, 1200.0,
        IconType.Dataset)
}