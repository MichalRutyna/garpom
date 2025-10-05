package mm.zamiec.garpom.controller.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.controller.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.controller.firebase.documentAsFlow
import mm.zamiec.garpom.domain.model.dto.MeasurementDto
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MeasurementRepository @Inject constructor() {
    private val db = Firebase.firestore

    fun mapper(doc: DocumentSnapshot): MeasurementDto? {
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

    fun getMeasurementById(id: String): Flow<MeasurementDto?> =
        db.documentAsFlow("measurements", id, ::mapper)

    fun getMeasurementsByOwner(ownerId: String): Flow<List<MeasurementDto>> =
        db.filteredCollectionAsFlow("measurements", "owner_id", ownerId, ::mapper)
}