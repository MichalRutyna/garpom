package mm.zamiec.garpom.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
fun BluetoothLeScanner.scanAsFlow(): Flow<ScanResult> = callbackFlow {
    val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d("btScanner", "Sending result")
            trySend(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { trySend(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            close(RuntimeException("Scan failed: $errorCode"))
        }
    }

    startScan(callback)
    Log.d("btScanner", "Started scan")

    awaitClose {
        stopScan(callback)
    }
}