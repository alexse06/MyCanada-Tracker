package com.example.ircctracker.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class ConnectivityStatus {
    Available, Unavailable, Lost, Losing
}

class NetworkConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observe(): Flow<ConnectivityStatus> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    launch { send(ConnectivityStatus.Available) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    launch { send(ConnectivityStatus.Losing) }
                }

                override fun onLost(network: Network) {
                    launch { send(ConnectivityStatus.Lost) }
                }

                override fun onUnavailable() {
                    launch { send(ConnectivityStatus.Unavailable) }
                }
            }
            
            // Initial State Check
            val activeNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            if (isConnected) trySend(ConnectivityStatus.Available) else trySend(ConnectivityStatus.Unavailable)

            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
}
