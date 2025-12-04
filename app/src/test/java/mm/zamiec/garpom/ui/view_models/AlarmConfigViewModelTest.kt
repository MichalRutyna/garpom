package mm.zamiec.garpom.ui.view_models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateMapOf
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.test.*
import mm.zamiec.garpom.MainDispatcherRule
import mm.zamiec.garpom.domain.managers.AlarmConfigManager
import mm.zamiec.garpom.ui.screens.alarm_config.AlarmConfigScreenViewModel
import mm.zamiec.garpom.ui.screens.alarm_config.AlarmConfigUiState
import mm.zamiec.garpom.ui.screens.alarm_config.StationChoice
import mm.zamiec.garpom.ui.screens.alarm_config.cardsToMap
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmConfigScreenViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var alarmConfigManager: AlarmConfigManager

    private val sampleState = AlarmConfigUiState.ConfigData(
        alarmId = "123",
        createAlarm = false,
        alarmEnabled = true,
        alarmName = "Test Alarm",
        alarmDescription = "Desc",
        userStations = listOf(
            StationChoice("1", "Station A", true),
            StationChoice("2", "Station B", false),
        ),
        alarmStart = Calendar.Builder().setTimeOfDay(8, 30, 0).build(),
        alarmEnd = Calendar.Builder().setTimeOfDay(15, 45, 0).build()
    )

    @Before
    fun setup() {
        alarmConfigManager = mockk()
    }

    @Test
    fun init_loads_and_emits_ConfigData() = runTest {
        coEvery { alarmConfigManager.alarmDetails("123") } returns sampleState

        val vm = AlarmConfigScreenViewModel(alarmConfigManager, "123")

        val first = vm.uiState.value
        assertThat("First emission should be Loading",
            first is AlarmConfigUiState.Loading)

        advanceUntilIdle()

        val second = vm.uiState.value
        assertThat("Second emission should be ConfigData",
            second is AlarmConfigUiState.ConfigData)
        assertThat("Alarm name should match",
            (second as AlarmConfigUiState.ConfigData).alarmName == "Test Alarm"
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun resetFromUiState_copies_correct_values() = runTest {
        coEvery { alarmConfigManager.alarmDetails(any()) } returns sampleState

        val vm = AlarmConfigScreenViewModel(alarmConfigManager, "123")
        advanceUntilIdle()

        val edit = vm.editState.value

        assertThat("alarmEnabled must match", edit.alarmEnabled.value)
        assertThat("alarmName must match", edit.alarmName.value == "Test Alarm")
        assertThat("alarmDescription must match", edit.alarmDescription.value == "Desc")
        assertThat("station list size must match", edit.stations.size == 2)
        assertThat("start hour must match", edit.alarmStart.hour == 8)
        assertThat("start minute must match", edit.alarmStart.minute == 30)
    }

    @Test
    fun saveStates_calls_manager_and_sets_GoBack() = runTest {
        coEvery { alarmConfigManager.alarmDetails("123") } returns sampleState
        coEvery { alarmConfigManager.saveUiState(any()) } just Runs

        val vm = AlarmConfigScreenViewModel(alarmConfigManager, "123")
        advanceUntilIdle()

        vm.editState.value.alarmName.value = "Changed"
        vm.editState.value.alarmEnabled.value = false

        vm.saveStates()
        advanceUntilIdle()

        coVerify {
            alarmConfigManager.saveUiState(withArg { saved ->
                assertThat("Name must match", saved.alarmName == "Changed")
                assertThat("Enabled must match", !saved.alarmEnabled)
                assertThat("ID must match", saved.alarmId == "123")
            })
        }

        val final = vm.uiState.first()
        assertThat("Should emit GoBack", final == AlarmConfigUiState.GoBack)
    }

    @Test
    fun resetSliders_restores_original_ranges() = runTest {
        coEvery { alarmConfigManager.alarmDetails(any()) } returns sampleState

        val vm = AlarmConfigScreenViewModel(alarmConfigManager, "123")
        advanceUntilIdle()

        val originalMap = cardsToMap(sampleState)

        val edit = vm.editState.value

        edit.sliderPositions.keys.forEach { key ->
            edit.sliderPositions[key] = 0f..1f
        }

        vm.resetSliders()
        advanceUntilIdle()

        val restored = vm.editState.value.sliderPositions.toMap()
        assertThat("Sliders must match default", restored == originalMap)
    }
}