package mm.zamiec.garpom.domain.model

data class Average(
    val co: Double,
    val airHumidity: Double,
    val groundHumidity: Double,
    val light: Double,
    val pressure: Double,
    val temperature: Double,
    val ph: Double,
    )