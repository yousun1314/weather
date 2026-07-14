$url = "https://nodejs.org/dist/v22.14.0/node-v22.14.0-x64.msi"
$outFile = "$env:TEMP\node-installer.msi"
Write-Output "Downloading Node.js from $url..."
Invoke-WebRequest -Uri $url -OutFile $outFile -UseBasicParsing
Write-Output "Downloaded to $outFile"
