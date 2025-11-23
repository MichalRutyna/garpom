package mm.zamiec.garpom.data.dataRepositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.data.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.data.firebase.documentAsFlow
import mm.zamiec.garpom.data.dto.MeasurementDto
import mm.zamiec.garpom.data.firebase.queryAsFlow
import mm.zamiec.garpom.domain.model.Measurement
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MeasurementRepository @Inject constructor() {
    private val db = Firebase.firestore

    private fun dtoMapper(doc: DocumentSnapshot): MeasurementDto? {
        val stationId = doc.getString("station_id") ?: return null
        val date = doc.getTimestamp("date") ?: return null

        return MeasurementDto(
            id = doc.id,
            stationId = stationId,
            date = date,
            airHumidity = doc.getDouble("air_humidity"),
            groundHumidity = doc.getDouble("ground_humidity"),
            co = doc.getDouble("co"),
            pressure = doc.getDouble("pressure"),
            light = doc.getDouble("light"),
            temperature = doc.getDouble("temperature"),
            ph = doc.getDouble("ph"),
            fire = doc.getBoolean("fire_detected"),
        )
    }

    private fun domainMapper(dto: MeasurementDto): Measurement =
        Measurement(
            id = dto.id,
            stationId = dto.stationId,
            date = dto.date.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
            co = dto.co,
            airHumidity = dto.airHumidity,
            groundHumidity = dto.groundHumidity,
            light = dto.light,
            pressure = dto.pressure,
            temperature = dto.temperature,
            ph = dto.ph,
            fire = dto.fire
        )

    fun getMeasurementById(id: String): Flow<Measurement?> =
        db.documentAsFlow("measurements", id, ::dtoMapper, ::domainMapper)

    fun getMeasurementsByStation(stationId: String): Flow<List<Measurement>> =
        db.filteredCollectionAsFlow("measurements",
            "station_id", stationId, ::dtoMapper, ::domainMapper)

    fun getMeasurementsByStationBetweenDates(stationId: String, start: LocalDateTime, end: LocalDateTime): Flow<List<Measurement>> {
        val startTimestamp = Timestamp(start.atZone(ZoneId.systemDefault()).toInstant())
        val endTimestamp = Timestamp(end.atZone(ZoneId.systemDefault()).toInstant())

        Log.d("MeasRepo", "start: " + startTimestamp.toDate() + " end: " + endTimestamp.toDate())

        return queryAsFlow(
            db
                .collection("measurements")
                .whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThanOrEqualTo("date", endTimestamp)
                .orderBy("date"),
            ::dtoMapper,
            ::domainMapper
        )
    }
}