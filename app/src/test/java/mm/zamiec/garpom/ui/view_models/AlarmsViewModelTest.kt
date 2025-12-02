package mm.zamiec.garpom.ui.view_models

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import mm.zamiec.garpom.domain.managers.AlarmsListManager
import mm.zamiec.garpom.domain.managers.AlarmOccurrencesListManager
import mm.zamiec.garpom.ui.screens.alarms.AlarmOccurrenceItemUiState
import mm.zamiec.garpom.ui.screens.alarms.AlarmSummaryItemUiState
import mm.zamiec.garpom.ui.screens.alarms.AlarmsScreenViewModel
import mm.zamiec.garpom.ui.screens.alarms.StationAlarmsItemUiState
import mm.zamiec.garpom.ui.screens.home.RecentAlarmOccurrenceItemUiState

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmsScreenViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var alarmsListManager: AlarmsListManager
    private lateinit var occurrencesManager: AlarmOccurrencesListManager

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        alarmsListManager = mockk()
        occurrencesManager = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits combined data`() = runTest {
        // Arrange
        val fakeAlarms = listOf(
            StationAlarmsItemUiState(
                "A1",
                alarmList = listOf(
                    AlarmSummaryItemUiState(
                        name = "Test alarm"
                    )
                )
            ),
            StationAlarmsItemUiState("A2", alarmList = emptyList())
        )
        val fakeRecentOcc = listOf(
            RecentAlarmOccurrenceItemUiState("Test occurrence")
        )
        val fakeAllOcc = listOf(
            AlarmOccurrenceItemUiState("Test occurrence2"),
            AlarmOccurrenceItemUiState("Test occurrence3")
        )

        every { alarmsListManager.alarmList() } returns MutableStateFlow(fakeAlarms)
        every { occurrencesManager.recentAlarmOccurrences() } returns MutableStateFlow(fakeRecentOcc)
        every { occurrencesManager.allAlarmOccurrences() } returns MutableStateFlow(fakeAllOcc)

        val viewModel = AlarmsScreenViewModel(
            occurrencesManager,
            alarmsListManager
        )

        // Act
        val state = viewModel.uiState.first()

        // Assert
        assertEquals(fakeAlarms, state.stationAlarmsList)
        assertEquals(fakeRecentOcc, state.recentAlarmOccurrencesList)
        assertEquals(fakeAllOcc, state.allAlarmOccurrencesList)
    }
}