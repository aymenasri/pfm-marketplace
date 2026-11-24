# PFM Marketplace

Projet de microservices Java/Spring Boot pour un marketplace : catalogue, stock,
commandes et notifications asynchrones avec RabbitMQ. PostgreSQL est utilisé
pour les données et Mailpit pour tester les emails.

## Démarrage

Prérequis : Java 21 et Docker Desktop.

1. Copier `.env.example` vers `.env` et définir `POSTGRES_PASSWORD`.
2. Démarrer l'infrastructure :

   ```powershell
   docker compose up -d
   ```

3. Lancer les applications dans cet ordre : Config Server, Eureka, catalogue,
   stock, commandes, puis notifications.

Le Config Server lit sa configuration depuis Git : les changements de
`config-repo` doivent être publiés sur la branche configurée. Mailpit est
accessible sur <http://localhost:8025> et RabbitMQ Management sur
<http://localhost:15672>.
