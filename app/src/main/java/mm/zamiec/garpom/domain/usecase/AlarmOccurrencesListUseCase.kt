package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import mm.zamiec.garpom.controller.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmRepository
import mm.zamiec.garpom.domain.model.state.AlarmOccurrence
import mm.zamiec.garpom.domain.model.state.RecentAlarmOccurrence
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlarmOccurrencesListUseCase @Inject constructor (
    private val alarmOccurrenceRepository: AlarmOccurrenceRepository,
    private val alarmRepository: AlarmRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun allAlarmOccurrences(userId: String): Flow<List<AlarmOccurrence>> =
        alarmOccurrenceRepository
            .getAlarmOccurrencesForUser(userId)
            .flatMapLatest { occurrences ->
                val flows = occurrences.map { occurrence ->
                    alarmRepository.getAlarmById(occurrence.alarmId).mapLatest { alarm ->
                        if (alarm == null)
                            return@mapLatest AlarmOccurrence("", "") //TODO
                        // single query
                        AlarmOccurrence(
                            alarm.name,
                            occurrence.measurementId
                        )
                    }
                }
                combine(flows) {
                    it.toList()
                }
            }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun recentAlarmOccurrences(userId: String): Flow<List<RecentAlarmOccurrence>> =
        alarmOccurrenceRepository.getRecentAlarmOccurrencesForUser(userId)
            .flatMapLatest { occurrences ->
                val alarmIds = occurrences.map { it.alarmId }.distinct()

                if (alarmIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    alarmRepository.getAlarmsByIdList(alarmIds).map { alarms ->
                        // batch query, stored in a map
                        val alarmMap = alarms.associateBy { it.id }

                        occurrences.map { occ ->
                            RecentAlarmOccurrence(
                                alarmName = alarmMap[occ.alarmId]?.name ?: "Unknown alarm",
                                measurementId = occ.measurementId
                            )
                        }
                    }
                }
            }
}