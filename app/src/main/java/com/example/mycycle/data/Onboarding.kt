package com.example.mycycle.data

enum class OnboardingMode { NONE, PROFILE_ONLY, FULL }

fun onboardingMode(profile: Profile?, hasData: Boolean): OnboardingMode = when {
    profile?.onboarded == true -> OnboardingMode.NONE
    hasData -> OnboardingMode.PROFILE_ONLY
    else -> OnboardingMode.FULL
}
