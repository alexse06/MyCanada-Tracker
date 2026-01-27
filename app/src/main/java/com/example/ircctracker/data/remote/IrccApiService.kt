package com.example.ircctracker.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Placeholder data classes
data class LoginRequest(val uci: String, val password: String)
data class LoginResponse(val token: String)

data class ApplicationSummary(
    @com.google.gson.annotations.SerializedName("appNum") val id: String,
    val status: String,
    val appType: String,
    val lastUpdated: String,
    val paFirstName: String,
    val paLastName: String
)

data class DashboardRequest(
    val method: String = "get-profile-summary",
    val startIndex: Int = 0,
    val limit: Int = 5,
    val lob: String = "",
    val lastActivityDecs: Boolean = false,
    val searchFilter: String = "",
    val statusFilter: String = "",
    val isAgent: Boolean = false
)

data class DashboardResponse(
    val apps: List<ApplicationSummary>?,
    val firstName: String?,
    val hasMore: Boolean?
)

data class DetailsRequest(
    val method: String = "get-application-details",
    val applicationNumber: String,
    val uci: String,
    val isAgent: Boolean = false
)

data class DetailsResponse(
    val app: AppMetadata?,
    val relations: List<Relation>?
)

data class AppMetadata(
    val appNumber: String?,
    val uci: String?,
    val status: String?,
    val lastUpdated: String?,
    val dateRecieved: String?,
    val lob: String?
)

data class Relation(
    val activities: Activities?,
    val history: List<HistoryEvent>?
)

data class Activities(
    val eligibility: String?,
    val medical: String?,
    val background: String?,
    val biometrics: String?
)

data class HistoryEvent(
    val key: String,
    val dateCreated: String?,
    val actStatus: Int?,
    val actType: Int?
)

interface IrccApiService {
    // Note: The login is handled via AWS Cognito (SRP Protocol). 
    // This interface interacts with the backend using the Bearer token from Cognito.

    @POST("user")
    suspend fun getProfileSummary(
        @retrofit2.http.Header("Authorization") token: String,
        @Body request: DashboardRequest = DashboardRequest()
    ): DashboardResponse

    @POST("user")
    suspend fun getApplicationDetails(
        @retrofit2.http.Header("Authorization") token: String,
        @Body request: DetailsRequest
    ): DetailsResponse
}

