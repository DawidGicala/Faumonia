/* $Id: HelpAction.java,v 1.37 2012/03/30 21:21:36 nhnb Exp $ */
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
package games.stendhal.client.actions;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.NotificationType;

/**
 * Display command usage. Eventually replace this with ChatCommand.usage().
 */
class HelpAction implements SlashAction {

	/**
	 * Execute a chat command.
	 * 
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 * 
	 * @return <code>true</code> if was handled.
	 */
	public boolean execute(final String[] params, final String remainder) {
		final String[] lines = {
				"Oficjalna strona gry: #http://www.faumonia.pl",
				"Oto najczęściej używane komendy:",
				"",
				"* Rozmowa: ",
				"- /k \t\t Wykrzykuje wiadomość na całą krainę",
				"- /me <akcja> \t\tPokazuje wiadomość o tym co teraz robisz",
				"- /tell <wojownik> <wiadomość> \tWysyła prywatną wiadomość do <wojownik>",
				"- /answer <wiadomość> \t\tWysyła prywatną wiadomość do wojownika, który ostatnio przysłał Tobie wiadomość",
				"- // <wiadomość> \t\tWysyła wiadomość do wojownika któremu ostatnio wysłałeś wiadomość",
				"- /storemessage <wojownik> <wiadomość> Zostawia do dostarczenia prywatną wiadomość rozłączonym wojownikom",
				"- /support <wiadomość> \tZapytaj administratora w celu uzyskania pomocy.",
				"- /who \t\tWyświetla listę wojowników którzy są aktualnie zalogowani",
				"- /where <wojownik> \t\tPodaje położenie <wojownik>",
				"",
				"* Przedmioty: ",
				"- /drop [ilość] <przedmiot>\tOdkłada określoną ilość danego przedmiotu",
				"",
				"* Przyjaciele i Wrogowie: ",
				"- /add <wojownik> \t\tDodaje <wojownik> do twojej listy przyjaciół",
				"- /remove <wojownik> \tUsuwa <wojownik> z twojej listy przyjaciół",
				"- /ignore <wojownik> [<minuty>|*|- [<powód>]] \tDodaj <wojownik> do listy ignorowanych",
				"- /ignore \t\t Sprawdź kto jest na twojej liście ignorowanych",
				"- /unignore <wojownik> \tUsuwa <wojownik> z listy ignorowanych",
				"",
				"* Status: ",
				"- /away <wiadomość> \tUstawia wiadomość dla statusu oddalony",
				"- /away \tUsuwa status oddalony",
				"- /grumpy <wiadomość> \t Ustawiasz wiadomość i ignorujesz wszystkich graczy spoza kręgu przyjaciół.",
				"- /grumpy \tUsuwa status niedostępności",
				"- /name <zwierzątko> <imię> \t\tNadaje imię twojemu zwierzątku",
				"",
				"* Różne: ",
				"- /clickmode <0|1> \t\tWyłącza lub włącza możliwość podwójnego kliknięcia przy jednokrotnym naciśnięciu lewego przycisku myszy",
				"- /info \t\tPokazuje aktualny czas z serwera",
				"- /mute\t\tUcisza lub przywraca dźwięki",
				"- /volume\t\tWylicza lub ustawia poziom głośności dla dźwięków i muzyki"
		};

		for (final String line : lines) {
			ClientSingletonRepository.getUserInterface().addEventLine(new HeaderLessEventLine(line, NotificationType.CLIENT));
		}

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMaximumParameters() {
		return 0;
	}

	/**
	 * Get the minimum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMinimumParameters() {
		return 0;
	}
}
