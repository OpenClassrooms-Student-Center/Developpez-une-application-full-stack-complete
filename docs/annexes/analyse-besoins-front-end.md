# Analyse des besoins front-end

Annexe du document [Documentation et rapport du projet MDD](../documentation-projet.md).

Cette annexe a pour but de relier chaque écran des maquettes aux fonctionnalités et aux règles des spécifications. Elle s’appuie sur les spécifications fonctionnelles, les maquettes Figma ordinateur et mobile, ainsi que la réunion de lancement.

## Écrans

### Accueil

- **Accès** : public.
- **Fonctionnalités** : Page d’accueil.
- **Contenu** : logo, puis les boutons « S’inscrire » et « Se connecter ».
- **Règles** : réservée aux visiteurs non connectés ; une fois connecté, la page d’accueil affiche le fil d’actualité.

### Inscription

- **Accès** : public.
- **Fonctionnalités** : Inscription.
- **Contenu** : bandeau, flèche de retour, titre « Inscription », champs « Nom d’utilisateur », « Adresse e-mail » et « Mot de passe », et bouton « S’inscrire ».
- **Règles** : le mot de passe doit compter au moins 8 caractères, dont au moins un chiffre, une minuscule, une majuscule et un caractère spécial.

### Connexion

- **Accès** : public.
- **Fonctionnalités** : Connexion.
- **Contenu** : bandeau, flèche de retour, titre « Se connecter », champs « Adresse e-mail » et « Mot de passe », et bouton « Se connecter ».
- **Règles** : connexion avec son e-mail (voir les écarts) ; elle persiste d’une session à l’autre pendant 24 heures.

### Fil d’actualité

- **Accès** : utilisateur connecté.
- **Fonctionnalités** : Fil d’actualité.
- **Contenu** : bandeau, boutons « Créer un article » et « Trier par », puis les articles, chacun avec son titre, sa date, son auteur et son contenu tronqué à 5 lignes.
- **Règles** : seuls les articles des thèmes auxquels l’utilisateur est abonné sont affichés, du plus récent au plus ancien ; l’ordre peut être inversé.

### Article

- **Accès** : utilisateur connecté.
- **Fonctionnalités** : Article (consultation), Commentaire.
- **Contenu** : bandeau, titre, date, auteur, thème et contenu de l’article, puis la section « Commentaires » : les commentaires avec leur auteur, suivis d’une zone de saisie « Écrivez ici votre commentaire » et d’un bouton d’envoi.
- **Règles** : l’auteur et la date des commentaires sont ajoutés automatiquement ; pas de sous-commentaires.

### Création d’article

- **Accès** : utilisateur connecté, depuis le fil d’actualité.
- **Fonctionnalités** : Article (ajout).
- **Contenu** : flèche de retour, titre « Créer un nouvel article », liste déroulante « Sélectionner un thème », champs « Titre de l’article » et « Contenu de l’article », et bouton « Créer ».
- **Règles** : l’utilisateur choisit le thème et saisit le titre et le contenu ; l’auteur et la date sont renseignés automatiquement.

### Thèmes

- **Accès** : utilisateur connecté.
- **Fonctionnalités** : Thème, Abonnement (abonnement).
- **Contenu** : bandeau, puis les thèmes, d’un à quatre par ligne selon la largeur de l’écran (voir les écarts), chacun avec un titre, une description tronquée à 3 lignes et un bouton actif « S’abonner » ou inactif « Déjà abonné » selon le cas.
- **Règles** : tous les thèmes sont listés, abonné ou non ; après un clic, le bouton devient inactif et affiche « Déjà abonné » ; le désabonnement se fait depuis le profil.

### Profil

- **Accès** : utilisateur connecté.
- **Fonctionnalités** : Profil, Abonnement (désabonnement).
- **Contenu** : bandeau, titre « Profil utilisateur », formulaire prérempli avec le nom d’utilisateur, l’e-mail et le bouton « Sauvegarder », puis le formulaire « Mot de passe » (« Mot de passe actuel », « Nouveau mot de passe », bouton « Modifier ») et un bouton « Supprimer mon compte ». Puis la section « Abonnements » : les thèmes suivis, chacun avec son titre, sa description et un bouton « Se désabonner ».
- **Règles** : l’e-mail, le nom d’utilisateur et le mot de passe sont modifiables, avec les règles de l’inscription ; changer de mot de passe demande l’actuel ; un message confirme chaque enregistrement ; on s’y désabonne d’un thème ; la suppression du compte demande une confirmation.

### Pages légales

- **Accès** : public, depuis le pied de page.
- **Fonctionnalités** : Pages légales.
- **Contenu** : les mentions légales (éditeur, hébergeur, contact) et la politique de confidentialité (données collectées, finalités, durée de conservation, droits des utilisateurs et contact).
- **Règles** : accessibles depuis toutes les pages, connecté ou non.

## Éléments communs

Chaque écran existe en version ordinateur et mobile (Responsive), et un pied de page mène aux pages légales.

Un bandeau dont le visuel change comme suit :

| Support | Accès | Pages | Contenu |
| --- | --- | --- | --- |
| Ordinateur | Public | Inscription et connexion | Logo sur la gauche. |
| Ordinateur | Utilisateur connecté | Toutes sauf page d’accueil, inscription et connexion | Logo sur la gauche ; « Se déconnecter », « Articles », « Thèmes » et l’icône du profil sur la droite. |
| Mobile | Utilisateur connecté | Toutes sauf page d’accueil, inscription et connexion | Logo sur la gauche et l’icône ☰ sur la droite, qui ouvre un panneau latéral avec « Se déconnecter », « Articles » et « Thèmes ». |

## Écarts avec les spécifications et les maquettes

- **Description des thèmes** : affichée par la maquette sous le titre de chaque thème, mais absente des spécifications ; elle est prévue dans les données.
- **Date des commentaires** : demandée par les spécifications, mais absente de la maquette ; elle est enregistrée sans être affichée.
- **Connexion par e-mail** : les spécifications et la maquette prévoient l’e-mail ou le nom d’utilisateur. Or le nom d’utilisateur, qui signe chaque article et chaque commentaire, est connu de tous : s’en servir pour se connecter donnerait à chacun la moitié des identifiants des autres. La connexion se fait donc avec l’e-mail, jamais montré aux autres utilisateurs.
- **Pages légales et suppression du compte** : absentes des spécifications et des maquettes, elles répondent aux obligations légales : mentions légales, politique de confidentialité et droit à l’effacement des données personnelles (RGPD).
- **Formulaire du mot de passe** : la maquette place le mot de passe dans le formulaire du profil ; il a son propre formulaire, qui demande le mot de passe actuel, pour qu’une session restée ouverte ne suffise pas à le changer.
- **Messages de confirmation** : absents de la maquette, « Profil mis à jour. » et « Mot de passe modifié. » indiquent que l’enregistrement a réussi, puisque la page reste affichée.
- **Cartes par ligne** : la maquette en montre deux sur ordinateur. Sur la page des thèmes comme dans les abonnements du profil, les cartes gardent une largeur fixe de 340 px, et chaque ligne en compte autant que l’écran le permet, jusqu’à quatre. Deux cartes étirées sur toute la largeur d’un grand écran donneraient des lignes de texte trop longues pour être lues confortablement.