# Centralisation des Logs avec Grafana Loki

---

## Introduction

Bonjour, aujourd'hui je vais vous presenter comment nous avons mis en place une solution de centralisation des logs pour
notre infrastructure. Cette presentation va couvrir le probleme que nous avions, la solution que nous avons choisie, et
comment nous l'avons deployee.

---

## Le probleme de depart

Quand on travaille avec plusieurs services deployes sur differentes machines, on se retrouve rapidement face a un
probleme : les logs sont eparpilles partout.

Imaginons la situation suivante : une erreur se produit en production. Pour comprendre ce qui s'est passe, il faut se
connecter en SSH sur la premiere machine, chercher dans les logs avec des commandes comme `grep`. Si on ne trouve rien,
on passe a la deuxieme machine, puis la troisieme... C'est long, fastidieux, et on perd un temps precieux.

En plus de ca, les logs des conteneurs Docker disparaissent quand on les redemarre. On perd donc l'historique, ce qui
rend le diagnostic encore plus complique.

Enfin, si une erreur critique se produit la nuit, personne n'est prevenu. On decouvre le probleme le lendemain matin
quand les utilisateurs se plaignent.

---

## La solution : Grafana Loki

Pour resoudre ces problemes, nous avons choisi d'utiliser la stack Grafana Loki. C'est une solution composee de trois
outils qui travaillent ensemble.

Le premier, c'est **Promtail**. Son role est simple : il collecte les logs de nos conteneurs Docker et les envoie vers
un serveur central. Il tourne sur chaque machine ou nous avons des applications.

Le deuxieme, c'est **Loki**. C'est le coeur du systeme. Il recoit les logs envoyes par Promtail, les stocke et les
indexe. C'est lui qui permet ensuite de faire des recherches rapides.

Le troisieme, c'est **Grafana**. C'est l'interface web qui nous permet de visualiser les logs, de faire des recherches,
et de creer des tableaux de bord.

---

## Pourquoi Loki et pas ELK ?

Vous vous demandez peut-etre pourquoi nous avons choisi Loki plutot que la stack ELK, qui est plus connue.

La raison principale, c'est la simplicite. ELK avec Elasticsearch est tres puissant, mais il demande beaucoup de
ressources : de la RAM, du CPU, de l'espace disque. Pour notre besoin, c'etait surdimensionne.

Loki, lui, est beaucoup plus leger. Il n'indexe pas tout le contenu des logs, seulement les metadonnees comme le nom du
service ou le niveau de log. Ca suffit largement pour notre usage, et ca consomme beaucoup moins de ressources.

En plus, Loki s'integre nativement avec Grafana, ce qui simplifie la configuration.

---

## Comment ca fonctionne

Laissez-moi vous expliquer le parcours d'un log dans notre systeme.

Quand notre application Spring Boot ecrit un message de log, ce message est capture par Docker qui gere la sortie
standard du conteneur.

Promtail, qui tourne sur la meme machine, surveille en permanence les conteneurs Docker. Des qu'il detecte un nouveau
log, il le recupere et l'envoie a Loki via une requete HTTP.

Loki recoit ce log, l'indexe avec des labels comme le nom du conteneur ou l'environnement, puis le stocke.

Ensuite, quand on veut consulter les logs, on ouvre Grafana dans notre navigateur. Grafana interroge Loki et nous
affiche les resultats de facon lisible.

Tout ce processus se fait en temps reel. Entre le moment ou l'application ecrit le log et le moment ou il apparait dans
Grafana, il se passe moins d'une seconde.

---

## Notre architecture

Concretement, nous avons deux types de machines.

D'un cote, nous avons la VM de monitoring, a l'adresse 172.31.250.178. C'est elle qui heberge Loki et Grafana. Elle
centralise tous les logs et fournit l'interface de visualisation.

De l'autre, nous avons les VMs applicatives. Chacune fait tourner nos services metier, comme le worker-replication. Sur
chaque VM applicative, nous avons aussi deploye Promtail qui se charge de collecter les logs et de les envoyer vers
Loki.

Cette architecture est simple a etendre. Si demain on ajoute une nouvelle VM avec de nouveaux services, il suffit d'y
installer Promtail et les logs seront automatiquement centralises.

---

## La mise en place

Je vais maintenant vous expliquer comment nous avons deploye cette solution.

Sur la VM de monitoring, nous avons cree un fichier docker-compose qui lance Loki et Grafana. Loki ecoute sur le port
3100 et Grafana sur le port 3000. Nous avons aussi configure Grafana pour qu'il se connecte automatiquement a Loki des
le demarrage.

