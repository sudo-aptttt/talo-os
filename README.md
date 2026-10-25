# 📺 Talo OS — Lanceur TV Haut de Gamme (Google TV Streamer 4K)

Application Android TV native (Kotlin + Jetpack Compose for TV) conçue spécifiquement pour le **Google TV Streamer 4K** afin d'offrir une expérience pure, fluide, sans publicité, avec une interface moderne et raffinée.

---

## 🚀 Fonctionnalités Clés

1. **Démarrage Direct (Boot Interceptor)** :
   * S'enregistre en tant que lanceur d'accueil système officiel (`CATEGORY_HOME` / `CATEGORY_LEANBACK_LAUNCHER`).
   * `BootReceiver` sur `BOOT_COMPLETED` pour un démarrage direct à l'allumage du Streamer.
   * `HomeKeyAccessibilityService` pour intercepter la touche Accueil de la télécommande en moins de 30ms.

2. **Optimisation Matérielle Dédiée au Google TV Streamer 4K** :
   * Tire pleinement parti des **4 Go de RAM** et du processeur du Google TV Streamer 4K.
   * Rendu graphique en profil **Ultra** : effets de verre dépoli en temps réel, animations fluides et affichage 4K HDR natif.
   * Chiffrement matériel local et stockage sécurisé.

3. **Préservation Intégrale des Certifications Google & DRM** :
   * Talo OS fonctionne comme une surcouche propre sans altérer le système Android TV de Google.
   * Lance les applications officielles (Netflix, Disney+, Prime Video, OQEE by Free, YouTube, etc.) via des intents natifs pour préserver les flux 4K HDR, Dolby Vision et Dolby Atmos.
   * **Mises à jour Google préservées** : les correctifs de sécurité mensuels officiels de Google TV continuent de s'installer normalement.

4. **Expérience TV Sans Publicité** :
   * Page d'accueil épurée sans bannières intrusives ni recommandations sponsorisées.
   * TopShelf dynamique mettant en avant vos contenus préférés.
   * Dock flottant et tiroir latéral d'accès rapide.

5. **Sécurité Intégrée & Mode Perdu** :
   * `RemoteLockOverlayService` affiche un écran plein écran infranchissable si l'appareil doit être verrouillé à distance ou protégé par code PIN.

---

## 🛠️ Compilation & Déploiement

### Prérequis
* Android Studio (Koala / Ladybug ou supérieur)
* JDK 17
* Android SDK 35 (Android 15 / Android 14 TV)

### Compilation APK
```bash
./gradlew assembleRelease
```
L'APK se trouve dans `app/build/outputs/apk/release/app-release.apk`.

---

## 📄 Licence

Ce projet est distribué sous la licence **PolyForm Noncommercial License 1.0.0**.  
Toute utilisation, modification et contribution non commerciale est autorisée. La revente commerciale est strictement interdite.
