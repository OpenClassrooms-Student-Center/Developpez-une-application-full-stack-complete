# Définition des données

Annexe du document [Documentation et rapport du projet MDD](../documentation-projet.md).

Cette annexe a pour but de décrire les tables, leurs formats, les règles de validation et la sécurisation des données. Le schéma est créé par les migrations Flyway, et la figure 2 du document principal montre les relations entre les tables.

## Tables

Toutes les colonnes sont obligatoires (`NOT NULL`), et l’unicité ne distingue pas les majuscules des minuscules. Sauf `subscriptions`, chaque table a pour clé primaire une colonne `id` (`BIGINT`), auto-incrémentée par MySQL.

### Utilisateurs (`users`)

- **`username`** : `VARCHAR(30)`, unique. Saisi par l’utilisateur : de 3 à 30 caractères, uniquement des lettres sans accent, des chiffres, `.`, `_` et `-`.
- **`email`** : `VARCHAR(255)`, unique. Saisi par l’utilisateur : une adresse e-mail valide, qui sert à se connecter.
- **`password`** : `VARCHAR(255)`, le mot de passe haché avec Argon2id, jamais en clair ; la taille de la colonne permettra de changer d’algorithme plus tard. Le mot de passe saisi compte de 8 à 64 caractères, dont au moins un chiffre, une minuscule, une majuscule et un caractère spécial (ni lettre ni chiffre).

### Thèmes (`topics`)

Les thèmes sont des données de référence, insérées par une migration Flyway ; aucun écran ne permet d’en créer.

- **`title`** : `VARCHAR(50)`, unique.
- **`description`** : `TEXT`.

### Articles (`posts`)

- **`title`** : `VARCHAR(100)`. Saisi par l’utilisateur : 100 caractères au plus.
- **`content`** : `TEXT`. Saisi par l’utilisateur : du texte brut (ni HTML ni Markdown), de 10 000 caractères au plus.
- **`created_at`** : `DATETIME`, en UTC, renseignée par le back-end à la création de l’article.
- **`author_id`** : `BIGINT`, clé étrangère vers `users`, renseignée par le back-end : c’est l’utilisateur connecté.
- **`topic_id`** : `BIGINT`, clé étrangère vers `topics` : le thème choisi par l’utilisateur dans la liste.

### Commentaires (`comments`)

- **`content`** : `TEXT`. Saisi par l’utilisateur : du texte brut (ni HTML ni Markdown), de 2 000 caractères au plus.
- **`created_at`** : `DATETIME`, en UTC, renseignée par le back-end à la création du commentaire. Elle est enregistrée sans être affichée, comme le prévoit l’[analyse des besoins front-end](analyse-besoins-front-end.md).
- **`author_id`** : `BIGINT`, clé étrangère vers `users`, renseignée par le back-end : c’est l’utilisateur connecté.
- **`post_id`** : `BIGINT`, clé étrangère vers `posts` : l’article commenté.

### Abonnements (`subscriptions`)

Chaque abonnement est une ligne, ajoutée par le bouton « S’abonner » de la page des thèmes et supprimée par « Se désabonner » dans le profil.

- **`user_id`** : `BIGINT`, clé étrangère vers `users` : l’utilisateur connecté.
- **`topic_id`** : `BIGINT`, clé étrangère vers `topics` : le thème choisi.

Ensemble, les deux colonnes forment la clé primaire : un utilisateur ne peut s’abonner qu’une fois au même thème.

## Formats d’échange

- **JSON** : l’API reçoit et renvoie du JSON.
- **Dates** : en UTC, au format ISO 8601 (`2026-10-27T14:30:00Z`).
- **DTO** : l’API échange des objets dédiés, jamais les entités JPA.

## Validation

Chaque règle est contrôlée à trois niveaux :

- **Front-end** : si une saisie est invalide, un message clair s’affiche immédiatement sous le champ.
- **Back-end** : c’est le contrôle de référence, car une requête peut contourner le front-end.
- **Base de données** : ses contraintes (`NOT NULL`, unicité, clés étrangères) sont le dernier rempart.

Règles communes :

- **Champ obligatoire** : non vide et pas seulement composé d’espaces.
- **Valeur déjà prise** : un nom d’utilisateur ou un e-mail déjà utilisé donne un message explicite.
- **Mot de passe du profil** : laissé vide, il reste inchangé. Un formulaire à part, qui exigerait le mot de passe actuel comme le recommande l’OWASP, serait plus sûr, mais la maquette place ce champ dans le profil.

## Sécurisation

- **Mots de passe et jetons** : mots de passe hachés avec Argon2id ; ni mot de passe ni jeton n’apparaît dans les réponses, les journaux ou les messages d’erreur.
- **E-mail** : identifiant de connexion, il n’est jamais renvoyé avec les articles ou les commentaires de son auteur.
- **Connexion** : un e-mail inconnu et un mauvais mot de passe reçoivent la même réponse, dans le même temps, pour ne pas révéler quelles adresses ont un compte.
- **Auteur et date** : fixés par le back-end, jamais par le front-end.
- **Accès** : l’API est réservée aux utilisateurs connectés, sauf l’inscription, la connexion et la déconnexion ; chacun ne peut modifier que son profil et ses abonnements, et ne supprimer que son compte.
- **Suppression du compte** : elle efface en cascade ses abonnements, ses commentaires et ses articles, avec leurs commentaires. Les conserver sans auteur n’aurait de sens que dans un fil de discussion, où chaque message répond au précédent ; ici, un commentaire ne répond jamais à un autre.
- **Injection SQL** : empêchée par les requêtes paramétrées de Spring Data JPA.
- **XSS** : Angular affiche les contenus comme du texte, si bien qu’un script glissé dans un article ou un commentaire ne s’exécute pas.
- **Secrets** : les identifiants de connexion à la base de données et la clé qui signe les jetons restent hors du dépôt.