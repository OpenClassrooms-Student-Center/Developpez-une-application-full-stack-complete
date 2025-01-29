create schema IF NOT EXISTS mdd;

use mdd;

CREATE TABLE `THEMES` (
  `theme_id` INT PRIMARY KEY AUTO_INCREMENT,
  `titre` VARCHAR(40),
  `description` VARCHAR(2000),
  `follow` boolean,
  `user_id` int
);

CREATE TABLE `ARTICLES` (
  `article_id` INT PRIMARY KEY AUTO_INCREMENT,
  `titre` VARCHAR(50),
  `auteur` VARCHAR(50),
  `contenu` VARCHAR(2000),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `theme_id` int,
  `user_id` int
);

CREATE TABLE `USERS` (
  `user_id` INT PRIMARY KEY AUTO_INCREMENT,
  `first_name` VARCHAR(40),
  `email` VARCHAR(255),
  `password` VARCHAR(255),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `COMMENTS` (
  `comment_id` INT PRIMARY KEY AUTO_INCREMENT,
  `commentaire` VARCHAR(40),
  `auteur` VARCHAR(40),
  `user_id` int,
  `article_id` int
);

create table `article_like` (
`user_id` INT, 
`article_id` INT );
    
create table `theme_like` (
`user_id` INT, 
`theme_id` INT );

ALTER TABLE `ARTICLES` ADD FOREIGN KEY (`theme_id`) REFERENCES `THEMES` (`theme_id`);

INSERT INTO THEMES (titre, description)
VALUES ('Titre du thème', 'lorem ipsum is simply dummy text of the printing and typesetting industry...'),
       ('Titre', 'lorem ipsum is simply dummy text of the printing and typesetting industry...');


INSERT INTO USERS (first_name, email, password)
VALUES ('Admin', 'user@mdd.com', '$2a$10$9BFwaEZcQyHmVUwMTFriQO7qikeOMANIe9f9PvQeZFSedeDfPXlQq'); 

INSERT INTO ARTICLE_LIKE (user_id, article_id)
VALUES(1,1);

INSERT INTO ARTICLES (titre, auteur, contenu, theme_id, user_id)
VALUES ('Titre', 'Auteur', 'description',1,1); 

