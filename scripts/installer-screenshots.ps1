# 설치 마법사를 띄워 화면마다 캡처하고, 기본 선택 그대로 다음 화면으로 넘겨 설치를 끝낸다.
# 설치 화면 미리보기 워크플로가 Windows 러너에서 쓴다. 설치 창은 클라우드나 리눅스에서 띄울 수 없어서 이렇게 확인한다.
#
#   pwsh -File scripts/installer-screenshots.ps1 -Installer QuickTTS-0.0.1.exe -OutDir shots -Prefix 1-fresh
#
# Windows Installer 창은 UI Automation 에 버튼이 보이지 않아서 user32 로 창과 버튼을 직접 찾는다.
# 버튼은 글자 대신 단축키로 알아본다. 한국어 "다음(&N)" 과 영어 "&Next" 를 모두 받는다.
param(
    [Parameter(Mandatory)] [string] $Installer,
    [Parameter(Mandatory)] [string] $OutDir,
    [Parameter(Mandatory)] [string] $Prefix,
    [int] $TimeoutSeconds = 300
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing, System.Windows.Forms
Add-Type @'
using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Text;
public static class Win {
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int Left, Top, Right, Bottom; }
    public delegate bool EnumProc(IntPtr hWnd, IntPtr lParam);
    [DllImport("user32.dll")] static extern bool EnumWindows(EnumProc proc, IntPtr lParam);
    [DllImport("user32.dll")] static extern bool EnumChildWindows(IntPtr parent, EnumProc proc, IntPtr lParam);
    [DllImport("user32.dll", CharSet = CharSet.Unicode)] static extern int GetClassName(IntPtr hWnd, StringBuilder name, int max);
    [DllImport("user32.dll", CharSet = CharSet.Unicode)] static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int max);
    [DllImport("user32.dll")] public static extern bool IsWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool IsWindowVisible(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool IsWindowEnabled(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool PostMessage(IntPtr hWnd, uint msg, IntPtr wParam, IntPtr lParam);

    public static string ClassOf(IntPtr h) { var sb = new StringBuilder(256); GetClassName(h, sb, sb.Capacity); return sb.ToString(); }
    public static string TextOf(IntPtr h) { var sb = new StringBuilder(512); GetWindowText(h, sb, sb.Capacity); return sb.ToString(); }

    // 화면에 보이는 Windows Installer 대화상자(MsiDialogCloseClass / MsiDialogNoCloseClass)
    public static IntPtr FindMsiDialog() {
        IntPtr found = IntPtr.Zero;
        EnumWindows((h, l) => {
            RECT r;
            if (ClassOf(h).StartsWith("MsiDialog") && IsWindowVisible(h) && GetWindowRect(h, out r)
                    && r.Right > r.Left && r.Bottom > r.Top) {
                found = h;
                return false;
            }
            return true;
        }, IntPtr.Zero);
        return found;
    }

    public static List<IntPtr> Buttons(IntPtr dialog) {
        var list = new List<IntPtr>();
        EnumChildWindows(dialog, (h, l) => {
            if (ClassOf(h) == "Button" && IsWindowVisible(h)) list.Add(h);
            return true;
        }, IntPtr.Zero);
        return list;
    }

    public static void Click(IntPtr button) { PostMessage(button, 0x00F5 /* BM_CLICK */, IntPtr.Zero, IntPtr.Zero); }
}
'@

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
Start-Transcript -Path (Join-Path $OutDir "$Prefix.log") | Out-Null

function Save-Shot([IntPtr] $window, $name) {
    [Win]::SetForegroundWindow($window) | Out-Null
    Start-Sleep -Milliseconds 700
    # 잠깐 떴다 사라지는 창(설치 준비 중)은 찍기 전에 없어질 수 있다. 그때는 건너뛴다.
    $r = [Win+RECT]::new()
    if (-not [Win]::GetWindowRect($window, [ref]$r) -or $r.Right -le $r.Left -or $r.Bottom -le $r.Top) {
        Write-Host "skip $name (window closed)"
        return
    }
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

$actions = @(
    @{ Kind = 'finish';  Patterns = @('\(&?F\)$', '^&?Finish$') },
    @{ Kind = 'yes';     Patterns = @('\(&?Y\)$', '^&?Yes$') },
    @{ Kind = 'install'; Patterns = @('\(&?I\)$', '^&?Install$') },
    @{ Kind = 'next';    Patterns = @('\(&?N\)$', '^&?Next$') }
)

function Find-Action([IntPtr] $window) {
    $buttons = [Win]::Buttons($window) | Where-Object { [Win]::IsWindowEnabled($_) }
    foreach ($a in $actions) {
        foreach ($b in $buttons) {
            $text = [Win]::TextOf($b)
            foreach ($p in $a.Patterns) {
                if ($text -match $p) { return @{ Kind = $a.Kind; Button = $b } }
            }
        }
    }
    return $null
}

$process = Start-Process -FilePath $Installer -PassThru
try {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $step = 0
    $lastHandle = [IntPtr]::Zero
    $savedWaiting = @{}

    while ((Get-Date) -lt $deadline) {
        $window = [Win]::FindMsiDialog()
        if ($window -eq [IntPtr]::Zero) {
            if ($process.HasExited -and $step -gt 0) { break }
            Start-Sleep -Milliseconds 500
            continue
        }
        if ($window -eq $lastHandle) { Start-Sleep -Milliseconds 500; continue }

        $action = Find-Action $window
        if ($null -eq $action) {
            # 설치 중 화면처럼 누를 버튼이 없으면 창마다 한 번만 찍고 다음 화면을 기다린다.
            if (-not $savedWaiting.ContainsKey($window)) {
                $names = [Win]::Buttons($window) | ForEach-Object { "$([Win]::TextOf($_)) (enabled=$([Win]::IsWindowEnabled($_)))" }
                Write-Host "no button to press, buttons: $($names -join ', ')"
                $step++
                Start-Sleep -Milliseconds 1500
                Save-Shot $window ('{0}-{1:D2}-waiting' -f $Prefix, $step)
                $savedWaiting[$window] = $true
            }
            Start-Sleep -Milliseconds 500
            continue
        }

        $step++
        Start-Sleep -Milliseconds 500
        Save-Shot $window ('{0}-{1:D2}-{2}' -f $Prefix, $step, $action.Kind)
        Write-Host "press $($action.Kind): $([Win]::TextOf($action.Button))"
        [Win]::Click($action.Button)
        $lastHandle = $window
        if ($action.Kind -eq 'finish') { break }
        # 다음 화면이 뜰 때까지 창이 닫히길 잠깐 기다린다. 창이 그대로면 같은 창을 다시 살핀다.
        $until = (Get-Date).AddSeconds(5)
        while ((Get-Date) -lt $until -and [Win]::IsWindow($window) -and [Win]::IsWindowVisible($window)) {
            Start-Sleep -Milliseconds 200
        }
        if ([Win]::IsWindow($window) -and [Win]::IsWindowVisible($window)) { $lastHandle = [IntPtr]::Zero }
    }
} catch {
    Write-Host "error: $_"
    Write-Host $_.ScriptStackTrace
    Save-Screen "$Prefix-error"
    Stop-Transcript | Out-Null
    throw
}
if ((Get-Date) -ge $deadline) {
    Save-Screen "$Prefix-timeout"
    throw "설치가 $TimeoutSeconds 초 안에 끝나지 않았다: $Installer"
}

if (-not $process.WaitForExit(60000)) { throw "설치 파일이 끝나지 않았다: $Installer" }
if ($step -eq 0) { throw "설치 창을 찾지 못했다: $Installer" }
Write-Host "$Prefix done in $step steps, exit code $($process.ExitCode)"
Stop-Transcript | Out-Null
