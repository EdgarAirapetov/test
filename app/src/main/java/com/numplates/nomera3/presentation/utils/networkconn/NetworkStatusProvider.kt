package com.numplates.nomera3.presentation.utils.networkconn

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.di.scopes.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@AppScope
class NetworkStatusProvider @Inject constructor(context: Context) {

    private val networkCallback: ConnectivityManager.NetworkCallback = createNetworkCallback()
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()
    private val DELAY_REQUEST_NETWORK = 100L

    private val networkConnectionLiveData = MutableLiveData<NetworkStatus>()

    init {
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            Timber.e("Network connection exception:$e")
            FirebaseCrashlytics.getInstance()
                .recordException(CustomRegisterNetworkCallbackException(e))
        }
    }

    fun getNetworkStatusLiveData(): LiveData<NetworkStatus> {
        return networkConnectionLiveData
    }

    /**
     * Check if the internet is connected
     *
     * If [NetworkStatus] is still not available, we use fallback [isActiveNetworkConnected]
     */
    fun isInternetConnected(): Boolean {
        val networkConnection = networkConnectionLiveData.value
        return when {
            networkConnection != null && networkConnection.isConnected -> true
            networkConnection == null -> isActiveNetworkConnected()
            else -> false
        }
    }

    fun isWifiNetwork(): Boolean {
        return cm.getActiveNetworkConnectionType() == ConnectionType.WiFi
    }

    fun isMobileDataNetwork(): Boolean {
        return cm.getActiveNetworkConnectionType() == ConnectionType.MobileData
    }

    private fun checkValidNetworks() {
        val isConnected = validNetworks.size > 0
        networkConnectionLiveData.postValue(
            NetworkStatus(isConnected = isConnected)
        )
    }

    /**
     * A fallback method for when we are still waiting for the [createNetworkCallback]'s processed networks
     */
    private fun isActiveNetworkConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnected  //isConnectedOrConnecting
        }
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Timber.d("onAvailable: $network, $hasInternetCapability")
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    delay(DELAY_REQUEST_NETWORK)
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.Main) {
                            // Timber.d("onAvailable: adding network. $network")
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            // Timber.e("onLost: $network")
            validNetworks.remove(network)
            checkValidNetworks()
        }

        /**
         * В данный момент метод не используется, но может понадобиться
         * в будущем для определения типа подключения к сети
         * @return - connection type. 0: none; 1: mobile data; 2: wifi
         */
        @Deprecated("Not used at this time")
        fun getConnectionType(context: Context): Int {
            val result = 0
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return cm?.getActiveNetworkConnectionType()?.type ?: result
        }

    }

    /**
     * @return - connection type. 0: none; 1: mobile data; 2: wifi
     */
    private fun ConnectivityManager.getActiveNetworkConnectionType(): ConnectionType {
        var result = ConnectionType.None
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        result = ConnectionType.WiFi
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        result = ConnectionType.MobileData
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        result = ConnectionType.VPN
                    }
                }
            }
        } else {
            activeNetworkInfo?.run {
                when (type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        result = ConnectionType.WiFi
                    }
                    ConnectivityManager.TYPE_MOBILE -> {
                        result = ConnectionType.MobileData
                    }
                    ConnectivityManager.TYPE_VPN -> {
                        result = ConnectionType.VPN
                    }
                }
            }
        }
        return result
    }

    private class CustomRegisterNetworkCallbackException(val exception: Exception) : Exception() {

        private val errMessage = "Noomeera custom exception. LiveNetworkConnection exception:$exception"

        override fun getLocalizedMessage(): String? = errMessage

        override val message: String?
            get() = errMessage
    }

    private enum class ConnectionType(val type: Int) {
        None(0),
        MobileData(1),
        WiFi(2),
        VPN(3)
    }
}

