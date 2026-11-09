/* $Id: FaumoniaNPC.java,v 1.15 2023/07/13 00:08:30 Szygolek / FaumoniaOnline Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.testing_grid;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ShoutBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class FaumoniaNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] text = {
			"Nie wiesz, gdzie expić? Wybierz swój przedział poziomów i sprawdź opisane expowiska, sugerowane statystyki, ekwipunek oraz dokładne nazwy map: #https://faumonia.pl/expowiska/",
			"Masz poziom 0-20? Wioska startowa, Podziemia Semos, Wioska Gnomów, Farma Ados i inne miejsca dla początkujących czekają tutaj: #https://faumonia.pl/expowiska-na-0-20-lvl/",
			"Pierwsze poziomy najlepiej zdobywać bez błądzenia po całej krainie. Zobacz expowiska przygotowane dla poziomów 0-20: #https://faumonia.pl/expowiska-na-0-20-lvl/",
			"Jeżeli dopiero zaczynasz, sprawdź gdzie zdobywać doświadczenie, podstawowy ekwipunek, jedzenie i pieniądze na poziomach 0-20: #https://faumonia.pl/expowiska-na-0-20-lvl/",
			"Masz poziom 20-60? Risecia, Kalavan, Wofol, Orcza Wioska, Mokradła Ados i inne expowiska znajdziesz tutaj: #https://faumonia.pl/expowiska-na-20-60-lvl/",
			"Na poziomach 20-60 warto zmieniać expowiska zależnie od tego, czego potrzebujesz: doświadczenia, przedmiotów, pieniędzy, leczenia albo skilli. Porównaj miejsca: #https://faumonia.pl/expowiska-na-20-60-lvl/",
			"Szukasz expowiska na 20-60 lvl? Strona podaje oceny lokacji oraz mapy, dzięki czemu łatwiej dobrać miejsce do swojej postaci: #https://faumonia.pl/expowiska-na-20-60-lvl/",
			"Masz poziom 60-120? Nalwor, Pałac Licha, Katakumby Semos, Turonie, Labirynt Minotaura i Smocza Jaskinia to tylko część możliwości: #https://faumonia.pl/expowiska-na-60-120-lvl/",
			"Na poziomach 60-120 zaczynają się znacznie trudniejsze wyprawy. Sprawdź sugerowane statystyki i wyposażenie przed wyruszeniem: #https://faumonia.pl/expowiska-na-60-120-lvl/",
			"Elfy w Nalwor, Otchłań Żywiołaków, nieumarli na Mokradłach i Smocza Jaskinia - zobacz pełną listę expowisk 60-120: #https://faumonia.pl/expowiska-na-60-120-lvl/",
			"Masz poziom 120-200? Elfy Albinosy, podziemia Nalwor, olbrzymy, Madaramy, Głęboka Jaskinia i Pieczara Wampirów czekają: #https://faumonia.pl/expowiska-na-120-200-lvl/",
			"Expowiska 120-200 mają bardzo różny charakter. Zanim wyruszysz, sprawdź poziom, sugerowany atak, obronę i wyposażenie: #https://faumonia.pl/expowiska-na-120-200-lvl/",
			"Jaskinia Semos-Ados, Tunele Assasynów, Drzewce, Barbarzyńcy i Demony na Malleus Plain należą do ścieżki 120-200: #https://faumonia.pl/expowiska-na-120-200-lvl/",
			"Masz poziom 200-300? Chaosy, superczłowiekowie, Dzika Jaskinia, Smocza Wyspa, Sedah, Oni i Mithrilbourght czekają: #https://faumonia.pl/expowiska-na-200-300-lvl/",
			"Na poziomach 200-300 różnica między expowiskami robi się ogromna. Sprawdź oceny, sugerowane statystyki i wyposażenie: #https://faumonia.pl/expowiska-na-200-300-lvl/",
			"Siedziba Chaosów jest tylko jednym z wielu miejsc dla silnych wojowników. Pełny przegląd expowisk 200-300 znajdziesz tutaj: #https://faumonia.pl/expowiska-na-200-300-lvl/",
			"Masz ponad 300 poziom? Podziemia Kikareukin, Amazonki, Magiczne Miasto, Piekło, Pustynia i Wieliczka należą do najtrudniejszych miejsc: #https://faumonia.pl/expowiska-na-300-plus-lvl/",
			"Najsilniejsi wojownicy też nie muszą szukać expowisk na ślepo. Zobacz miejsca dla poziomów 300+ wraz ze statystykami i ekwipunkiem: #https://faumonia.pl/expowiska-na-300-plus-lvl/",
			"Kikareukin, Wyspa Amazonek, Chmury, Piekło, Pustynia i Wieliczka - sprawdź pełną ścieżkę expowisk 300+: #https://faumonia.pl/expowiska-na-300-plus-lvl/",
			"Nie sugeruj się wyłącznie poziomem. Na stronach expowisk znajdziesz również sugerowany atak, obronę i wyposażenie: #https://faumonia.pl/expowiska/",
			"Każde expowisko na stronie ma ocenę pod kątem doświadczenia, przedmiotów, pieniędzy, leczenia i skilli. Porównaj je przed wyprawą: #https://faumonia.pl/expowiska/",
			"Chcesz znać dokładne nazwy map danego expowiska? Są podane przy opisach lokacji na stronie: #https://faumonia.pl/expowiska/",
			"Nie każde dobre expowisko jest najlepsze do wszystkiego. Jedne dają lepszy łup, inne leczenie, pieniądze albo skille. Sprawdź porównania: #https://faumonia.pl/expowiska/",
			"Nowy wojowniku, zamiast błądzić między mapami możesz skorzystać z gotowej ścieżki pierwszych expowisk: #https://faumonia.pl/gdzie-expic-na-starcie-w-faumonii-najlepsze-expowiska/",
			"Jeżeli pierwsze expowisko stało się zbyt łatwe albo rozwój zwolnił, to dobry moment, by sprawdzić kolejną lokację na swojej ścieżce: #https://faumonia.pl/gdzie-expic-na-starcie-w-faumonii-najlepsze-expowiska/",
			"Nie wiesz, jakie zadanie zrobić dalej? Główna baza zadań Faumonii pomoże wybrać kolejne wyzwanie: #https://faumonia.pl/zadania/",
			"W Faumonii czeka ponad 100 zadań. Zamiast szukać po omacku, zajrzyj do katalogu misji: #https://faumonia.pl/zadania/",
			"Zadania to nie tylko doświadczenie - prowadzą przez miasta, mechaniki i kolejne części krainy. Sprawdź dostępne misje: #https://faumonia.pl/zadania/",
			"Mistrzowie Gry prowadzą postać od podstaw aż do zaawansowanych wyzwań. Pełną listę zadań znajdziesz tutaj: #https://faumonia.pl/zadania-mistrzowie-gry/",
			"Nie wiesz, którego Mistrza Faumonii robić dalej? Sprawdź wymagania i kolejne etapy: #https://faumonia.pl/zadania-mistrzowie-gry/",
			"Zadania Mistrzów Gry uczą kolejnych mechanik i prowadzą przez rozwój postaci. Warto wykonywać je na bieżąco: #https://faumonia.pl/przeglad-zadan-u-mistrzow-gry/",
			"Pierwsze Mistrzowie Gry wprowadzają w walkę, handel, zadania i podstawowe mechaniki. Zobacz ich przegląd: #https://faumonia.pl/przeglad-zadan-u-mistrzow-gry/",
			"Jeżeli wracasz do Faumonii po przerwie i nie wiesz, co robić, sprawdź postęp u Mistrzów Gry: #https://faumonia.pl/przeglad-zadan-u-mistrzow-gry/",
			"Semos kryje wiele zadań dla początkujących i bardziej doświadczonych graczy. Zobacz pełną listę: #https://faumonia.pl/zadania-miasto-semos/",
			"Chcesz zdobywać doświadczenie i nagrody, pomagając mieszkańcom Semos? Sprawdź zadania miasta startowego: #https://faumonia.pl/zadania-miasto-semos/",
			"Obywatelstwo Semos, zadania mieszkańców i pierwsze większe przygody - wszystko znajdziesz w jednym miejscu: #https://faumonia.pl/zadania-miasto-semos/",
			"Ados ma własny zestaw zadań, od prostszych misji po znacznie trudniejsze wyzwania. Zobacz je tutaj: #https://faumonia.pl/zadania-stolica-ados/",
			"Licencje rybackie, zadania mieszkańców i obywatelstwo Ados to część przygód w stolicy. Sprawdź szczegóły: #https://faumonia.pl/zadania-stolica-ados/",
			"Jeżeli dotarłeś do Ados i nie wiesz, u kogo szukać nowych misji, sprawdź listę zadań stolicy: #https://faumonia.pl/zadania-stolica-ados/",
			"Fado, Kirdneh i Kalavan mają wiele własnych historii i zadań. Pełny przegląd południowych miast znajdziesz tutaj: #https://faumonia.pl/zadania-w-poludniowych-miastach-fado-kirdneh-kalavan/",
			"Szukasz zadań w Fado albo Kirdneh? Nie musisz obchodzić każdego domu po kolei - sprawdź zestawienie południowych miast: #https://faumonia.pl/zadania-w-poludniowych-miastach-fado-kirdneh-kalavan/",
			"Obywatelstwa Fado i Kirdneh są powiązane z wcześniejszą pomocą mieszkańcom. Sprawdź wymagania przed wyprawą: #https://faumonia.pl/zadania-w-poludniowych-miastach-fado-kirdneh-kalavan/",
			"Nie wszystkie zadania należą do głównych miast. Pozostałe przygody i misje znajdziesz tutaj: #https://faumonia.pl/zadania-pozostale/",
			"Jeśli wydaje Ci się, że wykonałeś już wszystkie oczywiste zadania, zajrzyj do kategorii pozostałych misji: #https://faumonia.pl/zadania-pozostale/",
			"Zadania odnawialne pozwalają po pewnym czasie ponownie zdobywać nagrody. Sprawdź dostępne misje i czasy odnowienia: #https://faumonia.pl/zadania-odnawialne/",
			"Regularnie wracaj do zadań odnawialnych. Część z nich po określonym czasie można wykonać ponownie: #https://faumonia.pl/zadania-odnawialne/",
			"Nie wiesz, które odnawialne zadania są już warte Twojej uwagi? Zobacz ich przegląd: #https://faumonia.pl/zadania-odnawialne/",
			"Lubisz polować na konkretne potwory? Zobacz odnawialne zadania polegające na pokonaniu mobów: #https://faumonia.pl/zadania-zadania-odnawialne-na-pokonanie-potworow/",
			"Przeklęte gnomy, smoki, chaosy, anioły i inne potwory pojawiają się w zadaniach odnawialnych. Sprawdź cele i nagrody: #https://faumonia.pl/zadania-zadania-odnawialne-na-pokonanie-potworow/",
			"Jeżeli i tak expisz na danym typie potworów, sprawdź najpierw, czy nie masz do zrobienia odnawialnego zadania na te moby: #https://faumonia.pl/zadania-zadania-odnawialne-na-pokonanie-potworow/",
			"Marzysz o jednym z najmocniejszych przedmiotów? Część potężnego wyposażenia zdobywa się poprzez rozbudowane zadania: #https://faumonia.pl/zadania-na-najsilniejsze-przedmioty-w-grze/",
			"Miecz Nieśmiertelnych, Tarcza z Mithrilu i inne wyjątkowe nagrody wymagają konkretnych zadań. Sprawdź je tutaj: #https://faumonia.pl/zadania-na-najsilniejsze-przedmioty-w-grze/",
			"Najsilniejsze przedmioty często wymagają wielu etapów, surowców i walk. Przed rozpoczęciem warto przeczytać cały opis zadania: #https://faumonia.pl/zadania-na-najsilniejsze-przedmioty-w-grze/",
			"Nie wiesz, od czego zacząć? W dziale Poradniki znajdziesz praktyczne wskazówki dotyczące rozwoju, zadań i świata gry: #https://faumonia.pl/poradniki-do-gry/",
			"Wracasz po dłuższej przerwie? Dział poradników pomoże przypomnieć sobie ważne mechaniki i kierunki rozwoju: #https://faumonia.pl/poradniki-do-gry/",
			"Obywatelstwo nie jest tylko tytułem - w różnych miastach wiąże się z zadaniami i praktycznymi korzyściami. Dowiedz się więcej: #https://faumonia.pl/obywatelstwo-w-faumonii/",
			"Nie odkładaj obywatelstwa na sam koniec. Warto robić je wraz z naturalnym postępem przez kolejne miasta: #https://faumonia.pl/obywatelstwo-w-faumonii/",
			"Pierwsze obywatelstwo najnaturalniej zdobywać w Semos, a później poznawać wymagania kolejnych miast. Przeczytaj poradnik: #https://faumonia.pl/obywatelstwo-w-faumonii/",
			"Karma zaczyna się w Fado u Sarziny. Jeśli jeszcze nie otworzyłeś tej mechaniki, warto zrobić to odpowiednio wcześnie: #https://faumonia.pl/karma-w-krainie-faumonii/",
			"Punkty karmy pojawiają się jako nagrody w wielu zadaniach. Zobacz jak działa ta ścieżka i dlaczego nie warto jej ignorować: #https://faumonia.pl/karma-w-krainie-faumonii/",
			"Karma najlepiej rozwija się przy okazji normalnego wykonywania zadań. Poradnik wyjaśnia, gdzie zaczyna się ta mechanika: #https://faumonia.pl/karma-w-krainie-faumonii/",
			"Nowy w Faumonii? Semos jest szkołą podstawowych mechanik, a pierwszych Mistrzów Gry warto robić po kolei: #https://faumonia.pl/semos-jako-szkola-przetrwania-jak-zrobic-pierwszych-5-mistrzow-gry/",
			"Nie pomijaj samouczka i pierwszych Mistrzów Gry - ich zadania prowadzą przez najważniejsze podstawy rozgrywki: #https://faumonia.pl/semos-jako-szkola-przetrwania-jak-zrobic-pierwszych-5-mistrzow-gry/",
			"Skille rozwijasz poprzez walkę i trening. W wersji 5.0 powstały specjalne rozwiązania ułatwiające skillowanie: #https://faumonia.pl/faumonia-5-0-nowe-skillowanie-samouczek-reborn/",
			"Do treningu przydaje się sprzęt pozwalający kontrolować zadawane obrażenia. Więcej o zmianach w skillowaniu znajdziesz tutaj: #https://faumonia.pl/faumonia-5-0-nowe-skillowanie-samouczek-reborn/",
			"Czarny sztylet i rękawice płytowe zostały przygotowane jako wyposażenie pomocne przy skillowaniu od 50 poziomu. Szczegóły: #https://faumonia.pl/faumonia-5-0-nowe-skillowanie-samouczek-reborn/",
			"Rozwijasz skille? Pilnuj postaci podczas treningu - arena treningowa pozostaje miejscem, w którym inni wojownicy mogą stanowić zagrożenie.",
			"Przed trudną wyprawą sprawdź wyposażenie, zapas leczenia i drogę powrotną. Najtrudniejsze expowiska potrafią szybko ukarać brak przygotowania: #https://faumonia.pl/expowiska/",
			"Nie wiesz, gdzie kupić potrzebny przedmiot? Baza sklepów dzieli handlarzy na Semos, żywność, surowce i zwoje oraz ekwipunek: #https://faumonia.pl/sklepy/",
			"Zanim zapytasz na chacie, gdzie coś kupić, sprawdź katalog sklepów i handlarzy: #https://faumonia.pl/sklepy/",
			"Szukasz zwojów, surowców albo wyposażenia? W dziale Sklepy możesz sprawdzić, gdzie prowadzić poszukiwania: #https://faumonia.pl/sklepy/",
			"Nie każdy przedmiot trzeba zdobywać z potworów. Sprawdź handlarzy i dostępne kategorie sklepów: #https://faumonia.pl/sklepy/",
			"Nie pamiętasz statystyk przedmiotu? Katalog przedmiotów zbiera broń, pancerze, biżuterię, zwoje, jedzenie i wiele innych kategorii: #https://faumonia.pl/przedmioty/",
			"Szukasz lepszego miecza, sztyletu, topora albo maczugi? Zacznij od katalogu przedmiotów: #https://faumonia.pl/przedmioty/",
			"Chcesz porównać tarcze, zbroje, płaszcze, hełmy, spodnie, buty albo rękawice? Sprawdź dział Przedmioty: #https://faumonia.pl/przedmioty/",
			"Łucznicy również znajdą na stronie łuki, kusze i amunicję. Zobacz katalog wyposażenia: #https://faumonia.pl/przedmioty/",
			"Zwoje, klucze, zioła, jedzenie i napoje też mają własne kategorie w bazie przedmiotów: #https://faumonia.pl/przedmioty/",
			"Zanim wyrzucisz nieznany przedmiot, sprawdź go w katalogu - może przydać się później lub w zadaniu: #https://faumonia.pl/przedmioty/",
			"Nie możesz znaleźć konkretnego potwora? Baza potworów pokazuje kategorie, statystyki, drop i miejsca występowania: #https://faumonia.pl/potwory/",
			"Polujesz na smoki, anioły, chaosy, Amazonki albo inne silne bestie? Sprawdź bazę potworów przed wyprawą: #https://faumonia.pl/potwory/",
			"Jeżeli potrzebujesz konkretnego dropu, zacznij od sprawdzenia potwora, który może go pozostawić: #https://faumonia.pl/potwory/",
			"Nie wiesz, jak silny jest przeciwnik? Katalog potworów pozwala przygotować się przed wejściem na nowe expowisko: #https://faumonia.pl/potwory/",
			"Potwory są podzielone na wiele grup, dzięki czemu łatwiej znaleźć interesującego Cię przeciwnika: #https://faumonia.pl/potwory/",
			"Gdzie znaleźć potwora, którego szukasz? Oprócz bazy na stronie możesz zapytać Starkada przechadzającego się po banku Semos.",
			"Jeżeli zadanie wymaga konkretnego moba, najpierw sprawdź jego kategorię i miejsce występowania w bazie potworów: #https://faumonia.pl/potwory/",
			"Zgubiłeś się? Atlas Map zbiera filmiki pokazujące drogi do ważnych miejsc w Faumonii: #https://faumonia.pl/atlas-map/",
			"Nie wiesz, jak dotrzeć na prom na Athor? Zajrzyj do Atlasu Map: #https://faumonia.pl/atlas-map/",
			"Szukasz siedziby Orków? Trasę znajdziesz w filmikach instruktażowych Atlasu Map: #https://faumonia.pl/atlas-map/",
			"Nie pamiętasz drogi do Wioski Barbarzyńców? Sprawdź Atlas Map: #https://faumonia.pl/atlas-map/",
			"Miasto Szczurów jest ukryte głęboko w tunelach. Film z drogą znajdziesz w Atlasie Map: #https://faumonia.pl/atlas-map/",
			"Nie wiesz, jak przejść z Semos do Kalavan? Atlas Map pokazuje trasę krok po kroku: #https://faumonia.pl/atlas-map/",
			"Zgubiłeś drogę do podziemnego miasta Wofol? Sprawdź film instruktażowy: #https://faumonia.pl/atlas-map/",
			"Szukasz osady duchów? Zajrzyj do filmów z trasami na stronie: #https://faumonia.pl/atlas-map/",
			"Nie możesz znaleźć osady Elfów Albinosów? Atlas Map pokaże Ci drogę: #https://faumonia.pl/atlas-map/",
			"Nalwor jest daleko od Semos, ale nie musisz szukać drogi na ślepo. Sprawdź Atlas Map: #https://faumonia.pl/atlas-map/",
			"Pierwsza wyprawa do stolicy Ados? Film z trasą znajdziesz tutaj: #https://faumonia.pl/atlas-map/",
			"Nie wiesz, jak dostać się na Mokradła? Skorzystaj z Atlasu Map: #https://faumonia.pl/atlas-map/",
			"Lodowa Góra Ados Rock ma własny film z trasą. Zajrzyj do Atlasu Map: #https://faumonia.pl/atlas-map/",
			"Pałac Licha nie musi być trudny do odnalezienia. Drogę pokazano w Atlasie Map: #https://faumonia.pl/atlas-map/",
			"Wybierasz się pierwszy raz na Dziką Wyspę? Sprawdź trasę przed wyruszeniem: #https://faumonia.pl/atlas-map/",
			"Nie pamiętasz drogi do Kirdneh? Atlas Map pomoże Ci wrócić na właściwy szlak: #https://faumonia.pl/atlas-map/",
			"Fado leży daleko od startowego miasta. Jeśli nie znasz trasy, sprawdź film instruktażowy: #https://faumonia.pl/atlas-map/",
			"Chcesz wiedzieć, co zmieniło się w Faumonii? Najnowsze wersje, eventy, poradniki i ogłoszenia znajdziesz w Aktualnościach: #https://faumonia.pl/aktualnosci-ze-swiata-faumonii/",
			"Przed powrotem po dłuższej przerwie warto przejrzeć Aktualności - gra regularnie otrzymuje nowe zadania, expowiska i poprawki: #https://faumonia.pl/aktualnosci-ze-swiata-faumonii/",
			"Faumonia 7.0 przyniosła między innymi zmiany w sklepach, zadaniach i expowiskach. Zobacz opis wersji: #https://faumonia.pl/faumonia-7-0-sklepy-zadania-expowiska/",
			"Chcesz porozmawiać z innymi graczami, zapytać o pomoc albo śledzić ogłoszenia? Dołącz na Discord: #https://discord.gg/sDPB6JKaVM",
			"Na Discordzie Faumonii znajdziesz graczy gotowych wymienić się poradami i informacjami o grze: #https://discord.gg/sDPB6JKaVM",
			"Śledź kanał ogłoszeń na Discordzie, aby nie przegapić informacji o eventach i zmianach: #https://discord.gg/sDPB6JKaVM",
			"Masz pytanie, na które nie znalazłeś odpowiedzi na stronie? Zapytaj społeczność na Discordzie: #https://discord.gg/sDPB6JKaVM",
			"Chcesz znaleźć ekipę do wspólnej wyprawy? Discord to dobre miejsce, by zgadać się z innymi wojownikami: #https://discord.gg/sDPB6JKaVM",
			"Handlujesz z innymi graczami? Możesz również zajrzeć do grupy Faumonia - Gracze na Facebooku: #https://www.facebook.com/groups/faumoniaonline",
			"Nie możesz znaleźć kupca na rzadki przedmiot? Sprawdź społeczność graczy na Facebooku: #https://www.facebook.com/groups/faumoniaonline",
			"Na kanale YouTube Faumonii znajdziesz materiały z gry oraz starsze filmy z trasami i przygodami: #https://www.youtube.com/@faumonia",
			"Chcesz zobaczyć więcej materiałów z Faumonii? Odwiedź oficjalny kanał YouTube: #https://www.youtube.com/@faumonia",
			"Śledź Faumonię na Facebooku, aby zaglądać do najnowszych informacji ze świata gry: #https://www.facebook.com/faumoniaonline",
			"Faumonia jest również na Instagramie. Obserwuj profil, jeśli chcesz widzieć materiały z krainy poza grą: #https://www.instagram.com/faumoniaonline/",
			"Krótkie materiały i ciekawostki z Faumonii znajdziesz także na TikToku: #https://www.tiktok.com/@faumonia",
			"Tworzysz fanart, mem albo materiał związany z grą? Podziel się nim ze społecznością na Discordzie: #https://discord.gg/sDPB6JKaVM",
			"Chcesz sprawdzić wyróżnionych wojowników Faumonii? Zajrzyj do Hall of Fame: #https://faumonia.pl/hall-of-fame/",
			"Lista zasłużonych wojowników znajduje się również na stronie Faumonii: #https://faumonia.pl/zasluzeni/",
			"Ktoś Cię obraża albo nie chcesz widzieć jego wiadomości? Użyj komendy #/ignore #nazwagracza",
			"Chcesz utworzyć wspólny czat z przyjaciółmi? Załóż grupę w zakładce #Grupa i zapraszaj graczy komendą #/group #invite #nazwagracza",
			"Dbaj o atmosferę w Faumonii - natarczywe przeklinanie na chacie globalnym może zakończyć się wyciszeniem nawet na 7 dni.",
			"Nie przeszkadzaj natarczywie innym wojownikom w grze. Takie zachowanie może zakończyć się więzieniem nawet na 8 godzin.",
			"Globalny chat służy całej społeczności. Rozmawiaj tak, aby inni gracze również mogli swobodnie z niego korzystać.",
			"Jeżeli ktoś Ci przeszkadza na chacie, nie musisz wdawać się w kłótnię - komenda #/ignore #nazwagracza ukryje jego wiadomości.",
			"Do wspólnych wypraw warto używać grupy. Dzięki temu łatwiej utrzymać rozmowę drużyny poza głównym chatem.",
			"Przed handlem upewnij się, że dokładnie widzisz, jakie przedmioty i ilości znajdują się w oknie wymiany.",
			"Nie znasz odpowiedzi na pytanie? Najpierw sprawdź stronę, a jeśli nadal potrzebujesz pomocy, zapytaj społeczność: #https://discord.gg/sDPB6JKaVM",
			"Masz pomysł na rozwój Faumonii albo znalazłeś problem? Skontaktuj się z administracją lub opisz sprawę na Discordzie: #https://discord.gg/sDPB6JKaVM"
		};

		// Losujemy kolejność przy starcie serwera, żeby po każdym restarcie
		// nie zaczynać zawsze od tych samych ogłoszeń.
		Collections.shuffle(Arrays.asList(text));

		// Jedno globalne ogłoszenie co 20 minut.
		new ShoutBehaviour(buildSemosTownhallAreaFaumonia(zone), text, 20);
	}

	/**
	 * A Faumonia of three cadets. He has an information giving role.
	 * @param zone zone to be configured with this npc
	 * @return the built NPC
	 */
	private SpeakerNPC buildSemosTownhallAreaFaumonia(final StendhalRPZone zone) {
		// We create an NPC
		final SpeakerNPC npc = new SpeakerNPC("Faumonia Ogłoszenia") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Och cześć. Rozmawialiśmy o przerwie. Moi trzej kadeci dostali właśnie nagrodę od Mayora za pomoc w obronie Semos.");
				addJob("Opiekuję się tymi trzema kadetami. Potrzebują wiele wskazówek. Muszę wracać. Posłuchaj ich może się czegoś nowego nauczysz!");
				addHelp("Mogę coś ci poradzić w kwestii broni ( #weapon ).");
				addQuest("Mogę ci doradzić w sprawie Twojej broni ( #weapon ).");
				addOffer("Mogę opowiedzieć o Twojej broni ( #weapon ) jeżeli mogę.");
				addGoodbye("Nie zapomnij posłuchać nauk dla moich trzech kadetów. Mogą się okazać pomocne!");
				add(ConversationStates.ATTENDING, "weapon", null, ConversationStates.ATTENDING,
				        null, new ChatAction() {

					        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					        	final Item weapon = player.getWeapon();
					        	if (weapon != null) {
					        		String comment;
					        		// this is, loosely, the formula used for damage of a weapon (not taking level into account here)
					        		final float damage = (weapon.getAttack() + 1) / weapon.getAttackRate();
					        		if (weapon.getName().endsWith(" hand sword")) {
					        			// this is a special case, we deal with explicitly
					        			comment = "Widzę, że używasz dwóch mieczy. Zadają sporo strat, ale nie możesz wtedy nosić tarczy. Będzie ciężko z obroną, gdy ktoś cię zaatakuje.";
					        		} else if (damage >= 5) {
					        			comment = "Oto " + weapon.getName() + " to potężna broń. Jak na wagę to zadaje dobre straty.";
					        			if (weapon.getAttackRate() < 3) {
					        				comment += " Pomimo wagi jest użyteczna. Niski atak nie pomaga w starciu z silnymi potworami. Czasami cięższa będzie lepsza niż ta broń.";
					        			} else if (weapon.getAttackRate() > 6) {
					        				comment +=  " Powinna być użyteczna na silniejsze potwory. Pamiętaj, że coś słabego, a szybkiego może wystarczyć przeciwko potworom z niskim poziomem.";
					        			}
					        		} else {
					        			comment = "Nie sądzisz, że Twój " + weapon.getName() + " zadaje małe straty? Powinieneś poszukać czegoś z lepszym atakiem w stosunku do wagi.";
					        			if (weapon.getAttackRate() < 3) {
					        				comment += " W końcu możesz uderzać szybko. Może być dobre przeciwko słabszym potworom niż ty.";
					        			}
						       		}
					        		// simple damage doesn't take into account lifesteal. this is a decision the player must make, so inform them about the stats
					        		if (weapon.has("lifesteal")) {
					        			double lifesteal = weapon.getDouble("lifesteal");
					        			if (lifesteal > 0) {
					        				comment += " Pozytywne wysysanie życia " + lifesteal + " podnosi Twoje zdrowie, gdy go używasz.";
					        			} else {
					        				comment += " Negatywne wysysanie życia " + lifesteal + " odbije się na Twoim zdrowiu, gdy będziesz tego używał.";
					        			}
					        		}
					        		raiser.say(comment);
					        	} else {
					        		// player didn't have a weapon, as getWeapon returned null.
					        		raiser.say("Och, nie mogę nic opowiedzieć o Twojej broni o ile jakieś nie będziesz używał. To niezbyt mądre w dzisiejszych czasach!");
					        	}
							} 
					    }
				);
			}
		};
		npc.setLevel(150);
		npc.setDescription("Oto Lieutenant Drilenun, który rozmawia ze swoimi trzema kadetami.");
		npc.setEntityClass("bossmannpc");
		npc.setPosition(23, 15);
		npc.initHP(100);
		zone.add(npc);
		
		return npc;
	}
}
