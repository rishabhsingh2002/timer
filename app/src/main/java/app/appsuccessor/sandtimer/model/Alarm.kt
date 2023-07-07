package app.appsuccessor.sandtimer.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable

@Entity(tableName = "alarms")
@TypeConverters(DaysTypeConverter::class)
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String?,
    val time: String?,
    val ringtoneUri: String?,
    var isEnabled: Boolean?,
    var days: ArrayList<String>
) : Serializable
