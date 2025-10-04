package mm.zamiec.garpom.controller.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.controller.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.controller.firebase.documentAsFlow
import mm.zamiec.garpom.domain.model.dto.StationDto
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StationRepository @Inject constructor() {
    private val db = Firebase.firestore

    fun mapper(doc: DocumentSnapshot): StationDto? {
        val ownerId = doc.getString("owner_id") ?: return null
        val name = doc.getString("name") ?: "Unnamed station"

        return StationDto(
            doc.id,
            name,
            ownerId,
        )
    }

    fun getStationById(id: String): Flow<StationDto?> =
        db.documentAsFlow("stations", id, ::mapper)

    fun getStationsByOwner(ownerId: String): Flow<List<StationDto>> =
        db.filteredCollectionAsFlow("stations", "owner_id", ownerId, ::mapper)
}