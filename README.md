# 🚲 BikeShare — Plateforme de location de vélos entre particuliers

Application web de location de vélos entre particuliers : un membre met son vélo en
ligne, un autre le réserve et paie en ligne, avec messagerie, évaluations et espace
d'administration. Architecture Spring Boot en couches.
Package racine : `be.iccbxl.tfe.Bikeshare`.

## Arborescence du code source (101 fichiers Java)

```
backend/src/main/java/be/iccbxl/tfe/Bikeshare/
├── BikeShareApplication.java
├── model/            (19 entités JPA)
│   ├── User, Role, Category, Price, Bike, Photo, Condition,
│   ├── Equipment, Feature, Unavailability, Reservation, Payment,
│   └── Gain, Refund, Evaluation, Claim, ChatMessage, Notification, Document
├── repository/       (19 repositories Spring Data JPA)
├── service/          (interfaces : BikeServiceI, UserServiceI, ReservationServiceI…)
│   └── serviceImpl/  (20 implémentations : BikeService, UserService, ReservationService,
│                      PaymentService, ClaimService, EvaluationService, ChatMessageService,
│                      NotificationService, EmailService, FileStorageService, etc.)
├── DTO/              (16 DTO + MapperDTO)
├── controller/       (MVC : Home, Bike, Auth, Account, Chat)
├── restController/   (BikeRestController, ReservationRestController, ChatMessageRestController)
│   └── admin/        (AdminUser, AdminClaim, AdminEvaluation, AdminDashboardKpi)
├── security/         (CustomUserDetail, CustomUserDetailService, SecurityConfig)
└── config/           (WebSocketConfig, OpenApiConfig)

backend/src/main/resources/
├── application.properties
└── templates/        (index, bike/, auth/, account/, fragments)

database/bikeshare.sql      — schéma MySQL (23 tables) + données de référence
prototype/index.html        — prototype web cliquable
assets/bikeshare-logo.png   — logo officiel
```

## Architecture

L'application est organisée en couches :

- **model** : 19 entités JPA (vélo, utilisateur, réservation, paiement, etc.).
- **repository** : accès aux données via Spring Data JPA.
- **service** : interfaces `XxxServiceI` et implémentations dans `serviceImpl/`
  (logique de réservation, calcul du prix avec réductions de durée, paiement avec
  commission de la plateforme `partBikeshare`, évaluations, réclamations, notifications).
- **DTO + MapperDTO** : objets de transfert et conversion depuis les entités.
- **controller** (MVC, Thymeleaf) et **restController** (API REST + sous-package `admin/`).
- **security** : Spring Security + BCrypt, trois rôles (visiteur, membre, administrateur).
- **config** : WebSocket/STOMP (messagerie de réservation) et OpenAPI (Swagger).

Champs spécifiques au vélo : `frameNumber`, `purchaseYear`, `bikeType`, `electric`,
`wheelSize`, `gears`, `frameSize`.

## Pile technique
Spring Boot 3.1.4 · Java 17 · Spring Security + BCrypt · Spring Data JPA / Hibernate ·
MySQL · Thymeleaf + Bootstrap · WebSocket/STOMP · Stripe · springdoc-openapi (Swagger).

## Démarrage
```bash
mysql -u root -p < database/bikeshare.sql           # 1. base de données
cd backend && mvn spring-boot:run                    # 2. backend (port 8080)
```
Swagger UI : http://localhost:8080/swagger-ui.html
Prototype sans installation : ouvrir `prototype/index.html`.
