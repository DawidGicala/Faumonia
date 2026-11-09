# Faumonia 8.0

Źródła klienta Java i serwera gry [Faumonia](https://faumonia.pl/).

Wersja 8.0 zawiera okno rozmowy z NPCami i sklepem, przebudowane dialogi
i zadania, samouczek, wspólnego Mistrza Faumonii z etapem XXV oraz poprawki
walki, zapisu postaci i obsługi opłat. Pełna lista zmian znajduje się
w [CHANGELOG-8.0.txt](CHANGELOG-8.0.txt).

## Kompilacja

Do przygotowania wydania użyto JDK 8 i Apache Ant 1.9.16. Klient został
uruchomiony na Javie 8 oraz 22. Biblioteki potrzebne do kompilacji są w `libs`.
Numer wersji i adres serwera są ustawiane w `build.ant.properties`.

```text
ant dist_client_binary dist_client_starter dist_server_binary
```

W katalogu `build` powstają archiwa klienta i serwera z aktualnym numerem
wersji. Rozpakuj pełną paczkę klienta, zachowując `lib` obok
`FaumoniaOnline.jar`. Starsze, historycznie zapisane w repozytorium pliki
w `build` nie są aktualnym wydaniem 8.0; przed użyciem zbuduj nowe paczki.

Instalator Windows bez dołączonej Javy oraz ZIP-y dla Windows/Java i macOS
przygotowuje skrypt `tools/distribution/prepare.py`. Instrukcja znajduje się
w [tools/distribution/INSTRUKCJA-BUDOWY.txt](tools/distribution/INSTRUKCJA-BUDOWY.txt).
Paczka macOS wymaga jeszcze testu uruchomienia na rzeczywistym Macu.

## Testy

Zestaw regresji jest w `tests/quest-pilot`. Przykład osobnej kompilacji i testów:

```text
python tools/quest-pilot/build.py --output KATALOG_TESTOW --java-home KATALOG_JDK --test
```

Przed wydaniem 8.0 skompilowano zestaw testów i uruchomiono go na nowych JAR-ach:
616 testów przeszło. Sprawdzono również kompletność paczek, instalator Windows,
instalację na starszych plikach i skrypty uruchamiające. Testy automatyczne
nie zastępują sprawdzenia rozgrywki ani testów na macOS.

## Własny serwer

`server.ini`, baza danych i lokalne ustawienia nie są plikami do publikacji.
Przy aktualizacji zachowaj swoją bazę, `server.ini` oraz listę administratorów.
Serwer obsługuje lokalne H2 i konfigurację MySQL. Nie kopiuj konfiguracji
z cudzego środowiska do działającego serwera.

## Licencje

Zasady licencjonowania kodu i zasobów opisują `LICENSE.txt`, `COPYING.txt`,
`SEPARATE_LICENCE_GRAPHICS.txt`, `SEPARATE_LICENCE_MUSIC.txt` oraz licencje
bibliotek w `libs`.
