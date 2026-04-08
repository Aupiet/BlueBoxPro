# BlueBoxPro - Open Source Racing Yacht Optimisation

A high-precision inertial measurement unit and nautical telemetry center, right in your pocket.

**[Version française ci-dessous]**

### About the project

The BlueBoxPro project stems from student research at the intersection of naval architecture and software engineering. It aims to solve a major problem for amateur skippers and one-design racers: accessing dynamic boat data (pitch, roll) without installing complex and expensive NMEA 2000 systems. By leveraging native smartphone sensors (GPS, accelerometer, gyroscope) through rigorous mathematical filtering, this application acts as a "Plug & Play" performance logger. Fully Open Source, it is designed to be robust, energy-efficient, and reliable in the marine environment.

### Current Versioning

The project is currently deployed in version Pre-Alpha 0.1.7. This development phase focuses on the stability of data acquisition (GPS and IMU), sensor fusion (EKF), and persistent session recording. Tactical decision support features (VMG, waypoints) will be the subject of future iterations.

### New in version 0.1.7

*   **Recording Persistence**: Disables the sleep timeout (5 min) as long as a recording is active, ensuring an uninterrupted track.
*   **Fail-Safe Backup**: Automatic backup of active sessions if the application is closed manually or stopped by the system.
*   **Dynamic Notification**: Visual adaptation of the notification (title "Recording..." and dedicated icon) for clear tracking from the notification panel.
*   **Adaptive Navigation (Smart UI)**: Dynamic switching between NavigationBar (portrait) and right NavigationRail (landscape).
*   **Consolidated Header**: Merges time, weather, and live metrics (SOG/COG) into a unified block.

### Features and Technical Implementation

Currently, the application is an Android monolith offering the following capabilities:

1.  **Sensor Fusion and EKF (Extended Kalman Filter)**
    The app uses a custom Extended Kalman Filter to estimate speed (SOG) and accelerometer bias.
    *   **Prediction (50Hz)**: Uses the accelerometer to predict speed between GPS updates.
    *   **Update (1Hz)**: Uses FusedLocationProvider data to correct IMU drift.

2.  **Background Service and Persistence**
    The application relies on a robust Foreground Service:
    *   **Lifecycle**: The service manages its own lifecycle and sensor updates independently of the UI (BlueBoxService.kt and CaptorListener.kt).
    *   **Timeout Management**: An automatic sleep system closes the service after 5 minutes of background inactivity to save battery, unless a recording is active.
    *   **Robustness**: Implementation of `onTaskRemoved` to ensure no data loss if the user swipes the app away.

3.  **Attitude Calculation (Pitch and Roll)**
    Device attitude is calculated via RollpitchCalculator.kt, fusing accelerometer and magnetometer data for optimal stability.

4.  **Map Visualization**
    The Map page offers an immersive experience via `CircularMapWithCompass`, overlaying a Jetpack Compose Canvas on top of an Osmdroid MapView with an animated compass rose.

5.  **Session Management and Persistence**
    *   **Storage**: Uses Kotlin Serialization to save sessions in JSON format.
    *   **Track Filtering**: Distance calculation via the Haversine formula with jitter management.
    *   **Export**: Generates CSV files via FileProvider for post-race analysis.

### Roadmap and Planned Improvements

*   [x] Implémentation du Filtre de Kalman pour le roulis (Roll) et le tangage (Pitch).
*   [x] Enregistrement hors-ligne ininterrompu des sessions de navigation (Service en arrière-plan).
*   [ ] Integration of VMG (Velocity Made Good) calculation and Polar target interpolation.
*   [ ] Architecture refactoring to KMP (Kotlin Multiplatform) to prepare for iOS portability.
*   [ ] "Water Lock" touchscreen mode to block unintended inputs caused by sea spray.
*   [ ] Instant tactical Waypoint marking (Man Over Board, Laylines, Marks).
*   [ ] NMEA 0183 over IP / BLE connectivity for direct interfacing with onboard sensors.

### Tech Stack

*   **Language**: Kotlin 1.9+ with Coroutines and SnapshotFlow. Essential tools for managing high-frequency hardware sampling without saturating the main thread.
*   **User Interface**: Jetpack Compose and Material 3. Declarative and high-performance deployment, with adaptive interface (Smart UI).
*   **Mapping**: Osmdroid.
*   **Location**: Google Play Services (FusedLocation).
*   **Charts**: Vico.

### Getting Started (Local Development)

1.  Clone the repository locally: `git clone https://github.com/your-organization/BlueBoxPro.git`
2.  Open the project with the Android Studio IDE (Hedgehog version or higher recommended).
3.  Synchronize Gradle dependencies (refer to build.gradle.kts and gradle/libs.versions.toml files).
4.  Build and run on a physical Android device (inertial sensor behavior cannot be accurately simulated on a standard emulator).

**Maritime Disclaimer**: This software is provided exclusively as a performance analysis tool. It in no way replaces certified onboard electronics, official navigation charts, or seamanship and human judgment regarding navigation and safety at sea.

---

## Version Française

# BlueBoxPro - Optimisation Open Source pour Voiliers de Course

Une centrale inertielle et de télémétrie nautique de haute précision, directement dans votre poche.

### A propos du projet

Le projet BlueBoxPro est né d'une recherche étudiante croisant l'architecture navale et l'ingénierie logicielle. Il vise à résoudre un problème majeur pour les skippers amateurs et les régatiers en monotypie : l'accès aux données dynamiques du bateau (tangage, roulis) sans avoir à installer des systèmes NMEA 2000 complexes et onéreux. En exploitant les capteurs natifs du smartphone (GPS, accéléromètre, gyroscope) via un filtrage mathématique rigoureux, cette application agit comme un enregistreur de performance "Plug & Play". Entièrement Open Source, elle est conçue pour être robuste, économe en énergie et fiable en milieu marin.

