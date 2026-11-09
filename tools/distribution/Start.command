#!/bin/bash
# The Java client and all resources stay beside this launcher.
cd -- "$(dirname -- "$0")" || exit 1

fail() {
    printf '\n%s\n' "$1"
    printf 'Naciśnij Enter, aby zamknąć okno. '
    read -r _
    exit 1
}

if [[ ! -f FaumoniaOnline.jar || ! -d lib ]]; then
    fail 'Brakuje plików gry. Rozpakuj cały ZIP, razem z folderem lib.'
fi

java_bin=''
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
    java_bin="$JAVA_HOME/bin/java"
elif [[ -x /usr/libexec/java_home ]]; then
    detected_home="$(/usr/libexec/java_home 2>/dev/null)" || detected_home=''
    if [[ -n "$detected_home" && -x "$detected_home/bin/java" ]]; then
        java_bin="$detected_home/bin/java"
    fi
fi
if [[ -z "$java_bin" ]]; then
    java_bin="$(command -v java 2>/dev/null)" || java_bin=''
fi
if [[ -z "$java_bin" ]]; then
    fail 'Zainstaluj Javę 8 lub nowszą dla swojego Maca, a następnie uruchom grę ponownie. Instrukcja: README.txt.'
fi

version_output="$("$java_bin" -version 2>&1)" || fail 'Nie udało się uruchomić Javy. Zainstaluj Javę 8 lub nowszą odpowiednią dla swojego Maca.'
java_version="$(printf '%s\n' "$version_output" | sed -n 's/.*version "\([^"]*\)".*/\1/p' | head -n 1)"
if [[ "$java_version" == 1.* ]]; then
    java_major="${java_version#1.}"
else
    java_major="$java_version"
fi
java_major="${java_major%%[._+-]*}"
if [[ ! "$java_major" =~ ^[0-9]+$ ]]; then
    fail 'Nie udało się odczytać wersji Javy. Sprawdź instalację Javy zgodnie z README.txt.'
fi
if (( java_major < 8 )); then
    fail "Wykryta Java $java_version jest za stara. Faumonia wymaga Javy 8 lub nowszej."
fi

"$java_bin" -Dfile.encoding=UTF-8 -jar FaumoniaOnline.jar "$@"
result=$?
if (( result != 0 )); then
    fail 'Nie udało się uruchomić gry. Zachowaj komunikat z tego okna i zgłoś problem administracji Faumonii.'
fi
