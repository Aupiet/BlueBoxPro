# BlueBoxPro - Version Pre Alpha 0.1.6

**[English version below]**

BlueBoxPro est une application Android de haute performance dédiée à l'analyse de mouvement nautique et terrestre. Elle fusionne les données GPS et inertielles (IMU) pour offrir une précision de navigation de niveau professionnel.

## Nouveautés de la version 0.1.6

*   **Navigation Adaptative (Smart UI)** : Basculement dynamique entre NavigationBar (portrait) et NavigationRail à droite (paysage).
*   **En-tête Consolidé** : Fusion du temps, de la météo et des métriques live (SOG/COG) dans un bloc unifié.
*   **Zéro Scroll** : Optimisation de la page d'accueil pour une visibilité totale sans défilement en mode paysage.
*   **Attitude en Temps Réel** : Calcul et affichage du tangage (Pitch) et du roulis (Roll).

## Fonctionnalités et Implémentation Technique

### 1. Fusion de Capteurs et EKF (Extended Kalman Filter)
L'application utilise un Filtre de Kalman Étendu personnalisé pour estimer la vitesse (SOG) et le biais de l'accéléromètre.
*   **Prédiction (50Hz)** : Utilise l'accéléromètre (projeté dans le référentiel monde via une matrice de rotation) pour prédire la vitesse entre deux points GPS.
*   **Mise à jour (1Hz)** : Utilise les données du FusedLocationProvider pour corriger la dérive de l'IMU. La matrice de covariance de mesure (R) est ajustée dynamiquement selon la précision rapportée par le GPS.
*   **ZUPT (Zero Velocity Update)** : Détecte l'immobilité (vitesse < seuil + faible bruit accéléro) pour forcer la vitesse à zéro et recalibrer le biais.

### 2. Calcul d'Attitude (Pitch et Roll)
L'attitude de l'appareil est calculée via l'objet RollpitchCalculator :
*   **Implémentation** : Fusion des données de l'accéléromètre (gravité) et du magnétomètre.
*   **Algorithme** : Utilisation de SensorManager.getRotationMatrix et SensorManager.getOrientation. Les angles sont convertis de radians en degrés et lissés pour éviter les sauts brusques.

### 3. Visualisation Cartographique et Rose des Vents
La page Map propose une expérience immersive :
*   **Composant Custom** : La CircularMapWithCompass superpose un Canvas Jetpack Compose sur une MapView (Osmdroid).
*   **Rose des Vents Animée** : L'anneau de la boussole tourne dynamiquement en fonction du COG (Course Over Ground) avec une interpolation fluide via animateFloatAsState.
*   **Coupure Circulaire** : Utilisation de Modifier.clip(CircleShape) pour intégrer parfaitement la carte dans l'instrument de navigation.

### 4. Gestion des Sessions et Persistance
*   **Stockage** : Utilisation de Kotlin Serialization pour sauvegarder les sessions au format JSON.
*   **Filtrage de Trace** : Calcul de distance via la formule de Haversine. Les points sont enregistrés selon un seuil de distance (ex: 3m) et une fréquence (1Hz) paramétrables pour éviter le "jitter" (tremblement du GPS à l'arrêt).
*   **Export** : Génération de fichiers CSV à la volée via un FileProvider pour le partage.

## Stack Technique
*   **UI** : Jetpack Compose et Material 3.
*   **Cartographie** : Osmdroid.
*   **Localisation** : Google Play Services (FusedLocation).
*   **Graphiques** : Vico (pour les profils de vitesse et d'altitude).
*   **Langage** : Kotlin 1.9+ avec Coroutines pour le traitement asynchrone des capteurs.

---

# BlueBoxPro - Pre Alpha Version 0.1.6

BlueBoxPro is a high-performance Android application dedicated to nautical and terrestrial movement analysis. It fuses GPS and inertial (IMU) data to provide professional-grade navigation precision.

## What's New in Version 0.1.6

*   **Adaptive Navigation (Smart UI)**: Dynamic switching between a bottom NavigationBar (portrait) and a right-side NavigationRail (landscape).
*   **Consolidated Header**: Merged time, weather, and live metrics (SOG/COG) into a single, unified block.
*   **Zero Scroll**: Home page optimized for total visibility without scrolling in landscape mode.
*   **Real-Time Attitude**: Calculation and display of Pitch and Roll angles.

## Features and Technical Implementation

### 1. Sensor Fusion and EKF (Extended Kalman Filter)
The app uses a custom Extended Kalman Filter to estimate speed (SOG) and accelerometer bias.
*   **Prediction (50Hz)**: Uses the accelerometer (projected into the world frame via rotation matrix) to predict speed between GPS updates.
*   **Update (1Hz)**: Uses FusedLocationProvider data to correct IMU drift. The measurement covariance matrix (R) is dynamically adjusted based on reported GPS accuracy.
*   **ZUPT (Zero Velocity Update)**: Detects stationarity (speed < threshold + low accel noise) to force speed to zero and recalibrate bias.

### 2. Attitude Calculation (Pitch and Roll)
Device attitude is calculated via the RollpitchCalculator object:
*   **Implementation**: Fuses accelerometer (gravity) and magnetometer data.
*   **Algorithm**: Uses SensorManager.getRotationMatrix and SensorManager.getOrientation. Angles are converted from radians to degrees and smoothed to prevent jitter.

### 3. Map Visualization and Compass Rose
The Map page offers an immersive experience:
*   **Custom Component**: CircularMapWithCompass overlays a Jetpack Compose Canvas on top of an Osmdroid MapView.
*   **Animated Compass Rose**: The compass ring rotates dynamically based on the COG (Course Over Ground) using smooth interpolation with animateFloatAsState.
*   **Circular Clipping**: Uses Modifier.clip(CircleShape) to perfectly integrate the map into the navigation instrument.

### 4. Session Management and Persistence
*   **Storage**: Uses Kotlin Serialization to save sessions in JSON format.
*   **Track Filtering**: Distance calculation via the Haversine formula. Points are recorded based on a configurable distance threshold (e.g., 3m) and frequency (1Hz) to prevent "GPS jitter" when stationary.
*   **Export**: Generates CSV files on-the-fly via FileProvider for easy sharing.

## Tech Stack
*   **UI**: Jetpack Compose and Material 3.
*   **Mapping**: Osmdroid.
*   **Location**: Google Play Services (FusedLocation).
*   **Charts**: Vico (for speed and altitude profiles).
*   **Language**: Kotlin 1.9+ with Coroutines for asynchronous sensor processing.
