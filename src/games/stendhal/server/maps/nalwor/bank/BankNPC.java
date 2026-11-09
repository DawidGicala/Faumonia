/* $Id: BankNPC.java,v 1.7 2012/02/13 00:44:28 bluelads99 Exp $ */
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
package games.stendhal.server.maps.nalwor.bank;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the nalwor bank npcs.
 *
 * @author kymara
 */
public class BankNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildoldNPC(zone, attributes);
		buildladyNPC(zone, attributes);
	}

	//
	// name inspired by a name in lotr
	// TODO: He complains if someone steals something from his chest: they should be sent to elf jail.

	private void buildoldNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC oldnpc = new SpeakerNPC("Grafindle") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.CollectionDialogueTopics.install(this);
				addGreeting("Pozdrawiam. Jeżeli potrzebujesz #pomocy to pytaj.");
				addJob("Pilnuję depozytów w banku Nalwor. Klient powierza nam dorobek, dlatego wymagam dokładności także od osób przewożących przesyłki. Pomoc Lorithien to dobra okazja, żeby wykazać się rzetelnością.");
				addHelp("Pokój ma dwie skrzynie należące do tego banku i dwie skrzynie należące do banku w Semos.");
				addGoodbye("Dowidzenia młodzieńcze.");
				//remaining behaviour defined in Take Gold for Grafindle quest
			}
		};

		oldnpc.setDirection(Direction.DOWN);
		oldnpc.setEntityClass("elfbankeroldnpc");
		oldnpc.setDescription("Widzisz Grafindla, starszego bankiera Nalwor. Każdą rozmowę o depozytach prowadzi z cierpliwą ostrożnością.");
		oldnpc.setPosition(13, 17);
		oldnpc.initHP(100);
		zone.add(oldnpc);
	}

	//
	// Ariannyddion is welsh for bank, so ...
	//
	private void buildladyNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC ladynpc = new SpeakerNPC("Nnyddion") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(26, 30));
				nodes.add(new Node(16, 30));
				nodes.add(new Node(16, 31));
				nodes.add(new Node(17, 31));
				nodes.add(new Node(17, 30));
				nodes.add(new Node(26, 30));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.CollectionDialogueTopics.install(this);
				addGreeting("Witam w banku Nalwor. Jestem tutaj, aby #pomóc.");
				addHelp("Klienci mogą u mnie zdeponować swoje przedmioty w skrzyniach w tym pokoiku znajdującym się za mną. Dwie skrzynie po prawej są administrowane przez Semos.");
				addOffer("Mogę Ci #pomóc.");
				addJob("Pomagam klientom banku, zarówno elfom, jak i przybyszom. Nie mogę opuszczać posterunku, więc niektóre dostawy muszę powierzyć innym. Każdy przyjęty egzemplarz zapisuję, żeby nic nie zginęło w rozliczeniu.");
				addQuest("Niczego nie potrzebuję. Dziękuję.");
				addGoodbye("Dowidzenia. Dziękuje za twój czas.");
			}
		};

		ladynpc.setDescription("Widzisz Nnyddion, elfkę doglądającą banku Nalwor. Z uwagą obserwuje klientów i skrzynie depozytowe.");
		ladynpc.setEntityClass("elfbankladynpc");
		ladynpc.setDirection(Direction.DOWN);
		ladynpc.setPosition(17, 31);
		ladynpc.initHP(100);
		zone.add(ladynpc);
	}
}
