package mm.zamiec.garpom.ui.view_models

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.domain.managers.AlarmOccurrencesListManager
import mm.zamiec.garpom.domain.managers.StationSummaryManager
import mm.zamiec.garpom.domain.model.AppUser
import mm.zamiec.garpom.ui.screens.home.HomeViewModel
import mm.zamiec.garpom.ui.screens.home.RecentAlarmOccurrenceItemUiState
import mm.zamiec.garpom.ui.screens.home.StationSummaryItemUiState
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var stationSummaryManager: StationSummaryManager
    private lateinit var alarmOccurrencesListManager: AlarmOccurrencesListManager

    @Before
    fun setup() {
        authRepository = mockk()
        stationSummaryManager = mockk()
        alarmOccurrencesListManager = mockk()
    }

    @Test
    fun `uiState should combine values from repositories`() = runTest {
        // Given
        val user = AppUser(
            id = "123",
            username = "testUser",
            isAnonymous = false
        )

        val stations = listOf(
            StationSummaryItemUiState("st1", "Station 1", hasNotification = true, hasError = true),
            StationSummaryItemUiState("st2", "Station 2")
        )

        val alarms = listOf(
            RecentAlarmOccurrenceItemUiState("alarm1"),
            RecentAlarmOccurrenceItemUiState("alarm2")
        )

        every { authRepository.currentUser } returns MutableStateFlow(user)
        every { stationSummaryManager.stationsSummaryForUser() } returns flowOf(stations)
        every { alarmOccurrencesListManager.recentAlarmOccurrences() } returns MutableStateFlow(
            alarms
        )

        val viewModel = HomeViewModel(
            authRepository,
            stationSummaryManager,
            alarmOccurrencesListManager
        )

        // When
        val result = viewModel.uiState.first()

        // Then
        Assert.assertEquals(false, result.isAnonymous)
        Assert.assertEquals("testUser", result.username)
        Assert.assertEquals(stations, result.stations)
        Assert.assertEquals(alarms, result.recentAlarmOccurrences)
    }
}