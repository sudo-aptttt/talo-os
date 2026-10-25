# 📺 Talo OS — Application Lanceur TV Haut de Gamme

Application Android TV native (Kotlin + Jetpack Compose for TV) conçue pour transformer n'importe quel boîtier TV (Fire TV, Google TV, Nvidia Shield, Xiaomi TV) en une expérience pure, fluide, sans publicité, inspirée de tvOS (Apple TV).

---

## 🚀 Fonctionnalités Clés

1. **Démarrage Direct (Boot Interceptor)** :
   * S'enregistre en tant que lanceur d'accueil système officiel (`CATEGORY_HOME` / `CATEGORY_LEANBACK_LAUNCHER`).
   * `BootReceiver` sur `BOOT_COMPLETED` pour un démarrage direct à l'allumage.
   * `HomeKeyAccessibilityService` pour intercepter la touche Accueil des télécommandes Amazon Fire TV en < 30ms.

2. **Moteur d'Adaptation Matérielle (Hardware Adaptive Engine)** :
   * Analyse au premier allumage (`ActivityManager.MemoryInfo` et `Display.supportedModes`).
   * Profils :
     * **Éco** : RAM ≤ 1.2 Go (Fire TV Lite, Mi Stick). 1080p60 ultra-léger.
     * **Standard** : RAM 1.5 - 2 Go (Google TV, Fire TV 4K). Flou modéré, animations fluides.
     * **Ultra** : RAM ≥ 3 Go (Shield TV Pro). Effets de verre temps réel, 4K cinématique.
     * **Personnalisé** : Réglage unitaire de chaque paramètre graphique.

3. **Préservation Intégrale Widevine L1 & Codecs 4K** :
   * Lance les applications officielles (Netflix, Disney+, Prime, Freebox OQEE, SFR TV, YouTube) via des intents natifs pour préserver les flux 4K HDR, Dolby Vision et Dolby Atmos.

4. **Appairage Immédiat Sans Saisie Télécommande** :
   * Écran d'accueil avec QR Code et code à 6 caractères (ex: `TALO-842`).
   * L'utilisateur valide sur `https://talo-tv.studio.ynh.fr/connect` depuis son smartphone.

5. **Mode Perdu & Verrouillage à Distance** :
   * `RemoteLockOverlayService` affiche un écran plein écran infranchissable si le téléviseur est verrouillé à distance depuis le site web.

---

## 🛠️ Compilation & Déploiement

### Prérequis
* Android Studio (Koala / Ladybug ou supérieur)
* JDK 17
* Android SDK 35 (Android 15)

### Compilation APK
```bash
./gradlew assembleRelease
```
L'APK se trouvera dans `app/build/outputs/apk/release/app-release.apk`.
