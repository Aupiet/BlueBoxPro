# BlueBoxPro - Version Pre Alpha 0.1.4

**[English version below]**

BlueBoxPro est une application Android performante conçue pour l'analyse de mouvement et la visualisation cartographique en temps réel, utilisant Jetpack Compose et Material 3. Elle combine les données des capteurs inertiels (IMU) et du GPS pour fournir une mesure de vitesse et d'orientation ultra-précise.

## Nouveautés de la version 0.1.4 (vs 0.1.3)

*   **Filtrage Avancé de la Vitesse** : Implémentation du **Zero Velocity Update (ZUPT)** et d'un **Filtre Passe-Haut (HPF)** pour éliminer radicalement la dérive (drift) de la vitesse IMU à l'arrêt et pendant le mouvement.
*   **Enregistrement de Sessions (Tracking)** : 
    *   Nouveau module d'enregistrement capable de capturer des traces GPS complètes (ID, Latitude, Longitude, Altitude, SOG, COG).
    *   **Cadence 1 Hz** : Les points sont enregistrés à une fréquence fixe de 1 point par seconde.
    *   **Persistence Arrière-plan** : L'enregistrement continue même si vous changez d'onglet ou si vous allez dans les paramètres.
*   **Système de Sauvegarde JSON** : Migration vers **Kotlinx Serialization**. Les sessions sont désormais sauvegardées dans un fichier `sessiontrace.json` lors de la fin d'un enregistrement et à la fermeture de l'application.
*   **Internationalisation Dynamique** : Support complet du Français et de l'Anglais avec basculement instantané sans redémarrage manuel (via `AppCompatDelegate`).
*   **Évolution du Système d'Unités** : Distinction entre **km/h** et **m/s** dans le système métrique.
*   **Interface Épurée** : Barre de navigation modernisée avec icônes agrandies (32dp) et suppression des labels textuels pour un look minimaliste.

## Fonctionnalités Actuelles

### 1. Analyse de Mouvement Avancée
*   **Vitesse Haute Fréquence (30Hz)** : Le SOG s'actualise en temps réel via l'accéléromètre (IMU) pour une fluidité maximale.
*   **Fusion IMU/GPS (Filtre de Kalman)** : Correction intelligente de la dérive de l'accéléromètre par les données GPS.
*   **ZUPT & HPF** : Algorithmes de stabilisation pour une vitesse IMU précise à l'arrêt.

### 2. Cartographie & Enregistrement
*   **Navigation Carte** : Suivi automatique de la position sur fond OpenStreetMap.
*   **Gestionnaire de Sessions** : Historique des sessions enregistrées avec détails (date, durée, distance, points GPS).
*   **Sauvegarde JSON** : Persistance robuste des données de course.

### 3. Gestion des Unités & Langues
*   **Unités** : Métrique (km/h ou m/s), Impérial (mph), Nautique (kn).
*   **Langues** : Français, Anglais (basculement via les réglages).

---

# BlueBoxPro - Pre Alpha Version 0.1.4

BlueBoxPro is a high-performance Android application designed for real-time movement analysis and map visualization, built with Jetpack Compose and Material 3. It combines inertial sensor data (IMU) and GPS to provide ultra-precise speed and orientation measurements.

## What's New in Version 0.1.4 (vs 0.1.3)

*   **Advanced Speed Filtering**: Implementation of **Zero Velocity Update (ZUPT)** and a **High Pass Filter (HPF)** to radically eliminate IMU speed drift when stationary and during movement.
*   **Session Recording (Tracking)**:
    *   New recording module capable of capturing full GPS tracks (ID, Latitude, Longitude, Altitude, SOG, COG).
    *   **1 Hz Cadence**: Points are recorded at a fixed frequency of 1 point per second.
    *   **Background Persistence**: Recording continues even if you switch tabs or go into settings.
*   **JSON Storage System**: Migration to **Kotlinx Serialization**. Sessions are now saved in a `sessiontrace.json` file when a recording ends and when the app closes.
*   **Dynamic Internationalization**: Full support for French and English with instant switching without manual restart (via `AppCompatDelegate`).
*   **Unit System Evolution**: Distinction between **km/h** and **m/s** in the Metric system.
*   **Sleek Interface**: Modernized navigation bar with enlarged icons (32dp) and removal of text labels for a minimalist look.

## Current Features

### 1. Advanced Movement Analysis
*   **High Frequency Speed (30Hz)**: SOG updates in real-time via the accelerometer (IMU) for maximum fluidity.
*   **IMU/GPS Fusion (Kalman Filter)**: Intelligent correction of accelerometer drift using GPS data.
*   **ZUPT & HPF**: Stabilization algorithms for precise IMU speed when stationary.

### 2. Mapping & Recording
*   **Map Navigation**: Automatic position tracking on OpenStreetMap.
*   **Session Manager**: History of recorded sessions with details (date, duration, distance, GPS points).
*   **JSON Persistence**: Robust race data storage.

### 3. Units & Language Management
*   **Units**: Metric (km/h or m/s), Imperial (mph), Nautical (kn).
*   **Languages**: French, English (switching via settings).

## Project Structure
*   `Process/` : `MovementProcessor` (Core engine), `CaptorListener` (Sensor management), `MovementResult`, `SimpleKalmanFilter`.
*   `Save/` : `SessionManager` (JSON persistence), `Recording` (Session capture).
*   `pages/` : Application screens (Analysis, Map, Recording, Settings).

## Required Permissions
*   `INTERNET`, `ACCESS_FINE_LOCATION`, `WRITE_EXTERNAL_STORAGE`.
