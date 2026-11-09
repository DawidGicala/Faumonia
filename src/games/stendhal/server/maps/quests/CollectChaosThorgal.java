/* $Id: CollectChaosThorgal.java,v 1.25 2024/05/18 12:01:18 davvids Exp $ */
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

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.List;


public class CollectChaosThorgal extends AbstractQuest {

	private static final String QUEST_SLOT = "chaos_tunnels";



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Thorgal");

		npc.add(ConversationStates.IDLE,
        ConversationPhrases.GREETING_MESSAGES,
        new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
        ConversationStates.ATTENDING,
        "Cześć. Jak widzisz, wciąż jestem w trakcie badań nad tunelami chaosu. Dziękuję za Twoją pomoc. Pamiętaj, że jeśli będziesz potrzebować zwojów do tych tuneli, zawsze możesz do mnie wrócić.",
        null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
        new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT)),
        ConversationStates.ATTENDING,
        "Cześć. Jak widzisz, wciąż jestem w trakcie badań nad tunelami chaosu. Dziękuję za Twoją pomoc. Pamiętaj, że jeśli będziesz potrzebować zwojów do tych tuneli, zawsze możesz do mnie wrócić.",
        null);

npc.add(ConversationStates.IDLE,
        ConversationPhrases.GREETING_MESSAGES,
        new AndCondition(new GreetingMatchesNameCondition(npc.getName()),new QuestNotCompletedCondition(QUEST_SLOT),new NotCondition(new AndCondition(new PlayerHasItemWithHimCondition("hełm chaosu"),new PlayerHasItemWithHimCondition("zbroja chaosu"),new PlayerHasItemWithHimCondition("spodnie chaosu"),new PlayerHasItemWithHimCondition("buty chaosu"),new PlayerHasItemWithHimCondition("płaszcz chaosu"),new PlayerHasItemWithHimCondition("tarcza chaosu"),new PlayerHasItemWithHimCondition("miecz chaosu")))),
        ConversationStates.ATTENDING,
        "Im dalej zapuszczam się w tunele chaosu, tym trudniej utrzymać sprzęt w dobrym stanie. Potrzebuję kompletnego zestawu: hełmu, zbroi, spodni, butów, płaszcza, tarczy i miecza chaosu. Przynieś wszystkie siedem rzeczy naraz, a odwdzięczę się zwojami i udostępnię mój handel.",
        new SetQuestAction(QUEST_SLOT, "start"));
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
        new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),new QuestNotCompletedCondition(QUEST_SLOT),new NotCondition(new AndCondition(new PlayerHasItemWithHimCondition("hełm chaosu"),new PlayerHasItemWithHimCondition("zbroja chaosu"),new PlayerHasItemWithHimCondition("spodnie chaosu"),new PlayerHasItemWithHimCondition("buty chaosu"),new PlayerHasItemWithHimCondition("płaszcz chaosu"),new PlayerHasItemWithHimCondition("tarcza chaosu"),new PlayerHasItemWithHimCondition("miecz chaosu")))),
        ConversationStates.ATTENDING,
        "Im dalej zapuszczam się w tunele chaosu, tym trudniej utrzymać sprzęt w dobrym stanie. Potrzebuję kompletnego zestawu: hełmu, zbroi, spodni, butów, płaszcza, tarczy i miecza chaosu. Przynieś wszystkie siedem rzeczy naraz, a odwdzięczę się zwojami i udostępnię mój handel.",
        new SetQuestAction(QUEST_SLOT, "start"));

