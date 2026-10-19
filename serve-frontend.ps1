param(
    [int]$Port = 3000,
    [string]$Root = "src\main\resources\static"
)

$resolvedRoot = (Resolve-Path $Root).Path
if (-not (Test-Path $resolvedRoot)) {
    Write-Error "Static directory not found: $resolvedRoot"
    exit 1
}

$ipEndpoint = New-Object System.Net.IPEndPoint([System.Net.IPAddress]::Any, $Port)
$listener = New-Object System.Net.Sockets.TcpListener($ipEndpoint)

try {
    $listener.Start()
} catch {
    Write-Error "Failed to bind to 0.0.0.0:$Port - $($_.Exception.Message)"
    exit 1
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " RoadResQ Frontend Static Server" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Listening on   : 0.0.0.0:$Port"
Write-Host " Local URL      : http://localhost:$Port"
Write-Host " Wi-Fi / LAN URL: http://192.168.31.219:$Port"
Write-Host " Serving Files  : $resolvedRoot"
Write-Host " Connected API  : http://192.168.31.219:8080"
Write-Host " Press Ctrl+C to stop the server."
Write-Host "==========================================================" -ForegroundColor Cyan

$mimeTypes = @{
    ".html"  = "text/html; charset=utf-8"
    ".htm"   = "text/html; charset=utf-8"
    ".css"   = "text/css; charset=utf-8"
    ".js"    = "application/javascript; charset=utf-8"
    ".mjs"   = "application/javascript; charset=utf-8"
    ".json"  = "application/json; charset=utf-8"
    ".png"   = "image/png"
    ".jpg"   = "image/jpeg"
    ".jpeg"  = "image/jpeg"
    ".gif"   = "image/gif"
    ".svg"   = "image/svg+xml"
    ".ico"   = "image/x-icon"
    ".webp"  = "image/webp"
    ".woff"  = "font/woff"
    ".woff2" = "font/woff2"
    ".ttf"   = "font/ttf"
}

try {
    while ($true) {
        $client = $listener.AcceptTcpClient()
        $stream = $client.GetStream()
        $reader = New-Object System.IO.StreamReader($stream)
        
        $requestLine = $reader.ReadLine()
        if ([string]::IsNullOrWhiteSpace($requestLine)) {
            $client.Close()
            continue
        }
        
        $parts = $requestLine.Split(" ")
        if ($parts.Length -lt 2) {
            $client.Close()
            continue
        }
        
        $method = $parts[0]
        $rawPath = $parts[1].Split("?")[0]
        if ($rawPath -eq "/" -or [string]::IsNullOrWhiteSpace($rawPath)) {
            $rawPath = "/index.html"
        }
        
        $relativePath = $rawPath.TrimStart("/").Replace("/", [System.IO.Path]::DirectorySeparatorChar)
        $filePath = [System.IO.Path]::Combine($resolvedRoot, $relativePath)
        
        if ((Test-Path $filePath -PathType Leaf) -and $filePath.StartsWith($resolvedRoot)) {
            $ext = [System.IO.Path]::GetExtension($filePath).ToLower()
            $mime = if ($mimeTypes.ContainsKey($ext)) { $mimeTypes[$ext] } else { "application/octet-stream" }
            $bytes = [System.IO.File]::ReadAllBytes($filePath)
            
            $header = "HTTP/1.1 200 OK`r`n" +
                      "Content-Type: $mime`r`n" +
                      "Content-Length: $($bytes.Length)`r`n" +
                      "Access-Control-Allow-Origin: *`r`n" +
                      "Connection: close`r`n`r`n"
            $headerBytes = [System.Text.Encoding]::ASCII.GetBytes($header)
            
            $stream.Write($headerBytes, 0, $headerBytes.Length)
            if ($method -ne "HEAD") {
                $stream.Write($bytes, 0, $bytes.Length)
            }
        } else {
            $notFound = "HTTP/1.1 404 Not Found`r`nContent-Type: text/plain`r`nContent-Length: 9`r`nConnection: close`r`n`r`nNot Found"
            $notFoundBytes = [System.Text.Encoding]::ASCII.GetBytes($notFound)
            $stream.Write($notFoundBytes, 0, $notFoundBytes.Length)
        }
        
        $stream.Flush()
        $client.Close()
    }
} finally {
    $listener.Stop()
}
