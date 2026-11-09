/***************************************************************************
 *                   (C) Faumonia                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerCanEquipItemCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;

/**
 * Płaszcz pradawnych smoków.
 *
 * Terry przygotowuje materiały ze smoczych płaszczy i skór.
 * Josephine wykorzystuje płaszcz ze smoczych łusek jako bazę
 * i przez 24 godziny tworzy finalny przedmiot.
 */
public class AncientDragonCloak extends AbstractQuest {

	private static final String QUEST_SLOT = "ancient_dragon_cloak";
	private static final String DRAGON_SEARCH_QUEST = "find_dragons";

	private static final String TERRY = "Terry";
	private static final String JOSEPHINE = "Josephine";

	private static final int REQUIRED_LEVEL = 350;
	private static final int DRAGONS_TO_FIND = 11;

	private static final String GREEN_CLOAK = "szmaragdowy płaszcz smoczy";
	private static final String BONE_CLOAK = "kościany płaszcz smoczy";
	private static final String BLUE_CLOAK = "lazurowy płaszcz smoczy";
	private static final String RED_CLOAK = "karmazynowy płaszcz smoczy";
	private static final String BLACK_CLOAK = "czarny płaszcz smoczy";

	private static final String GREEN_SKIN = "skóra zielonego smoka";
	private static final String BLUE_SKIN = "skóra niebieskiego smoka";
	private static final String RED_SKIN = "skóra czerwonego smoka";
	private static final String BLACK_SKIN = "skóra czarnego smoka";
	private static final String GOLD_SKIN = "skóra złotego smoka";

	private static final String BASE_CLOAK = "płaszcz ze smoczych łusek";
	private static final String REWARD_CLOAK = "płaszcz pradawnych smoków";

	private static final int CLOAK_AMOUNT = 10;
	private static final int SKIN_AMOUNT = 5;
	private static final int SEWING_TIME_MINUTES = 24 * 60;

	private static final List<String> TRIGGERS = Arrays.asList(
		"pradawny",
		"pradawny płaszcz",
		"płaszcz pradawnych smoków",
		"pradawnych smoków"
	);

	/**
	 * FindDragons nie kończy się standardowym stanem "done".
	 * Quest zapisuje nazwy odnalezionych smoków w jednym slocie,
	 * dlatego sprawdzamy go dokładnie tak, jak robi to FindDragons.
	 */
	private static boolean hasCompletedDragonSearch(final Player player) {
		if (!player.hasQuest(DRAGON_SEARCH_QUEST)) {
			return false;
		}

		final String state = player.getQuest(DRAGON_SEARCH_QUEST);
		if (state == null) {
			return false;
		}

        if ("done".equals(state) || state.startsWith("done;")) { return true; }
        return new HashSet<String>(Arrays.asList(state.split(";"))).containsAll(Arrays.asList(
                "Gorath", "Velmord", "Saphiron", "Ardavyr", "Kharion", "Morthax",
                "Dravok", "Ignathar", "Faldurion", "Nalthar", "Atherion"));
	}

    /** The panel reads the same constants used by the native quest transitions. */
    public static Map<String, Integer> currentRequirements(Player player, String npcName) {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        String stage = player.getQuest(QUEST_SLOT, 0);
        if (TERRY.equals(npcName)) {
            String[] stages = {"green_cloaks", "bone_cloaks", "blue_cloaks", "red_cloaks", "black_cloaks"};
            String[] items = {GREEN_CLOAK, BONE_CLOAK, BLUE_CLOAK, RED_CLOAK, BLACK_CLOAK};
            for (int i = 0; i < stages.length; i++) {
                if (stages[i].equals(stage)) { result.put(items[i], CLOAK_AMOUNT); }
            }
            if ("skins".equals(stage)) {
                for (String item : Arrays.asList(GREEN_SKIN, BLUE_SKIN, RED_SKIN, BLACK_SKIN, GOLD_SKIN)) {
                    result.put(item, SKIN_AMOUNT);
                }
            }
        } else if (JOSEPHINE.equals(npcName) && "josephine".equals(stage)) {
            result.put(BASE_CLOAK, 1);
        }
        return result;
    }