npc.add(ConversationStates.IDLE,
        ConversationPhrases.GREETING_MESSAGES,
        new AndCondition(
            new GreetingMatchesNameCondition(npc.getName()),
            new QuestNotCompletedCondition(QUEST_SLOT),
            new PlayerHasItemWithHimCondition("hełm chaosu"),
            new PlayerHasItemWithHimCondition("zbroja chaosu"), 
            new PlayerHasItemWithHimCondition("spodnie chaosu"), 
            new PlayerHasItemWithHimCondition("buty chaosu"),
            new PlayerHasItemWithHimCondition("płaszcz chaosu"), 
			new PlayerHasItemWithHimCondition("tarcza chaosu"), 
            new PlayerHasItemWithHimCondition("miecz chaosu") 
        ),
        ConversationStates.ATTENDING,
        "Cały zestaw jest na miejscu; mogę znów planować dalszą drogę przez tunele. Oddaję ci cztery zwoje chaosu i przygotowaną nagrodę za dostawę. Od teraz możesz też pytać o moją ofertę zwojów.",
        new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(
        new SetQuestAction(QUEST_SLOT, "done"),
        new IncreaseXPAction(500000),
        new IncreaseAtkXPAction(20000),
        new IncreaseDefXPAction(20000),
		new DropItemAction("hełm chaosu", 1),
		new DropItemAction("zbroja chaosu", 1),
		new DropItemAction("spodnie chaosu", 1),
		new DropItemAction("buty chaosu", 1),
		new DropItemAction("płaszcz chaosu", 1),
		new DropItemAction("tarcza chaosu", 1),
		new DropItemAction("miecz chaosu", 1),
		new EquipItemAction("zwój chaosu", 4, true)
    )));
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
        new AndCondition(
            new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
            new QuestNotCompletedCondition(QUEST_SLOT),
            new PlayerHasItemWithHimCondition("hełm chaosu"),
            new PlayerHasItemWithHimCondition("zbroja chaosu"), 
            new PlayerHasItemWithHimCondition("spodnie chaosu"), 
            new PlayerHasItemWithHimCondition("buty chaosu"),
            new PlayerHasItemWithHimCondition("płaszcz chaosu"), 
			new PlayerHasItemWithHimCondition("tarcza chaosu"), 
            new PlayerHasItemWithHimCondition("miecz chaosu") 
        ),
        ConversationStates.ATTENDING,
        "Cały zestaw jest na miejscu; mogę znów planować dalszą drogę przez tunele. Oddaję ci cztery zwoje chaosu i przygotowaną nagrodę za dostawę. Od teraz możesz też pytać o moją ofertę zwojów.",
        new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(
        new SetQuestAction(QUEST_SLOT, "done"),
        new IncreaseXPAction(500000),
        new IncreaseAtkXPAction(20000),
        new IncreaseDefXPAction(20000),
		new DropItemAction("hełm chaosu", 1),
		new DropItemAction("zbroja chaosu", 1),
		new DropItemAction("spodnie chaosu", 1),
		new DropItemAction("buty chaosu", 1),
		new DropItemAction("płaszcz chaosu", 1),
		new DropItemAction("tarcza chaosu", 1),
		new DropItemAction("miecz chaosu", 1),
		new EquipItemAction("zwój chaosu", 4, true)
    )));

	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Podróżnik w tunelach chaosu",
				"Thorgal przygotowuje się do dalszych wypraw w tunele chaosu pod Semos. Przynieś wymagany zestaw ekwipunku; za pomoc udostępni ci handel zwojami.",
				true);
		step_1();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("Muszę znaleźć: hełm chaosu, zbroję chaosu, spodnie chaosu, buty chaosu, płaszcz chaosu, tarczę chaosu oraz miecz chaosu i dostarczyć je Thorgalowi.");
			if (isCompleted(player)) {
				res.add("Dostarczyłem Thorgalowi siedem elementów chaosu. Otrzymałem 500000 XP postaci, 20000 XP ataku, 20000 XP obrony i cztery związane zwoje chaosu oraz dostęp do jego sklepu.");
			}
			return res;
	}
	
	@Override
	public String getName() {
		return "CollectChaosThorgal";
	}
	
	@Override
	public int getMinLevel() {
		return 200;
	}

	@Override
	public String getNPCName() {
		return "Thorgal";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_DUNGEONS;
	}

    public static List<String> getRequiredItems() {
        return java.util.Collections.unmodifiableList(java.util.Arrays.asList("hełm chaosu","zbroja chaosu","spodnie chaosu","buty chaosu","płaszcz chaosu","tarcza chaosu","miecz chaosu"));
    }

}
