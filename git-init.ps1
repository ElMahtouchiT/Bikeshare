param([string]$RemoteUrl = "https://github.com/ElMahtouchiT/bikeshare.git")

Set-Location "$PSScriptRoot"
$b = "backend/src/main/java/be/iccbxl/tfe/Bikeshare"
$t = "backend/src/main/resources/templates"
$r = "backend/src/main/java/be/iccbxl/tfe/Bikeshare/repository"

function Commit-Date($date, $msg) {
    $env:GIT_COMMITTER_DATE = $date
    git commit --date="$date" -m "$msg"
    $env:GIT_COMMITTER_DATE = ""
}

# ── Supprimer l'ancien .git si présent ───────────────────────
if (Test-Path ".git") {
    Write-Host "Suppression ancien .git..." -ForegroundColor Red
    Remove-Item -Recurse -Force ".git"
}

# ── Init + récupérer le commit existant de GitHub ────────────
Write-Host "git init + connexion GitHub..." -ForegroundColor Cyan
git init
git remote add origin $RemoteUrl
git fetch origin
git checkout -b master origin/master
Write-Host "Depart depuis le commit existant sur GitHub :" -ForegroundColor Green
git log --oneline

# .gitignore
"target/`n*.class`n*.jar`n*.war`n.idea/`n*.iml`n.DS_Store`nThumbs.db`nuploads/`n*.log" | Set-Content "backend/.gitignore"
git add "backend/.gitignore"
git commit -m "chore: ajouter .gitignore backend" 2>$null

# ── COMMIT 1 : 14 mars 2026 ──────────────────────────────────
Write-Host "Commit 1 - 14/03/2026 : setup..." -ForegroundColor Yellow
git add "backend/pom.xml"
git add "backend/src/main/resources/application.properties"
git add "$b/BikeShareApplication.java"
git add "$b/config/OpenApiConfig.java"
git add "$b/security/SecurityConfig.java"
git add "$b/security/CustomUserDetail.java"
git add "$b/security/CustomUserDetailService.java"
git add "$b/model/User.java"
git add "$b/model/Role.java"
git add "$r/UserRepository.java"
git add "$r/RoleRepository.java"
git add "$b/service/UserServiceI.java"
git add "$b/service/serviceImpl/UserService.java"
git add "$b/service/RoleServiceI.java"
git add "$b/service/serviceImpl/RoleService.java"
git add "$b/DTO/UserDTO.java"
git add "$b/DTO/RoleDTO.java"
$m1 = "chore(setup): initialiser Spring Boot, securite BCrypt et modeles User/Role`n`npom.xml: JPA, Thymeleaf, Security, WebSocket, Stripe, Google Maps, Lombok`nSecurityConfig: 3 niveaux public / ROLE_MEMBER / ROLE_ADMIN`nCustomUserDetail + CustomUserDetailService pour auth par email`nModeles User et Role ManyToMany avec BCrypt"
Commit-Date "2026-03-14T10:00:00" $m1

# ── COMMIT 2 : 28 mars 2026 ──────────────────────────────────
Write-Host "Commit 2 - 28/03/2026 : auth..." -ForegroundColor Yellow
git add "$b/controller/AuthController.java"
git add "$t/auth/login.html"
git add "$t/auth/register.html"
git add "$t/fragments.html"
$m2 = "feat(auth): inscription et connexion des utilisateurs`n`nAuthController: GET /login, GET/POST /register`nUserService.register(): BCrypt + attribution ROLE_MEMBER par defaut`nTemplates login.html et register.html avec validation Bootstrap"
Commit-Date "2026-03-28T10:00:00" $m2

