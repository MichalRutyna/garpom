package mm.zamiec.garpom.mocks

import kotlinx.coroutines.flow.MutableStateFlow
import mm.zamiec.garpom.data.interfaces.IStationRepository
import mm.zamiec.garpom.domain.model.Station
import mm.zamiec.garpom.ui.screens.alarm_config.StationChoice
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FakeStationRepository @Inject constructor() : IStationRepository {

    override fun getStationById(id: String) = MutableStateFlow<Station?>(null)

    override fun getStationsByOwner(ownerId: String) = MutableStateFlow(
        listOf(
            Station("station1", "Station 1"),
            Station("station2", "Station 2")
        )
    )
}