# PFM Marketplace

Projet pédagogique de marketplace construit avec des microservices Java et
Spring Boot. Il couvre la gestion du catalogue, du stock et des commandes,
ainsi que l'envoi asynchrone d'une notification après la création d'une commande.

## Architecture

- **API Gateway** route les requêtes vers le catalogue, les commandes et le stock.
- **Service Commande** vérifie le stock et récupère le prix avec Feign. Ces appels
  synchrones ont des délais d'attente, des retries et un Circuit Breaker.
- Après l'enregistrement d'une commande, un message JSON est publié dans RabbitMQ.
  **Service Notification** le consomme et envoie un email via Mailpit sans bloquer
  la réponse de création.
- **Config Server** centralise la configuration et **Eureka** permet la découverte
  des services.
- Un conteneur PostgreSQL héberge trois bases logiques : `pfm_catalogue`,
  `pfm_stock` et `pfm_commandes`. Le service Notification n'a pas de base.

![Diagramme d’architecture](<./architecture diagram.png>)

Les appels Feign sont synchrones : la commande attend les réponses du Catalogue
et du Stock pour être validée. La notification est asynchrone : elle est envoyée
après l'enregistrement, via RabbitMQ. Le cache Catalogue sert uniquement à la
consultation détaillée en cas d'indisponibilité ; la validation du stock reste
stricte.

## Modèle métier

Une commande contient ses lignes. `productId` relie une ligne de commande et
une entrée de stock à un produit par identifiant ; ce ne sont pas des relations
JPA entre les bases des différents services.

![Diagramme de classes métier](<./class diagram.png>)

## Technologies et ports

| Composant | Port |
|---|---:|
| API Gateway | 8080 |
| Service Catalogue | 8081 |
| Service Commande | 8082 |
| Service Stock | 8083 |
| Service Notification | 8084 |
| Eureka | 8761 |
| Config Server | 8888 |
| PostgreSQL (hôte) | 5433 |
| RabbitMQ AMQP / Management | 5672 / 15672 |
| Mailpit SMTP / interface | 1025 / 8025 |

## Démarrage local

Prérequis : Java 21 et Docker Desktop.

1. À la racine du dépôt, crée `.env` à partir de `.env.example`, puis remplace
   `POSTGRES_PASSWORD` par un mot de passe local. `.env` ne doit pas être publié.
2. Démarre PostgreSQL, RabbitMQ et Mailpit :

   ```powershell
   docker compose up -d
   ```

3. Lance les applications dans cet ordre, chacune dans son propre terminal ou
   sa configuration IntelliJ : Config Server, Eureka, API Gateway, Catalogue,
   Stock, Commande, puis Notification.
4. Les services Catalogue, Stock et Commande ont besoin de `POSTGRES_PASSWORD`
   dans leurs variables d'environnement IntelliJ. IntelliJ ne charge pas
   automatiquement `.env`.

Le Config Server récupère `config-repo` depuis la branche Git configurée.
Publie les modifications de configuration sur cette branche avant de démarrer
ou redémarrer les services concernés.

## Vérifications

- Créer un produit et son stock, puis une commande avec
  `POST http://localhost:8082/api/orders` et l'en-tête `Idempotency-Key`.
- Rejouer la création avec la même clé : la commande existante est renvoyée,
  sans doublon.
- Vérifier l'email dans <http://localhost:8025>. Pour le traitement différé,
  arrêter Service Notification, créer une commande, puis redémarrer le service ;
  RabbitMQ conserve le message en attente.
- Pour la résilience, vérifier les propriétés avec
  `GET http://localhost:8888/service-commande/default`, puis arrêter Catalogue
  ou Stock et observer l'erreur `503`. Une lecture détaillée peut utiliser le
  cache Catalogue ; la validation d'une commande n'utilise pas de stock en cache.

RabbitMQ Management est accessible sur <http://localhost:15672> (`tp` / `tp123`).
