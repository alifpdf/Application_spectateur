# 🏉 ROBAFIS™ – IHM Coach & Spectateur  
**Concours Robafis 2025 / 2026 – CY Tech**

---

## 📌 Présentation

Cette application web permet :

- le **pilotage d’un robot Makeblock** via Bluetooth (IHM Entraîneur),
- la **gestion du score**,
- l’**affichage en temps réel** des informations de match (IHM Spectateur).

Le projet est développé dans le cadre du concours **ROBAFIS™ 2025/2026**.

---

## 👥 Équipe

- **Établissement** : CY Tech  
- **Chef de projet** : Donovan Cardenas Temiquel  
- **Équipe projet** : Robafis CY Tech  
- **Référente** : Sonia Yassa  

---

## 🧰 Prérequis

### 🔧 Matériel

- Un ordinateur équipé du **Bluetooth** (activé avant le lancement)
- Un module **Makeblock** compatible Bluetooth
- Le robot Makeblock doit être **allumé**

### 💻 Logiciel

- **Java 17** (recommandé)
- Un navigateur compatible **Web Bluetooth** :
  - ✅ Chrome (recommandé)
  - ✅ Edge (Chromium)
  - ❌ Firefox (non supporté)
- Le fichier **JAR** de l’application

---

## 🖥️ Construction & lancement du serveur

### 📦 Construction du JAR

```bash
mill server.assembly
Fichier généré :

text
Copier le code
out/server/assembly.dest/out.jar
▶️ Lancer l’application (Windows / Linux / macOS)
bash
Copier le code
java -jar out.jar
⚠️ Avant de lancer :

Vérifier que le Bluetooth est activé

Vérifier que le Makeblock est allumé

Le JAR est multiplateforme, aucune recompilation n’est nécessaire.

🌐 Accès aux interfaces IHM
👀 Mode Spectateur
Accessible sans authentification :

text
Copier le code
http://localhost:8080/
Affiche :

le score total

le type du dernier score (essai, transformation, pénalité)

📱 Note iPhone / Safari
Si la page reste blanche :

Ouvrir le menu Safari

Réduire / désactiver les protections de confidentialité

➡️ Autorise l’exécution complète des scripts JavaScript.

🎮 Mode Coach (IHM Entraîneur)
Accès protégé par mot de passe :

text
Copier le code
http://localhost:8080/coach
Mot de passe : abc

Fonctionnalités :

contrôle du robot Makeblock

commandes de score

chronomètres

visualisation du terrain

gestion des obstacles

retour des messages du robot

🔌 Bluetooth (Makeblock)
📡 Appairage
Cliquer sur Rechercher des appareils Makeblock

Sélectionner l’appareil dans la fenêtre du navigateur

Cliquer sur Associer

Sélectionner l’appareil dans la liste déroulante

Cliquer sur Se connecter

✔️ Les commandes robot deviennent actives après connexion.

⚠️ Règles Bluetooth importantes
Plusieurs Makeblock peuvent être connectés simultanément

Un seul appareil est actif (sélectionné) à la fois

Les commandes et notifications concernent uniquement l’appareil sélectionné

Déconnecter un appareil non sélectionné n’affecte pas l’appareil actif

🎛️ Commandes (Score + Robot)
📊 Commandes de score
Bouton	Effet
+2	Ajoute 2 points (transformation)
+3	Ajoute 3 points (pénalité)
+5	Ajoute 5 points (essai)
Reset Score	Remet uniquement le score à 0

Le type du dernier score est automatiquement affiché sur l’IHM Spectateur.

🤖 Commandes robot (Bluetooth Makeblock)
Bouton	Code envoyé	Effet
Démarrer	"0"	Démarre le robot + lance le chrono principal
Arrêt d’urgence général	"1"	Arrêt + chrono urgence (5 min)
Arrêt d’urgence mécanique	"2"	Arrêt + chrono urgence (5 min)
Arrêt d’urgence électrique	"3"	Arrêt + chrono urgence (2 min)
Interruption	"4"	Met le robot en pause
Reprendre	"5"	Reprend après interruption / urgence
Autotest	"6"	Lance l’autotest MegaPi
Essai	"7"	Lance la séquence “Essai” côté robot
Transformation	"8"	Lance la séquence “Transformation” côté robot

🔄 Reset (différences)
Bouton	Effet
Reset	Remise à zéro générale (chronos, obstacles, ballon, log Bluetooth)
Reset Score	Réinitialise uniquement le score

⏱️ Chronomètres
Chronomètre principal
démarre avec la commande Démarrer

Chronomètre d’urgence
électrique : 2 minutes

mécanique / général : 5 minutes

Lors d’un arrêt d’urgence :

le chrono principal s’arrête

le chrono d’urgence démarre

À la reprise :

le chrono principal repart

le chrono d’urgence se fige

🗺️ Terrain & obstacles
Terrain : 4 colonnes × 5 lignes

Messages position envoyés par le robot :

text
Copier le code
POS r c
Gestion des obstacles
text
Copier le code
POS r c D   → ajout d’un obstacle
POS r c E   → suppression de l’obstacle
Couleurs :

vert : position actuelle du robot

jaune : obstacle détecté

⚽ Détection du ballon
Messages envoyés par le robot :

text
Copier le code
Ballon 1   → ballon détecté
Ballon 0   → pas de ballon
Affichage :

vert : ballon détecté

rouge : pas de ballon

🧾 Terminal Bluetooth
Le terminal intégré affiche en temps réel :

les messages reçus du robot

les commandes envoyées

les événements importants (démarrage, arrêt, erreurs)

🔄 Déconnexion
Si :

l’utilisateur clique sur Se déconnecter

ou la carte Makeblock est hors tension

➡️ Les commandes Bluetooth deviennent grisées
➡️ L’IHM reste fonctionnelle (score, affichage)

🔁 Communication Makeblock → IHM (côté robot)
Définir la liaison Bluetooth :

cpp
Copier le code
#define BT Serial3
Envoyer un texte :

cpp
Copier le code
BT.println(F("DEMARRAGE"));
Envoyer un nombre :

cpp
Copier le code
int N = 42;
BT.println(N);
Toutes les données envoyées via BT.println() apparaissent dans le terminal de l’IHM.

⚠️ Limitations connues
Web Bluetooth :

nécessite HTTPS ou localhost

non supporté par Firefox

Safari iOS :

nécessite un réglage de confidentialité

Une seule IHM Entraîneur active recommandée en compétition

✅ Bonnes pratiques
Utiliser Chrome

Vérifier l’appareil sélectionné avant chaque commande

Ne connecter qu’un robot actif à la fois

Déconnecter proprement le robot avant extinction

📄 Licence & usage
Projet académique – usage pédagogique et concours ROBAFIS™ uniquement.

markdown
Copier le code

---

👉 Si tu veux, je peux maintenant :
- faire une **version anglaise**
- créer une **FAQ**
- séparer en **README IHM / README Robot**
- ajouter des **schémas d’architecture**
- ou adapter le README au **template officiel ROBAFIS™**

Dis-moi 👍
