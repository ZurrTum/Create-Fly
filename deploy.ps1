<#
.SYNOPSIS
    Deploy a self-built Flywheel Vulkan backend into the Create jar inside the PCL instance.

.DESCRIPTION
    Replaces ONLY the com/zurrtum/create/client/flywheel/ package inside the Create jar.
    The other ~14000 entries are left byte-for-byte untouched.

    NOTE: The Create jar filename contains CJK characters and the console codepage
    on this machine is GBK. Therefore this script NEVER hardcodes the filename -
    it always locates the jar with a wildcard + regex match.

    NOTE: All output is ASCII on purpose. PowerShell 5.1 reads .ps1 files as ANSI
    unless they carry a UTF-8 BOM, and mixing in non-ASCII breaks string parsing.

.PARAMETER DryRun
    Report what would happen; change nothing.

.PARAMETER Rollback
    Restore the Create jar from backup.

.EXAMPLE
    .\deploy.ps1 -DryRun
    .\deploy.ps1
    .\deploy.ps1 -Rollback
#>
[CmdletBinding()]
param(
    [switch]$DryRun,
    [switch]$Rollback
)

$ErrorActionPreference = 'Stop'

# ---- config -------------------------------------------------------------
$MODS    = 'D:\package\PCL formal 2.10.3\.minecraft\versions\Vulkan Optimized\mods'
$PROJECT = 'F:\Flywheel'
$PKG     = 'com/zurrtum/create/client/flywheel'
$TMP     = Join-Path $env:TEMP 'fwdeploy'
# -----------------------------------------------------------------------

# The real mods path contains CJK; rebuild it from the known-good root we
# discovered earlier by globbing the versions directory instead.
$versionsRoot = Get-ChildItem -LiteralPath 'D:\package' -Directory |
                Where-Object { $_.Name -match 'PCL' } |
                Select-Object -First 1
if (-not $versionsRoot) { throw 'Cannot locate the PCL installation under D:\package' }

$MODS = Join-Path $versionsRoot.FullName '.minecraft\versions\Vulkan Optimized\mods'
if (-not (Test-Path -LiteralPath $MODS)) { throw "Mods dir not found: $MODS" }

function Say($msg, $color = 'Gray') { Write-Host $msg -ForegroundColor $color }

# ---- locate Create jar (never hardcode the name) -------------------------
$createJar = Get-ChildItem -LiteralPath $MODS -File |
             Where-Object { $_.Name -match 'create-fly' } |
             Select-Object -First 1
if (-not $createJar) { throw "Create jar not found under $MODS" }

# Backup must NOT end in .jar or Fabric Loader will try to load it.
$backup = Join-Path $MODS 'CREATE-ORIGINAL.jar.bak'

Say "Create jar : $($createJar.Name)"
Say ("Size       : {0:N0} B" -f $createJar.Length)
Say "SHA256     : $((Get-FileHash -LiteralPath $createJar.FullName -Algorithm SHA256).Hash)"
Say "Mods dir   : $MODS"
Say ""

# ---- rollback -----------------------------------------------------------
if ($Rollback) {
    if (-not (Test-Path -LiteralPath $backup)) { throw "No backup found: $backup" }
    Copy-Item -LiteralPath $backup -Destination $createJar.FullName -Force
    Say 'Rolled back to the original jar.' 'Green'
    Say "SHA256: $((Get-FileHash -LiteralPath $createJar.FullName -Algorithm SHA256).Hash)"
    exit 0
}

# ---- backup -------------------------------------------------------------
if (-not (Test-Path -LiteralPath $backup)) {
    if ($DryRun) {
        Say "[DryRun] Would create backup: $backup" 'Yellow'
    } else {
        Copy-Item -LiteralPath $createJar.FullName -Destination $backup
        Say "Backup created -> $backup" 'Green'
    }
} else {
    Say 'Backup already exists, skipping.' 'DarkGray'
}

# ---- build --------------------------------------------------------------
$libs = Join-Path $PROJECT 'build\libs'