### Versionnement Actuel

Le projet est actuellement déployé en version Pre-Alpha 0.1.7. Cette phase de développement se concentre sur la stabilité de l'acquisition des données (GPS et IMU), la fusion de capteurs (EKF), et l'enregistrement persistant des sessions. Les fonctionnalités d'aide à la décision tactique (VMG, waypoints) feront l'objet d'itérations futures.

### Nouveautés de la version 0.1.7

*   **Persistance de l'Enregistrement** : Désactivation du timeout de mise en veille (5 min) tant qu'un enregistrement est actif, garantissant une trace ininterrompue.
*   **Sauvegarde de Sécurité** : Sauvegarde automatique des sessions en cours si l'application est fermée manuellement ou arrêtée par le système.
*   **Notification Dynamique** : Adaptation visuelle de la notification (titre "Enregistrement..." et icône dédiée) pour un suivi clair depuis le volet de notifications.
*   **Navigation Adaptative (Smart UI)** : Basculement dynamique entre NavigationBar (portrait) et NavigationRail à droite (paysage).
*   **En-tête Consolidé** : Fusion du temps, de la météo et des métriques live (SOG/COG) dans un bloc unifié.

### Fonctionnalités et Implémentation Technique

Actuellement, l'application est un monolithe Android offrant les capacités suivantes :

1.  **Fusion de Capteurs et EKF (Extended Kalman Filter)**
    L'application utilise un Filtre de Kalman Étendu personnalisé pour estimer la vitesse (SOG) et le biais de l'accéléromètre.
    *   **Prédiction (50Hz)** : Utilise l'accéléromètre pour prédire la vitesse entre deux mises à jour GPS.
    *   **Mise à jour (1Hz)** : Utilise les données FusedLocationProvider pour corriger la dérive de l'IMU.

2.  **Service en Arrière-plan et Persistance**
    L'application s'appuie sur un Foreground Service robuste :
    *   **Cycle de vie** : Le service gère son propre cycle de vie et les mises à jour des capteurs indépendamment de l'UI (BlueBoxService.kt et CaptorListener.kt).
    *   **Gestion du Timeout** : Un système de mise en veille automatique ferme le service après 5 minutes d'inactivité en arrière-plan pour économiser la batterie, sauf si un enregistrement est actif.
    *   **Robustesse** : Implémentation de `onTaskRemoved` pour s'assurer qu'aucune donnée n'est perdue si l'utilisateur ferme l'application.

3.  **Calcul d'Attitude (Roulis et Tangage)**
    L'attitude de l'appareil est calculée via RollpitchCalculator.kt, fusionnant les données de l'accéléromètre et du magnétomètre pour une stabilité optimale.

4.  **Visualisation Cartographique**
    La page Map offre une expérience immersive via `CircularMapWithCompass`, superposant un Canvas Jetpack Compose sur une MapView Osmdroid avec une rose des vents animée.

5.  **Gestion et Persistance des Sessions**
    *   **Stockage** : Utilise Kotlin Serialization pour sauvegarder les sessions au format JSON.
    *   **Filtrage de Trace** : Calcul de distance via la formule de Haversine avec gestion du jitter.
    *   **Export** : Génère des fichiers CSV via FileProvider pour l'analyse post-régate.

### Feuille de Route et Améliorations Prévues

*   [x] Implémentation du Filtre de Kalman pour le roulis (Roll) et le tangage (Pitch).
*   [x] Enregistrement hors-ligne ininterrompu des sessions de navigation (Service en arrière-plan).
*   [ ] Intégration du calcul de la VMG (Velocity Made Good) et interpolation des cibles de Polaires.
*   [ ] Refonte de l'architecture vers KMP (Kotlin Multiplatform) pour préparer la portabilité iOS.
*   [ ] Mode "Verrouillage Tactile" (Water Lock) pour bloquer les saisies intempestives dues aux embruns.
*   [ ] Marquage de Waypoints tactiques instantanés (Man Over Board, Laylines, Bouées).
*   [ ] Connectivité NMEA 0183 sur IP / BLE pour interfaçage direct avec les capteurs du bord.

### Pile Technique

*   **Langage** : Kotlin 1.9+ avec Coroutines et SnapshotFlow. Outils essentiels pour gérer la haute fréquence d'échantillonnage matériel sans saturer le thread principal.
*   **Interface Utilisateur** : Jetpack Compose et Material 3. Déploiement déclaratif et performant, avec interface adaptative (Smart UI).
*   **Cartographie** : Osmdroid.
*   **Localisation** : Google Play Services (FusedLocation).
*   **Graphiques** : Vico.

### Installation et Utilisation (Développement Local)

1.  Cloner le dépôt localement : `git clone https://github.com/votre-organisation/BlueBoxPro.git`
2.  Ouvrir le projet avec l'IDE Android Studio (Version Hedgehog ou supérieure recommandée).
3.  Synchroniser les dépendances Gradle (se référer aux fichiers build.gradle.kts et gradle/libs.versions.toml).
4.  Compiler et exécuter sur un appareil Android physique (le comportement des capteurs inertiels ne peut pas être simulé avec précision sur un émulateur standard).

**Avertissement Maritime** : Ce logiciel est fourni exclusivement à titre d'outil d'analyse des performances. Il ne remplace en aucun cas l'électronique de bord certifiée, les cartes de navigation officielles, ni le jugement marin pour la navigation et la sécurité en mer.
