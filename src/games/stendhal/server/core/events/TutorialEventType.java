/* $Id: TutorialEventType.java,v 1.29 2012/08/06 21:20:20 kymara edited by davvids Exp $ */
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
package games.stendhal.server.core.events;

/**
 * Event types used in the tutorial.
 * 
 * @author hendrik
 */
public enum TutorialEventType {

	FIRST_LOGIN(
			"Witaj w Faumonii! Poruszaj się strzałkami lub klikaj miejsca na mapie. Przewodnik pomoże ci wykonać pierwsze zadania."),
	FIRST_MOVE(
			"Podejdź do mieszkańca i napisz \"cześć\" na czacie. W rozmowie korzystaj z niebieskich słów i gotowych odpowiedzi. Listę komend znajdziesz pod /help."),
	RETURN_HOME(
			"Wróć do swojego rozmówcy i napisz \"cześć\", aby kontynuować zadanie."),
	VISIT_SEMOS_CITY(
			"Witaj w Semos! Otwórz Przewodnik w menu gry - pokaże ci bieżący cel i drogę do rozmówcy. Mistrz Faumonii czeka w swoim domu w zachodniej części miasta i zleca główne zadania. Jeśli utkniesz, wpisz /k i treść pytania, aby poprosić graczy o pomoc."),
	VISIT_SEMOS_DUNGEON(
			"Jedzenie pomaga odzyskać zdrowie. Kliknij dwukrotnie ser, mięso lub inne jedzenie w plecaku i pilnuj paska PZ podczas walki."),
	VISIT_SEMOS_DUNGEON_2(
			"Im głębiej wejdziesz do podziemi, tym silniejsze potwory spotkasz. W razie potrzeby wróć do Semos i poproś Carmen o leczenie."),
	VISIT_SEMOS_TAVERN(
			"Przywitaj się ze sprzedawcą i zapytaj o \"ofertę\". W sklepie wybierz przedmiot oraz ilość, sprawdź cenę i potwierdź zakup. Możesz też handlować przez czat."),
	VISIT_SEMOS_PLAINS(
			"Jedzenie pomaga odzyskiwać zdrowie. Jeśli kończą ci się zapasy, odwiedź farmę na północnym wschodzie."),
	FIRST_ATTACKED(
			"Potwory zaznaczone żółtym kółkiem atakują cię. Kliknij przeciwnika, aby podjąć walkę, i obserwuj swoje zdrowie."),
	FIRST_KILL(
			"Kliknij na przedmioty w zwłokach, aby przenieść je do swojego plecaka. "),
    FIRST_PLAYER_KILL(
			"Czerwona czaszka oznacza piętno zabójcy. Część mieszkańców odmówi ci handlu, leczenia lub zleceń, a zakupy będą droższe. Aby zdjąć piętno, porozmawiaj z Wikarym przy kaplicy w Fado i powiedz: zdejmij. Przed zapłatą poznasz cenę."),
	FIRST_POISONED(
			"Zostałeś zatruty i tracisz PZ. Użyj antidotum, jeśli masz je przy sobie, i pilnuj zdrowia. W razie zagrożenia wycofaj się z walki."),
	FIRST_PLAYER(
			"Postacie z białymi imionami to inni gracze. Jeśli potrzebujesz pomocy, napisz na czacie globalnym: /k twoje pytanie."),
	FIRST_DEATH(
			"Twoja postać poległa, ale możesz kontynuować przygodę. Śmierć wiąże się ze stratami, więc sprawdź ekwipunek i zapasy przed kolejną wyprawą."),
	FIRST_PRIVATE_MESSAGE(
			"Otrzymałeś prywatną wiadomość. Aby odpisać, wpisz /msg, imię gracza i treść odpowiedzi."),
	FIRST_EQUIPPED(
			"Otrzymałeś przedmiot. Sprawdź plecak oraz dłonie."),
	TIMED_HELP(
			"Sprawdź naszą stronę internetową: #www.faumonia.pl  "),
	TIMED_NAKED(
			"Czy nie jest Ci zimno? Naciśnij na sobie prawy przycisk i wybierz \"Ustaw wygląd\", aby się ubrać.  "),
	TIMED_PASSWORD(
			"Nie udostępniaj nikomu hasła do konta, także znajomym ani osobom podającym się za administrację."),
	TIMED_OUTFIT(
			"Chcesz zmienić wygląd postaci? Kliknij ją prawym przyciskiem myszy i wybierz \"Ustaw wygląd\", aby wybrać fryzurę, twarz i ubranie."),
	TIMED_RULES(
			"Zapoznaj się z regulaminem na www.faumonia.pl, aby poznać zasady gry.");
	private String message;

	/**
	 * Creates a new TutorialEventType.
	 * 
	 * @param message
	 *            human readable message
	 */
	private TutorialEventType(final String message) {
		this.message = message;
	}

	/**
	 * Gets the descriptive message.
	 * 
	 * @return message
	 */
	String getMessage() {
		return message;
	}
}
