package mm.zamiec.garpom.data.interfaces


import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.domain.model.Station

interface IStationRepository {

    fun getStationById(id: String): Flow<Station?>
    fun getStationsByOwner(ownerId: String): Flow<List<Station>>
}