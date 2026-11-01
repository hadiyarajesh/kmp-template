package com.hadiyarajesh.kmptemplate.ui.onboarding

internal sealed interface OnboardingAction {
    data object Start : OnboardingAction
    data object Complete : OnboardingAction
}
