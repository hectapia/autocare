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

        val app = application as AutoCareApplication
        val userPrefs = app.userPreferences

        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(userPrefs.isDarkTheme) }
            var isAutoLoginEnabled by remember { mutableStateOf(userPrefs.isAutoLoginEnabled) }
            
            AutoCareTheme(darkTheme = isDarkTheme) {
                // Si la Seguridad Biométrica / AutoLogin está ACTIVADA, se requiere autenticación (isAuthenticated = false)
                // Si está DESACTIVADA, se permite el acceso directo (isAuthenticated = true)
                var isAuthenticated by remember { 
                    mutableStateOf(!isAutoLoginEnabled) 
                }
                var showAuthPrompt by remember { 
                    mutableStateOf(isAutoLoginEnabled)
                }

                if (isAuthenticated) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AutoCareNavGraph(
                            darkTheme = isDarkTheme,
                            onThemeChange = { 
                                isDarkTheme = it
                                userPrefs.isDarkTheme = it
                            },
                            isBiometricSecurityEnabled = isAutoLoginEnabled,
                            onBiometricSecurityChange = {
                                isAutoLoginEnabled = it
                                userPrefs.isAutoLoginEnabled = it
                            }
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
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt(onResult)
            }
            else -> {
                // Si no hay biométricos configurados, permitimos el acceso
                onResult(true)
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