if (-not $DryRun) {
    Say 'Building... (first run downloads Gradle + Loom + MC, this is slow)' 'Cyan'
    Push-Location $PROJECT
    try {
        & .\gradlew.bat build --no-daemon
        if ($LASTEXITCODE -ne 0) { throw "Gradle build failed (exit $LASTEXITCODE)" }
    } finally {
        Pop-Location
    }
}

if (-not (Test-Path -LiteralPath $libs)) {
    if ($DryRun) {
        Say "[DryRun] No build output yet - run without -DryRun first to build." 'Yellow'
    } else {
        throw "Build output dir missing: $libs"
    }
    $built = $null
} else {
    $built = Get-ChildItem -LiteralPath $libs -File |
             Where-Object {
                 $_.Extension -eq '.jar' -and
                 $_.Name -notmatch 'sources|javadoc|server|dev' -and
                 $_.Name -notmatch 'rc2|unobfuscated'
             } |
             Sort-Object LastWriteTime -Descending |
             Select-Object -First 1
}

if ($built) {
    Say ("Built jar   : {0}  ({1:N2} MB)" -f $built.Name, ($built.Length / 1MB))
} else {
    Say 'Built jar   : <none>' 'Yellow'
}
Say ''

# ---- extract the flywheel package ---------------------------------------
$newCount = 0
if ($built) {
    # Extract even in DryRun - it only writes to a temp dir, and without it the
    # reported class count would be a misleading 0.
    Remove-Item -Recurse -Force $TMP -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force $TMP | Out-Null
    Push-Location $TMP
    try {
        & jar xf $built.FullName $PKG
        if ($LASTEXITCODE -ne 0) { throw 'Failed to extract from built jar' }
    } finally {
        Pop-Location
    }
    $srcDir = Join-Path $TMP ($PKG -replace '/', '\')
    if (Test-Path -LiteralPath $srcDir) {
        $newCount = (Get-ChildItem -LiteralPath $srcDir -Recurse -Filter '*.class').Count
    }
    if ($newCount -eq 0 -and -not $DryRun) { throw 'Extracted 0 classes - aborting' }
}

Say "New flywheel class count: $newCount  (baseline in jar: 525)"
$vkPath = Join-Path $TMP ($PKG -replace '/', '\')
if ($vkPath) { $vkPath = Join-Path $vkPath 'backend\vulkan\VulkanDrawManager.class' }
$hasVk = ($vkPath) -and (Test-Path -LiteralPath $vkPath)
Say "VulkanDrawManager present: $hasVk" $(if ($hasVk) { 'Green' } else { 'Yellow' })
Say ''

# ---- deploy -------------------------------------------------------------
if ($DryRun) {
    Say '[DryRun] Would run:' 'Yellow'
    Say "  jar uf `"$($createJar.FullName)`" -C `"$TMP`" `"$PKG`""
    Say "Replaces $newCount classes under $PKG only."
    exit 0
}

if (-not $built) { throw 'Nothing to deploy - build first.' }

Push-Location $TMP
try {
    & jar uf $createJar.FullName $PKG
    if ($LASTEXITCODE -ne 0) { throw "jar uf failed (exit $LASTEXITCODE)" }
} finally {
    Pop-Location
}

# ---- verify -------------------------------------------------------------
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z = [System.IO.Compression.ZipFile]::OpenRead($createJar.FullName)
try {
    $fwCount = @($z.Entries | Where-Object { $_.FullName -like "$PKG/*.class" }).Count
    $hasVk   = @($z.Entries | Where-Object { $_.FullName -eq "$PKG/backend/vulkan/VulkanDrawManager.class" }).Count -gt 0
} finally {
    $z.Dispose()
}

Say 'Deploy complete.' 'Green'
Say "  flywheel classes in jar : $fwCount  (baseline 525)"
Say "  VulkanDrawManager       : $hasVk"
Say "  new SHA256              : $((Get-FileHash -LiteralPath $createJar.FullName -Algorithm SHA256).Hash)"
Say ''
Say 'Launch the game now; look for log lines starting with [FW/VK].'
Say 'To undo: .\deploy.ps1 -Rollback'