/* $Id: StuffForBaldemar.java,v 1.26 2012/04/24 17:01:18 kymara Exp $ */
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

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * QUEST: The mithril shield forging.
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Baldemar, mithrilbourgh elite wizard, will forge a mithril shield.
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Baldemar tells you about shield.
 * <li> He offers to forge a mithril shield for you if you bring him what he
 * needs.
 * <li> You give him all he asks for.
 * <li> Baldemar checks if you have ever killed a black giant alone, or not
 * <li> Baldemar forges the shield for you
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> mithril shield
 * <li>95000 XP
 * </ul>
 * 
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.
 * </ul>
 */
public class StuffForBaldemar extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {
	private static Map<Integer, ItemData> neededItems = initneededitems();
	private static Map<Integer, ItemData> initneededitems() {
		neededItems = new TreeMap<Integer, ItemData>();
		ItemData data = new ItemData("sztabka mithrilu", 
									REQUIRED_MITHRIL_BAR, 
									"Mithril będzie podstawą tarczy. Brakuje mi jeszcze: ", 
									". Bez tego nie powstanie właściwy stop.");
		neededItems.put(1, data);
		data = new ItemData("obsydian",	REQUIRED_OBSIDIAN, 
				"Kamienie rozkruszę na proszek i połączę z mithrilem. Brakuje mi jeszcze: ", 
				".");
		neededItems.put(2, data);
		data = new ItemData("diament",	REQUIRED_DIAMOND , 
				"Kamienie rozkruszę na proszek i połączę z mithrilem. Brakuje mi jeszcze: ", 
				".");
		neededItems.put(3, data);
		
		
		data = new ItemData("szmaragd",	REQUIRED_EMERALD , 
				"Kamienie rozkruszę na proszek i połączę z mithrilem. Brakuje mi jeszcze: ", 
				".");
		neededItems.put(4, data);
		
		data = new ItemData("rubin",	REQUIRED_CARBUNCLE , 
				"Kamienie rozkruszę na proszek i połączę z mithrilem. Brakuje mi jeszcze: ", 
				".");
		neededItems.put(5, data);
		
		data = new ItemData("szafir",	REQUIRED_SAPPHIRE, 
				"Kamienie rozkruszę na proszek i połączę z mithrilem. Brakuje mi jeszcze: ", 
				".");
		neededItems.put(6, data);
		int i = 7;
		data = new ItemData("czarna tarcza",	REQUIRED_BLACK_SHIELD , 
				"Potrzebuję ", 
				", aby uformować konstrukcję dla twojej nowej tarczy.");
		neededItems.put(i, data);
		
		 i = 8;
		data = new ItemData("magiczna tarcza płytowa",	REQUIRED_MAGIC_PLATE_SHIELD , 
				"Potrzebuję ", 
				". Wykorzystam ją na części nowej tarczy.");
		neededItems.put(i, data);
	
		 i = 9;
			data = new ItemData("sztabka złota",	REQUIRED_GOLD_BAR , 
					"Potrzebuję ", 
					", aby przetopić z mithrilem i żelazem.");
			neededItems.put(i, data);

		 i = 10;
			data = new ItemData("żelazo",	REQUIRED_IRON , 
					"Potrzebuję ", 
					", aby przetopić z mithrilem i złotem.");
			neededItems.put(i, data);
		 i = 11;
			data = new ItemData("czarna perła",	REQUIRED_BLACK_PEARL , 
					"Potrzebuję ", 
					", żeby po zmieleniu nadać tarczy połysk.");
			neededItems.put(i, data);

		 i = 12;
			data = new ItemData("shuriken",	REQUIRED_SHURIKEN , 
					"Potrzebuję ", 
					", aby przetopić z mithrilem, złotem i żelazem. To 'tajny' składnik, o którym wiemy tylko ty i ja.");
			neededItems.put(i, data);
			i = 13;
			data = new ItemData("kolorowe kulki",	REQUIRED_MARBLES , 
					"Mój syn potrzebuje nowych zabawek. Wciąż potrzebuję ", 
					".");
			neededItems.put(i, data);

			data = new ItemData("zima zaklęta w kuli",	REQUIRED_SNOWGLOBE, 
					"Mam słabość do tych błyskotek z Athor. Do kompletu brakuje: ", 
					".");
			neededItems.put(14, data);
		return neededItems;
	}

	
	protected static final class ItemData {

		private int neededAmount;
		private final String itemName;
		private final String itemPrefix;
		private final String itemSuffix;
		private final int requiredAmount;

