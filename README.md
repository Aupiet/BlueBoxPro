# BlueBoxPro - Version Pre Alpha 0.1.7

**[English version below]**

BlueBoxPro est une application Android de haute performance dédiée à l'analyse de mouvement nautique et terrestre. Elle fusionne les données GPS et inertielles (IMU) pour offrir une précision de navigation de niveau professionnel.

## Nouveautés de la version 0.1.7

*   **Persistance de l'Enregistrement** : Désactivation du timeout de mise en veille (5 min) tant qu'un enregistrement est actif, garantissant une trace ininterrompue.
*   **Sauvegarde de Sécurité** : Sauvegarde automatique des sessions en cours si l'application est fermée manuellement ou arrêtée par le système.
*   **Notification Dynamique** : Adaptation visuelle de la notification (titre "Enregistrement..." et icône dédiée) pour un suivi clair depuis le volet de notifications.
*   **Navigation Adaptative (Smart UI)** : Basculement dynamique entre NavigationBar (portrait) et NavigationRail à droite (paysage).
*   **En-tête Consolidé** : Fusion du temps, de la météo et des métriques live (SOG/COG) dans un bloc unifié.

## Fonctionnalités et Implémentation Technique

### 1. Fusion de Capteurs et EKF (Extended Kalman Filter)
L'application utilise un Filtre de Kalman Étendu personnalisé pour estimer la vitesse (SOG) et le biais de l'accéléromètre.
*   **Prédiction (50Hz)** : Utilise l'accéléromètre pour prédire la vitesse entre deux points GPS.
*   **Mise à jour (1Hz)** : Utilise les données du FusedLocationProvider pour corriger la dérive de l'IMU.

### 2. Service d'Arrière-plan et Persistance
L'application s'appuie sur un *Foreground Service* robuste :
*   **Cycle de vie** : Le service gère son propre cycle de vie et celui des capteurs, indépendamment de l'interface utilisateur.
*   **Gestion du Timeout** : Un système de veille automatique ferme le service après 5 minutes d'inactivité en arrière-plan pour économiser la batterie, sauf si un enregistrement est actif.
*   **Robustesse** : Utilisation de `onTaskRemoved` pour garantir qu'aucune donnée n'est perdue si l'utilisateur "swipe" l'application.

### 3. Calcul d'Attitude (Pitch et Roll)
L'attitude de l'appareil est calculée via l'objet RollpitchCalculator en fusionnant l'accéléromètre et le magnétomètre pour une stabilité optimale.

### 4. Visualisation Cartographique
La page Map propose une expérience immersive via `CircularMapWithCompass` qui superpose un Canvas Jetpack Compose sur une MapView (Osmdroid) avec une rose des vents animée.

### 5. Gestion des Sessions et Persistance
*   **Stockage** : Utilisation de Kotlin Serialization pour sauvegarder les sessions au format JSON.
*   **Filtrage de Trace** : Calcul de distance via la formule de Haversine avec gestion du jitter.
*   **Export** : Génération de fichiers CSV via un FileProvider.

## Stack Technique
*   **UI** : Jetpack Compose et Material 3.
*   **Cartographie** : Osmdroid.
*   **Localisation** : Google Play Services (FusedLocation).
*   **Graphiques** : Vico.
*   **Langage** : Kotlin 1.9+ avec Coroutines et SnapshotFlow.

---

# BlueBoxPro - Pre Alpha Version 0.1.7

BlueBoxPro is a high-performance Android application dedicated to nautical and terrestrial movement analysis. It fuses GPS and inertial (IMU) data to provide professional-grade navigation precision.

## What's New in Version 0.1.7

*   **Recording Persistence**: Disabling the 5-minute background timeout while a recording is active, ensuring an uninterrupted track.
*   **Safety Auto-Save**: Immediate saving of ongoing sessions if the app is manually closed or killed by the system.
*   **Dynamic Notification**: Visual adaptation of the notification (title "Recording..." and dedicated icon) for clear tracking from the notification shade.
*   **Adaptive Navigation (Smart UI)**: Dynamic switching between a bottom NavigationBar (portrait) and a right-side NavigationRail (landscape).
*   **Consolidated Header**: Merged time, weather, and live metrics (SOG/COG) into a single, unified block.

## Features and Technical Implementation

### 1. Sensor Fusion and EKF (Extended Kalman Filter)
The app uses a custom Extended Kalman Filter to estimate speed (SOG) and accelerometer bias.
*   **Prediction (50Hz)**: Uses the accelerometer to predict speed between GPS updates.
*   **Update (1Hz)**: Uses FusedLocationProvider data to correct IMU drift.

### 2. Background Service and Persistence
The application relies on a robust Foreground Service:
*   **Lifecycle**: The service manages its own lifecycle and sensor updates independently of the UI.
*   **Timeout Management**: An automatic sleep system closes the service after 5 minutes of background inactivity to save battery, unless a recording is active.
*   **Robustness**: Implementation of `onTaskRemoved` to ensure no data loss if the user swipes the app away.

### 3. Attitude Calculation (Pitch and Roll)
Device attitude is calculated via the RollpitchCalculator object, fusing accelerometer and magnetometer data for optimal stability.

### 4. Map Visualization
The Map page offers an immersive experience via `CircularMapWithCompass`, overlaying a Jetpack Compose Canvas on top of an Osmdroid MapView with an animated compass rose.

### 5. Session Management and Persistence
*   **Storage**: Uses Kotlin Serialization to save sessions in JSON format.
*   **Track Filtering**: Distance calculation via the Haversine formula with jitter management.
*   **Export**: Generates CSV files via FileProvider.

## Tech Stack
*   **UI**: Jetpack Compose and Material 3.
*   **Mapping**: Osmdroid.
*   **Location**: Google Play Services (FusedLocation).
*   **Charts**: Vico.
*   **Language**: Kotlin 1.9+ with Coroutines and SnapshotFlow.
