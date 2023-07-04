package app.appsuccessor.sandtimer.datasource.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CityApiService {

    @GET("timezone/")
    fun getAllTimeZones(): Call<List<String>>

    @GET("timezone/{region}")
    fun getTimeZone(
        @Path("region") region: String
    ): Call<TimeZoneResponse>
}

data class TimeZoneResponse(
    val abbreviation: String,
    val client_ip: String,
    val datetime: String,
    val day_of_week: Int,
    val day_of_year: Int,
    val dst: Boolean,
    val dst_from: Any,
    val dst_offset: Int,
    val dst_until: Any,
    val raw_offset: Int,
    val timezone: String,
    val unixtime: Int,
    val utc_datetime: String,
    val utc_offset: String,
    val week_number: Int
)



