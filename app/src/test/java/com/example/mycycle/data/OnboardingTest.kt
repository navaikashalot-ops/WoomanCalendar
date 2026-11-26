package com.example.mycycle.data

import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingTest {
    @Test fun freshInstallFull() = assertEquals(OnboardingMode.FULL, onboardingMode(null, false))
    @Test fun existingUserGetsProfileOnly() = assertEquals(OnboardingMode.PROFILE_ONLY, onboardingMode(null, true))
    @Test fun doneIsNone() = assertEquals(OnboardingMode.NONE, onboardingMode(Profile(onboarded = true), true))
}
