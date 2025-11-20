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
    TEMPERATURE("Temperature", "°C", "temperature",
        "temperatures", -30.0, 50.0,
        IconType.Dataset),
    AIR_HUMIDITY("Air humidity", "%", "air_humidity",
        "air humidity levels", 0.0, 100.0,
        IconType.Dataset),
    CO("CO", "ppm", "co",
        "carbon dioxide levels", 0.0, 100.0, // TODO maxval
        IconType.Dataset),
    GROUND_HUMIDITY("Ground humidity", "%", "ground_humidity",
        "ground humidity levels", 0.0, 100.0,
        IconType.Dataset),
    LIGHT("Light level", " lux", "light",
        "light levels", 1.0, 350.0,
        IconType.Dataset),
    PH("Ground pH", "pH", " ph",
        "pH levels", 0.0, 14.0,
        IconType.Dataset),
    PRESSURE("Air pressure", " bar", "pressure",
        "pressure levels", 900.0, 1200.0,
        IconType.Dataset)
}