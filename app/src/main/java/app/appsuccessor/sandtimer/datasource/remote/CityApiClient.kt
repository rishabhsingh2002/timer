package app.appsuccessor.sandtimer.datasource.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CityApiClient {
    private const val BASE_URL = "http://worldtimeapi.org/api/"

    fun create(): CityApiService {
        // Create the logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create an OkHttpClient with the interceptor
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        // Create the Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient) // Set the custom OkHttpClient
            .build()

        return retrofit.create(CityApiService::class.java)
    }
}
