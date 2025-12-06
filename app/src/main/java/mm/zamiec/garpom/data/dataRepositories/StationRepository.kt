package mm.zamiec.garpom.data.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.data.dto.StationDto
import mm.zamiec.garpom.data.firebase.documentAsFlow
import mm.zamiec.garpom.data.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.data.interfaces.IStationRepository
import mm.zamiec.garpom.domain.model.Station
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class StationRepository @Inject constructor() : IStationRepository {

    private val db = Firebase.firestore

    private fun dtoMapper(doc: DocumentSnapshot): StationDto? {
        val ownerId: String = doc.getString("owner_id") ?: return null
        val name: String = doc.getString("name") ?: "Unnamed station"
        return StationDto(
            id = doc.id,
            name = name,
            ownerId = ownerId
        )
    }

    private fun domainMapper(dto: StationDto): Station =
        Station(
            id = dto.id,
            name = dto.name
        )

    override fun getStationById(id: String): Flow<Station?> =
        db.documentAsFlow("stations", id, ::dtoMapper, ::domainMapper)

    override fun getStationsByOwner(ownerId: String): Flow<List<Station>> =
        db.filteredCollectionAsFlow("stations", "owner_id", ownerId, ::dtoMapper, ::domainMapper)
}
