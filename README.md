# BlueBoxPro - Version Pre Alpha 0.1.5

**[English version below]**

BlueBoxPro est une application Android performante conçue pour l'analyse de mouvement et la visualisation cartographique en temps réel, utilisant Jetpack Compose et Material 3. Elle combine les données des capteurs inertiels (IMU) et du GPS pour fournir une mesure de vitesse et d'orientation ultra-précise via un filtre de Kalman étendu (EKF).

## Nouveautés de la version 0.1.5 (vs 0.1.4)

*   **Refonte de l'Expérience Utilisateur (UX)** : 
    *   **Interface Épurée** : Les données techniques secondaires (Moyenne, GPS brut, IMU brut) ont été retirées de la page d'accueil pour une lisibilité maximale en navigation.
    *   **Paramètres Avancés Centralisés** : Création d'une page dédiée aux réglages fins du moteur de calcul, incluant désormais les outils de diagnostic en temps réel.
*   **Maintenance & Diagnostic** :
    *   **Bouton Reset Global** : Ajout d'une fonction de réinitialisation complète des filtres, de la vitesse et de la position dans les paramètres avancés.
    *   **Monitoring Temps Réel** : Déplacement des indicateurs de sources de vitesse (IMU vs GPS) dans la section de configuration avancée pour les utilisateurs experts.
*   **Amélioration du Moteur de Calcul** :
    *   Optimisation des transitions entre les sources de données lors de la perte du signal GPS.
    *   Meilleure gestion du cycle de vie des capteurs pour réduire la consommation batterie lors des phases de veille.

## Fonctionnalités Actuelles

### 1. Analyse de Mouvement Avancée
*   **Vitesse Haute Fréquence (50Hz)** : Calcul du SOG en temps réel via l'accéléromètre projeté dans le référentiel monde.
*   **Extended Kalman Filter (EKF)** : Fusion intelligente IMU/GPS avec estimation du biais de l'accéléromètre pour une précision chirurgicale.
*   **Filtrage Avancé** : Utilisation du **Zero Velocity Update (ZUPT)** et de filtres passe-bas (LPF) pour éliminer le bruit des capteurs.

### 2. Cartographie & Enregistrement
*   **Suivi Temps Réel** : Visualisation de la position et du cap (COG) sur OpenStreetMap.
*   **Historique des Sessions** : Gestionnaire complet des traces enregistrées avec statistiques détaillées (vitesse max, moyenne, distance, durée).
*   **Visualisation de Traces** : Consultation des parcours passés avec graphiques interactifs de vitesse et de cap.

### 3. Personnalisation & Internationalisation
*   **Unités Flexibles** : Support complet Métrique (km/h, m/s), Impérial (mph) et Nautique (kn).
*   **Multilingue** : Support natif Français/Anglais avec changement à la volée.
*   **Mode Sombre** : Interface adaptative pour une utilisation de jour comme de nuit.

---

# BlueBoxPro - Pre Alpha Version 0.1.5

BlueBoxPro is a high-performance Android application designed for real-time movement analysis and map visualization, built with Jetpack Compose and Material 3. It combines inertial sensor data (IMU) and GPS to provide ultra-precise speed and orientation measurements using an Extended Kalman Filter (EKF).

## What's New in Version 0.1.5 (vs 0.1.4)

*   **User Experience (UX) Overhaul**:
    *   **Streamlined Interface**: Secondary technical data (Average, raw GPS, raw IMU) has been removed from the home page for maximum readability during navigation.
    *   **Centralized Advanced Settings**: Created a dedicated page for fine-tuning the calculation engine, now including real-time diagnostic tools.
*   **Maintenance & Diagnostics**:
    *   **Global Reset Button**: Added a complete filter, speed, and position reset function within the advanced settings.
    *   **Real-time Monitoring**: Moved speed source indicators (IMU vs GPS) to the advanced configuration section for expert users.
*   **Engine Improvements**:
    *   Optimized transitions between data sources during GPS signal loss.
    *   Better sensor lifecycle management to reduce battery consumption during standby phases.

## Current Features

### 1. Advanced Movement Analysis
*   **High Frequency Speed (50Hz)**: SOG calculation in real-time via accelerometer projection into the world frame.
*   **Extended Kalman Filter (EKF)**: Intelligent IMU/GPS fusion with accelerometer bias estimation for surgical precision.
*   **Advanced Filtering**: Implementation of **Zero Velocity Update (ZUPT)** and Low-Pass Filters (LPF) to eliminate sensor noise.

### 2. Mapping & Recording
*   **Real-time Tracking**: Visualization of position and heading (COG) on OpenStreetMap.
*   **Session History**: Full manager for recorded tracks with detailed statistics (max speed, average, distance, duration).
*   **Track Visualization**: View past trips with interactive speed and heading charts.

### 3. Customization & Internationalization
*   **Flexible Units**: Full support for Metric (km/h, m/s), Imperial (mph), and Nautical (kn).
*   **Multilingual**: Native French/English support with on-the-fly switching.
*   **Dark Mode**: Adaptive interface for day or night use.

## Project Structure
*   `Process/` : `MovementProcessor` (Core EKF engine), `CaptorListener` (Sensor management), `EkfSpeedEstimator`.
*   `Save/` : `SessionManager` (JSON persistence), `Recording` (Session capture).
*   `pages/` : Application screens (Analysis, Map, Recording, Settings, Advanced Settings).
*   `ui/` : Reusable components, themes, and map abstractions.
