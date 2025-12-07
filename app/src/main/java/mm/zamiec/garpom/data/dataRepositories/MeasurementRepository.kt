package mm.zamiec.garpom.data.dataRepositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import mm.zamiec.garpom.data.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.data.firebase.documentAsFlow
import mm.zamiec.garpom.data.dto.MeasurementDto
import mm.zamiec.garpom.data.firebase.queryAsFlow
import mm.zamiec.garpom.domain.model.Measurement
import mm.zamiec.garpom.domain.model.Parameter
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
            airHumidity = doc.getDouble(Parameter.AIR_HUMIDITY.dbName),
            groundHumidity = doc.getDouble(Parameter.GROUND_HUMIDITY.dbName),
            pressure = doc.getDouble(Parameter.PRESSURE.dbName),
            light = doc.getDouble(Parameter.LIGHT.dbName),
            temperature = doc.getDouble(Parameter.TEMPERATURE.dbName),
        )
    }

    private fun domainMapper(dto: MeasurementDto): Measurement =
        Measurement(
            id = dto.id,
            stationId = dto.stationId,
            date = dto.date.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
            airHumidity = dto.airHumidity,
            groundHumidity = dto.groundHumidity,
            light = dto.light,
            pressure = dto.pressure,
            temperature = dto.temperature,
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
                .whereEqualTo("station_id", stationId)
                .whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThanOrEqualTo("date", endTimestamp)
                .orderBy("date"),
            ::dtoMapper,
            ::domainMapper
        )
    }

    fun deleteMeasurement(measurementId: String) = callbackFlow<Unit> {
        val docRef = db.collection("measurements").document(measurementId)

        docRef.delete()
            .addOnSuccessListener {
                trySend(Unit)
                close()
            }
            .addOnFailureListener { e ->
                close(e)
            }

        awaitClose { }
    }
}