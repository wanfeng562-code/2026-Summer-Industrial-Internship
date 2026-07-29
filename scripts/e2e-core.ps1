param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$DemoPassword = "123456",
    [switch]$TestAi
)

$ErrorActionPreference = "Stop"

function Invoke-TicketApi {
    param(
        [ValidateSet("GET", "POST", "PUT", "PATCH", "DELETE")]
        [string]$Method,
        [string]$Path,
        [string]$Token,
        [object]$Body
    )

    $arguments = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        ContentType = "application/json; charset=utf-8"
    }
    if ($Token) {
        $arguments.Headers = @{ Authorization = "Bearer $Token" }
    }
    if ($null -ne $Body) {
        $arguments.Body = $Body | ConvertTo-Json -Depth 8
    }
    Invoke-RestMethod @arguments
}

function Login-DemoUser {
    param([string]$Username)
    $response = Invoke-TicketApi -Method POST -Path "/user/login" -Body @{
        username = $Username
        password = $DemoPassword
    }
    if ($response.code -ne 200 -or -not $response.data.token) {
        throw "$Username login failed: $($response.msg)"
    }
    $response.data
}

function Assert-ApiSuccess {
    param([object]$Response, [string]$Step)
    if ($Response.code -ne 200) {
        throw "$Step failed: $($Response.msg)"
    }
    Write-Host "[PASS] $Step" -ForegroundColor Green
}

Write-Host "Checking backend: $BaseUrl" -ForegroundColor Cyan
$user = Login-DemoUser -Username "user_wang"
$agent = Login-DemoUser -Username "agent_zhang"
$admin = Login-DemoUser -Username "admin"
Write-Host "[PASS] USER / AGENT / ADMIN login" -ForegroundColor Green

$orders = Invoke-TicketApi -Method GET -Path "/orders?current=1&size=10" -Token $user.token
Assert-ApiSuccess -Response $orders -Step "User order page"
$order = $orders.data.records | Select-Object -First 1
if (-not $order) {
    throw "user_wang has no demo order. Import data.sql first."
}

$suffix = Get-Date -Format "MMddHHmmss"
$created = Invoke-TicketApi -Method POST -Path "/tickets" -Token $user.token -Body @{
    orderId = $order.id
    title = "E2E smoke ticket $suffix"
    description = "E2E smoke test: logistics tracking has not updated; please transfer to an agent."
    category = "LOGISTICS"
    priority = "MEDIUM"
}
Assert-ApiSuccess -Response $created -Step "User creates ticket"
$ticketId = $created.data.id

$ticket = Invoke-TicketApi -Method GET -Path "/tickets/$ticketId" -Token $user.token
if ($ticket.data.status -eq "AI_PROCESSING") {
    $manual = Invoke-TicketApi -Method POST -Path "/tickets/$ticketId/transfer-manual" -Token $user.token
    Assert-ApiSuccess -Response $manual -Step "User transfers ticket to manual review"
}

$claimed = Invoke-TicketApi -Method POST -Path "/tickets/$ticketId/claim" -Token $agent.token
Assert-ApiSuccess -Response $claimed -Step "Agent claims ticket"

$message = Invoke-TicketApi -Method POST -Path "/tickets/$ticketId/messages" -Token $agent.token -Body @{
    content = "The logistics status has been checked and is being handled."
}
Assert-ApiSuccess -Response $message -Step "Agent replies"

$resolved = Invoke-TicketApi -Method POST -Path "/tickets/$ticketId/resolve" -Token $agent.token -Body @{
    content = "The logistics issue has been resolved."
}
Assert-ApiSuccess -Response $resolved -Step "Agent resolves ticket"

$closed = Invoke-TicketApi -Method POST -Path "/tickets/$ticketId/close" -Token $agent.token -Body @{
    reason = "E2E smoke test completed"
}
Assert-ApiSuccess -Response $closed -Step "Agent closes ticket"

$finalTicket = Invoke-TicketApi -Method GET -Path "/tickets/$ticketId" -Token $user.token
Assert-ApiSuccess -Response $finalTicket -Step "User reads final ticket"
if ($finalTicket.data.status -ne "CLOSED") {
    throw "Expected CLOSED but got $($finalTicket.data.status)"
}

$users = Invoke-TicketApi -Method GET -Path "/users?current=1&size=10" -Token $admin.token
Assert-ApiSuccess -Response $users -Step "Admin reads user page"
$stats = Invoke-TicketApi -Method GET -Path "/stats/tickets" -Token $admin.token
Assert-ApiSuccess -Response $stats -Step "Admin reads dashboard stats"

if ($TestAi) {
    $ai = Invoke-TicketApi -Method POST -Path "/ai/chat" -Token $user.token -Body @{
        message = "How long does a refund usually take?"
    }
    Assert-ApiSuccess -Response $ai -Step "DashScope chat"
}

Write-Host ""
Write-Host "Core E2E smoke test passed. Ticket ID: $ticketId" -ForegroundColor Green
Write-Host "Use -TestAi after DASHSCOPE_API_KEY is configured." -ForegroundColor Cyan
