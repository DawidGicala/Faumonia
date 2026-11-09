/* $Id: SevenCherubs.java,v 1.70 2011/05/01 19:50:05 martinfuchs Exp $ */
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
package games.stendhal.server.maps.quests;
import games.stendhal.server.entity.npc.action.DialogueRewardAction;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Find the seven cherubs that are all around the world. PARTICIPANTS: -
 * Cherubiel - Gabriel - Ophaniel - Raphael - Uriel - Zophiel - Azazel STEPS: -
 * Find them and they will reward you.
 *
 * REWARD: -
 *
 * REPETITIONS: - Just once.
 */
public class SevenCherubs extends AbstractQuest {
	private static final String QUEST_SLOT = "seven_cherubs";

	private final HashMap<String, String> cherubsHistory = new HashMap<String,String>();
	
	private void fillHistoryMap() {
		cherubsHistory.put("Cherubiel", "Spotkałem Cherubiela");
		cherubsHistory.put("Ophaniel",  "Spotkałem Ophaniela");
		cherubsHistory.put("Gabriel",   "Spotkałem Gabriela.");
		cherubsHistory.put("Raphael",   "Spotkałem Raphaela");
		cherubsHistory.put("Zophiel",   "Spotkałem Zophiela");
		cherubsHistory.put("Azazel",    "Spotkałem Azazela");
		cherubsHistory.put("Uriel",     "Spotkałem Uriela.");		
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
    /** Accept legacy name lists and explicit completion without counting empty tokens. */
    public static java.util.Set<String> visited(String saved) {
        java.util.Set<String> result = new java.util.LinkedHashSet<String>();
        if (saved != null) {
            for (String token : saved.split(";")) {
                String name = token.trim();
                if (getMeetingNames().contains(name)) { result.add(name); }
            }
        }
        return result;
    }

    private static boolean completed(String saved) {
        return saved != null && ("done".equals(saved) || saved.startsWith("done;")
                || visited(saved).size() == getMeetingNames().size());
    }

    @Override
    public boolean isCompleted(final Player player) {
        return completed(player.getQuest(QUEST_SLOT));
    }

    @Override
    public List<String> getHistory(final Player player) {
        final List<String> res = new ArrayList<String>();
        String saved = player.getQuest(QUEST_SLOT);
        if (saved == null) { return res; }
        res.add("Zacząłem szukać siedmiu aniołków.");
        for (String name : visited(saved)) {
            String entry = cherubsHistory.get(name);
            res.add(entry == null ? "Spotkałem anioła: " + name + "." : entry);
        }
        if (completed(saved)) {
            res.add("Znalazłem wszystkie siedem aniołków. Nagroda: anielski pierścień, 10000 XP, 3000 XP ataku i 5000 XP obrony.");
        } else {
            res.add(getMeetingHint());
        }
        return res;
    }

	    private static String meetingWords(String name) {
        switch (name) {
            case "Cherubiel": return "W Semos wiele dróg dopiero się zaczyna. Dobrze, że zatrzymałeś się tutaj na chwilę.";
            case "Gabriel": return "Nalwor to jeden z przystanków twojej wędrówki. Rozejrzyj się, zanim ruszysz dalej.";
            case "Ophaniel": return "Nad rzeką Orril łatwo na chwilę zwolnić krok. Witaj, wędrowcze.";
            case "Raphael": return "Most prowadzi dalej w stronę Fado. Zanim go miniesz, odpocznij chwilę.";
            case "Zophiel": return "Dotarłeś w północno-zachodnie góry Semos. Droga tutaj wymaga więcej uwagi niż miejska ulica.";
            case "Uriel": return "Zachodnie góry Orril bywają niebezpieczne. Przy zbójach nie trać czujności.";
            case "Azazel": return "Nad wielkim jeziorem Ados można złapać oddech po długiej drodze.";
            default: return "Witaj, dobrze cię spotkać.";
        }
    }

    static class CherubNPC extends SpeakerNPC {
		public CherubNPC(final String name, final int x, final int y) {
			super(name);

			setEntityClass("angelnpc");
            setDescription("Widzisz anioła o imieniu "+name+". Jego obecność przynosi chwilę wytchnienia po trudach drogi.");
			setPosition(x, y);
			initHP(100);

			final List<Node> nodes = new LinkedList<Node>();
			nodes.add(new Node(x, y));
			nodes.add(new Node(x - 2, y));
			nodes.add(new Node(x - 2, y - 2));
			nodes.add(new Node(x, y - 2));
			setPath(new FixedPath(nodes, true));
		}

		@Override
		protected void createPath() {
			// do nothing
		}

		@Override
		protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.JourneyDialogueTopics.install(this);
            addJob("Jest nas siedmioro, a każde spotkanie stanowi część tej samej wędrówki. Odnajdź pozostałych i przywitaj się z nimi; ostatni przekaże ci anielski pierścień. Mogę przypomnieć miejsca poszukiwań.");
			add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new GreetingMatchesNameCondition(getName()), true,
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
                        String saved = player.getQuest(QUEST_SLOT);
                        java.util.Set<String> seen = visited(saved);
                        if (completed(saved)) {
                            raiser.say("Cała siódemka została odnaleziona, a nagroda przekazana. Twoja wędrówka dobiegła końca. Miło znów cię spotkać.");
                        } else if (!seen.add(raiser.getName())) {
                            raiser.say("To spotkanie masz już za sobą. Wyrusz do pozostałych aniołów; w dzienniku znajdziesz miejsca, od których warto zacząć. Nie musisz wracać do tych, których już znasz.");
                        } else {
                            StringBuilder progress = new StringBuilder();
                            for (String name : seen) { progress.append(';').append(name); }
                            player.setQuest(QUEST_SLOT, progress.toString());
                            DialogueRewardAction.healing((p, text, n) -> { p.heal(); }).fire(player, sentence, raiser);
                            player.healPoison();
                            int left = getMeetingNames().size() - seen.size();
                            if (left > 0) {
                                raiser.say(meetingWords(raiser.getName()) + " Pozostało jeszcze " + left + " aniołków do odnalezienia. Możemy porozmawiać o miejscach, w których ich szukać.");
                                // Keep the original rewards for the ordinary sequence of meetings.
                                player.addXP(raiser.getZone().getName().equals("0_semos_village_w")
                                        ? 20 : (seen.size() + 1) * 200);
                            } else {
                                final Item item = SingletonRepository.getEntityManager().getItem("anielski pierścień");
                                item.setBoundTo(player.getName());
                                player.equipOrPutOnGround(item);
                                player.addXP(10000);
                                player.addatk_xp(3000);
                                player.adddef_xp(5000);
                                // A real completion marker also works with the standard quest API.
                                player.setQuest(QUEST_SLOT, "done" + progress);
                                raiser.say("Siedem spotkań zamknęło twoją wędrówkę. Przyjmij anielski pierścień, 10000 XP, 3000 XP ataku i 5000 XP obrony. Niech pierścień przypomina ci miejsca poznane po drodze.");
                            }
                        }
						player.notifyWorldAboutChanges();
					}
				});
			addGoodbye();
		}
	}

	@Override
	public void addToWorld() {
		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		super.addToWorld();
		fillHistoryMap();
		fillQuestInfo(
				"Siedem Aniołków",
				"Odnajdź siedem aniołków i porozmawiaj z każdym z nich. Cherubiel przebywa w Semos, Gabriel w Nalwor, Ophaniel nad rzeką Orril, a Raphael w okolicy mostu w stronę Fado. Zophiela szukaj w północno-zachodnich górach Semos, Uriela w zachodnich górach Orril, a Azazela przy wielkim jeziorze Ados.",
				false);
		StendhalRPZone zone;
		SpeakerNPC npc;

		zone = world.getZone("0_semos_city");
		npc = new CherubNPC("Cherubiel", 32, 60);
		zone.add(npc);

		zone = world.getZone("0_nalwor_city");
		npc = new CherubNPC("Gabriel", 105, 17);
		zone.add(npc);

		zone = world.getZone("0_orril_river_s");
		npc = new CherubNPC("Ophaniel", 105, 79);
		zone.add(npc);

		zone = world.getZone("0_orril_river_s_w2");
		npc = new CherubNPC("Raphael", 95, 30);
		zone.add(npc);

		zone = world.getZone("0_orril_mountain_w2");
		npc = new CherubNPC("Uriel", 47, 27);
		zone.add(npc);

		zone = world.getZone("0_semos_mountain_n2_w2");
		npc = new CherubNPC("Zophiel", 16, 3);
		zone.add(npc);

		zone = world.getZone("0_ados_rock");
		npc = new CherubNPC("Azazel", 67, 24);
		zone.add(npc);
	}

	@Override
	public String getName() {
		return "SevenCherubs";
	}

    public static List<String> getMeetingNames(){return java.util.Collections.unmodifiableList(Arrays.asList("Cherubiel","Ophaniel","Gabriel","Raphael","Zophiel","Azazel","Uriel"));}
    public static String getMeetingHint(){return "Szukaj Cherubiela w Semos, Gabriela w Nalwor, Ophaniela nad rzeką Orril, Raphaela przy moście w stronę Fado, Uriela w górach Orril przy zbójach, Zophiela w północno-zachodnich górach Semos, a Azazela nad wielkim jeziorem Ados.";}

}
