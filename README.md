# BlueBoxPro - Version Pre Alpha 0.1.1

BlueBoxPro est une application Android performante conçue pour l'analyse de mouvement et la visualisation cartographique en temps réel, utilisant Jetpack Compose et Material 3.

## Nouveautés de la version 0.1.1 (vs 0.1.0)

*   **Persistance Globale** : Les capteurs et le GPS ne s'arrêtent plus lors de la navigation entre les pages grâce à une initialisation centralisée dans l'activité principale.
*   **Réactivité accrue** : Actualisation du SOG (vitesse) à 30Hz via l'accéléromètre, corrigée périodiquement par le GPS.
*   **Boussole et Topographie** : Ajout de l'affichage de l'Azimuth (orientation magnétique) et de l'Altitude.
*   **Mode Carte Étendu** : Passage en mode plein écran interactif avec bouton de recentrage manuel.
*   **Architecture Modulaire** : Restructuration complète du code avec un fichier par écran et une séparation nette entre logique de calcul et interface.

## Fonctionnalités Actuelles

### 1. Analyse de Mouvement Avancée
*   **Vitesse Haute Fréquence (30Hz)** : Le SOG (Speed Over Ground) s'actualise en temps réel via l'IMU pour une fluidité maximale.
*   **Fusion IMU/GPS (Kalman)** : Correction automatique de la dérive de l'accéléromètre par le GPS avec un système de confiance dynamique (plus l'écart est faible, plus le GPS est écouté).
*   **Boussole Magnétique** : Affichage de l'Azimuth (orientation du téléphone) via les capteurs de champ magnétique.
*   **COG (Course Over Ground)** : Direction réelle du mouvement calculée par le GPS (actif au-dessus de 1.0 m/s).
*   **Filtres de Bruit Intelligents** : Seuils de vitesse distincts pour l'IMU (0.5 m/s) et le GPS (0.8 m/s) pour une stabilité parfaite à l'arrêt.

### 2. Cartographie & Topographie
*   **Mini-carte Native** : Intégrée en Page 2 avec suivi automatique de la position.
*   **Exploration Plein Écran (Page 4)** : Mode interactif complet accessible d'un simple clic sur la minicarte.
*   **Contrôle du Recentrage** : Recentrage manuel sur la grande carte via un bouton dédié pour permettre l'exploration libre.
*   **Données Topographiques** : Affichage en temps réel de la Latitude, Longitude (précision 6 décimales) et de l'Altitude.

### 3. Architecture & Navigation
*   **Persistence des Capteurs** : Le système de capture est global ; les calculs continuent en arrière-plan pendant toute l'utilisation de l'application.
*   **Navigation Hybride** : Utilisation d'une NavigationBar pour les onglets principaux et d'un HorizontalPager pour le balayage entre pages.
*   **Code Modulaire** : Séparation entre l'interface (pages), les éléments réutilisables (ui/components) et la logique métier (Process).

### 4. Personnalisation (Settings)
*   **Mode Sombre** : Bascule dynamique sur l'ensemble de l'interface utilisateur.
*   **Préférences** : Gestion du système d'unités (Métrique/Impérial) et de la langue.

## Structure du Projet
*   `Process/` : MovementProcessor (Calculs), CaptorListener (Acquisition), SimpleKalmanFilter.
*   `pages/` : Un fichier .kt dédié par écran (Page1, Page2, Page4, SettingsPage).
*   `ui/components/` : Interface.kt (Layouts et composants de carte réutilisables).

## Permissions requises
*   `INTERNET` : Chargement des cartes OpenStreetMap.
*   `ACCESS_FINE_LOCATION` : Position et vitesse GPS de haute précision.
*   `MAGNETIC_SENSOR` : Fonctionnement de la boussole.
