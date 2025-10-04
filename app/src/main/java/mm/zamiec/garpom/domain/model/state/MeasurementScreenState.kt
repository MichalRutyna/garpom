package mm.zamiec.garpom.domain.model.state

import android.os.Message
import com.google.firebase.Timestamp

open class MeasurementScreenState () {
    class MeasurementData(
        val date: Timestamp,
        val co: Double?,
        val humidity: Double?,
        val light: Double?,
        val pressure: Double?,
        val temperature: Double?,

        val triggeredAlarms: List<TriggeredAlarm>
    ): MeasurementScreenState()

    data class Error(val message: String) : MeasurementScreenState()

    object Loading : MeasurementScreenState()
}


data class TriggeredAlarm(
    val alarmId: String,
    val alarmName: String,
    val parameter: String,
)