	private static final class DragonSearchCompletedCondition implements ChatCondition {
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			return hasCompletedDragonSearch(player);
		}
	}

	private static final class AncientDragonCloakActiveCondition implements ChatCondition {
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			return player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT);
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Płaszcz pradawnych smoków",
			"Terry zna sposób na przygotowanie materiałów potrzebnych do stworzenia wyjątkowego smoczego płaszcza.",
			false
		);

		prepareTerry();
		prepareJosephine();
	}

	private void prepareTerry() {
		final SpeakerNPC terry = npcs.get(TERRY);

		/*
		 * Odkrycie questa.
		 *
		 * Nie podpinamy nowego zadania pod "task", ponieważ Terry ma już
		 * istniejące zadanie korzystające z tego słowa.
		 *
		 * Gracz spełniający wymagania zobaczy wskazówkę od razu po "hi".
		 */
		terry.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(terry.getName()),
				new QuestNotStartedCondition(QUEST_SLOT),
				new DragonSearchCompletedCondition(),
				new LevelGreaterThanCondition(REQUIRED_LEVEL - 1)
			),
			ConversationStates.ATTENDING,
			"Witaj ponownie. Widzę, że poznałeś już smoki Faumonii lepiej niż większość "
				+ "wojowników. Od dawna zbieram ich trofea i wiem, że odpowiednio połączone "
				+ "mogą posłużyć do stworzenia wyjątkowego okrycia. Podobno istnieje "
				+ "#pradawny #płaszcz, którego nie da się zdobyć podczas zwykłego polowania. "
				+ "Jeżeli interesuje cię takie wyzwanie, mogę opowiedzieć ci więcej.",
			null
		);

		/*
		 * Przypomnienie po rozpoczęciu zadania.
		 */
		terry.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(terry.getName()),
				new AncientDragonCloakActiveCondition()
			),
			ConversationStates.ATTENDING,
			"Witaj ponownie. Wciąż mamy sporo pracy przy płaszczu pradawnych smoków. "
				+ "Jeżeli nie pamiętasz kolejnego etapu, wspomnij #pradawny #płaszcz, "
				+ "a przypomnę ci, czego teraz potrzebuję.",
			null
		);

		/*
		 * Delikatna zapowiedź dla silniejszych graczy, którzy nie ukończyli jeszcze
		 * Poszukiwania Smoków. Dzięki temu gracz wie, że Terry ma później coś więcej.
		 */
		terry.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(terry.getName()),
				new QuestNotStartedCondition(QUEST_SLOT),
				new NotCondition(new DragonSearchCompletedCondition()),
				new LevelGreaterThanCondition(299)
			),
			ConversationStates.ATTENDING,
			"Witaj. Widzę, że nie jesteś już początkującym wojownikiem. "
				+ "Jeżeli kiedyś poznasz wszystkie smoki Faumonii i zdobędziesz ich najcenniejsze "
				+ "trofea, wróć do mnie. Krążą opowieści o przedmiocie zwanym "
				+ "#pradawny #płaszcz, ale jego stworzenie wymaga naprawdę wielkiego doświadczenia.",
			null
		);

		/*
		 * Jeśli gracz zna słowo wcześniej, Terry wyjaśnia wymagania zamiast
		 * pozwolić rozpocząć zadanie bez wymaganej progresji.
		 */
		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new NotCondition(new DragonSearchCompletedCondition())
			),
			ConversationStates.ATTENDING,
			"Słyszałeś już o pradawnym płaszczu? To nie jest przedmiot dla przypadkowego "
				+ "łowcy. Najpierw dobrze poznaj smoki rozsiane po Faumonii i odnajdź je wszystkie. "
				+ "Gdy zdobędziesz odpowiednie doświadczenie oraz płaszcz ze smoczych łusek, "
				+ "wróć do mnie. Wtedy będziemy mogli porozmawiać o czymś znacznie potężniejszym.",
			null
		);

		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new DragonSearchCompletedCondition(),
				new LevelLessThanCondition(REQUIRED_LEVEL)
			),
			ConversationStates.ATTENDING,
			"Odnalazłeś już wszystkie smoki, więc jesteś na dobrej drodze. "
				+ "Do pracy nad takim płaszczem potrzeba jednak wojownika o ogromnym doświadczeniu. "
				+ "Wróć do mnie, gdy osiągniesz 350 poziom.",
			null
		);

		/*
		 * Start właściwego zadania.
		 */
		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new DragonSearchCompletedCondition(),
				new LevelGreaterThanCondition(REQUIRED_LEVEL - 1)
			),
			ConversationStates.ATTENDING,
			"Słyszałem o sposobie na stworzenie płaszcza z trofeów wielu smoków. "
				+ "Nie da się jednak przygotować wszystkiego naraz. Zacznij od zdobycia "
				+ "10 szmaragdowych płaszczy smoczych. Gdy je zbierzesz, wróć do mnie "
				+ "i powiedz #pradawny #płaszcz. Josephine pobierze przy odbiorze gotowego płaszcza 300000 monet za nici i szycie.",
			new SetQuestAction(QUEST_SLOT, "green_cloaks")
		);

		/*
		 * Pięć partii płaszczy. Każdy rodzaj oddawany jest osobno,
		 * żeby gracz nie musiał nosić 50 płaszczy jednocześnie.
		 */
		addCloakStage(
			terry,
			"green_cloaks",
			GREEN_CLOAK,
			"bone_cloaks",
			"Szmaragdowe płaszcze są już u mnie; ich łuski zachowały wytrzymałość. Teraz potrzebuję "
				+ "10 kościanych płaszczy smoczych. Gdy je zbierzesz, powiedz #pradawny #płaszcz."
		);

		addCloakStage(
			terry,
			"bone_cloaks",
			BONE_CLOAK,
			"blue_cloaks",
			"Kościane płaszcze dołożę do poprzedniej partii. Kolejny materiał musi pochodzić z błękitnych smoków. "
				+ "Przynieś mi 10 lazurowych płaszczy smoczych i ponownie powiedz #pradawny #płaszcz."
		);

		addCloakStage(
			terry,
			"blue_cloaks",
			BLUE_CLOAK,
			"red_cloaks",
			"Lazurowe płaszcze mamy zebrane. Teraz przynieś 10 karmazynowych płaszczy smoczych. "
				+ "Gdy je zdobędziesz, powiedz #pradawny #płaszcz."
		);

		addCloakStage(
			terry,
			"red_cloaks",
			RED_CLOAK,
			"black_cloaks",
			"Karmazynowe płaszcze zamykają tę część przygotowań. Ostatnia partia musi pochodzić z czarnych smoków. "
				+ "Przynieś mi 10 czarnych płaszczy smoczych i powiedz #pradawny #płaszcz."
		);

		addCloakStage(
			terry,
			"black_cloaks",
			BLACK_CLOAK,
			"skins",
			"Czarne płaszcze dopełniają zestaw. Pozostały skóry do przygotowania materiału. "
				+ "\nSkóry - po 5 sztuk: zielonego, niebieskiego, czerwonego, "
				+ "czarnego i złotego smoka. Gdy zbierzesz wszystkie skóry, powiedz #pradawny #płaszcz."
		);

		/*
		 * Smocze skóry.
		 */
		final AndCondition hasAllSkins = new AndCondition(
			new PlayerHasItemWithHimCondition(GREEN_SKIN, SKIN_AMOUNT),
			new PlayerHasItemWithHimCondition(BLUE_SKIN, SKIN_AMOUNT),
			new PlayerHasItemWithHimCondition(RED_SKIN, SKIN_AMOUNT),
			new PlayerHasItemWithHimCondition(BLACK_SKIN, SKIN_AMOUNT),
			new PlayerHasItemWithHimCondition(GOLD_SKIN, SKIN_AMOUNT)
		);

		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, "skins"),
				hasAllSkins
			),
			ConversationStates.ATTENDING,
			"To wszystko, czego potrzebowałem. Przygotowałem materiał, ale wykonanie "
				+ "takiego płaszcza wymaga kogoś, kto naprawdę zna się na tej sztuce. "
				+ "Idź do Josephine w Fado. Zabierz ze sobą płaszcz ze smoczych łusek "
				+ "i powiedz jej #pradawny #płaszcz.",
			new MultipleActions(
				new DropItemAction(GREEN_SKIN, SKIN_AMOUNT),
				new DropItemAction(BLUE_SKIN, SKIN_AMOUNT),
				new DropItemAction(RED_SKIN, SKIN_AMOUNT),
				new DropItemAction(BLACK_SKIN, SKIN_AMOUNT),
				new DropItemAction(GOLD_SKIN, SKIN_AMOUNT),
				new SetQuestAction(QUEST_SLOT, "josephine")
			)
		);

		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, "skins"),
				new NotCondition(hasAllSkins)
			),
			ConversationStates.ATTENDING,
			"Skóry przyjmę dopiero w komplecie.\nSkóry - po 5 sztuk: zielonego, niebieskiego, "
				+ "czerwonego, czarnego i złotego smoka. Gdy będziesz miał komplet, "
				+ "powiedz #pradawny #płaszcz.",
			null
		);

		/*
		 * Terry przypomina o dalszych etapach.
		 */
		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new QuestInStateCondition(QUEST_SLOT, "josephine"),
			ConversationStates.ATTENDING,
			"Materiały są już przygotowane. Idź do Josephine w Fado z "
				+ "płaszczem ze smoczych łusek i powiedz jej #pradawny #płaszcz.",
			null
		);

		/*
		 * Po dodaniu timestampu slot ma postać sewing;TIMESTAMP,
		 * dlatego stan sprawdzamy na indeksie 0.
		 */
		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new QuestInStateCondition(QUEST_SLOT, 0, "sewing"),
			ConversationStates.ATTENDING,
			"Josephine ma już wszystko. Teraz pozostaje zaczekać, aż ukończy płaszcz. "
				+ "To ona powie ci, ile czasu pozostało.",
			null
		);

		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new QuestInStateCondition(QUEST_SLOT, "done"),
			ConversationStates.ATTENDING,
			"Masz już płaszcz pradawnych smoków. Niewielu wojowników zdoła zebrać "
				+ "tyle smoczych trofeów.",
			null
		);
	}

	private void addCloakStage(final SpeakerNPC terry,
			final String state,
			final String item,
			final String nextState,
			final String successText) {

		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, state),
				new PlayerHasItemWithHimCondition(item, CLOAK_AMOUNT)
			),
			ConversationStates.ATTENDING,
			successText,
			new MultipleActions(
				new DropItemAction(item, CLOAK_AMOUNT),
				new SetQuestAction(QUEST_SLOT, nextState)
			)
		);

		terry.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, state),
				new NotCondition(
					new PlayerHasItemWithHimCondition(item, CLOAK_AMOUNT)
				)
			),
			ConversationStates.ATTENDING,
			"Tę partię muszę otrzymać w całości. Potrzebny płaszcz: " + item + " - 10 sztuk. "
				+ "Wróć, gdy zbierzesz całą partię, i powiedz #pradawny #płaszcz.",
			null
		);
	}

	private void prepareJosephine() {
		final SpeakerNPC josephine = npcs.get(JOSEPHINE);

		/*
		 * Terry jasno wysyła gracza do Josephine z hasłem "pradawny płaszcz",
		 * więc nie podpinamy się pod jej istniejące słowo "ubrania".
		 */

		/*
		 * Przekazanie bazowego płaszcza i rozpoczęcie 24 godzin pracy.
		 */
		josephine.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, "josephine"),
				new PlayerHasItemWithHimCondition(BASE_CLOAK, 1)
			),
			ConversationStates.ATTENDING,
			"Terry przygotował materiał z przyniesionych trofeów. Dopasuję go do twojego "
				+ "płaszcza ze smoczych łusek i zszyję całość, ale taka praca wymaga czasu. "
				+ "Zostaw go u mnie i wróć za 24 godziny. Nici i szycie kosztują 300000 monet, płatne przy odbiorze. Wtedy ponownie powiedz #pradawny #płaszcz.",
			new MultipleActions(
				new DropItemAction(BASE_CLOAK, 1),
				new SetQuestAction(QUEST_SLOT, 0, "sewing"),
				new SetQuestToTimeStampAction(QUEST_SLOT, 1)
			)
		);

		josephine.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, "josephine"),
				new NotCondition(
					new PlayerHasItemWithHimCondition(BASE_CLOAK, 1)
				)
			),
			ConversationStates.ATTENDING,
			"Terry przekazał mi, czego potrzebuję, ale brakuje podstawy. "
				+ "Przynieś mi płaszcz ze smoczych łusek i powiedz #pradawny #płaszcz.",
			null
		);

		/*
		 * Josephine nadal szyje.
		 */
		josephine.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "sewing"),
				new NotCondition(
					new TimePassedCondition(
						QUEST_SLOT,
						1,
						SEWING_TIME_MINUTES
					)
				)
			),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(
				QUEST_SLOT,
				1,
				SEWING_TIME_MINUTES,
				"Jeszcze dopasowuję i zszywam smocze materiały. Niczego już nie przynoś, zamówienie jest u mnie. Wróć za "
			)
		);

		/*
		 * Gotowe, ale gracz nie ma miejsca na nagrodę.
		 */
		josephine.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "sewing"),
				new TimePassedCondition(
					QUEST_SLOT,
					1,
					SEWING_TIME_MINUTES
				),
				new NotCondition(
					new PlayerCanEquipItemCondition(REWARD_CLOAK)
				)
			),
			ConversationStates.ATTENDING,
			"Płaszcz jest już gotowy, ale nie masz miejsca, aby go odebrać. "
				+ "Zrób miejsce w ekwipunku i ponownie powiedz #pradawny #płaszcz.",
			null
		);

		/*
		 * Odbiór nagrody.
		 */
		josephine.add(
			ConversationStates.ANY,
			TRIGGERS,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "sewing"),
				new TimePassedCondition(
					QUEST_SLOT,
					1,
					SEWING_TIME_MINUTES
				),
				new PlayerCanEquipItemCondition(REWARD_CLOAK)
			),
			ConversationStates.ATTENDING,
			"Gotowe. Dziesiątki smoczych płaszczy, skóry pięciu smoków i twój "
				+ "płaszcz ze smoczych łusek stały się jednym przedmiotem. "
				+ "Szwy są ukończone. Odbierz płaszcz pradawnych smoków, nad którym wspólnie pracowaliśmy.",
			new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(
				new EquipItemAction(REWARD_CLOAK, 1, true),
				new SetQuestAction(QUEST_SLOT, "done")
			))
		);

		josephine.add(
			ConversationStates.ANY,
			TRIGGERS,
			new QuestInStateCondition(QUEST_SLOT, "done"),
			ConversationStates.ATTENDING,
			"Płaszcz pradawnych smoków został już ukończony. Dbaj o niego, "
				+ "bo drugi taki nie powstaje łatwo.",
			null
		);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> history = new ArrayList<String>();

		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}

		final String state = player.getQuest(QUEST_SLOT, 0);

		history.add("Terry zna sposób na stworzenie płaszcza pradawnych smoków. Josephine pobiera 300000 monet za nici i szycie przy odbiorze płaszcza.");

		if ("green_cloaks".equals(state)) {
			history.add("Muszę przynieść Terry'emu 10 szmaragdowych płaszczy smoczych.");
			return history;
		}

		history.add("Dostarczyłem 10 szmaragdowych płaszczy smoczych.");

		if ("bone_cloaks".equals(state)) {
			history.add("Muszę przynieść Terry'emu 10 kościanych płaszczy smoczych.");
			return history;
		}

		history.add("Dostarczyłem 10 kościanych płaszczy smoczych.");

		if ("blue_cloaks".equals(state)) {
			history.add("Muszę przynieść Terry'emu 10 lazurowych płaszczy smoczych.");
			return history;
		}

		history.add("Dostarczyłem 10 lazurowych płaszczy smoczych.");

		if ("red_cloaks".equals(state)) {
			history.add("Muszę przynieść Terry'emu 10 karmazynowych płaszczy smoczych.");
			return history;
		}

		history.add("Dostarczyłem 10 karmazynowych płaszczy smoczych.");

		if ("black_cloaks".equals(state)) {
			history.add("Muszę przynieść Terry'emu 10 czarnych płaszczy smoczych.");
			return history;
		}

		history.add("Dostarczyłem 10 czarnych płaszczy smoczych.");

		if ("skins".equals(state)) {
			history.add("Muszę przynieść Terry'emu po 5 skór zielonego, niebieskiego, "
				+ "czerwonego, czarnego i złotego smoka.");
			return history;
		}

		history.add("Dostarczyłem Terry'emu skóry pięciu rodzajów smoków.");

		if ("josephine".equals(state)) {
			history.add("Muszę udać się do Josephine w Fado z płaszczem ze smoczych łusek.");
			return history;
		}

		if ("sewing".equals(state)) {
			history.add("Josephine szyje płaszcz pradawnych smoków. Praca zajmuje 24 godziny.");
			return history;
		}

		if ("done".equals(state)) {
			history.add("Josephine ukończyła płaszcz pradawnych smoków. Odebrałem nagrodę.");
		}

		return history;
	}

	@Override
	public List<String> getHint(final Player player) {
		final List<String> hints = new ArrayList<String>();

		if (isCompleted(player)) {
			return hints;
		}

		if (!hasCompletedDragonSearch(player)) {
			hints.add("Najpierw odnajdź wszystkie 11 smoków i zdobądź płaszcz ze smoczych łusek.");
			return hints;
		}

		if (player.getLevel() < REQUIRED_LEVEL) {
			hints.add("Do rozpoczęcia zadania na płaszcz pradawnych smoków potrzebujesz 350 poziomu.");
			return hints;
		}

		if (!player.hasQuest(QUEST_SLOT)) {
			hints.add("Porozmawiaj z Terrym w jaskini Semos. Wspomni ci o pradawnych smokach.");
			return hints;
		}

		final String state = player.getQuest(QUEST_SLOT, 0);

		if ("josephine".equals(state)) {
			hints.add("Udaj się do Josephine w Fado z płaszczem ze smoczych łusek.");
		} else if ("sewing".equals(state)) {
			hints.add("Josephine pracuje nad płaszczem. Wróć do niej po upływie 24 godzin.");
		} else {
			hints.add("Porozmawiaj z Terrym i wspomnij pradawny płaszcz, aby sprawdzić kolejny etap zadania.");
		}

		return hints;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public int getMinLevel() {
		return REQUIRED_LEVEL;
	}

	@Override
	public String getName() {
		return "Płaszcz pradawnych smoków";
	}

	@Override
	public String getNPCName() {
		return TERRY;
	}
}
