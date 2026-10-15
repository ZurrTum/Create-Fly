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
# The java package we replace wholesale...
$PKG     = 'com/zurrtum/create/client/flywheel'
# ...and the resource tree, which is updated the same way. New GLSL has to reach the jar too:
# a file that only exists on our side (vertex_input_vk.vert) is otherwise invisible to the game
# and every pipeline compile fails with "was not found".
$ASSETS  = 'assets/flywheel'

# The mixin package lives outside $PKG. Without this the running game kept executing the upstream
# jar's copies while we edited ours - a silent gap that made mixin-side experiments meaningless,
# because the probes added to them never reached the game.
$MIXINS  = 'com/zurrtum/create/client/mixin'

# The mixin configs sit at the jar root, outside both $MIXINS and $PKG. Deploying the mixin classes
# without them is not enough: a newly added mixin class that no config lists is never loaded, so the
# game keeps running the upstream behaviour and the change looks like it did nothing at all.
$MIXIN_CONFIGS = @(
    'create.client.mixins.json'
    'create.mixins.json'
)

# Classes outside $PKG that we patched and therefore have to replace individually.
#
# BracketedKineticBlockEntityRenderer#extractRenderState read state.direction *after* calling into
# its superclass, which returns early once Flywheel owns the block - leaving direction null and
# NPEing on every large cogwheel. It is Create code, not Flywheel code, but enabling Flywheel is
# what makes that path reachable in the first place, so the fix ships with the backend.
$EXTRA_CLASSES = @(
    'com/zurrtum/create/client/content/kinetics/simpleRelays/BracketedKineticBlockEntityRenderer.class'
)
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

