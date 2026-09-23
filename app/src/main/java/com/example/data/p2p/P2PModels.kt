package com.example.data.p2p

enum class P2PConnectionState {
    DISABLED,
    DISCONNECTED,
    SEARCHING,
    CONNECTING,
    CONNECTED,
    SYNCING,
    ERROR
}

enum class P2PRole {
    HOST,    // Dispositivo que fornece a chave (detentor dos dados originais)
    CLIENT   // Dispositivo que recebe a chave (recebe o snapshot e sobrepõe seus dados locais)
}

data class P2PDevice(
    val id: String,
    val name: String,
    val ip: String,
    val port: Int,
    val role: P2PRole = P2PRole.CLIENT,
    val isConnected: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis()
)

data class P2PSyncStatus(
    val isEnabled: Boolean = false,
    val syncKey: String = "",
    val role: P2PRole = P2PRole.HOST,
    val state: P2PConnectionState = P2PConnectionState.DISABLED,
    val isInitialSyncDone: Boolean = false,
    val connectedPeers: List<P2PDevice> = emptyList(),
    val discoveredPeers: List<P2PDevice> = emptyList(),
    val localIp: String = "",
    val localPort: Int = 0,
    val lastSyncTimestamp: Long = 0L,
    val syncedItemsCount: Int = 0,
    val lastEventMessage: String? = null,
    val errorMessage: String? = null
)
