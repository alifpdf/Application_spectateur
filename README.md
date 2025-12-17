# 🏉 ROBAFIS™ – IHM Coach & Spectateur
**Concours Robafis 2025 / 2026 – CY Tech**

> **Statut** : 🟢 Actif | **Version** : 2025.1

Cette application web est le centre de contrôle pour le concours ROBAFIS™. Elle assure le **pilotage du robot Makeblock** via Bluetooth, la **gestion du score** et l’**affichage public** des informations de match.

---

## 📑 Sommaire
1. [Présentation](#-présentation)
2. [Architecture du système](#-architecture-du-système)
3. [Équipe Projet](#-équipe)
4. [Prérequis](#-prérequis)
5. [Installation & Démarrage](#-installation--démarrage)
6. [Guide d'utilisation](#-guide-dutilisation)
    - [Mode Spectateur](#-mode-spectateur)
    - [Mode Coach](#-mode-coach-ihm-entraîneur)
    - [Connexion Bluetooth](#-connexion-bluetooth-makeblock)
7. [Commandes & Protocole](#-commandes--protocole)
8. [Intégration Robot (C++)](#-intégration-robot-c)
9. [Limitations & Dépannage](#-limitations--dépannage)

---

## 📌 Présentation

L'application se divise en deux interfaces :
* **IHM Entraîneur (Coach)** : Interface sécurisée pour piloter le robot, gérer les arrêts d'urgence et visualiser la télémétrie.
* **IHM Spectateur** : Affichage passif du score et des actions de jeu en temps réel.

---

## 📐 Architecture du système

```mermaid
graph TD
    User([Utilisateur]) -->|Ouvre| Browser[Navigateur Web Chrome/Edge]
    Browser -->|HTTP| Server[Serveur Java - Port 8080]
    
    subgraph Interfaces
        Coach[IHM Coach]
        Spectator[IHM Spectateur]
    end
    
    Server --> Coach
    Server --> Spectator
    
    Coach -->|Web Bluetooth API| Robot[🤖 Robot Makeblock]
    Robot -->|Télémétrie & Logs| Coach

👥 Équipe
Établissement : CY Tech

Projet : Robafis CY Tech (Concours 2025/2026)

Chef de projet : Donovan Cardenas Temiquel

Référente : Sonia Yassa

🧰 Prérequis
🔧 Matériel
[x] Ordinateur équipé du Bluetooth (activé avant le lancement).

[x] Module Makeblock compatible Bluetooth.

[x] Le robot Makeblock doit être sous tension.

💻 Logiciel
Java 17 (Recommandé).

Un navigateur compatible (voir section Dépannage).

🚀 Installation & Démarrage
1. Construction du JAR
Si vous avez les sources, compilez le projet avec Mill :


mill server.assembly
# Fichier généré : out/server/assembly.dest/out.jar

2. Lancement du ServeurLe fichier JAR est multiplateforme (Windows, Linux, macOS).Bashjava -jar out.jar
⚠️ Avant de lancer : Assurez-vous que le Bluetooth de votre PC est activé et que le robot est allumé.📖 Guide d'utilisation👀 Mode SpectateurAccessible librement pour l'affichage public.URL : http://localhost:8080/Affichage : Score total et type de la dernière action (Essai, Transformation, Pénalité).🎮 Mode Coach (IHM Entraîneur)Interface de contrôle protégée.URL : http://localhost:8080/coachMot de passe : abcFonctionnalités :Contrôle complet du robot (Départ, Arrêt d'urgence).Gestion du score.Visualisation du terrain (4x5) et obstacles.Terminal de logs (messages reçus/envoyés).🔌 Connexion Bluetooth (Makeblock)Dans l'IHM Coach, cliquer sur "Rechercher des appareils Makeblock".Une fenêtre navigateur s'ouvre : sélectionner le module Makeblock et cliquer sur Associer.Dans l'IHM, sélectionner l'appareil dans la liste déroulante.Cliquer sur "Se connecter".⚠️ Règles Bluetooth importantes :Plusieurs Makeblock peuvent être appairés, mais un seul est actif à la fois.Les commandes ne sont envoyées qu'à l'appareil sélectionné dans la liste.Déconnecter un appareil non sélectionné n'interrompt pas le robot en cours de match.🎛 Commandes & Protocole📊 Gestion du Score (Arbitrage)BoutonEffetNote+2Ajoute 2 pointsTransformation+3Ajoute 3 pointsPénalité+5Ajoute 5 pointsEssaiReset ScoreRemise à 0 du scoreN'affecte pas le robot🤖 Commandes Robot (Protocole Bluetooth)Ces codes sont envoyés par l'IHM vers le robot via le port série Bluetooth.Bouton IHMCode EnvoyéDescription / EffetDémarrer"0"Lance le robot + Chrono PrincipalArrêt Général"1"Arrêt urgence + Chrono urgence (5 min)Arrêt Mécanique"2"Arrêt urgence + Chrono urgence (5 min)Arrêt Électrique"3"Arrêt urgence + Chrono urgence (2 min)Interruption"4"Pause le robotReprendre"5"Reprise après pause ou urgenceAutotest"6"Lance l'autotest MegaPiEssai"7"Lance la séquence "Essai" sur le robotTransformation"8"Lance la séquence "Transformation" sur le robot⏱️ Gestion des ChronomètresChrono Principal : Démarre avec la commande "Démarrer".Chrono d'Urgence : Démarre automatiquement lors d'un arrêt d'urgence ("1", "2" ou "3").À la reprise ("5"), le chrono principal repart et le chrono d'urgence se fige.🔄 Reset (Distinction)Bouton Reset (Global) : Remise à zéro totale (Chronos, obstacles, état du ballon, logs).Bouton Reset Score : Ne réinitialise que les points.📡 Télémétrie (Robot → IHM)Le robot peut envoyer des informations pour mettre à jour l'IHM Coach en temps réel.🗺️ Terrain & ObstaclesLe terrain est une grille de 4 colonnes x 5 lignes.Syntaxe MessageAction sur l'IHMPOS r cMet à jour la position du robot (Vert) en ligne r, colonne c.POS r c DAjoute un obstacle (Jaune) en ligne r, colonne c.POS r c EEfface l'obstacle en ligne r, colonne c.⚽ Détection du BallonSyntaxe MessageAction sur l'IHMBallon 1Indicateur Ballon passe au Vert (Possession).Ballon 0Indicateur Ballon passe au Rouge (Pas de ballon).💻 Intégration Robot (C++)Exemple de code pour communiquer avec l'IHM depuis un Arduino/Makeblock MegaPi.C++// Définition du port Bluetooth (ex: Serial3 sur MegaPi)
#define BT Serial3

void setup() {
    BT.begin(115200); // Vitesse bauds standard
    
    // Envoyer un message texte (log)
    BT.println(F("DEMARRAGE SYSTEME"));
}

void loop() {
    // Envoyer la position (Ligne 2, Colonne 3)
    BT.println("POS 2 3");
    
    // Envoyer un état de capteur
    int sensorValue = 42;
    BT.println(sensorValue);
    
    delay(1000);
}
Toutes les données envoyées via BT.println() apparaissent dans le terminal de l'IHM.⚠️ Limitations & Dépannage🌍 Compatibilité Navigateur1. Côté Spectateur (Affichage uniquement)L'interface Spectateur fonctionne sur tous les navigateurs modernes (Chrome, Firefox, Edge, Safari, Opéra).2. Côté Entraîneur (Contrôle Bluetooth)Cette interface nécessite l'API Web Bluetooth, qui n'est pas standardisée partout.❌ Firefox : Non supporté.✅ Google Chrome / Edge : Compatible. Sur Linux ou Windows ancien, voir configuration ci-dessous.⚙️ Configuration Chrome (Flags)Si le Bluetooth ne fonctionne pas ou n'est pas détecté, activez les options expérimentales :Ouvrez un nouvel onglet Chrome et allez à l'adresse : chrome://flagsCherchez et activez (Enabled) les options suivantes :Nom du FlagDescriptionRechercheWeb BluetoothActive l'API Web Bluetooth (Indispensable sur Linux).#enable-web-bluetoothNew Permissions BackendActive le nouveau système de permissions.#enable-web-bluetooth-new-permissions-backendPairing SupportActive le support de l'appairage par code PIN.#enable-web-bluetooth-confirm-pairing-supportRedémarrez Chrome pour appliquer les changements.📱 Problème Safari (iOS)Si la page reste blanche (White Screen of Death) sur un iPhone/iPad :Ouvrir les Réglages de l'iPhone.Aller dans Safari > Avancé.Désactiver temporairement les protections de confidentialité ou activer JavaScript si désactivé.Dans Experimental Features, vérifier que WebGL et WebSockets sont actifs.🔌 DéconnexionSi l'utilisateur clique sur "Se déconnecter" ou si le robot s'éteint :Les commandes deviennent grisées.L'affichage (Score, Logs) reste accessible.Conseil : Toujours déconnecter proprement via l'IHM avant d'éteindre le robot.Projet académique – Usage pédagogique et concours ROBAFIS™ uniquement.
