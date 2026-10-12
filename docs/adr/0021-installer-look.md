# 설치 마법사는 앱 색의 그림과 한국어 문구로 꾸민다

jpackage 기본 설치 마법사는 WiX 기본 그림(빨간 CD 배너)과 영어 문구를 그대로 씁니다. QuickTTS 의 아이콘이나 디스코드 스타일 입력창과 어울리지 않고, 한국어로 쓰는 앱인데 설치 화면만 영어입니다. 새 버전을 옛 버전 위에 설치할 때는 "The folder ... already exist" 확인 창이 매번 떠서, 처음 보는 사람은 무언가 잘못된 줄 압니다.

## 결정

- 설치 방식(jpackage `--type exe` + WiX 3, [ADR 0018](0018-windows-installer.md))은 그대로 두고, `--resource-dir` 로 jpackage 가 쓰는 WiX 파일만 바꿔 끼웁니다. `prepareInstallerResources` 가 그 폴더를 만듭니다.
- 그림: 첫 화면과 마지막 화면 왼쪽(`dialog.bmp`, 493×312)에는 앱 아이콘 색의 보라 그림을, 나머지 화면 위쪽(`banner.bmp`, 493×58)에는 아이콘 배너를 넣습니다. 원본 HTML 은 `desktop-client/packaging/installer/source/` 에 있습니다. 설치 창은 그림을 세로로 더 늘려 보여 주므로 세로로 조금 길게 그린 뒤 줄여 둡니다.
- 문구: jpackage 의 기본 문구 파일 `MsiInstallerStrings_en.wxl` 을 ko-kr 문구 파일(`MsiInstallerStrings_ko.wxl`)로 바꿔 끼웁니다. 설치 파일이 ko-kr 문화권으로 만들어져 WiX 의 한국어 기본 문구까지 같이 쓰이고, 자주 보는 화면(시작, 설치 폴더, 바로가기, 설치 준비, 설치 중, 마침)은 해요체로 다시 씁니다. 큰 제목과 화면 제목은 맑은 고딕으로, 첫 화면과 마지막 화면 제목은 아이콘 색으로 씁니다.
- `main.wxs` 와 `InstallDirNotEmptyDlg.wxs` 는 저장소에 복사해 두지 않고, 빌드할 때 JDK 안의 jpackage 원본을 읽어 필요한 곳만 고칩니다. JDK 가 바뀌어 고칠 자리가 없으면 빌드가 멈춥니다.
- 옛 버전이 설치되어 있으면(같은 업그레이드 UUID) "폴더가 이미 있다" 확인 창을 건너뜁니다. 새로 설치할 때 비어 있지 않은 폴더를 고르면 지금처럼 묻습니다.
- **설치 화면 미리보기** 워크플로가 Windows 러너에서 처음 설치와 업그레이드를 실제로 돌려 화면마다 캡처합니다. 캡처는 아티팩트(`installer-screenshots`)와 `ci/installer-screenshots` 브랜치(매번 덮어씀)에 올라가서, 브랜치에서는 압축을 풀지 않고 GitHub 에서 바로 볼 수 있습니다. 설치 화면을 바꾸는 PR 에서만 돕니다.

## 고르지 않은 것

- Inno Setup, NSIS, WiX 4 의 WPF 부트스트래퍼: 버튼과 바탕까지 마음대로 그릴 수 있지만 설치 스크립트를 따로 관리해야 하고, ADR 0018 의 업그레이드·사용자 폴더 설치를 다시 맞춰야 합니다. 그림과 문구만 바꿔도 첫인상이 충분히 달라집니다.
- jpackage 의 `main.wxs` 를 저장소에 복사해 고치기: JDK 를 올릴 때 jpackage 가 바꾼 내용을 놓치기 쉽습니다.
- 마지막 화면의 "QuickTTS 실행" 체크박스: 필요해지면 따로 더합니다.

## 결과

- 바꿀 수 없는 부분은 그대로입니다. 버튼, 회색 바탕, 창 크기와 배치, 일반 글자 글꼴은 Windows Installer 가 그립니다. "알 수 없는 게시자" 경고(SmartScreen)와 "Windows 가 설치를 구성하는 중" 같은 Windows 자체 창도 바꿀 수 없습니다.
- 설치 파일은 한국어(949) 코드 페이지로 만들어집니다. 영어 Windows(GitHub 러너의 Windows Server 2025)에서도 한글과 맑은 고딕이 제대로 보이는 것을 캡처로 확인했습니다.
- jpackage 를 한국어가 아닌 다른 언어(독일어, 일본어, 중국어) Windows 에서 돌리면 그 언어의 기본 문구 파일을 써서 이 한국어 문구가 빠집니다. 설치 파일은 GitHub Actions(영어 Windows)에서 만듭니다.
