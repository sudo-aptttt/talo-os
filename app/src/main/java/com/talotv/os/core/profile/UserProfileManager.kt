package com.talotv.os.core.profile

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class UserProfileManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("talo_user_profiles_prefs", Context.MODE_PRIVATE)

    private val defaultProfiles = listOf(
        UserProfile(
            id = "user-1",
            name = "Alexandre",
            ageTier = "25 - 34",
            isPrimary = true,
            avatarColorHex = 0xFFFFFFFF,
            hasPinLock = false,
            pinCode = "",
            growthStage = 5
        ),
        UserProfile(
            id = "user-2",
            name = "Enfants",
            ageTier = "7 - 11",
            isPrimary = false,
            avatarColorHex = 0xFF34C759,
            hasPinLock = true,
            pinCode = "1234",
            growthStage = 2
        ),
        UserProfile(
            id = "user-3",
            name = "Cinéma & Invités",
            ageTier = "18 - 24",
            isPrimary = false,
            avatarColorHex = 0xFFFF9500,
            hasPinLock = false,
            pinCode = "",
            growthStage = 4
        )
    )

    private val _profiles = MutableStateFlow<List<UserProfile>>(defaultProfiles)
    val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

    private val _activeProfile = MutableStateFlow<UserProfile>(defaultProfiles.first())
    val activeProfile: StateFlow<UserProfile> = _activeProfile.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        val jsonStr = prefs.getString("talo_saved_profiles_json", null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<UserProfile>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        UserProfile(
                            id = obj.optString("id", "user-$i"),
                            name = obj.optString("name", "Profil"),
                            ageTier = obj.optString("ageTier", "25 - 34"),
                            isPrimary = obj.optBoolean("isPrimary", i == 0),
                            avatarColorHex = obj.optLong("avatarColorHex", 0xFFFFFFFF),
                            hasPinLock = obj.optBoolean("hasPinLock", false),
                            pinCode = obj.optString("pinCode", ""),
                            growthStage = obj.optInt("growthStage", 5)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _profiles.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _profiles.value = defaultProfiles
            }
        } else {
            _profiles.value = defaultProfiles
        }

        val activeId = prefs.getString("active_profile_id", "user-1") ?: "user-1"
        _activeProfile.value = _profiles.value.find { it.id == activeId } ?: _profiles.value.first()
    }

    private fun persistProfiles() {
        try {
            val array = JSONArray()
            for (p in _profiles.value) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("ageTier", p.ageTier)
                    put("isPrimary", p.isPrimary)
                    put("avatarColorHex", p.avatarColorHex)
                    put("hasPinLock", p.hasPinLock)
                    put("pinCode", p.pinCode)
                    put("growthStage", p.growthStage)
                }
                array.put(obj)
            }
            prefs.edit().putString("talo_saved_profiles_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun switchProfile(profile: UserProfile): Boolean {
        _activeProfile.value = profile
        prefs.edit().putString("active_profile_id", profile.id).apply()
        return true
    }

    fun updateFromCloud(cloudProfiles: List<UserProfile>) {
        if (cloudProfiles.isEmpty()) return
        _profiles.value = cloudProfiles
        val active = cloudProfiles.find { it.id == _activeProfile.value.id }
            ?: cloudProfiles.find { it.isPrimary }
            ?: cloudProfiles.first()
        _activeProfile.value = active
        prefs.edit().putString("active_profile_id", active.id).apply()
        persistProfiles()
    }

    fun addProfile(name: String, ageTier: String, colorHex: Long, pin: String = ""): UserProfile {
        val newProfile = UserProfile(
            id = "user-${System.currentTimeMillis()}",
            name = name,
            ageTier = ageTier,
            isPrimary = false,
            avatarColorHex = colorHex,
            hasPinLock = pin.isNotEmpty(),
            pinCode = pin,
            growthStage = when (ageTier) {
                "0 - 6" -> 1
                "7 - 11" -> 2
                "12 - 17" -> 3
                "18 - 24" -> 4
                "25 - 34" -> 5
                "35 - 49" -> 6
                "50 - 64" -> 7
                else -> 8
            }
        )
        val updated = _profiles.value + newProfile
        _profiles.value = updated
        persistProfiles()
        return newProfile
    }

    fun verifyPin(pin: String): Boolean {
        // Universal parent PIN check or specific child profile PIN
        return pin == "1234" || _activeProfile.value.pinCode == pin
    }
}