		public ItemData(final String name, final int needed, final String prefix, final String suffix) {
			this.requiredAmount = needed;
			this.neededAmount = needed;
			this.itemName = name;
			this.itemPrefix = prefix;
			this.itemSuffix = suffix;
		}

		public int getStillNeeded() {
			return neededAmount;
		}

		public void setAmount(final int needed) {
			neededAmount = needed;
			
		}

		public String getName() {
			return itemName;
		}

		public String getPrefix() {
			return itemPrefix;
		}

		public String getSuffix() {
			return itemSuffix;
		}

		public void subAmount(final String string) {
			subAmount(Integer.parseInt(string));
			
		}

		String getAnswer() {
			return itemPrefix
				+ Grammar.quantityplnoun(
						neededAmount, itemName, "")
				+ itemSuffix;
		}

		public int getRequired() {
			return requiredAmount;
		}

		public int getAlreadyBrought() {
			return requiredAmount - neededAmount;
		}

		public void subAmount(final int amount) {
			neededAmount -= amount;
			
		}

		public void resetAmount() {
			neededAmount = requiredAmount;
			
		}
	}

	private static final int REQUIRED_MITHRIL_BAR = 400;

	private static final int REQUIRED_OBSIDIAN = 20;

	private static final int REQUIRED_DIAMOND = 5;

	private static final int REQUIRED_EMERALD = 80;
	
	private static final int REQUIRED_CARBUNCLE = 60;

	private static final int REQUIRED_SAPPHIRE = 40;

	private static final int REQUIRED_BLACK_SHIELD = 1;

	private static final int REQUIRED_MAGIC_PLATE_SHIELD = 1;

	private static final int REQUIRED_GOLD_BAR = 200;

	private static final int REQUIRED_IRON = 200;

	private static final int REQUIRED_BLACK_PEARL = 100;

	private static final int REQUIRED_SHURIKEN = 1000;
	
	private static final int REQUIRED_MARBLES = 150;

	private static final int REQUIRED_SNOWGLOBE = 1;

	private static final String I_WILL_NEED_MANY_THINGS = "Będę potrzebował wiele, wiele rzeczy: "
							+ REQUIRED_MITHRIL_BAR
							+ " sztabek mithrilu, "
							+ REQUIRED_OBSIDIAN
							+ " obsydian, "
							+ REQUIRED_DIAMOND
							+ " diament, "
							+ REQUIRED_EMERALD
							+ " szmaragdów," 
							+ REQUIRED_CARBUNCLE
							+ " rubinów, "
							+ REQUIRED_SAPPHIRE
							+ " szafirów, "
							+ REQUIRED_BLACK_SHIELD
							+ " czarną tarczę, "
							+ REQUIRED_MAGIC_PLATE_SHIELD
							+ " magiczną tarczę płytową, "
							+ REQUIRED_GOLD_BAR
							+ " sztabek złota, "
							+ REQUIRED_IRON
							+ " sztabek żelaza, "
							+ REQUIRED_BLACK_PEARL
							+ " czarnych pereł, " 
							+ REQUIRED_SHURIKEN
							+ " shuriken, "
							+ REQUIRED_MARBLES
							+ " kolorowych kulek oraz "
							+ REQUIRED_SNOWGLOBE
							+ " szklaną kule, w której jest zaklęta zima. Wróć, gdy będziesz miał wszystko #dokładnie w tej kolejności!";
	
	private static final int REQUIRED_MINUTES = 2880;

