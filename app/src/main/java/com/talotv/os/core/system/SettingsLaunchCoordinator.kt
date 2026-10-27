package com.talotv.os.core.system

import android.os.SystemClock

/**
 * Coordonne les transitions entre Talo OS Settings et les Paramètres système Android TV natifs (com.android.tv.settings).
 * Permet à HomeKeyAccessibilityService de distinguer :
 * 1. Une ouverture autorisée (l'utilisateur a explicitement cliqué sur "Paramètres TV" dans Talo OS) -> autorisée sans redirection
 * 2. Une tentative externe d'ouverture (via Extended Controls d'Android Studio, un raccourci système ou une touche télécommande) -> interceptée et redirigée vers le panneau global Talo OS
 */
object SettingsLaunchCoordinator {

    @Volatile
    private var authorizedSession: Boolean = false

    @Volatile
    private var lastAuthorizedTimestamp: Long = 0L

    @Volatile
    private var awaitingReturnToSettings: Boolean = false

    /**
     * Marque le lancement de l'activité native Android TV Settings comme explicitement autorisé par l'utilisateur
     * et active l'attente du retour pour ré-ouvrir le volet des paramètres Talo OS.
     */
    fun authorizeNativeLaunch() {
        authorizedSession = true
        awaitingReturnToSettings = true
        lastAuthorizedTimestamp = SystemClock.elapsedRealtime()
    }

    /**
     * Indique si la session courante de Paramètres TV est issue d'une demande légitime de Talo OS.
     * Conserve la validité pendant toute la durée où l'utilisateur interagit avec com.android.tv.settings,
     * ou au minimum pendant 5 secondes après le déclenchement initial.
     */
    fun isAuthorized(): Boolean {
        if (authorizedSession) return true
        val elapsed = SystemClock.elapsedRealtime() - lastAuthorizedTimestamp
        return elapsed in 0..5000L
    }

    /**
     * Consomme le signal de retour depuis les paramètres TV natifs.
     * Retourne true une seule fois si l'utilisateur revient d'une session native autorisée.
     */
    /**
     * Consomme le signal de retour depuis les paramètres TV natifs.
     * Retourne true une seule fois si l'utilisateur revient d'une session native autorisée.
     */
    fun shouldRestoreSettingsPanel(): Boolean {
        val restore = awaitingReturnToSettings
        awaitingReturnToSettings = false
        return restore
    }

    /**
     * Annule l'attente du retour aux paramètres si l'utilisateur appuie explicitement sur la touche Accueil (Home).
     */
    fun cancelReturnToSettings() {
        awaitingReturnToSettings = false
        authorizedSession = false
        lastAuthorizedTimestamp = 0L
    }

    /**
     * Réinitialise l'autorisation dès que l'utilisateur quitte les paramètres TV et retourne sur Talo OS.
     */
    fun clearAuthorization() {
        authorizedSession = false
        lastAuthorizedTimestamp = 0L
    }
}

