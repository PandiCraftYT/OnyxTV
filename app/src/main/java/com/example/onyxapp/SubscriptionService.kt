package com.example.onyxapp

import java.text.SimpleDateFormat
import java.util.*

object SubscriptionService {

    fun isAccountExpired(expiryDateStr: String?, currentDate: Date): Boolean {
        if (expiryDateStr == null) return true 
        return try {
            val cleanDate = if (expiryDateStr.contains("T")) {
                expiryDateStr.substringBefore("T")
            } else {
                expiryDateStr
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = sdf.parse(cleanDate) ?: return true
            
            val calExpiry = Calendar.getInstance().apply {
                time = expiry
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            
            currentDate.after(calExpiry.time)
        } catch (e: Exception) {
            true
        }
    }

    fun getDeviceLimit(plan: String?): Int {
        val p = plan?.uppercase() ?: ""
        return if (p.contains("3") || p.contains("12") || p == "3" || p == "12") 2 else 1
    }

    fun checkDeviceAuthorization(
        currentDeviceId: String,
        profile: UserProfile
    ): DeviceAuthResult {
        val limit = getDeviceLimit(profile.subscriptionPlan)
        
        if (profile.deviceId == currentDeviceId) return DeviceAuthResult.Authorized
        if (limit >= 2 && profile.deviceId2 == currentDeviceId) return DeviceAuthResult.Authorized
        if (profile.deviceId == null) return DeviceAuthResult.LinkToSlot1
        if (limit >= 2 && profile.deviceId2 == null) return DeviceAuthResult.LinkToSlot2

        return DeviceAuthResult.NotAuthorized(
            if (limit == 1) "Esta cuenta ya está vinculada a otro dispositivo. El plan de 1 mes solo permite 1 TV."
            else "Límite de 2 dispositivos alcanzado. Tu cuenta ya está vinculada en otros equipos."
        )
    }

    sealed class DeviceAuthResult {
        object Authorized : DeviceAuthResult()
        object LinkToSlot1 : DeviceAuthResult()
        object LinkToSlot2 : DeviceAuthResult()
        data class NotAuthorized(val message: String) : DeviceAuthResult()
    }
}
