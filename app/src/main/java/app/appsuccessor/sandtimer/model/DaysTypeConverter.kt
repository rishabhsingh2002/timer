package app.appsuccessor.sandtimer.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DaysTypeConverter {
    @TypeConverter
    fun fromArrayList(value: ArrayList<String>): String {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toArrayList(value: String): ArrayList<String> {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(value, type)
    }
}
