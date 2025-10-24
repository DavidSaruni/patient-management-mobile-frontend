package com.example.patientmanagement.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("patients/")
    suspend fun registerPatient(@Body patient: Patient): Response<Patient>

    @POST("vitals/")
    suspend fun submitVitals(@Body vitals: Vitals): Response<Vitals>

    @POST("visit-form-a/")
    suspend fun submitVisitFormA(@Body visitFormA: VisitA): Response<VisitA>

    @POST("visit-form-b/")
    suspend fun submitVisitFormB(@Body visitFormB: VisitB): Response<VisitB>

    @GET("patients/")
    suspend fun getPatients(): Response<List<Patient>>
}