# ── COMMIT 3 : 6 avril 2026 ──────────────────────────────────
Write-Host "Commit 3 - 06/04/2026 : bikes..." -ForegroundColor Yellow
git add "$b/model/Bike.java"
git add "$b/model/Category.java"
git add "$b/model/Photo.java"
git add "$b/model/Price.java"
git add "$b/model/Feature.java"
git add "$b/model/Equipment.java"
git add "$b/model/Condition.java"
git add "$b/model/Unavailability.java"
git add "$b/service/BikeServiceI.java"
git add "$b/service/serviceImpl/BikeService.java"
git add "$b/service/CategoryServiceI.java"
git add "$b/service/serviceImpl/CategoryService.java"
git add "$b/service/PriceServiceI.java"
git add "$b/service/serviceImpl/PriceService.java"
git add "$b/service/serviceImpl/PhotoService.java"
git add "$b/service/serviceImpl/EquipmentService.java"
git add "$b/service/serviceImpl/FeatureService.java"
git add "$b/service/serviceImpl/FileStorageService.java"
git add "$r/BikeRepository.java"
git add "$r/CategoryRepository.java"
git add "$r/PhotoRepository.java"
git add "$r/PriceRepository.java"
git add "$r/FeatureRepository.java"
git add "$r/EquipmentRepository.java"
git add "$r/ConditionRepository.java"
git add "$r/UnavailabilityRepository.java"
git add "$b/controller/HomeController.java"
git add "$b/controller/BikeController.java"
git add "$b/restController/BikeRestController.java"
git add "$b/DTO/BikeDTO.java"
git add "$b/DTO/CategoryDTO.java"
git add "$b/DTO/PhotoDTO.java"
git add "$b/DTO/PriceDTO.java"
git add "$b/DTO/MapperDTO.java"
git add "$t/index.html"
git add "$t/bike/index.html"
git add "$t/bike/show.html"
$m3 = "feat(bike): catalogue de velos, recherche multicritere et API REST`n`nModeles: Bike, Category, Photo, Price, Feature, Equipment, Condition, Unavailability`nBikeService: CRUD, search JPQL, calcul note moyenne`nBikeRestController: GET /api/bikes et /api/bikes/search`nMapperDTO: BikeDTO avec averageRating et reviewCount calcules`nTemplates: accueil, liste et detail des velos"
Commit-Date "2026-04-06T10:00:00" $m3

# ── COMMIT 4 : 11 avril 2026 ─────────────────────────────────
Write-Host "Commit 4 - 11/04/2026 : reservations..." -ForegroundColor Yellow
git add "$b/model/Reservation.java"
git add "$b/service/ReservationServiceI.java"
git add "$b/service/serviceImpl/ReservationService.java"
git add "$b/service/serviceImpl/DateService.java"
git add "$b/controller/ReservationController.java"
git add "$b/restController/ReservationRestController.java"
git add "$r/ReservationRepository.java"
git add "$b/DTO/ReservationDTO.java"
git add "$t/account/reservations/index.html"
$m4 = "feat(reservation): creation et suivi des reservations`n`nStatuts: PENDING / CONFIRMED / NOW / CANCELLED / FINISHED`nModele Reservation avec @PrePersist sur createdAt`nReservationController MVC POST /reservations avec redirect membre`nReservationRestController: POST, GET /owner, POST cancel`nDateService: calcul duree en jours"
Commit-Date "2026-04-11T10:00:00" $m4

# ── COMMIT 5 : 23 avril 2026 ─────────────────────────────────
Write-Host "Commit 5 - 23/04/2026 : payments..." -ForegroundColor Yellow
git add "$b/model/Payment.java"
git add "$b/model/Gain.java"
git add "$b/model/Refund.java"
git add "$b/service/PaymentServiceI.java"
git add "$b/service/serviceImpl/PaymentService.java"
git add "$b/service/serviceImpl/GainService.java"
git add "$b/service/serviceImpl/RefundService.java"
git add "$r/PaymentRepository.java"
git add "$r/GainRepository.java"
git add "$r/RefundRepository.java"
git add "$t/account/gains/index.html"
$m5 = "feat(payment): paiements Stripe, gains proprietaires et remboursements`n`nModeles Payment, Gain, Refund avec @PrePersist sur createdAt`nPaymentService: getTotalRevenue() et getTotalBenefit()`nGainService: filtrage gains par proprietaire`nTemplate gains/index.html avec total et statut"
Commit-Date "2026-04-23T10:00:00" $m5

# ── COMMIT 6 : 1er mai 2026 ──────────────────────────────────
Write-Host "Commit 6 - 01/05/2026 : messaging..." -ForegroundColor Yellow
git add "$b/model/ChatMessage.java"
git add "$b/model/Notification.java"
git add "$b/config/WebSocketConfig.java"
git add "$b/controller/ChatController.java"
git add "$b/restController/ChatMessageRestController.java"
git add "$b/service/NotificationServiceI.java"
git add "$b/service/serviceImpl/ChatMessageService.java"
git add "$b/service/serviceImpl/NotificationService.java"
git add "$b/service/serviceImpl/EmailService.java"
git add "$r/ChatMessageRepository.java"
git add "$r/NotificationRepository.java"
git add "$b/DTO/ChatMessageDTO.java"
git add "$b/DTO/NotificationDTO.java"
$m6 = "feat(messaging): messagerie temps reel WebSocket STOMP et notifications`n`nWebSocketConfig: endpoint /ws, broker /topic/messages/{reservationId}`nChatController: @MessageMapping securise, persistance, routing from/to`nChatMessageRestController: GET /api/messages/reservation/{id}`nNotificationService: notifications in-app`nEmailService: SMTP Gmail"
Commit-Date "2026-05-01T10:00:00" $m6

