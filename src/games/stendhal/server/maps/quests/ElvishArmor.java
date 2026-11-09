/* $Id: ElvishArmor.java,v 1.39 2012/04/20 17:40:43 kymara Exp $ */
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

import games.stendhal.server.entity.npc.dialogue.NpcDialogueService;

import games.stendhal.common.NpcDialogueSnapshot.Reward;

import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;

import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.Arrays;
import java.util.List;

/**
 * QUEST: ElvishArmor
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Lupos, an albino elf who live in Fado Forest</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Lupos wants to see every piece of elvish equipment you can bring him</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 20000 XP</li>
 * <li> Karma:25</li>
 * <li> ability to sell elvish stuff and also drow sword</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class ElvishArmor extends AbstractQuest implements
		BringListOfItemsQuest {

	private static final String QUEST_SLOT = "elvish_armor";
	
	private BringListOfItemsQuestLogic bringItems;
	
	private static final List<String> NEEDEDITEMS = Arrays.asList(
			"zbroja elficka", "spodnie elfickie", "buty elfickie", "miecz elficki",
			"płaszcz elficki", "tarcza elficka");

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		return bringItems.getHistory(player);
	}


	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		bringItems = new BringListOfItemsQuestLogic(concreteQuest) {
            @Override protected void acceptQuest() {
                getNPC().add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES, null,
                    ConversationStates.ATTENDING, respondToQuestAcception(),
                    new SetQuestAndModifyKarmaAction(QUEST_SLOT, "", getKarmaDiffForQuestResponse()));
            }
            @Override protected void rejectQuest() {
                getNPC().add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
                    ConversationStates.ATTENDING, respondToQuestRefusal(),
                    new DecreaseKarmaAction(getKarmaDiffForQuestResponse()));
            }
            @Override protected void playerDoesNotWantToGiveItems() {
                getNPC().add(new ConversationStates[] {ConversationStates.ATTENDING, ConversationStates.QUESTION_1},
                    ConversationPhrases.NO_MESSAGES, null, ConversationStates.ATTENDING,
                    respondToPlayerSayingHeHasNoItems(java.util.Collections.<String>emptyList()), null);
            }
        };
		bringItems.addToWorld();
	}

  	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zbroja Elficka",
				"Lupos, rzemieślnik elfów albinosów, bada wyroby zielonych elfów. Potrzebuje sześciu różnych części elfickiego rynsztunku.",
				true);
		offerSteps();
		setupAbstractQuest();
	}

	public SpeakerNPC getNPC() {
		return npcs.get("Lupos");
	}

	public List<String> getNeededItems() {
		return NEEDEDITEMS;
	}

	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("equipment", "rynsztunku");
	}

	public List<String> getAdditionalTriggerPhraseForQuest() {
		return Arrays.asList("secrets", "sekretów");
	}

	public double getKarmaDiffForQuestResponse() {
		return 5.0;
	}

	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return true;
	}

	public String welcomeBeforeStartingQuest() {
		return "Witaj, podróżniku. W tej pracowni próbuję zrozumieć kunszt zielonych elfów z Nalwor, lecz same opowieści nie wystarczą. Może pomożesz mi poznać kilka ich #sekretów?";
	}

	public String respondToQuest() {
		return "Elficka zbroja jest lekka i starannie wykonana, ale nie znam wszystkich tajników jej produkcji. Potrzebuję sześciu różnych części #rynsztunku, po jednej z każdego rodzaju, żeby porównać ich wykonanie. Zechcesz zbierać dla mnie te próbki?";
	}

	public String respondToQuestAcception() {
		return "Znakomicie, wreszcie będę mógł zbadać prawdziwe wyroby zamiast polegać na pogłoskach. Potrzebuję elfickiej zbroi, spodni, butów, miecza, płaszcza i tarczy; możesz przynosić je pojedynczo. Zapytaj o #rynsztunku, a przypomnę, których części jeszcze brakuje.";
	}

	public String respondToQuestRefusal() {
		return "Rozumiem, zdobycie takiego kompletu wymaga czasu i ostrożności. Nie będę cię zatrzymywał. Jeśli kiedyś zechcesz pomóc w badaniach, wróć do mnie.";
	}

	public String welcomeDuringActiveQuest() {
		return "Witaj ponownie. Przygotowałem miejsce na kolejne próbki, a dostarczone części już odłożyłem do badań. Porozmawiajmy o #rynsztunku, jeśli udało ci się zdobyć coś nowego.";
	}

	// this one not actually used here
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Do badań brakuje mi jeszcze: " + Grammar.enumerateCollection(missingItems) + ". Potrzebuję po jednej sztuce każdego rodzaju. Możesz przekazać części, które masz teraz, a po resztę wrócić później.";
	}

	public String askForMissingItems(final List<String> missingItems) {
		return "Do badań brakuje mi jeszcze: " + Grammar.enumerateCollection(missingItems) + ". Potrzebuję po jednej sztuce każdego rodzaju. Możesz przekazać części, które masz teraz, a po resztę wrócić później.";
	}

	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Doskonale, przyjrzyjmy się znaleziskom. Wskaż część #rynsztunku, którą chcesz przekazać. Wezmę po jednej sztuce, a każdą dostawę zapamiętam.";
	}

	public String respondToItemBrought() {
		return "Dziękuję, tę część mogę już porównać z pozostałymi próbkami. Nie musisz przynosić jej drugi raz. Masz przy sobie jeszcze coś z brakującego rynsztunku?";
	}


	public String respondToLastItemBrought() {
		return "To ostatnia brakująca część - mam już cały komplet do badań. Dzięki tobie mogę wreszcie porównać kunszt zielonych elfów z naszym. Od teraz będę również skupował od ciebie wybrane wyroby; zapytaj o moją #ofertę.";
	}
								
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Nie widzę przy tobie przedmiotu: " + itemName + ". Sprawdź plecak i wróć, gdy będziesz go mieć ze sobą.";
	}
	public String respondToOfferOfNotMissingItem() {
		return "Tę część już mi przekazano i leży wśród próbek. Zachowaj następny egzemplarz dla siebie. Do zakończenia badań potrzebuję tylko brakujących rodzajów rynsztunku.";
	}

	public String respondToOfferOfNotNeededItem() {
		return "Ten przedmiot nie należy do sześciu próbek, których potrzebuję. Zbieram elficką zbroję, spodnie, buty, miecz, płaszcz i tarczę. Możemy ponownie wrócić do #rynsztunku, żeby sprawdzić braki.";
	}

	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Nic nie szkodzi, badania wymagają cierpliwości. Przekazane próbki mam już u siebie. Wróć z następną częścią, kiedy ją zdobędziesz. Nie ryzykuj niepotrzebnie dla pośpiechu.";
	}

	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Komplet do badań jest już u mnie, a twoja pomoc bardzo przyspieszyła pracę. Nie potrzebuję kolejnej takiej dostawy w ramach zadania. Jeśli masz dodatkowy ekwipunek, zapytaj o moją #ofertę skupu.";
	}

	public void rewardPlayer(final Player player) {
		double karmaBefore = player.getKarma();
        int xpBefore = player.getXP();
        player.addKarma(20.0);
        player.addXP(20000);
        if (player.getXP() > xpBefore) {
            NpcDialogueService.reward(getNPC(), player, new Reward("XP postaci", "gui/dialogue/xp", player.getXP() - xpBefore, false));
        }
        if (player.getKarma() > karmaBefore) {
            NpcDialogueService.reward(getNPC(), player, new Reward("Karma", "gui/dialogue/karma", player.getKarma() - karmaBefore, false));
        }
        // The caller marks this quest done immediately afterwards, unlocking the existing buyer.
        NpcDialogueService.reward(getNPC(), player, new Reward("Skup u Luposa", "gui/dialogue/unlock", 1, false));
	}

	public String welcomeAfterQuestIsCompleted() {
		return "Witaj, dobrze znów cię widzieć w pracowni. Nadal porównuję części, które mi przyniesiono. Jeśli masz kolejny ekwipunek na sprzedaż, możemy omówić moją #ofertę.";
	}

	// the bring list of items quest doesn't include this logic:
		// player returns when the quest is in progress and says quest
		//				"As you already know, I seek elvish #equipment.";


	private void offerSteps() {
  		final SpeakerNPC npc = npcs.get("Lupos");

		// player returns after finishing the quest and says offer
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dzięki dostarczonym próbkom wiem już, którym wyrobom warto się przyglądać. Możesz mi #sprzedać przedmioty z oferty skupu; za każdą sztukę podam cenę przed potwierdzeniem. Zachowaj to, czego nadal używasz w podróży.",
				null);


		// player returns when the quest is in progress and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Najpierw potrzebuję kompletu próbek do badań. Kiedy zakończymy zadanie, otworzę dla ciebie skup wybranych wyrobów. Na razie porozmawiajmy o brakującym #rynsztunku.", null);
	}



	@Override
	public String getName() {
		return "ElvishArmor";
	}

	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "Lupos";
	}
	
	// it's technically in Fado forest but much nearer Kirdneh city
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
}
