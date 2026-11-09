/* $Id: TelepathNPC.java,v 1.29 2011/05/01 19:50:06 martinfuchs Exp $ */
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
package games.stendhal.server.maps.semos.temple;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextWithPlayerNameAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TelepathNPC implements ZoneConfigurator {
	private static final long PEACE_PERIOD = 2L * MathHelper.MILLISECONDS_IN_ONE_DAY;

	/** Rechecked both when offering the service and immediately before removing the mark. */
	private boolean canRemoveSkull(final Player player, final EventRaiser raiser) {
		if (!player.isBadBoy()) {
			raiser.say("Nie nosisz już piętna zabójcy.");
			return false;
		}
		final long timeRemaining = player.getLastPVPActionTime() + PEACE_PERIOD - System.currentTimeMillis();
		if (timeRemaining > 0) {
			raiser.say("Musisz powstrzymać się od atakowania innych wojowników przez dwa dni. Wróć za "
					+ TimeUtil.approxTimeUntil((int) ((timeRemaining + 999) / 1000)) + ".");
			return false;
		}
		if (player.getKarma() < 5) {
			raiser.say("Twoja karma nie jest jeszcze wystarczająca, abym mogła zdjąć piętno. Pomagaj innym i wróć, gdy się poprawi.");
			return false;
		}
		return true;
	}

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosTempleArea(zone, attributes);
	}

	private void buildSemosTempleArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Io Flotto") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 19));
				nodes.add(new Node(8, 20));
				nodes.add(new Node(15, 20));
				nodes.add(new Node(15, 19));
				nodes.add(new Node(16, 19));
				nodes.add(new Node(16, 14));
				nodes.add(new Node(15, 14));
				nodes.add(new Node(15, 13));
				nodes.add(new Node(12, 13));
				nodes.add(new Node(8, 13));
				nodes.add(new Node(8, 14));
				nodes.add(new Node(7, 14));
				nodes.add(new Node(7, 19));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {

				// player has met io before and has a pk skull
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestStartedCondition("meet_io"),
								new ChatCondition() {
									public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
										return player.isBadBoy() ;
									}
								}),
				        ConversationStates.QUESTION_1,
				        null,
				        new SayTextWithPlayerNameAction("Witaj ponownie [name]. Wyczuwam, że zostałeś naznaczony znakiem czaszki. Czy chcesz, abym to usunęła?"));
				
				// player has met io before and has not got a pk skull
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestStartedCondition("meet_io"),
								new ChatCondition() {
									public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
										return !player.isBadBoy() ;
									}
								}),
				        ConversationStates.ATTENDING,
				        null,
				        new SayTextWithPlayerNameAction("Witaj ponownie [name]. W czym mogę #pomóc? Nie to. Tego jeszcze nie wiem..."));
				
				// first meeting with player
				add(ConversationStates.IDLE, 
						ConversationPhrases.GREETING_MESSAGES, 
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition("meet_io")),
						ConversationStates.ATTENDING,
				        null, 
				        new MultipleActions(
				        		new SayTextWithPlayerNameAction("Czekałam na Ciebie [name]. Skąd znam twoje imię? To proste jestem Flotto telepatka. Chcesz, abym pokazała Ci sześć podstaw telepatii?"),
				        		new SetQuestAction("meet_io", "start")));
	
				add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING,
						null, new ChatAction() {
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								if (canRemoveSkull(player, raiser)) {
									raiser.say("Żałujesz swoich czynów?");
									raiser.setCurrentState(ConversationStates.QUESTION_2);
								}
							}
						});

				// player didn't want pk icon removed, offer other help
				add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null, ConversationStates.ATTENDING, "Dobrze! Mogę ci #pomóc w różnych rzeczach jeżeli chcesz.", null);
				// player satisfied the pk removal requirements and said yes they were sorry
				add(ConversationStates.QUESTION_2, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING,
						null, new ChatAction() {
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								if (canRemoveSkull(player, raiser)) {
									player.rehabilitate();
									raiser.say("Zdjęłam z ciebie piętno zabójcy. Niech twoje dalsze czyny przyniosą innym spokój.");
								}
							}
						});

				// player said no they are not really sorry
				add(ConversationStates.QUESTION_2, ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE, "Myślałam, że nie! Dowidzenia!", null);
				addJob("Potrafię użyć całą moc ludzkiego umysłu. Zrobiłam wielkie postępy w telepatii i telekinezie. Jednakże nadal nie mogę przewidywać przyszłości. Nie jestem pewna czy będziemy w stanie zniszczyć mroczne legiony Blordroughów...");
				addQuest("Na razie nie potrzebuję pomocy od nikogo i... Hej! Nie próbujesz czasem czytać w moich myślach? Zawsze przed zrobieniem czegoś takiego powinieneś zapytać o pozwolenie!");
				addGoodbye();
				// further behaviour is defined in the MeetIo quest.
			}
		};

		npc.setEntityClass("floattingladynpc");
		npc.setDescription("Oto Io Flotto. Widzi ciebie.");
		npc.setPosition(8, 19);
		npc.initHP(100);
		zone.add(npc);
	}
}
