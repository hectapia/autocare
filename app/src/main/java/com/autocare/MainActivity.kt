package com.autocare

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.autocare.ui.AutoCareNavGraph
import com.autocare.ui.theme.AutoCareTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            
            AutoCareTheme(darkTheme = isDarkTheme) {
                var isAuthenticated by remember { mutableStateOf(false) }
                var showAuthPrompt by remember { mutableStateOf(true) }

                if (isAuthenticated) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AutoCareNavGraph(
                            darkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it }
                        )
                    }
                } else {
                    if (showAuthPrompt) {
                        LaunchedEffect(Unit) {
                            checkAndShowBiometric(
                                onResult = { success ->
                                    if (success) {
                                        isAuthenticated = true
                                    } else {
                                        this@MainActivity.finish()
                                    }
                                    showAuthPrompt = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndShowBiometric(onResult: (Boolean) -> Unit) {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt(onResult)
            }
            else -> {
                // Biometrics not available or not enrolled
                onResult(true) // Bypass
            }
        }
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Seguro AutoCare")
            .setSubtitle("Usa tu huella o PIN para entrar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
