package com.talotv.os.core.profile

data class UserProfile(
    val id: String,
    val name: String,
    val ageTier: String,
    val isPrimary: Boolean = false,
    val avatarColorHex: Long = 0xFFFFFFFF,
    val hasPinLock: Boolean = false,
    val pinCode: String = "",
    val growthStage: Int = 5 // 1 to 8 matching botanical growth stages
) {
    val isChildProfile: Boolean
        get() = ageTier == "0 - 6" || ageTier == "7 - 11" || ageTier == "12 - 17"
}
