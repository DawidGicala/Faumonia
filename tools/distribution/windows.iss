#ifndef ClientDir
  #error ClientDir must point to the prepared client directory
#endif
#ifndef OutputDir
  #error OutputDir must point to the installer output directory
#endif
#ifndef AppVersion
  #define AppVersion "8.0"
#endif

[Setup]
AppId={code:InstallationId}
AppName=Faumonia
AppVersion={#AppVersion}
AppPublisher=Faumonia
AppPublisherURL=https://faumonia.pl/
AppSupportURL=https://faumonia.pl/
DefaultDirName={autopf}\Faumonia
DefaultGroupName=Faumonia
AllowNoIcons=yes
UninstallDisplayIcon={app}\lib\logo.ico
SetupIconFile={#ClientDir}\lib\logo.ico
InfoBeforeFile={#ClientDir}\README.txt
OutputDir={#OutputDir}
OutputBaseFilename=Faumonia-{#AppVersion}-Windows-Setup
Compression=lzma2
SolidCompression=yes
LanguageDetectionMethod=uilanguage
UsePreviousLanguage=no
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=commandline
Uninstallable=not IsPackagingTest
CloseApplications=no
RestartApplications=no
WizardStyle=modern

[Languages]
Name: "polish"; MessagesFile: "compiler:Languages\Polish.isl"

[Tasks]
Name: "desktopicon"; Description: "Utwórz skrót do gry na pulpicie"; GroupDescription: "Skróty:"; Check: not IsPackagingTest

[Files]
Source: "{#ClientDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Faumonia"; Filename: "{app}\FaumoniaOnline.jar"; WorkingDir: "{app}"; IconFilename: "{app}\lib\logo.ico"; Check: not IsPackagingTest
Name: "{autodesktop}\Faumonia"; Filename: "{app}\FaumoniaOnline.jar"; WorkingDir: "{app}"; IconFilename: "{app}\lib\logo.ico"; Tasks: desktopicon; Check: not IsPackagingTest

[Run]
Filename: "{app}\FaumoniaOnline.jar"; Description: "Uruchom Faumonię"; Flags: shellexec postinstall nowait skipifsilent; Check: not IsPackagingTest

[Code]
// Installs the actual public payload into an isolated folder for verification.
// Test mode does not register an uninstaller, create shortcuts or run the game.
function IsPackagingTest: Boolean;
begin
  Result := ExpandConstant('{param:FAUMONIATEST|0}') = '1';
end;

function InstallationId(Param: String): String;
begin
  if IsPackagingTest then
    Result := 'Faumonia-Package-Verification'
  else
    Result := 'Faumonia';
end;

procedure InitializeWizard();
begin
  WizardForm.WelcomeLabel2.Caption :=
    'Instalator przygotuje Faumonię {#AppVersion} na Twoim komputerze.'#13#10#13#10 +
    'Przed rozpoczęciem zamknij grę.'#13#10#13#10 +
    'Do uruchomienia gry potrzebna jest Java 8 lub nowsza. Java nie jest częścią tej paczki. ' +
    'Jeśli jej nie masz, pobierz ją ze strony https://www.java.com/pl/download/.';
end;
