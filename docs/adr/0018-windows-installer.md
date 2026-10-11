# Windows 에는 jpackage 설치 마법사(.exe)로 설치한다

지금까지 QuickTTS 는 `packageZip` 이 만든 압축 파일을 사용자가 직접 `%LOCALAPPDATA%\Programs\QuickTTS` 에 풀어서 썼습니다. 바로가기도 없고, 새 버전을 넣을 때 옛 폴더를 손으로 지워야 하며, 다른 사람에게 건네기에도 낯선 방식입니다. 보통 프로그램처럼 설치 경로를 고르고 바탕화면 아이콘을 만들지 묻는 설치 마법사가 필요합니다.

## 결정

- `./gradlew :desktop-client:packageInstaller` 가 기존 `packageExe` 의 exe 폴더(app-image)를 그대로 jpackage `--type exe` 로 감싸 `QuickTTS-버전.exe` 설치 파일을 만듭니다. 서버 실행기의 클래스패스를 고친 폴더를 그대로 쓰므로 압축 파일과 설치된 프로그램의 내용이 같습니다.
- 설치 마법사에서 설치 경로를 고르고(`--win-dir-chooser`), 바탕화면·시작 메뉴 바로가기를 만들지 체크박스로 고릅니다(`--win-shortcut`, `--win-menu`, `--win-shortcut-prompt`). 바로가기는 `QuickTTS.exe` 에만 만들고, 클라이언트가 띄우는 `QuickTTS-Server.exe` 에는 만들지 않습니다. jpackage 는 exe 폴더(app-image)로 설치 파일을 만들 때 실행기별 `win-shortcut=false` 를 잃어버리므로, `packageExe` 가 `.jpackage.xml` 의 서버 실행기에 바로가기를 끄는 값을 직접 적습니다.
- 사용자 폴더에 설치합니다(`--win-per-user-install`, 기본 `%LOCALAPPDATA%\QuickTTS`). 관리자 권한을 묻지 않습니다. 압축 파일을 풀던 `%LOCALAPPDATA%\Programs\QuickTTS` 로 맞추려 했지만, 사용자 폴더 설치는 설치 경로의 모든 폴더를 제거할 때 지울 수 있어야 해서(WiX ICE64) jpackage 기본 경로를 씁니다.
- 경로가 바뀌어도 트레이의 Start with Windows 가 끊기지 않도록, 켜 둔 상태면 QuickTTS 가 켜질 때 자동 시작 경로를 지금 exe 로 고칩니다.
- 업그레이드 UUID 를 고정해서, 새로 만든 설치 파일이 이전 설치를 지우고 그 자리에 설치되게 합니다. 이 값은 바꾸지 않습니다.
- 압축 파일(`packageZip`)도 남겨 설치 없이 쓰거나 개발 중에 빠르게 확인할 때 씁니다.
- 설치 파일을 만들려면 Windows 에 WiX 3 이 있어야 합니다. GitHub Actions 의 **설치 파일** 워크플로가 Windows 에서 설치 파일을 만들어 보고 아티팩트로 올립니다. 클라이언트나 빌드 설정이 바뀐 PR 에서만 돕니다.

## 고르지 않은 것

- `.msi`: jpackage 로 만드는 방법은 같지만, 더블클릭으로 바로 설치되는 모양과 이름이 일반 사용자에게 더 익숙한 `.exe` 를 고릅니다. 회사에서 그룹 정책으로 나눠 줄 일이 생기면 `--type msi` 로 같이 만들면 됩니다.
- Inno Setup, NSIS: 설치 화면을 한국어로 하거나 더 꾸밀 수 있지만 설치 스크립트를 따로 관리해야 합니다. jpackage 만으로 원하는 것(경로 선택, 바로가기 선택, 업그레이드)이 다 됩니다.
- 모든 사용자용 설치(`Program Files`): 설치와 업그레이드 때마다 관리자 권한을 물어야 하고, 사용자 설정은 어차피 사용자 폴더(`~/.discord-quick-tts`)에 있습니다.

## 결과

- 설치 화면은 영어입니다. jpackage 에 한국어 설치 화면 문구가 없습니다.
- 서명하지 않아서 설치 파일을 처음 실행할 때 "알 수 없는 게시자" 경고가 뜹니다. 남들에게 널리 나눠 주는 단계에서 코드 서명을 검토합니다.
- 제거해도 사용자 설정·데이터와 Start with Windows 레지스트리 값은 남습니다.
- 버전을 올릴 때는 `appVersion` 만 바꾸면 됩니다.