# ---- extract the flywheel package + resources ----------------------------
$newCount = 0
$resCount = 0
if ($built) {
    # Extract even in DryRun - it only writes to a temp dir, and without it the
    # reported class count would be a misleading 0.
    Remove-Item -Recurse -Force $TMP -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force $TMP | Out-Null
    Push-Location $TMP
    try {
        & jar xf $built.FullName $PKG
        if ($LASTEXITCODE -ne 0) { throw 'Failed to extract classes from built jar' }

        & jar xf $built.FullName $ASSETS
        if ($LASTEXITCODE -ne 0) { throw 'Failed to extract resources from built jar' }

        & jar xf $built.FullName $MIXINS
        if ($LASTEXITCODE -ne 0) { throw 'Failed to extract mixin classes from built jar' }

        foreach ($cfg in $MIXIN_CONFIGS) {
            & jar xf $built.FullName $cfg
            if ($LASTEXITCODE -ne 0) { throw "Failed to extract $cfg from built jar" }
        }

        foreach ($extra in $EXTRA_CLASSES) {
            & jar xf $built.FullName $extra
            if ($LASTEXITCODE -ne 0) { throw "Failed to extract $extra from built jar" }
        }
    } finally {
        Pop-Location
    }
    $srcDir = Join-Path $TMP ($PKG -replace '/', '\')
    if (Test-Path -LiteralPath $srcDir) {
        $newCount = (Get-ChildItem -LiteralPath $srcDir -Recurse -Filter '*.class').Count
    }
    $resDir = Join-Path $TMP ($ASSETS -replace '/', '\')
    if (Test-Path -LiteralPath $resDir) {
        $resCount = @(Get-ChildItem -LiteralPath $resDir -Recurse -File).Count
    }
    if ($newCount -eq 0 -and -not $DryRun) { throw 'Extracted 0 classes - aborting' }
}

Say "New flywheel class count: $newCount  (baseline in jar: 525)"
Say "New flywheel resource count: $resCount"
$vkPath = Join-Path $TMP ($PKG -replace '/', '\')
if ($vkPath) { $vkPath = Join-Path $vkPath 'backend\vulkan\VulkanDrawManager.class' }
$hasVk = ($vkPath) -and (Test-Path -LiteralPath $vkPath)
Say "VulkanDrawManager present: $hasVk" $(if ($hasVk) { 'Green' } else { 'Yellow' })
$vkVert = Join-Path $TMP ($ASSETS -replace '/', '\')
if ($vkVert) { $vkVert = Join-Path $vkVert 'flywheel\internal\vertex_input_vk.vert' }
$hasVkVert = ($vkVert) -and (Test-Path -LiteralPath $vkVert)
Say "vertex_input_vk.vert present: $hasVkVert" $(if ($hasVkVert) { 'Green' } else { 'Yellow' })
Say ''

# ---- deploy -------------------------------------------------------------
if ($DryRun) {
    Say '[DryRun] Would run:' 'Yellow'
    Say "  jar uf `"$($createJar.FullName)`" -C `"$TMP`" `"$PKG`" `"$ASSETS`""
    Say "Replaces $newCount classes under $PKG and $resCount resources under $ASSETS."
    exit 0
}

if (-not $built) { throw 'Nothing to deploy - build first.' }
if (-not $hasVkVert) { throw 'vertex_input_vk.vert missing from the built jar - aborting before a partial deploy' }

Push-Location $TMP
try {
    & jar uf $createJar.FullName $PKG
    if ($LASTEXITCODE -ne 0) { throw "jar uf failed for $PKG (exit $LASTEXITCODE)" }

    & jar uf $createJar.FullName $ASSETS
    if ($LASTEXITCODE -ne 0) { throw "jar uf failed for $ASSETS (exit $LASTEXITCODE)" }

    & jar uf $createJar.FullName $MIXINS
    if ($LASTEXITCODE -ne 0) { throw "jar uf failed for $MIXINS (exit $LASTEXITCODE)" }

    foreach ($cfg in $MIXIN_CONFIGS) {
        & jar uf $createJar.FullName $cfg
        if ($LASTEXITCODE -ne 0) { throw "jar uf failed for $cfg (exit $LASTEXITCODE)" }
    }

    foreach ($extra in $EXTRA_CLASSES) {
        & jar uf $createJar.FullName $extra
        if ($LASTEXITCODE -ne 0) { throw "jar uf failed for $extra (exit $LASTEXITCODE)" }
    }
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
    $hasVkVert = @($z.Entries | Where-Object { $_.FullName -eq "$ASSETS/flywheel/internal/vertex_input_vk.vert" }).Count -gt 0
    $mixinCount = @($z.Entries | Where-Object { $_.FullName -like "$MIXINS/*.class" }).Count
    # A mixin class that no config lists is silently never loaded, so a stale root-level config can
    # make a deploy look successful while the game keeps running the upstream behaviour. Compare the
    # config in the jar against the one we just built and report any entry that only exists in ours.
    $unlisted = @()
    foreach ($cfg in $MIXIN_CONFIGS) {
        $e = $z.Entries | Where-Object { $_.FullName -eq $cfg }
        if (-not $e) { $unlisted += "${cfg}:missing"; continue }
        $sr = New-Object System.IO.StreamReader($e.Open())
        $inJar = $sr.ReadToEnd() | ConvertFrom-Json
        $sr.Close()
        $builtCfg = Join-Path (Join-Path $TMP $CFG_DIR) $cfg
        if (-not (Test-Path $builtCfg)) { continue }
        $inBuild = [System.IO.File]::ReadAllText($builtCfg) | ConvertFrom-Json
        $builtNames = @($inBuild.client) + @($inBuild.mixins) + @($inBuild.server)
        $jarNames   = @($inJar.client)   + @($inJar.mixins)   + @($inJar.server)
        $unlisted += @($builtNames | Where-Object { $_ -notin $jarNames } | ForEach-Object { "${cfg}:$_" })
    }
    $patchedCount = 0
    foreach ($extra in $EXTRA_CLASSES) {
        if (@($z.Entries | Where-Object { $_.FullName -eq $extra }).Count -gt 0) {
            $patchedCount++
        }
    }
} finally {
    $z.Dispose()
}

Say 'Deploy complete.' 'Green'
Say "  flywheel classes in jar : $fwCount  (baseline 525)"
Say "  VulkanDrawManager       : $hasVk"
Say "  vertex_input_vk.vert    : $hasVkVert" $(if ($hasVkVert) { 'Green' } else { 'Red' })
Say "  mixin classes in jar   : $mixinCount" $(if ($mixinCount -gt 0) { 'Green' } else { 'Red' })
Say "  mixin config drift     : $(if ($unlisted.Count -eq 0) { 'none' } else { $unlisted -join ', ' })" $(if ($unlisted.Count -eq 0) { 'Green' } else { 'Red' })
Say "  patched Create classes  : $patchedCount / $($EXTRA_CLASSES.Count)" $(if ($patchedCount -eq $EXTRA_CLASSES.Count) { 'Green' } else { 'Red' })
Say "  new SHA256              : $((Get-FileHash -LiteralPath $createJar.FullName -Algorithm SHA256).Hash)"
Say ''
Say 'Launch the game now; look for log lines starting with [FW/VK].'
Say 'To undo: .\deploy.ps1 -Rollback'