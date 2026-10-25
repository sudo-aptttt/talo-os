<div align="center">

# 📺 Talo OS

*L'expérience télévisuelle pure, fluide et sans publicité conçue exclusivement pour le **Google TV Streamer 4K**.*

<br/>

[![Platform](https://img.shields.io/badge/PLATFORM-GOOGLE%20TV%20STREAMER%204K-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/sudo-aptttt/talo-tv)
[![Downloader Code](https://img.shields.io/badge/DOWNLOADER%20CODE-6790525-FF6F00?style=for-the-badge&logo=googleplay&logoColor=white)](http://aftv.news/6790525)

[![Language](https://img.shields.io/badge/LANGUAGE-KOTLIN-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI Engine](https://img.shields.io/badge/UI-COMPOSE%20FOR%20TV-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose/tv)
[![Footprint](https://img.shields.io/badge/APK%20FOOTPRINT-1.38%20MB-success?style=for-the-badge)](https://github.com/sudo-aptttt/talo-tv/releases/latest)

[![Privacy](https://img.shields.io/badge/ADS-100%25%20Z%C3%89RO%20PUB-brightgreen?style=for-the-badge)](https://github.com/sudo-aptttt/talo-tv)
[![License](https://img.shields.io/badge/LICENSE-POLYFORM%20NONCOMMERCIAL-red?style=for-the-badge)](LICENSE)

<br/>

[![Android](https://img.shields.io/badge/Android-14%2B%20(API%2034)-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose%20TV-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![DRM](https://img.shields.io/badge/DRM-Widevine%20L1%204K-orange?style=flat-square)](https://github.com/sudo-aptttt/talo-tv)
[![Status](https://img.shields.io/badge/Status-En%20D%C3%A9veloppement%20(Alpha)-orange?style=flat-square&logo=github&logoColor=white)](https://github.com/sudo-aptttt/talo-tv)
[![Wiki](https://img.shields.io/badge/Documentation-Wiki-blueviolet?style=flat-square&logo=gitbook&logoColor=white)](https://github.com/sudo-aptttt/talo-tv/wiki)

</div>

---

## 📲 Installation Rapide (Google TV Streamer 4K)

### Via l'application Downloader :
1. Installez et ouvrez l'application **Downloader** sur votre Google TV.
2. Entrez simplement le code rapide permanent :
   ### `6790525`
3. Cliquez sur **Go** : le téléchargement démarre immédiatement et la TV vous propose d'installer Talo OS.

> [!TIP]
> Ce code est permanent : il délivre automatiquement la toute dernière version officielle de Talo OS à chaque mise à jour.

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
