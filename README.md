# BlueBoxPro - Version Pre Alpha 0.1.2

BlueBoxPro est une application Android performante conçue pour l'analyse de mouvement et la visualisation cartographique en temps réel, utilisant Jetpack Compose et Material 3.

## Nouveautés de la version 0.1.2 (vs 0.1.1)

*   **Enregistrement de Session (Page 3)** : Ajout d'un nouvel onglet "Cours" dédié au suivi et à la sauvegarde des parcours. Gestion centralisée des sessions via un gestionnaire dédié.
*   **Boussole Haute Précision** : L'Azimuth est désormais arondie à l'entier le plus proche.
*   **Mode Immersif Avancé** : Implémentation du comportement "Swipe to show bars". La barre de navigation système est masquée pour maximiser l'espace, tandis que la barre de statut (heure, batterie) reste visible.
*   **Affichage SOG & COG** : Intégration du Speed Over Ground (Vitesse fond) et du Course Over Ground (Route fond) directement sur le tableau de bord principal.
*   **Optimisation de l'Interface** : Correction des chevauchements sur la Page 2 grâce à un nouveau layout structuré et ajout du support du défilement (Scroll) sur les pages denses.

## Fonctionnalités Actuelles

### 1. Analyse de Mouvement Avancée
*   **Vitesse Haute Fréquence (30Hz)** : Le SOG s'actualise en temps réel via l'IMU pour une fluidité maximale.
*   **Fusion IMU/GPS (Kalman)** : Correction automatique de la dérive par le GPS avec un système de confiance dynamique basé sur l'écart de vitesse.
*   **Boussole Filtrée** : Affichage de l'orientation magnétique stabilisée par Kalman.
*   **Données Multi-sources** : Affichage simultané des vitesses GPS, IMU et fusionnée.

### 2. Cartographie & Topographie
*   **Mini-carte Native** : Intégrée avec suivi automatique et layout anti-chevauchement.
*   **Exploration Plein Écran** : Mode interactif complet avec recentrage manuel.
*   **Topographie** : Lecture en temps réel de l'altitude et des coordonnées GPS (6 décimales).

### 3. Gestion des Parcours (En cours)
*   **Historique des Sessions** : Visualisation des sauvegardes récentes.
*   **Exportation** : Préparation pour l'exportation des données de course (CSV/GPX).

### 4. Architecture & Navigation
*   **Persistence Globale** : Les capteurs restent actifs sur l'ensemble des pages et modes d'affichage.
*   **Navigation à 4 Onglets** : Analyse, Carte, Cours et Réglages.
*   **Modularité** : Code strictement découpé par fonctionnalité et par écran.

### 5. Personnalisation
*   **Mode Sombre** : Support intégral du thème sombre.
*   **Préférences** : Choix des unités (Métrique/Impérial) et de la langue.

## Structure du Projet
*   `Process/` : MovementProcessor, CaptorListener, Filtres de Kalman.
*   `Save/` : SessionManager (Gestion des données enregistrées).
*   `pages/` : Page1, Page2, Page3, Page4, SettingsPage.
*   `ui/components/` : Interface.kt (Composants réutilisables).

## Permissions requises
*   `INTERNET` : Cartographie OpenStreetMap.
*   `ACCESS_FINE_LOCATION` : GPS de haute précision.
*   `MAGNETIC_SENSOR` : Orientation.
