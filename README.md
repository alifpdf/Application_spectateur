# README

## Prérequis

### Matériel
- Un ordinateur équipé du **Bluetooth**, qui doit être **activé avant le lancement** de l'application.
- Un module **Makeblock** pouvant communiquer via Bluetooth.

### Logiciel
- Java installé (version permettant l’exécution d’un fichier `.jar`).
- Le fichier `mon-app.jar`.

---

## Lancement de l’application

Pour démarrer l’application, exécuter la commande suivante dans un terminal :

```bash
java -jar mon-app.jar
```

Assurez-vous que :
- Le **Bluetooth de l’ordinateur est activé** avant d’exécuter la commande.
- Le Makeblock est allumé et connecté si nécessaire.

---

## Accès à l’interface IHM

Deux modes d’accès sont disponibles :

### Mode Spectateur
http://localhost:8080/

### Mode Coach
http://localhost:8080/coach  
**Mot de passe :** `abc`

---

## Communication entre le Makeblock et l’IHM

Dans le code Makeblock, définir la liaison Bluetooth :

```cpp
#define BT Serial3
```

### Envoyer un texte à l’IHM

```cpp
BT.println(F("Texte que tu veux mettre "));
```

### Envoyer un nombre à l’IHM

```cpp
int N = 42;
BT.println(N);
```

Toutes les données envoyées via `BT.println()` seront visibles dans le terminal de l’application Java.

---

## Notes

- Le terminal intégré permet de voir en temps réel ce que renvoie le Makeblock.
- Le mode Coach nécessite un mot de passe (voir plus haut).
- Vérifiez qu'aucune autre application n’utilise le port Bluetooth au même moment.


