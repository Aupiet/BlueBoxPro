# BlueBoxPro - Version Pre Alpha 0.1.3

BlueBoxPro est une application Android performante conçue pour l'analyse de mouvement et la visualisation cartographique en temps réel, utilisant Jetpack Compose et Material 3. Elle combine les données des capteurs inertiels (IMU) et du GPS pour fournir une mesure de vitesse et d'orientation ultra-précise.

## Nouveautés de la version 0.1.3 (vs 0.1.2)

*   **Gestion des Unités (Triple Système)** : Support complet du système **Nautique** (Noeuds). L'application bascule désormais dynamiquement entre Métrique (km/h, m), Impérial (mph, ft) et Nautique (kn, m) sur tous les écrans.
*   **Fiabilité GPS & Sécurité** : 
    *   **Filtrage par Précision** : Les données GPS ne sont traitées que si l'exactitude (accuracy) est inférieure à un seuil défini (ex: 50m).
    *   **Timeout Intelligent** : Réinitialisation automatique de la vitesse GPS après 5 secondes sans signal valide pour éviter les valeurs "fantômes".
*   **Paramètres Avancés** : Nouvel écran de configuration technique permettant de visualiser les seuils de déclenchement et les paramètres de filtrage.
*   **Amélioration UI Page 2** : Affichage en temps réel de la précision du signal GPS (en mètres ou pieds selon l'unité) pour aider l'utilisateur à évaluer la qualité de ses données.
*   **Refonte de l'Architecture de Données** : Introduction de la classe `MovementResult` qui centralise toutes les conversions d'unités et garantit que l'interface affiche exactement ce que les algorithmes de fusion calculent.

## Fonctionnalités Actuelles

### 1. Analyse de Mouvement Avancée
*   **Vitesse Haute Fréquence (30Hz)** : Le SOG s'actualise en temps réel via l'accéléromètre (IMU) pour une fluidité maximale, même entre deux points GPS.
*   **Fusion IMU/GPS (Filtre de Kalman)** : Correction intelligente de la dérive de l'accéléromètre par les données GPS.
*   **Boussole Stabilisée** : Orientation magnétique filtrée et arrondie pour une lecture stable du cap.
*   **Données Multi-sources** : Comparaison directe entre vitesse brute GPS, vitesse calculée IMU et vitesse finale fusionnée.

### 2. Cartographie & Topographie
*   **Mini-carte Native** : Suivi automatique de la position avec un layout optimisé pour éviter les chevauchements de texte.
*   **Exploration Plein Écran** : Accès à une carte interactive complète via un simple clic sur la minicarte.
*   **Topographie** : Altitude précise et coordonnées GPS haute résolution.

### 3. Gestion des Unités & Internationalisation
*   **Conversion Dynamique** : Basculement instantané des unités de vitesse (km/h, mph, noeuds) et de distance/hauteur (m, ft).
*   **Multilingue** : Support initial du Français et de l'Anglais.

### 4. Architecture & Ergonomie
*   **Persistence en Arrière-plan** : Les capteurs et le processeur de mouvement continuent de fonctionner de manière cohérente lors de la navigation entre les onglets.
*   **Mode Immersif** : Masquage de la barre de navigation système (Swipe-to-show) pour libérer de l'espace visuel.
*   **Thème Dynamique** : Support complet du Mode Sombre (Dark Mode).

## Structure du Projet
*   `Process/` : `MovementProcessor` (Cœur de calcul), `CaptorListener` (Gestion des capteurs), `MovementResult` (Encapsulation des données), `SimpleKalmanFilter`.
*   `pages/` : `Page1` (Analyse), `Page2` (Carte), `Page3` (Session/Cours), `SettingsPage` (Réglages), `AdvancedSettingsPage`.
*   `ui/components/` : `Interface.kt` (Composants graphiques et layouts cartographiques).

## Permissions requises
*   `INTERNET` : Téléchargement des tuiles OpenStreetMap.
*   `ACCESS_FINE_LOCATION` : Accès aux données satellites haute précision.
*   `MAGNETIC_SENSOR / ACCELEROMETER` : Calcul de l'orientation et de l'accélération linéaire.
