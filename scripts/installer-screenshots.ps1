# 설치 마법사를 띄워 화면마다 캡처하고, 기본 선택 그대로 다음 화면으로 넘겨 설치를 끝낸다.
# 설치 화면 미리보기 워크플로가 Windows 러너에서 쓴다. 설치 창은 클라우드나 리눅스에서 띄울 수 없어서 이렇게 확인한다.
#
#   powershell -File scripts/installer-screenshots.ps1 -Installer QuickTTS-0.0.1.exe -OutDir shots -Prefix 1-fresh
#
# 버튼은 글자 대신 단축키로 알아본다. 한국어 "다음(N)" 과 영어 "Next" 를 모두 받는다.
param(
    [Parameter(Mandatory)] [string] $Installer,
    [Parameter(Mandatory)] [string] $OutDir,
    [Parameter(Mandatory)] [string] $Prefix,
    [int] $TimeoutSeconds = 300
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName UIAutomationClient, UIAutomationTypes, System.Drawing, System.Windows.Forms
Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class Win {
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int Left, Top, Right, Bottom; }
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool IsWindowVisible(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);
}
'@

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$root = [System.Windows.Automation.AutomationElement]::RootElement
$windowScope = [System.Windows.Automation.TreeScope]::Children
$buttonCondition = New-Object System.Windows.Automation.PropertyCondition(
    [System.Windows.Automation.AutomationElement]::ControlTypeProperty, [System.Windows.Automation.ControlType]::Button)

function Get-Rect($window) {
    $r = [Win+RECT]::new()
    [Win]::GetWindowRect([IntPtr]$window.Current.NativeWindowHandle, [ref]$r) | Out-Null
    return $r
}

# 화면에 보이는 Windows Installer 대화상자 창(MsiDialogCloseClass / MsiDialogNoCloseClass)을 찾는다.
function Find-MsiDialog {
    foreach ($w in $root.FindAll($windowScope, [System.Windows.Automation.Condition]::TrueCondition)) {
        if ($w.Current.ClassName -notlike 'MsiDialog*') { continue }
        if (-not [Win]::IsWindowVisible([IntPtr]$w.Current.NativeWindowHandle)) { continue }
        $r = Get-Rect $w
        if ($r.Right - $r.Left -gt 0 -and $r.Bottom - $r.Top -gt 0) { return $w }
    }
    return $null
}

function Save-Shot($window, $name) {
    [Win]::SetForegroundWindow([IntPtr]$window.Current.NativeWindowHandle) | Out-Null
    Start-Sleep -Milliseconds 700
    $r = Get-Rect $window
    $bmp = New-Object System.Drawing.Bitmap ($r.Right - $r.Left), ($r.Bottom - $r.Top)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.CopyFromScreen($r.Left, $r.Top, 0, 0, $bmp.Size)
    $path = Join-Path $OutDir "$name.png"
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
    Write-Host "saved $path"
}

# 일이 꼬였을 때 원인을 볼 수 있게 화면 전체를 찍는다.
function Save-Screen($name) {
    $b = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds
    $bmp = New-Object System.Drawing.Bitmap $b.Width, $b.Height
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.CopyFromScreen($b.Left, $b.Top, 0, 0, $bmp.Size)
    $bmp.Save((Join-Path $OutDir "$name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

function Find-Button($window, $patterns) {
    foreach ($b in $window.FindAll([System.Windows.Automation.TreeScope]::Descendants, $buttonCondition)) {
        if (-not $b.Current.IsEnabled -or $b.Current.IsOffscreen) { continue }
        foreach ($p in $patterns) {
            if ($b.Current.Name -match $p) { return $b }
        }
    }
    return $null
}

$actions = @(
    @{ Kind = 'finish';  Patterns = @('\(F\)$', '^Finish$') },
    @{ Kind = 'yes';     Patterns = @('\(Y\)$', '^Yes$') },
    @{ Kind = 'install'; Patterns = @('\(I\)$', '^Install$') },
    @{ Kind = 'next';    Patterns = @('\(N\)$', '^Next$') }
)

$process = Start-Process -FilePath $Installer -PassThru
try {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $step = 0
    $lastHandle = 0
    $savedWaiting = @{}

    while ((Get-Date) -lt $deadline) {
        $window = Find-MsiDialog
        if ($null -eq $window) {
            if ($process.HasExited -and $step -gt 0) { break }
            Start-Sleep -Milliseconds 500
            continue
        }
        $handle = $window.Current.NativeWindowHandle
        if ($handle -eq $lastHandle) { Start-Sleep -Milliseconds 500; continue }

        $action = $null
        $button = $null
        foreach ($a in $actions) {
            $button = Find-Button $window $a.Patterns
            if ($null -ne $button) { $action = $a; break }
        }

        if ($null -eq $action) {
            # 설치 중 화면처럼 누를 버튼이 없으면 창마다 한 번만 찍고 다음 화면을 기다린다.
            if (-not $savedWaiting.ContainsKey($handle)) {
                $step++
                Start-Sleep -Milliseconds 1500
                Save-Shot $window ('{0}-{1:D2}-waiting' -f $Prefix, $step)
                $savedWaiting[$handle] = $true
            }
            Start-Sleep -Milliseconds 500
            continue
        }

        $step++
        Start-Sleep -Milliseconds 500
        Save-Shot $window ('{0}-{1:D2}-{2}' -f $Prefix, $step, $action.Kind)
        $button.GetCurrentPattern([System.Windows.Automation.InvokePattern]::Pattern).Invoke()
        $lastHandle = $handle
        if ($action.Kind -eq 'finish') { break }
    }
} catch {
    Save-Screen "$Prefix-error"
    throw
}
if ((Get-Date) -ge $deadline) {
    Save-Screen "$Prefix-timeout"
    throw "설치가 $TimeoutSeconds 초 안에 끝나지 않았다: $Installer"
}

if (-not $process.WaitForExit(60000)) { throw "설치 파일이 끝나지 않았다: $Installer" }
if ($step -eq 0) { throw "설치 창을 찾지 못했다: $Installer" }
Write-Host "$Prefix done in $step steps, exit code $($process.ExitCode)"
