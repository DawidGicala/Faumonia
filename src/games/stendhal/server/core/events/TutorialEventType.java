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
			"Witaj w FaumoniaOnline. Możesz się poruszać używając klawiszy strzałek na klawiaturze lub klikając myszką. "),
	FIRST_MOVE(
			"Możesz porozmawiać z NPCem, pisząc \"cześć\". Możesz sprawdzić najważniejsze komendy w grze wpisująć /help "),
	RETURN_HOME(
			"Porozmawiaj ponownie z NPCem mówiąc \"cześć\". "),
	VISIT_SEMOS_CITY(
			"Znalazłeś się w mieście startowym Semos. Spójrz na minimapę w lewym górnym rogu ekranu. W lewym górnych rogu mapy 0_semos_city znajduje się budynek Mistrza Faumonii. Wybierz się do niego a dostaniesz główne zadanie w którym nauczysz się podstaw gry. "),
	VISIT_SEMOS_DUNGEON(
			"Pamiętaj, aby jeść regularnie podczas walki z potworami. Naciśnij podwójnie na ser, mięso lub na innego rodzaju jedzenia, które posiadasz. "),
	VISIT_SEMOS_DUNGEON_2(
			"Ostrożnie. Jeżeli będziesz dalej się zagłębiał to spotkasz coraz silniejsze potwory. Możesz uciec z powrotem do Semos, aby Carmen wyleczyła twoje rany. "),
	VISIT_SEMOS_TAVERN(
			"Możesz handlować z NPC-ami mówiąc \"cześć\", a później pytając o jego ofertę \"oferta\". Jeżeli chciałbyś coś kupić np flaszę to powiedz \"kupię flasza\". "),
	VISIT_SEMOS_PLAINS(
			"Regularne jedzenie jest kluczem do odzyskania swojego zdrowia. Jeżeli posiadasz małe zapasy jedzenia to odwiedź farmę, która znajduje się na północny-wschód stąd. "),
	FIRST_ATTACKED(
			"Potwory w źółtym kółku atakują Ciebie! Naciśnij na nie, aby walczyć z nimi.  "),
	FIRST_KILL(
			"Kliknij na przedmioty w zwłokach, aby przenieść je do swojego plecaka. "),
    FIRST_PLAYER_KILL(
			"Zostałeś oznaczony czerwoną czaszką ponieważ zabiłeś wojownika. Możesz spotkać osoby, które będą Cię obserwować. W celu usunięcia tego porozmawiaj z Wikarym pod kościołem w Fado. "),
	FIRST_POISONED(
			"Zostałeś właśnie zatruty. Jeżeli nie wypiłeś trucizny to oznacza, że zostałeś zatruty przez potwora, który cię atakuje. Szybko zabij potwora ponieważ przy zatruciu twoje PZ z biegiem czasu maleje. "),
	FIRST_PLAYER(
			"Czy zauważyłeś postacie z nazwą w kolorze białym? Oznaczają one innych wojowników.  "),
	FIRST_DEATH(
			"Właśnie poległeś w boju, ale to nie koniec przygody. W tym świecie śmierć nie oznacza końca gry. Możesz nadal grać, ale śmierć ma swoją cenę. "),
	FIRST_PRIVATE_MESSAGE(
			"Otrzymałeś prywatną wiadomość, aby odpisać użyj #/msg #imię #wiadomość. "),
	FIRST_EQUIPPED(
			"Właśnie coś dostałeś! Sprawdź swój plecak oraz dłonie.  "),
	TIMED_HELP(
			"Sprawdź naszą stronę internetową: #www.faumonia.pl  "),
	TIMED_NAKED(
			"Czy nie jest Ci zimno? Naciśnij na sobie prawy przycisk i wybierz \"Ustaw wygląd\", aby się ubrać.  "),
	TIMED_PASSWORD(
			"Pamiętaj, żeby nikomu nie udostępniać swojego hasła. Nigdy nie podawaj przyjacielowi, innemu wojownikowi czy administratorowi. "),
	TIMED_OUTFIT(
			"Podoba się Tobie swój własny wygląd? Jeżeli nie to możesz go zmienić. Naciśnij na sobie prawy przycisk myszy i wybierz \"Ustaw wygląd\", aby poeksperymentować z nowymi fryzurami, twarzami, ubraniami i ciałem.  "),
	TIMED_RULES(
			"Dziękujemy za pozostanie z nami. Ze względu, że grasz już dość długo to ważne, abyś przeczytał regulamin. ");
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