Sur les VMs applicatives, nous avons ajoute Promtail dans le docker-compose existant. Promtail a besoin d'acceder au
socket Docker pour pouvoir lire les logs des conteneurs. On lui a aussi donne un fichier de configuration qui lui
indique l'adresse de Loki.

Pour que Promtail sache quels conteneurs surveiller, nous utilisons un systeme de labels Docker. Chaque service qui doit
etre surveille recoit le label "logging: promtail". Promtail ne collecte que les logs des conteneurs qui ont ce label.
Ca nous permet de controler precisement ce qu'on envoie a Loki.

---

## Les logs structures

Pour aller plus loin, nous avons aussi configure notre application Spring Boot pour produire des logs au format JSON.

Avant, nos logs ressemblaient a ca : une date, un niveau, une classe, et un message, le tout sur une seule ligne.
C'etait lisible par un humain, mais difficile a parser automatiquement.

Maintenant, chaque log est un objet JSON avec des champs bien definis : timestamp, level, logger, message, application.
Ca permet a Promtail d'extraire automatiquement ces informations et de les transformer en labels dans Loki.

L'avantage, c'est qu'on peut ensuite filtrer tres precisement. Par exemple, on peut demander a voir uniquement les logs
de niveau ERROR de l'application worker-replication. C'est beaucoup plus puissant.

---

## Utilisation au quotidien

Au quotidien, l'utilisation est tres simple.

On ouvre Grafana dans le navigateur, on va dans la section Explore, et on ecrit une requete. La syntaxe s'appelle LogQL
et elle est assez intuitive.

Pour voir tous les logs d'un service, on ecrit le nom du conteneur entre accolades. Pour filtrer les erreurs, on ajoute
un filtre avec le mot ERROR. On peut aussi faire des recherches par mot-cle, exclure certains termes, ou utiliser des
expressions regulieres.

Nous avons egalement cree un tableau de bord pre-configure qui affiche les logs en temps reel, les erreurs dans un
panneau dedie, les warnings dans un autre, et un graphique qui montre l'evolution du volume de logs par niveau. Ca donne
une vue d'ensemble rapide de la sante de nos services.

---

## Les benefices

Depuis que nous avons mis en place cette solution, nous avons constate plusieurs benefices.

D'abord, le temps de diagnostic a ete reduit drastiquement. Avant, il fallait parfois une heure pour trouver l'origine
d'un probleme. Maintenant, en quelques minutes, on peut voir tous les logs de tous les services au meme endroit.

Ensuite, nous avons une meilleure visibilite sur ce qui se passe en production. On peut voir en temps reel l'activite de
nos services, detecter des anomalies, et reagir plus vite.

Enfin, l'ajout de nouveaux services est tres simple. Il suffit d'ajouter un label Docker, et les logs sont
automatiquement collectes. Pas besoin de modifier la configuration de Loki ou de Grafana.

---

## Pour ajouter un nouveau service

Si demain vous voulez ajouter un nouveau service a la centralisation, voici ce qu'il faut faire.

Dans le docker-compose de votre service, ajoutez simplement le label "logging: promtail". C'est tout. Au prochain
demarrage, Promtail detectera automatiquement ce conteneur et commencera a collecter ses logs.

Si votre service tourne sur une nouvelle VM ou Promtail n'est pas encore installe, il faudra d'abord ajouter Promtail
dans le docker-compose de cette VM. Le fichier de configuration est toujours le meme, il pointe vers notre serveur Loki
central.

Une fois le service demarre, vous pouvez verifier que les logs arrivent bien dans Grafana en faisant une requete avec le
nom de votre conteneur.

---

## Conclusion

Pour resumer, nous avons mis en place une solution de centralisation des logs basee sur Grafana Loki.

Cette solution resout notre probleme de logs eparpilles en les regroupant tous au meme endroit. Elle est simple a
utiliser grace a l'interface Grafana. Elle est facile a etendre puisqu'un simple label suffit pour ajouter un nouveau
service. Et elle est economique en ressources comparee a des solutions plus lourdes comme ELK.

Les trois composants a retenir sont Promtail pour la collecte, Loki pour le stockage, et Grafana pour la visualisation.

Si vous avez des questions sur la mise en place ou l'utilisation, n'hesitez pas.

Merci pour votre attention.

---

## Informations pratiques

Pour acceder a Grafana : http://172.31.250.178:3000

Identifiants : admin / admin

Le dashboard des logs est accessible directement a cette adresse : http://172.31.250.178:3000/d/worker-logs

La documentation complete de la procedure est disponible dans le fichier LOGGING-PROCEDURE.md du projet.
