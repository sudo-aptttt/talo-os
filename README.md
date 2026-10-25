# 📺 Talo OS — Application Lanceur TV Haut de Gamme

Application Android TV native (Kotlin + Jetpack Compose for TV) conçue pour transformer votre boîtier TV (particulièrement optimisée pour le **Google TV Streamer 4K**, ainsi que Fire TV, Nvidia Shield et Xiaomi TV) en une expérience pure, fluide, sans publicité, avec une interface moderne et raffinée.

---

## 🚀 Fonctionnalités Clés

1. **Démarrage Direct (Boot Interceptor)** :
   * S'enregistre en tant que lanceur d'accueil système officiel (`CATEGORY_HOME` / `CATEGORY_LEANBACK_LAUNCHER`).
   * `BootReceiver` sur `BOOT_COMPLETED` pour un démarrage direct à l'allumage.
   * `HomeKeyAccessibilityService` pour intercepter la touche Accueil des télécommandes en moins de 30ms.

2. **Moteur d'Adaptation Matérielle (Hardware Adaptive Engine)** :
   * Analyse au premier allumage (`ActivityManager.MemoryInfo` et `Display.supportedModes`).
   * Profils dynamiques :
     * **Éco** : RAM ≤ 1.2 Go (Fire TV Lite, Mi Stick). Rendu 1080p60 ultra-léger.
     * **Standard** : RAM 1.5 - 2 Go (Google TV standard). Flou modéré, animations fluides.
     * **Ultra** : RAM ≥ 3 Go (Google TV Streamer 4K, Shield TV Pro). Effets de verre en temps réel, 4K cinématique.
     * **Personnalisé** : Réglage unitaire de chaque paramètre graphique.

3. **Préservation Intégrale Widevine L1 & Codecs 4K** :
   * Lance les applications officielles (Netflix, Disney+, Prime Video, OQEE by Free, YouTube, etc.) via des intents natifs pour préserver les flux 4K HDR, Dolby Vision et Dolby Atmos sans altérer les certifications Google.

4. **Expérience TV Sans Publicité** :
   * Page d'accueil épurée sans bannières publicitaires ni recommandations sponsorisées.
   * TopShelf dynamique mettant en avant vos contenus préférés.
   * Dock flottant et tiroir latéral d'accès rapide.

5. **Sécurité Intégrée & Mode Perdu** :
   * `RemoteLockOverlayService` affiche un écran plein écran infranchissable si l'appareil doit être verrouillé à distance ou protégé par code PIN.
   * Chiffrement local des préférences sensibles.

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

---

## 📄 Licence

Ce projet est distribué sous la licence **PolyForm Noncommercial License 1.0.0**.  
Toute utilisation, modification et contribution non commerciale est autorisée. La revente commerciale est strictement interdite.
