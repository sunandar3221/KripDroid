package com.kripdroid.app.security

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun Context.findFragmentActivity(): FragmentActivity? {
        var current: Context? = this
        while (current is ContextWrapper) {
            if (current is FragmentActivity) {
                return current
            }
            current = current.baseContext
        }
        return null
    }

    fun isBiometricAvailable(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val canStrong = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            if (canStrong == BiometricManager.BIOMETRIC_SUCCESS) return true

            val canWeak = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            if (canWeak == BiometricManager.BIOMETRIC_SUCCESS) return true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val canDevice = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                if (canDevice == BiometricManager.BIOMETRIC_SUCCESS) return true
            }

            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.isKeyguardSecure == true
        } catch (_: Throwable) {
            false
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Verifikasi Kunci Perangkat",
        subtitle: String = "Pindai sidik jari, wajah, atau PIN/Pola untuk otentikasi",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isDeviceSecure = keyguardManager?.isKeyguardSecure == true

            val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (isDeviceSecure) {
                    promptBuilder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                } else {
                    promptBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    promptBuilder.setNegativeButtonText("Batal")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (isDeviceSecure) {
                    @Suppress("DEPRECATION")
                    promptBuilder.setDeviceCredentialAllowed(true)
                } else {
                    promptBuilder.setNegativeButtonText("Batal")
                }
            } else {
                promptBuilder.setNegativeButtonText("Batal")
            }

            val promptInfo = promptBuilder.build()
            val executor = ContextCompat.getMainExecutor(activity)

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                            errorCode != BiometricPrompt.ERROR_CANCELED
                        ) {
                            onError(errString.toString())
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            onError("Gagal mengaktifkan biometrik: ${e.message}")
        }
    }
}