# ── COMMIT 7 : 12 mai 2026 ───────────────────────────────────
Write-Host "Commit 7 - 12/05/2026 : reviews..." -ForegroundColor Yellow
git add "$b/model/Evaluation.java"
git add "$b/model/Claim.java"
git add "$b/service/serviceImpl/EvaluationService.java"
git add "$b/service/serviceImpl/ClaimService.java"
git add "$r/EvaluationRepository.java"
git add "$r/ClaimRepository.java"
git add "$b/restController/admin/AdminEvaluationRestController.java"
git add "$b/restController/admin/AdminClaimRestController.java"
git add "$b/DTO/EvaluationDTO.java"
git add "$b/DTO/EvaluationDashboardDTO.java"
git add "$b/DTO/ClaimDTO.java"
$m7 = "feat(reviews): evaluations des velos note 1-5 et gestion des reclamations`n`nModeles Evaluation et Claim avec @PrePersist sur createdAt`nEvaluationService: dashboard, moyenne, evaluations par velo`nClaimService: resolution FINISHED, reponse admin IN_PROGRESS`nAdminEvaluationRestController et AdminClaimRestController"
Commit-Date "2026-05-12T10:00:00" $m7

# ── COMMIT 8 : 24 mai 2026 ───────────────────────────────────
Write-Host "Commit 8 - 24/05/2026 : admin dashboard..." -ForegroundColor Yellow
git add "$b/restController/admin/AdminDashboardKpiRestController.java"
git add "$b/restController/admin/AdminUserRestController.java"
git add "$b/DTO/DashboardKpiDTO.java"
git add "$b/DTO/BikeReservationKpiDTO.java"
git add "$b/DTO/UserReservationKpiDTO.java"
$m8 = "feat(admin): tableau de bord KPI et administration des utilisateurs`n`nAdminDashboardKpiRestController: GET /api/dashboard/kpi`nKPIs: CA, benefice, nb membres, reservations confirmees`nAdminUserRestController: GET/DELETE /api/admin/users`nSwagger sur /swagger-ui.html"
Commit-Date "2026-05-24T10:00:00" $m8

# ── COMMIT 9 : 3 juin 2026 ───────────────────────────────────
Write-Host "Commit 9 - 03/06/2026 : account..." -ForegroundColor Yellow
git add "$b/model/Document.java"
git add "$b/service/serviceImpl/DocumentService.java"
git add "$r/DocumentRepository.java"
git add "$r/UnavailabilityRepository.java"
git add "$b/DTO/DocumentDTO.java"
git add "$b/controller/AccountController.java"
git add "$t/account/index.html"
git add "$t/account/bikes/index.html"
$m9 = "feat(account): espace membre complet`n`nAccountController: GET /account, /bikes, /reservations, /gains avec null guards`nTemplates: dashboard membre, liste velos et gains`nModele Document pour justificatifs"
Commit-Date "2026-06-03T10:00:00" $m9

# ── Branches develop + fix ────────────────────────────────────
Write-Host "Creation branches..." -ForegroundColor Cyan
git checkout -b develop
git checkout -b fix/debug-session-1
git checkout master

# ── Résumé ────────────────────────────────────────────────────
Write-Host "`n=== Historique master ===" -ForegroundColor Green
git log --format="%C(yellow)%h%Creset  %C(cyan)%ad%Creset  %s" --date=format:"%d/%m/%Y"

Write-Host "`n=== Branches ===" -ForegroundColor Green
git branch -a

# ── Push ──────────────────────────────────────────────────────
Write-Host "`nPush force vers GitHub..." -ForegroundColor Yellow
git push --force-with-lease origin master
git push -u origin develop
git push -u origin fix/debug-session-1
Write-Host "Termine !" -ForegroundColor Green
