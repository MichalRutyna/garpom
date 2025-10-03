package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.controller.firebase.FirestoreRepository
import mm.zamiec.garpom.domain.model.RecentAlarm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentAlarmsUseCase @Inject constructor(private val repository: FirestoreRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun recentAlarms(userId: String): Flow<List<RecentAlarm>> =
        repository.getRecentAlarmOccurrences(userId)
            .flatMapLatest { occurrences ->
                val alarmIds = occurrences.map { it.alarmId }.distinct()

                if (alarmIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    repository.getAlarmsByIds(alarmIds).map { alarms ->
                        val alarmMap = alarms.associateBy { it.id }

                        occurrences.map { occ ->
                            RecentAlarm(
                                alarmName = alarmMap[occ.alarmId]?.name ?: "Unknown alarm",
                                measurementId = occ.measurementId
                            )
                        }
                    }
                }
            }
}