package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components

data class QRScannerUiState(
    val hasCameraPermission: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isScanning: Boolean = true
)
