package com.example.ircctracker.data.repository

import android.content.Context
import android.util.Log
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.example.ircctracker.data.AmplifyConfig
import com.example.ircctracker.data.remote.IrccApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository(private val apiService: IrccApiService, private val context: Context) {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private var currentUserUci: String = ""

    init {
        configureAmplify()
        checkSession()
    }

    private fun configureAmplify() {
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            val config = AmplifyConfig.getConfiguration(context)
            Amplify.configure(AmplifyConfiguration.fromJson(config), context)
            Log.i("IrccAuth", "Initialized Amplify")
        } catch (e: Exception) {
            Log.e("IrccAuth", "Could not initialize Amplify", e)
        }
    }
    
    // Check if user is already signed in on app launch
    private fun checkSession() {
        _authState.value = AuthState.Loading
        fetchSession()
    }

    suspend fun login(uci: String, password: String) {
        _authState.value = AuthState.Loading
        
        try {
            val sanitizedUci = uci.replace(Regex("[^0-9]"), "")
            currentUserUci = sanitizedUci
            val isSignedIn =  suspendCoroutine<Boolean> { continuation ->
                Amplify.Auth.signIn(
                    sanitizedUci,
                    password,
                    { result ->
                        if (result.isSignedIn) {
                            Log.i("IrccAuth", "Sign in succeeded")
                            continuation.resume(true)
                        } else {
                            Log.w("IrccAuth", "Sign in not complete: ${result.nextStep.signInStep}")
                            continuation.resume(false)
                        }
                    },
                    { error ->
                        // If user is already signed in, we can proceed to fetch session
                        if (error.localizedMessage?.contains("already a user signed in", ignoreCase = true) == true) {
                            Log.i("IrccAuth", "User already signed in, proceeding to fetch token")
                            continuation.resume(true)
                        } else {
                            Log.e("IrccAuth", "Sign in failed: $error", error)
                            _authState.value = AuthState.Error("${error.localizedMessage}\n(${error.javaClass.simpleName})")
                            continuation.resume(false)
                        }
                    }
                )
            }

            if (isSignedIn) {
                 fetchSession()
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun refreshToken(): String? {
        return suspendCoroutine { continuation ->
             Amplify.Auth.fetchAuthSession(
                { result ->
                    val session = result as? AWSCognitoAuthSession
                    val token = session?.userPoolTokensResult?.value?.idToken
                    continuation.resume(token)
                },
                { error ->
                    Log.e("IrccAuth", "Failed to refresh token", error)
                    continuation.resume(null)
                }
            )
        }
    }

    private fun fetchSession() {
        Amplify.Auth.fetchAuthSession(
            { result ->
                val session = result as? AWSCognitoAuthSession
                val token = session?.userPoolTokensResult?.value?.idToken
                if (token != null) {
                    val uci = extractUciFromToken(token)
                     Log.i("IrccAuth", "Got token for UCI: $uci")
                    _authState.value = AuthState.LoggedIn(token, uci)
                } else {
                    _authState.value = AuthState.Error("Could not retrieve session token")
                }
            },
            { error ->
                _authState.value = AuthState.Error("Failed to fetch session: ${error.localizedMessage}")
            }
        )
    }

    private fun extractUciFromToken(token: String): String {
        try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                val json = org.json.JSONObject(payload)
                if (json.has("cognito:username")) {
                    return json.getString("cognito:username")
                }
            }
        } catch (e: Exception) {
            Log.e("IrccAuth", "Failed to parse JWT", e)
        }
        return currentUserUci // Fallback if parsing fails
    }
}

sealed class AuthState {
    object LoggedOut : AuthState()
    object Loading : AuthState()
    data class LoggedIn(val token: String, val uci: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