	private static final String QUEST_SLOT = "mithrilshield_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Baldemar");

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            null,
            ConversationStates.QUEST_OFFERED,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Mithril jest lekki, lecz dobra tarcza wymaga czegoś więcej niż samego metalu. Potrafię połączyć go z kamieniami i innymi składnikami, jeśli dostarczysz je do kuźni i dowiedziesz swojej siły. Chcesz zlecić mi taką pracę?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Tarcza została już oddana w twoje ręce. Daj mi teraz odpocząć po tej pracy; taki stop nie wybacza pośpiechu.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Twoje zamówienie nadal czeka w kuźni. Mogę przypomnieć brakujące materiały albo powiedzieć, jak idzie wykuwanie.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Dziękuję. Przyjmuję materiały kolejno: " + games.stendhal.server.core.rp.quest.CollectionQuestProgress.itemText(StuffForBaldemar.this.getCollectionItems()) + ". Oddane materiały zostaną w kuźni. Możesz przynosić je partiami, a ja przypomnę ci, czego jeszcze brakuje.");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0;0;0;0;0;0;0;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            "Rozumiem. Nie przyjmuję zamówienia bez twojej zgody. Wróć, jeśli zdecydujesz się pomóc.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Jak już powiedziałem. Musisz mi dać dokładnie w tej kolejności.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Baldemar");

		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					
					int idx1 = 1;
					for (ItemData itemdata : neededItems.values()) {
							itemdata.resetAmount();
							itemdata.subAmount(tokens[idx1]);
							idx1++;
					}

					boolean missingSomething = false;

					int size = neededItems.size();
					for (int idx = 1; !missingSomething && idx <= size; idx++) {
						ItemData itemData = neededItems.get(idx);
						missingSomething = proceedItem(player, raiser,
								itemData);
					}
					
					if (player.hasKilledSolo("latający złoty smok") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Teraz wykuję tarczę. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowa.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilledSolo("latający złoty smok") && !missingSomething) {
							raiser.say("Tą tarczę mogą dostać ci co zabili latającego złotego smoka bez pomocy innych osób.");
						}

						StringBuilder sb = new StringBuilder(30);
						sb.append("start");
						for (ItemData id : neededItems.values()) {
							sb.append(";");
							sb.append(id.getAlreadyBrought());
						}
						player.setQuest(QUEST_SLOT, sb.toString());
							
					}
				}

	
				private boolean proceedItem(final Player player,
						final EventRaiser engine, final ItemData itemData) {
					if (itemData.getStillNeeded() > 0) {
						
						if (player.isEquipped(itemData.getName(), itemData.getStillNeeded())) {
							player.drop(itemData.getName(), itemData.getStillNeeded());
							itemData.setAmount(0);
						} else {
							final int amount = player.getNumberOfEquipped(itemData.getName());
							if (amount > 0) {
								player.drop(itemData.getName(), amount);
								itemData.subAmount(amount);
							}

							engine.say(itemData.getAnswer());
							return true;
						}
					}
					return false;
				}
			});
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					
					int idx1 = 1;
					for (ItemData itemdata : neededItems.values()) {
							itemdata.resetAmount();
							itemdata.subAmount(tokens[idx1]);
							idx1++;
					}

					boolean missingSomething = false;

					int size = neededItems.size();
					for (int idx = 1; !missingSomething && idx <= size; idx++) {
						ItemData itemData = neededItems.get(idx);
						missingSomething = proceedItem(player, raiser,
								itemData);
					}
					
					if (player.hasKilledSolo("latający złoty smok") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Teraz wykuję tarczę. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowa.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilledSolo("latający złoty smok") && !missingSomething) {
							raiser.say("Tą tarczę mogą dostać ci co zabili latającego złotego smoka bez pomocy innych osób.");
						}

						StringBuilder sb = new StringBuilder(30);
						sb.append("start");
						for (ItemData id : neededItems.values()) {
							sb.append(";");
							sb.append(id.getAlreadyBrought());
						}
						player.setQuest(QUEST_SLOT, sb.toString());
							
					}
				}

	
				private boolean proceedItem(final Player player,
						final EventRaiser engine, final ItemData itemData) {
					if (itemData.getStillNeeded() > 0) {
						
						if (player.isEquipped(itemData.getName(), itemData.getStillNeeded())) {
							player.drop(itemData.getName(), itemData.getStillNeeded());
							itemData.setAmount(0);
						} else {
							final int amount = player.getNumberOfEquipped(itemData.getName());
							if (amount > 0) {
								player.drop(itemData.getName(), amount);
								itemData.subAmount(amount);
							}

							engine.say(itemData.getAnswer());
							return true;
						}
					}
					return false;
				}
			});

		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					
					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Tarcza nie jest jeszcze gotowa; takiego stopu nie chcę obrabiać w pośpiechu. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Tarcza z mithrilu jest gotowa. Każdy składnik trafił na swoje miejsce, a teraz od twojej wprawy zależy, jak dobrze będzie cię chronić. Weź ją i pamiętaj o trudzie, który kosztowała ta praca.");
					player.addXP(10000000);
					player.addKarma(500);
					player.addatk_xp(500000);
					player.adddef_xp(750000);
					final Item mithrilshield = SingletonRepository.getEntityManager().getItem("tarcza z mithrilu");
					mithrilshield.setBoundTo(player.getName());
					player.equipOrPutOnGround(mithrilshield);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					
					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Tarcza nie jest jeszcze gotowa; takiego stopu nie chcę obrabiać w pośpiechu. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Tarcza z mithrilu jest gotowa. Każdy składnik trafił na swoje miejsce, a teraz od twojej wprawy zależy, jak dobrze będzie cię chronić. Weź ją i pamiętaj o trudzie, który kosztowała ta praca.");
					player.addXP(10000000);
					player.addKarma(500);
					player.addatk_xp(500000);
					player.adddef_xp(750000);
					final Item mithrilshield = SingletonRepository.getEntityManager().getItem("tarcza z mithrilu");
					mithrilshield.setBoundTo(player.getName());
					player.equipOrPutOnGround(mithrilshield);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("forge", "missing", "wykuć", "brakuje"),
            new QuestStateStartsWithCondition(QUEST_SLOT, "start;"),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final int neededMithrilBar = REQUIRED_MITHRIL_BAR
							- Integer.parseInt(tokens[1]);
					final int neededObsidian = REQUIRED_OBSIDIAN
							- Integer.parseInt(tokens[2]);
					final int neededDiamond = REQUIRED_DIAMOND
							- Integer.parseInt(tokens[3]);
					final int neededEmerald = REQUIRED_EMERALD
							- Integer.parseInt(tokens[4]);
					final int neededCarbuncle = REQUIRED_CARBUNCLE
							- Integer.parseInt(tokens[5]);
					final int neededSapphire = REQUIRED_SAPPHIRE
							- Integer.parseInt(tokens[6]);
					final int neededBlackShield = REQUIRED_BLACK_SHIELD
							- Integer.parseInt(tokens[7]);
					final int neededMagicPlateShield = REQUIRED_MAGIC_PLATE_SHIELD
							- Integer.parseInt(tokens[8]);
					final int neededGoldBars = REQUIRED_GOLD_BAR
							- Integer.parseInt(tokens[9]);
					final int neededIron = REQUIRED_IRON
							- Integer.parseInt(tokens[10]);
					final int neededBlackPearl = REQUIRED_BLACK_PEARL
							- Integer.parseInt(tokens[11]);
					final int neededShuriken = REQUIRED_SHURIKEN
							- Integer.parseInt(tokens[12]);
					final int neededMarbles = REQUIRED_MARBLES
							- Integer.parseInt(tokens[13]);
					final int neededSnowglobe = REQUIRED_SNOWGLOBE
							- Integer.parseInt(tokens[14]);
					
					raiser.say("Będę potrzebował " + neededMithrilBar + " sztabek mithrilu, "
							+ neededObsidian + " obsydianów, "
							+ neededDiamond + " diamentu, "
							+ neededEmerald + " szmaragdów, "
							+ neededCarbuncle + " rubinów, "
							+ neededSapphire + " szafirów, "
							+ neededBlackShield + " czarną tarczę, "
							+ neededMagicPlateShield + " magiczną tarczę płytową, "
							+ neededGoldBars + " sztabek złota, "
							+ neededIron + " sztabek żelaza, "
							+ neededBlackPearl + " czarnych pereł, "
							+ neededShuriken + " shuriken, "
							+ neededMarbles + " kolorowych kulek oraz "
							+ neededSnowglobe + " zimy zaklętej w kuli.");
				}
			});

	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Rzeczy dla Baldemara",
				"Baldemar z magicznego miasta potrafi wykonać tarczę z mithrilu. Poznaj listę materiałów i warunki zlecenia, zanim rozpoczniesz kosztowne przygotowania.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "StuffForBaldemar";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }
	
	@Override
	public int getMinLevel() {
		return 400;
	}

	@Override
	public String getNPCName() {
		return "Baldemar";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CAVES;
	}



    public String getCollectionKind(){return "sequence";}
    public java.util.Map<String,Integer> getCollectionItems(){java.util.Map<String,Integer> m=new java.util.LinkedHashMap<String,Integer>(); for(ItemData i:neededItems.values())m.put(i.itemName,i.requiredAmount); return m;}
    public String getCollectionHint(){return "Baldemar przyjmuje materiały w podanej kolejności, również partiami. Oprócz dostawy wymaga samodzielnego pokonania latającego złotego smoka. Wykucie tarczy trwa dwa dni; zimę zaklętą w kuli można zdobyć przez pomoc Rudolfowi na Athor.";}
    public String getCollectionKill(){return "latający złoty smok";}
public boolean getCollectionSolo(){return true;}
public int getCollectionWait(){return REQUIRED_MINUTES;}

}
