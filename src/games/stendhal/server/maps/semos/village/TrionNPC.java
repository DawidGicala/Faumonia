/* $Id: TrionNPC.java,v 1.9 2024/12/17 4:09:45 davvids Exp $ */
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
package games.stendhal.server.maps.semos.village;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasShieldEquippedCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;


public class TrionNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildMineArea(zone, attributes);
	}

	private void buildMineArea(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Trion") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(122, 13));
				nodes.add(new Node(104, 13));
				nodes.add(new Node(104, 18));
				nodes.add(new Node(90, 18));
				nodes.add(new Node(90, 28));
				nodes.add(new Node(84, 28));
				nodes.add(new Node(84, 44));
				nodes.add(new Node(69, 44));
				nodes.add(new Node(69, 40));
				nodes.add(new Node(54, 40));
				nodes.add(new Node(54, 39));
				nodes.add(new Node(56, 39));
				nodes.add(new Node(56, 40));
				nodes.add(new Node(69, 40));
				nodes.add(new Node(69, 35));
				nodes.add(new Node(68, 35));
				nodes.add(new Node(68, 24));
				nodes.add(new Node(62, 24));
				nodes.add(new Node(62, 26));
				nodes.add(new Node(50, 26));
				nodes.add(new Node(50, 14));
				nodes.add(new Node(59, 14));
				nodes.add(new Node(59, 13));
				nodes.add(new Node(85, 13));
				nodes.add(new Node(85, 15));
				nodes.add(new Node(104, 15));
				nodes.add(new Node(104, 12));
				nodes.add(new Node(104, 13));
				setPath(new FixedPath(nodes, true));
			}

    @Override
protected void createDialog() {

    String greetingBasis = "Witaj, wojowniku! Przemierzam miasto Semos, aby uczyć nowych graczy podstaw gry. Mogę opowiedzieć Ci o #skillach, #zadaniach, #globalnym #chacie, #mistrzach #gry, #handlowaniu i innych mechanikach. Zapytaj o cokolwiek, co cię interesuje!";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(600)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "Pamiętaj, że słowa podświetlone na niebiesko możesz wpisywać, aby uzyskać więcej informacji!",
            null);

    addHelp("Możesz zadać mi pytania o #skille, #zadania, #mistrzów #gry, #globalny #czat, #handlowanie, #mikstury albo o to, jak #podnosić #przedmioty.");

    addReply(Arrays.asList("skille", "skillowanie", "trening", "skillach"),
             "Skille to twoje umiejętności walki. Znajdziesz je po lewej stronie ekranu pod minimapką. Przykład: ATK: 14x10 oznacza, że skill ataku to 14, a broń ma wartość 10. Im wyższe skille, tym większe obrażenia lub obrona.");

    addReply(Arrays.asList("arena", "trening na arenie", "skillowanie na arenie"),
             "Na arenie możesz trenować skille. Znajdziesz tam #mgły, które pozwalają rozwijać atak, oraz #źródełka, gdzie możesz trenować bezpiecznie, bo twoje zdrowie jest stale odnawiane. Znajdziesz ją w okolicach kamieniołomu, na samej górze mapy.");

    addReply(Arrays.asList("mgły", "trening mgieł", "kryształy"),
             "Aby wejść do mgieł i trenować atak, potrzebujesz niebieskich kryształów. Możesz je zdobyć z przeróżnych zadań, na przykład od burmistrza w ratuszu.");

    addReply(Arrays.asList("źródełka", "leczenie", "trening źródełka"),
             "Źródełka pozwalają trenować bezpiecznie bez pilnowania postaci, ponieważ twoje zdrowie automatycznie się odnawia. Aby tam wejść, potrzebujesz czerwonych kryształów, możesz je zdobyć wykonując odnawialne zadania.");

    addReply(Arrays.asList("handlowanie", "kupowanie", "sprzedawanie", "handlowaniu"),
             "Handlowanie to podstawa w każdej społeczności. Aby coś kupić lub sprzedać, porozmawiaj z NPC i użyj słów takich jak oferta, kup lub sprzedaj.");

    addReply(Arrays.asList("globalny czat", "czat globalny", "pomoc na czacie", "globalnym chacie"),
             "Globalny czat to miejsce, gdzie możesz zadać pytanie innym graczom. Aby go użyć, wpisz #/k #wiadomość, a twoja wiadomość trafi do wszystkich.");

    addReply(Arrays.asList("zadania", "questy", "misje", "zadaniach"),
             "Zadania są bardzo ważne, aby szybciej zdobywać doświadczenie i nagrody. Zacznij od #mistrzów #gry, którzy znajdują się w czerwonym domku przy domku startowym.");

    addReply(Arrays.asList("mistrzowie gry", "mistrzowie", "mistrz gry", "mistrzach gry", "mistrzach", "mistrzów gry"),
             "Mistrzowie gry znajdują się w czerwonym domku po prawej stronie od domku startowego. Pomogą ci zrozumieć podstawy gry i zlecą przeróżne zadania.");

    addReply(Arrays.asList("podnoszenie przedmiotów", "przedmioty", "jak podnosić", "podnosić przedmioty"),
             "Podnoszenie przedmiotów jest banalnie proste. Po prostu kliknij na przedmiot na ziemi, przytrzymaj go i przeciągnij do swojego plecaka.");

    addReply(Arrays.asList("mikstury", "leczenie", "jedzenie"),
             "Mikstury i jedzenie są niezbędne do odzyskiwania zdrowia. Kliknij prawym przyciskiem na miksturę i wybierz 'Użyj', aby się uleczyć. Pamietaj, że warto mieć ich zapas!");

    addQuest("Moim zadaniem jest nauczenie cię podstaw gry. Zapytaj o #skille, #zadania, #handlowanie, #mistrzów #gry i inne rzeczy, które cię interesują.");
    addGoodbye("Powodzenia, wojowniku! Odwiedź czerwony domek i wykonuj zadania u Mistrzów Gry. Pamiętaj o regularnym korzystaniu z globalnego czatu. Trzymaj się!");
}

			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("TrionFirstChat")) {
					player.setQuest("TrionFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("TrionNPC");
		npc.setDescription("Widzisz Triona, przewodnika po mechanikach gry. Wyjaśnia Ci on podstawy skillowania, handlu i zadań.");
		npc.setPosition(122, 13);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
