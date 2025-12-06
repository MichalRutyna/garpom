package mm.zamiec.garpom

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mm.zamiec.garpom.domain.managers.AlarmConfigManager
import mm.zamiec.garpom.ui.screens.alarm_config.AlarmConfigScreenViewModel
import mm.zamiec.garpom.ui.screens.alarm_config.AlarmConfigUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class AlarmConfigScreenViewModelHiltTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var alarmConfigManager: AlarmConfigManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testNewAlarmStateInitialization() = runTest {
        val viewModel = AlarmConfigScreenViewModel(alarmConfigManager, "")
        viewModel.uiState.first { it is AlarmConfigUiState.ConfigData }
        val edit = viewModel.editState.first { it.stations.isNotEmpty() }
        assertEquals(2, edit.stations.size)
    }

    @Test
    fun testSaveStatesCreatesAlarm() = runTest {
        val vm = AlarmConfigScreenViewModel(alarmConfigManager, "")
        vm.uiState.first { it is AlarmConfigUiState.ConfigData }
        vm.editState.first { it.sliderPositions.isNotEmpty() || it.stations.isNotEmpty() }
        vm.editState.value.alarmName.value = "Test Alarm"
        vm.editState.value.alarmEnabled.value = true
        if (vm.editState.value.stations.isNotEmpty()) {
            vm.editState.value.stations[0].hasThisAlarm = true
        }
        vm.saveStates()
        vm.uiState.first { it is AlarmConfigUiState.GoBack }
    }

    @Test
    fun testResetSliders() = runTest {
        val viewModel = AlarmConfigScreenViewModel(alarmConfigManager, "")
        viewModel.uiState.first { it is AlarmConfigUiState.ConfigData }
        viewModel.editState.first { it.sliderPositions.isNotEmpty() }
        val initialSliders = viewModel.editState.value.sliderPositions.toMap()
        viewModel.resetSliders()
        val resetSliders = viewModel.editState.value.sliderPositions.toMap()
        assertEquals(initialSliders, resetSliders)
    }
}
