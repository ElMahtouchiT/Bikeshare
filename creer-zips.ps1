Set-Location "$PSScriptRoot"
$base = "$PSScriptRoot"
$b = "backend\src\main\java\be\iccbxl\tfe\Bikeshare"
$t = "backend\src\main\resources\templates"
$r = "backend\src\main\java\be\iccbxl\tfe\Bikeshare\repository"
$out = "$PSScriptRoot\zips-commits"

if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Force $out | Out-Null

function Make-Zip($num, $label, $files) {
    $tmp = "$out\tmp_$num"
    New-Item -ItemType Directory -Force $tmp | Out-Null
    foreach ($f in $files) {
        $src = Join-Path $base $f
        if (-not (Test-Path $src)) { Write-Warning "MANQUANT: $f"; continue }
        $dst = Join-Path $tmp $f
        $dir = Split-Path $dst -Parent
        if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force $dir | Out-Null }
        Copy-Item $src $dst
    }
    $zip = "$out\commit$num`_$label.zip"
    Compress-Archive -Path "$tmp\*" -DestinationPath $zip -Force
    Remove-Item -Recurse -Force $tmp
    Write-Host "OK  commit$num`_$label.zip" -ForegroundColor Green
}

# ── COMMIT 1 — 14 mars 2026 ───────────────────────────────────
Make-Zip 1 "14mars" @(
    "backend\pom.xml"
    "backend\src\main\resources\application.properties"
    "$b\BikeShareApplication.java"
    "$b\config\OpenApiConfig.java"
    "$b\security\SecurityConfig.java"
    "$b\security\CustomUserDetail.java"
    "$b\security\CustomUserDetailService.java"
    "$b\model\User.java"
    "$b\model\Role.java"
    "$r\UserRepository.java"
    "$r\RoleRepository.java"
    "$b\service\UserServiceI.java"
    "$b\service\serviceImpl\UserService.java"
    "$b\service\RoleServiceI.java"
    "$b\service\serviceImpl\RoleService.java"
    "$b\DTO\UserDTO.java"
    "$b\DTO\RoleDTO.java"
)

# ── COMMIT 2 — 28 mars 2026 ───────────────────────────────────
Make-Zip 2 "28mars" @(
    "$b\controller\AuthController.java"
    "$t\auth\login.html"
    "$t\auth\register.html"
    "$t\fragments.html"
)

# ── COMMIT 3 — 6 avril 2026 ───────────────────────────────────
Make-Zip 3 "06avril" @(
    "$b\model\Bike.java"
    "$b\model\Category.java"
    "$b\model\Photo.java"
    "$b\model\Price.java"
    "$b\model\Feature.java"
    "$b\model\Equipment.java"
    "$b\model\Condition.java"
    "$b\model\Unavailability.java"
    "$b\service\BikeServiceI.java"
    "$b\service\serviceImpl\BikeService.java"
    "$b\service\CategoryServiceI.java"
    "$b\service\serviceImpl\CategoryService.java"
    "$b\service\PriceServiceI.java"
    "$b\service\serviceImpl\PriceService.java"
    "$b\service\serviceImpl\PhotoService.java"
    "$b\service\serviceImpl\EquipmentService.java"
    "$b\service\serviceImpl\FeatureService.java"
    "$b\service\serviceImpl\FileStorageService.java"
    "$r\BikeRepository.java"
    "$r\CategoryRepository.java"
    "$r\PhotoRepository.java"
    "$r\PriceRepository.java"
    "$r\FeatureRepository.java"
    "$r\EquipmentRepository.java"
    "$r\ConditionRepository.java"
    "$r\UnavailabilityRepository.java"
    "$b\controller\HomeController.java"
    "$b\controller\BikeController.java"
    "$b\restController\BikeRestController.java"
    "$b\DTO\BikeDTO.java"
    "$b\DTO\CategoryDTO.java"
    "$b\DTO\PhotoDTO.java"
    "$b\DTO\PriceDTO.java"
    "$b\DTO\MapperDTO.java"
    "$t\index.html"
    "$t\bike\index.html"
    "$t\bike\show.html"
)

# ── COMMIT 4 — 11 avril 2026 ──────────────────────────────────
Make-Zip 4 "11avril" @(
    "$b\model\Reservation.java"
    "$b\service\ReservationServiceI.java"
    "$b\service\serviceImpl\ReservationService.java"
    "$b\service\serviceImpl\DateService.java"
    "$b\controller\ReservationController.java"
    "$b\restController\ReservationRestController.java"
    "$r\ReservationRepository.java"
    "$b\DTO\ReservationDTO.java"
    "$t\account\reservations\index.html"
)

# ── COMMIT 5 — 23 avril 2026 ──────────────────────────────────
Make-Zip 5 "23avril" @(
    "$b\model\Payment.java"
    "$b\model\Gain.java"
    "$b\model\Refund.java"
    "$b\service\PaymentServiceI.java"
    "$b\service\serviceImpl\PaymentService.java"
    "$b\service\serviceImpl\GainService.java"
    "$b\service\serviceImpl\RefundService.java"
    "$r\PaymentRepository.java"
    "$r\GainRepository.java"
    "$r\RefundRepository.java"
    "$t\account\gains\index.html"
)

# ── COMMIT 6 — 1er mai 2026 ───────────────────────────────────
Make-Zip 6 "01mai" @(
    "$b\model\ChatMessage.java"
    "$b\model\Notification.java"
    "$b\config\WebSocketConfig.java"
    "$b\controller\ChatController.java"
    "$b\restController\ChatMessageRestController.java"
    "$b\service\NotificationServiceI.java"
    "$b\service\serviceImpl\ChatMessageService.java"
    "$b\service\serviceImpl\NotificationService.java"
    "$b\service\serviceImpl\EmailService.java"
    "$r\ChatMessageRepository.java"
    "$r\NotificationRepository.java"
    "$b\DTO\ChatMessageDTO.java"
    "$b\DTO\NotificationDTO.java"
)

# ── COMMIT 7 — 12 mai 2026 ────────────────────────────────────
Make-Zip 7 "12mai" @(
    "$b\model\Evaluation.java"
    "$b\model\Claim.java"
    "$b\service\serviceImpl\EvaluationService.java"
    "$b\service\serviceImpl\ClaimService.java"
    "$r\EvaluationRepository.java"
    "$r\ClaimRepository.java"
    "$b\restController\admin\AdminEvaluationRestController.java"
    "$b\restController\admin\AdminClaimRestController.java"
    "$b\DTO\EvaluationDTO.java"
    "$b\DTO\EvaluationDashboardDTO.java"
    "$b\DTO\ClaimDTO.java"
)

# ── COMMIT 8 — 24 mai 2026 ────────────────────────────────────
Make-Zip 8 "24mai" @(
    "$b\restController\admin\AdminDashboardKpiRestController.java"
    "$b\restController\admin\AdminUserRestController.java"
    "$b\DTO\DashboardKpiDTO.java"
    "$b\DTO\BikeReservationKpiDTO.java"
    "$b\DTO\UserReservationKpiDTO.java"
)

# ── COMMIT 9 — 3 juin 2026 ────────────────────────────────────
Make-Zip 9 "03juin" @(
    "$b\model\Document.java"
    "$b\service\serviceImpl\DocumentService.java"
    "$r\DocumentRepository.java"
    "$r\UnavailabilityRepository.java"
    "$b\DTO\DocumentDTO.java"
    "$b\controller\AccountController.java"
    "$t\account\index.html"
    "$t\account\bikes\index.html"
)

Write-Host "`n9 ZIPs generes dans : $out" -ForegroundColor Cyan
explorer $out
