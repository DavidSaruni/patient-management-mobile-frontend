package com.example.patientmanagement.network

import com.example.patientmanagement.network.ApiClient.BASE_URL
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @POST("patients/")
    suspend fun registerPatient(@Body patient: Patient): Response<Patient>

    @POST("vitals/")
    suspend fun submitVitals(@Body vitals: Vitals): Response<Vitals>

    @POST("visit_form_a/")
    suspend fun submitVisitFormA(@Body visitFormA: VisitA): Response<VisitA>

    @POST("visit_form_b/")
    suspend fun submitVisitFormB(@Body visitFormB: VisitB): Response<VisitB>

    @GET("patients/")
    suspend fun getPatients(): Response<List<Patient>>

    companion object {
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }

}
