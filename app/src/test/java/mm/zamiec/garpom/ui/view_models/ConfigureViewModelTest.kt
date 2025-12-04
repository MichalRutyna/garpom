package mm.zamiec.garpom.ui.view_models

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import mm.zamiec.garpom.ui.screens.configure.BluetoothManager
import mm.zamiec.garpom.ui.screens.configure.ConfigureScreenViewModel
import mm.zamiec.garpom.ui.screens.configure.DialogState
import mm.zamiec.garpom.ui.screens.configure.ScreenState
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ConfigureScreenViewModelTest {

    private lateinit var viewModel: ConfigureScreenViewModel
    private lateinit var context: Context
    private val btManager: BluetoothManager = mockk(relaxed = true)
    private val activity: Activity = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = org.robolectric.RuntimeEnvironment.getApplication()
        viewModel = ConfigureScreenViewModel(context, btManager)

        // Static mocks
        mockkStatic(ContextCompat::class)
        mockkStatic(ActivityCompat::class)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun grantAllPermissions() {
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) } returns PackageManager.PERMISSION_GRANTED
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) } returns PackageManager.PERMISSION_GRANTED
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
    }

    private fun denyAllPermissions() {
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) } returns PackageManager.PERMISSION_DENIED
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) } returns PackageManager.PERMISSION_DENIED
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED
    }

    @Test
    fun `hasBtPermissions returns true when all granted`() {
        grantAllPermissions()
        val result = viewModel.hasBtPermissions()
        assertThat(result, `is`(true))
    }

    @Test
    fun `hasBtPermissions returns false when any denied`() {
        denyAllPermissions()
        val result = viewModel.hasBtPermissions()
        assertThat(result, `is`(false))
    }

    @Test
    fun `initialConfiguration requests permissions when missing`() = runTest(testDispatcher) {
        denyAllPermissions()
        every { ActivityCompat.shouldShowRequestPermissionRationale(activity, any()) } returns false

        var requested = false
        viewModel.setRequestPermissionsCallback { requested = true }

        viewModel.initialConfiguration(activity)
        testScheduler.advanceUntilIdle()

        assertThat(requested, `is`(true))
        assertThat(viewModel.uiState.value.dialog, nullValue())
    }

    @Test
    fun `initialConfiguration shows rationale dialog`() = runTest(testDispatcher) {
        denyAllPermissions()
        every { ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_SCAN) } returns true
        every { ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_CONNECT) } returns false

        viewModel.initialConfiguration(activity)
        testScheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.dialog, `is`(DialogState.PermissionExplanationNeeded))
    }

    @Test
    fun `showScanResult adds unique devices`() = runTest(testDispatcher) {
        val dev = mockk<BluetoothDevice> {
            every { address } returns "ADDR1"
            every { name } returns "Device1"
        }
        val result = mockk<ScanResult> { every { device } returns dev }

        viewModel.showScanResult(result)

        assertThat(viewModel.scanResults.size, `is`(1))
        assertThat(viewModel.uiState.value.screenState, `is`(ScreenState.ScanResults))
    }

    @Test
    fun `showScanResult ignores duplicates`() = runTest(testDispatcher) {
        val dev = mockk<BluetoothDevice> {
            every { address } returns "ADDR1"
            every { name } returns "Device1"
        }
        val result = mockk<ScanResult> { every { device } returns dev }

        viewModel.showScanResult(result)
        viewModel.showScanResult(result)

        assertThat(viewModel.scanResults.size, `is`(1))
    }

    @Test
    fun `onPermissionsDenied updates dialog state`() = runTest(testDispatcher) {
        viewModel.onPermissionsDenied()
        assertThat(viewModel.uiState.value.dialog, `is`(DialogState.PermissionsDenied))
    }

    @Test
    fun `clearDialog resets UI dialog`() = runTest(testDispatcher) {
        viewModel.onPermissionsDenied()
        viewModel.clearDialog()
        assertThat(viewModel.uiState.value.dialog, nullValue())
    }
}
