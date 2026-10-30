# Projet bancaire micro-services

Petit projet réalisé pour l'épreuve technique ADRIA. On peut consulter des comptes bancaires, enregistrer des
transactions, faire des virements, et poser des questions à un assistant (chatbot) qui sait faire ces opérations.

## Ce qu'il y a dans le projet

- **account-service** (8081) : gère les comptes (solde, devise, statut actif ou bloqué).
- **transaction-service** (8082) : enregistre les transactions et les virements. Il appelle account-service
  avec OpenFeign, et Resilience4J évite que tout plante si account-service est arrêté.
- **gateway-service** (8080) : la porte d'entrée, tout passe par lui.
- **discovery-service** (8761) : Eureka, pour que les services se retrouvent.
- **config-service** (8888) : donne le plafond des transactions (`bank.transaction.max-amount=1000`).
- **assistant-service** (8084) : le chatbot. Il utilise un LLM et les outils MCP des deux services métier.
- **bank-client** (4200) : l'interface web en Angular.

Account et transaction exposent la même chose de trois façons : REST, GraphQL et MCP. Les trois passent
par la même couche `service`.

## Lancer le projet

Il faut Java 17, Maven (ou celui d'IntelliJ) et Node pour Angular.

Pour le chatbot, il faut une clé d'API. J'utilise Groq. Créer le fichier
`assistant-service/src/main/resources/application-local.properties` (il est ignoré par git) :

```properties
spring.ai.openai.api-key=ta_clé
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
```

Ensuite, démarrer les applications dans cet ordre :

1. ConfigServiceApplication
2. DiscoveryServiceApplication
3. AccountServiceApplication
4. TransactionServiceApplication
5. AssistantServiceApplication
6. GatewayServiceApplication

Et le site :

```bash
cd bank-client
npm install
ng serve
```

Puis ouvrir http://localhost:4200.

## Comptes de test

Ils sont créés automatiquement au démarrage d'account-service (base en mémoire, donc remis à zéro à chaque fois).

| Compte | Id | Devise | Solde | Statut |
|---|---|---|---|---|
| A | 11111111-1111-1111-1111-111111111111 | EUR | 500 | actif |
| B | 22222222-2222-2222-2222-222222222222 | EUR | 200 | actif |
| C | 33333333-3333-3333-3333-333333333333 | USD | 300 | actif |
| D | 44444444-4444-4444-4444-444444444444 | EUR | 100 | bloqué |

## Quelques requêtes

Toutes passent par le gateway (port 8080).

- `GET /api/accounts` : liste des comptes
- `POST /api/transactions` : nouvelle transaction, par exemple
  `{"accountId":"11111111-1111-1111-1111-111111111111","amount":50,"currency":"EUR","merchantCategory":"GROCERY","country":"FR"}`
- `POST /api/transactions/transfers` : virement entre deux comptes
- `POST /api/chat` : parler au chatbot, par exemple `{"message":"Liste tous les comptes"}`

Chaque service a aussi un Swagger (`/swagger-ui.html`) et GraphiQL (`/graphiql`) sur son port.

## Les règles d'une transaction

- le montant doit être positif et ne pas dépasser 1000
- le compte doit exister et être actif
- la devise doit être la même que celle du compte
- il faut assez de solde, et le solde est mis à jour après la transaction

## Tester la tolérance aux pannes

Faire une transaction normalement, puis arrêter account-service et recommencer : transaction-service répond
une erreur 503 propre au lieu de planter. Après quelques échecs, le disjoncteur s'ouvre
(visible sur http://localhost:8082/actuator/circuitbreakers). Quand on relance account-service, tout revient.

## Ce qui reste à faire

- Kafka (événement `bank.transactions.created` et consumer)
- Détection de fraude en Python
- Dockerfile, Docker Compose et pipeline GitHub Actions
