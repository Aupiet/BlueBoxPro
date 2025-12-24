# BlueBoxPro - Version Pre Alpha 0.1

BlueBoxPro est une application Android performante conçue pour l'analyse de mouvement et la visualisation cartographique en temps réel, utilisant Jetpack Compose et Material 3.

## Fonctionnalités Actuelles

### 1. Analyse de Mouvement (IMU & GPS)
*   **Accéléromètre en temps réel** : Affichage des axes X, Y et Z (m/s²).
*   **Calcul de Vitesse Multi-Source** :
    *   Vitesse brute issue de l'IMU (intégration de l'accélération).
    *   Vitesse précise via GPS.
    *   **Fusion de données (Filtre de Kalman)** : Combinaison intelligente de l'accéléromètre et du GPS pour une vitesse stable et réactive.
*   **Filtre de Bruit** : Offset automatique forçant la vitesse à 0 si elle est inférieure à 1 m/s (évite la dérive à l'arrêt).

### 2. Cartographie & Topographie
*   **Mini-carte Interactive** : Intégrée dans l'interface, verrouillée sur la position de l'utilisateur.
*   **Mode Plein Écran** : Un simple clic sur la carte permet de passer en mode navigation libre (zoom et déplacement manuels).
*   **Bouton de Recentrage** : Sur la carte plein écran, un bouton permet de revenir instantanément à sa position réelle.
*   **Infos Précises** : Affichage en temps réel de la Latitude, Longitude et de l'Altitude (Topographie).

### 3. Navigation & Interface
*   **Navigation Intuitive** : Passage d'une page à l'autre par balayage horizontal (Pager) ou via la barre de navigation inférieure.
*   **Interface Moderne** : Entièrement construite avec Material Design 3.

### 4. Personnalisation (Settings)
*   **Mode Sombre** : Bascule dynamique entre Light et Dark mode.
*   **Unités** : Choix entre le système Métrique (m/s, km/h) et Impérial.
*   **Langue** : Support multilingue (Français, Anglais).

## Structure du Projet

*   `Process/` : Cœur logique (Filtre de Kalman, processeur de mouvement, écouteur de capteurs).
*   `pages/` : Définition des écrans de l'application.
*   `ui/components/` : Éléments d'interface réutilisables (Conteneurs de cartes, etc.).

## Permissions requises
*   `INTERNET` : Pour le chargement des tuiles OpenStreetMap.
*   `ACCESS_FINE_LOCATION` : Pour la position GPS précise.
*   `ACCESS_COARSE_LOCATION` : Pour la position approximative.
