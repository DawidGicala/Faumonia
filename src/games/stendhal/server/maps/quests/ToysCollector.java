/* $Id: ToysCollector.java,v 1.49 2011/11/13 17:15:18 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Toys Collector
 * 
 * PARTICIPANTS: <ul>
 * <li> Anna, a girl who live in Ados </ul>
 * 
 * STEPS:
 * <ul><li> Anna asks for some toys
 * <li> You guess she might like a teddy, dice or dress
 * <li> You bring the toy to Anna
 * <li> Repeat until Anna received all toys. (Of course you can bring several
 * toys at the same time.)
 * <li> Anna gives you a reward
 * </ul>
 * REWARD:<ul>
 * <li> 3 pies
 * <li> 100 XP
 * <li> 10 Karma
 * </ul>
 * REPETITIONS: <ul><li> None.</ul>
 */
public class ToysCollector extends AbstractQuest implements
		BringListOfItemsQuest {

	private static final String QUEST_SLOT = "toys_collector";
	
	private BringListOfItemsQuestLogic bringItems;
	
	private static final List<String> neededToys = 
		Arrays.asList("pluszowy miś", "kości do gry", "koszula");
		
	// don't want to use the standard history for this kind of quest for anna as we dont want to say what she needs.
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			if (!"done".equals(questState)) {
				res.add("Anna chce coś do zabawy. Muszę pomyśleć co ją uszczęśliwi!");
			} else {
				res.add("Mam kilka zabawek dla Anna, Jens i George do zabawy.");
			}
			return res;
	}

	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		bringItems = new BringListOfItemsQuestLogic(concreteQuest);
		bringItems.addToWorld();
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Kolekcjoner Zabawek",
				"Spróbuj znaleść zabawki dla Anny i jej przyjaciół.",
				false);
		setupAbstractQuest();
		specialStuff();
	}

	private void specialStuff() {
		getNPC().add(
				ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Powinieneś odejść nim wpadnę w tarapaty za rozmowę z tobą. Dowidzenia.",
				null);
	}

	public SpeakerNPC getNPC() {
		return npcs.get("Anna");
	}

	public List<String> getNeededItems() {
		return neededToys;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return ConversationPhrases.EMPTY;
	}

	public List<String> getAdditionalTriggerPhraseForQuest() {
		return Arrays.asList("toys", "zabawki");
	}

	public double getKarmaDiffForQuestResponse() {
		return 8.0;
	}

	public String welcomeBeforeStartingQuest() {
		return "Mama powiedziała, że nie powinniśmy rozmawiać z obcymi. Ona martwi się o zaginioną dziewczynkę, ale ja się nudzę. Chcę jakieś #zabawki ( #toys )!";
	}

	public String welcomeDuringActiveQuest() {
		return "Cześć! Wciąż się nudzę. Przyniosłeś mi zabawki?";
	}

	public String welcomeAfterQuestIsCompleted() {
		return "Cześć! Jestem zajęta zabawą moimi zabawkami.";
	}

	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return true;
	}

	public String respondToQuest() {
		return "Nie jestem pewna jakie zabawki! Przyniesiesz mi coś? Proszę.";
	}

	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Zabawki są wspaniałe! Dziękuję!";
	}

	public String respondToQuestAcception() {
		return "Huuraa! Ale zabawa. Do zobaczenia.";
	}

	public String respondToQuestRefusal() {
		return "Och ... masz na myśli.";
	}
	
	// not used
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Nie jestem pewna jakie zabawki! Przyniesiesz mi coś? Proszę";
	}
	
	public String askForMissingItems(final List<String> missingItems) {
		return "Jakie zabawki mi przyniosłeś?";
	}

	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze. Wróć później.";
	}

	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Co mi przyniosłeś?!";
	}

	public String respondToItemBrought() {
		return "Dziękuję bardzo! Co jeszcze mi przyniosłeś";
	}

	public String respondToLastItemBrought() {
		return "Te zabawki będą mnie bawić przez lata! Proszę weź te placki. Arlindo upiekł je dla nas, ale chyba ty powinieneś je mieć.";
	}

	public void rewardPlayer(final Player player) {
		final StackableItem pie = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"tarta");
		pie.setQuantity(30);
		player.equipOrPutOnGround(pie);
		player.addXP(2500);
		player.addatk_xp(1000);
		player.adddef_xp(1000);
		player.addKarma(10.0);
	}

	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Hej! To kłamstwo! Nie masz ze sobą "
				+ Grammar.a_noun(itemName) + ".";
	}

	public String respondToOfferOfNotMissingItem() {
		return "Mam już tą zabawkę!";
	}

	public String respondToOfferOfNotNeededItem() {
		return "To nie najlepsza zabawka!";
	}

	@Override
	public String getName() {
		return "ToysCollector";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Anna";
	}
}
