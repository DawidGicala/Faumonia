package games.stendhal.server.core.rp.quest;

import static org.junit.Assert.*;
import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;
import games.stendhal.common.QuestProgress.State;
import games.stendhal.common.Constants;
import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.actions.query.ProgressStatusQueryAction;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ProgressStatusEvent;
import games.stendhal.server.maps.quests.*;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.server.db.TransactionPool;
import marauroa.server.db.command.DBCommandQueue;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.Test;
import java.util.Collections;

/** Uses real Player slots, inventory, quest conditions and NPC state machines, in memory. */
public class PilotQuestProgressTest {
    private static HerbsForCarmen carmen;
    private static HelpSedro sedro;
    private static BowsForOuchit ouchit;
    private Player player;

    @BeforeClass public static void init() {
        assertNull("This harness must never use a database", TransactionPool.get());
        new RPClassGenerator().createRPClasses();
        for (String name : new String[] {"Carmen", "Sedro", "Ouchit", "Karl"}) {
            NPCList.get().add(new SpeakerNPC(name));
        }
        carmen = new HerbsForCarmen(); sedro = new HelpSedro(); ouchit = new BowsForOuchit();
        StendhalQuestSystem.get().loadQuest(carmen);
        StendhalQuestSystem.get().loadQuest(sedro);
        StendhalQuestSystem.get().loadQuest(ouchit);
        assertNotNull(StendhalQuestSystem.get().getQuest("HelpSedro"));
    }

    @Before public void player() {
        player = new Player(new RPObject());
        player.put("name", "PilotLocal");
        player.put("base_hp", 100); player.put("hp", 100);
        player.put("atk", 10); player.put("def", 10);
        player.put("level", 3); player.put("atk_xp", 0); player.put("def_xp", 0);
        for (String slot : Constants.CARRYING_SLOTS) { player.addSlot(slot); }
        for (String slot : new String[] {"!quests", "!kills", "!tutorial", "!visited"}) {
            player.addSlot(slot);
            player.getSlot(slot).add(new RPObject());
        }
        player.update();
        for (String name : new String[] {"Carmen", "Sedro", "Ouchit", "Karl"}) {
            NPCList.get().get(name).getEngine().setCurrentState(ConversationStates.ATTENDING);
        }
    }

    @AfterClass public static void shutdown() {
        assertNull(TransactionPool.get());
        // Existing actions enqueue audit logs; with no transaction pool they are skipped.
        DBCommandQueue.get().finish();
    }

    private void save(IQuest quest, String state) { player.setKeyedSlot("!quests", quest.getSlotName(), state); }
    private QuestProgress read(IQuest quest) { return PilotQuestProgress.read(quest, player); }
    private void carry(String name, int count, String slot) {
        StackableItem item = new StackableItem(name, "test", "test", Collections.<String, String>emptyMap());
        item.setQuantity(count);
        player.getSlot(slot).add(item);
    }
    private void say(String npc, String text) {
        NPCList.get().get(npc).clearEvents();
        assertTrue(npc + " did not understand: " + text, NPCList.get().get(npc).getEngine().step(player, text));
    }
    private String lastSpeech(SpeakerNPC npc) {
        java.util.List<RPEvent> events = npc.events();
        for (int i = events.size() - 1; i >= 0; i--) {
            if (Events.PUBLIC_TEXT.equals(events.get(i).getName())) { return events.get(i).get("text"); }
        }
        throw new AssertionError(npc.getName() + " emitted no speech for the last engine step");
    }
    private Objective objective(QuestProgress progress, String id) {
        for (Objective objective : progress.getObjectives()) {
            if (id.equals(objective.getId())) { return objective; }
        }
        throw new AssertionError(id);
    }

    @Test public void undiscoveredQuestsDoNotExposeSnapshots() {
        assertNull(read(carmen)); assertNull(read(sedro)); assertNull(read(ouchit));
        assertNull(StendhalQuestSystem.get().getVisibleQuest(player, "Łuki dla Ouchita"));
    }

    @Test public void snapshotsOnlyReadTheRequestingPlayersState() {
        save(carmen, "arandula=3"); carry("arandula", 2, "bag");
        String before = player.toString();
        for (int i = 0; i < 10; i++) { read(carmen); }
        assertEquals(before, player.toString());
        Player other = new Player(new RPObject());
        other.addSlot("!quests");
        other.getSlot("!quests").add(new RPObject());
        assertNull(PilotQuestProgress.read(carmen, other));
    }

    @Test public void carmenSeparatesDeliveredFromCarriedAndCountsMultipleSlots() {
        save(carmen, "arandula=3"); carry("arandula", 1, "bag"); carry("arandula", 1, "lhand");
        QuestProgress progress = read(carmen);
        assertEquals(State.ACTIVE, progress.getState());
        assertEquals(2, objective(progress, "arandula").getCurrent());
        assertEquals(2, objective(progress, "arandula").getCarried());
        carry("arandula", 1, "rhand");
        assertEquals(State.READY_TO_DELIVER, read(carmen).getState());
        assertEquals(2, objective(read(carmen), "arandula").getCurrent());
    }

    @Test public void bankItemsDoNotMakeDeliveryReady() {
        save(ouchit, "polano"); player.addSlot("bank"); carry("polano", 10, "bank");
        assertEquals(State.ACTIVE, read(ouchit).getState());
        assertEquals(0, read(ouchit).getObjectives().get(0).getCurrent());
    }

    @Test public void malformedCollectionIsUnknownWithoutRepairingSave() {
        for (String state : new String[] {"", "arandula=x", "arandula=6", "arandula=-1",
                "unknown=2", "arandula=1;arandula=2", "arandula=1;"}) {
            save(carmen, state);
            assertEquals(state, State.UNKNOWN, read(carmen).getState());
            assertEquals(state, player.getQuest(carmen.getSlotName()));
        }
    }

    @Test public void rejectedAndCompletedStatesAreNotActiveObjectives() {
        save(carmen, "rejected"); assertEquals(State.DECLINED, read(carmen).getState());
        save(sedro, "done;1700000000000");
        assertEquals(State.COMPLETED, read(sedro).getState());
        assertTrue(read(sedro).getObjectives().isEmpty());
        save(ouchit, "done"); assertEquals(State.COMPLETED, read(ouchit).getState());
    }

    @Test public void sedroDoesNotCountKillsBeforeAcceptance() {
        player.setSoloKill("gnom"); player.setSharedKill("gnom");
        say("Sedro", "zadanie"); say("Sedro", "tak");
        assertEquals(0, objective(read(sedro), "gnom:total").getCurrent());
        player.setSharedKill("gnom");
        assertEquals(1, objective(read(sedro), "gnom:total").getCurrent());
    }

    @Test public void mixedSoloAndSharedRequirementsAgreeWithRewardCondition() {
        save(sedro, "start;szczur,2,3,0,0");
        for (int i = 0; i < 5; i++) { player.setSharedKill("szczur"); }
        assertEquals(State.ACTIVE, read(sedro).getState());
        assertEquals(5, objective(read(sedro), "szczur:total").getRequired());
        player.setSoloKill("szczur"); player.setSoloKill("szczur");
        assertEquals(State.READY_TO_DELIVER, read(sedro).getState());
        assertTrue(new KilledForQuestCondition("help_sedro", 1).fire(player, null, null));
    }

    @Test public void soloKillsCanFulfilAllOfAnAdditionalRequirement() {
        save(sedro, "start;szczur,0,5,0,0");
        for (int i = 0; i < 8; i++) { player.setSoloKill("szczur"); }
        assertEquals(State.READY_TO_DELIVER, read(sedro).getState());
        assertEquals(5, objective(read(sedro), "szczur:total").getCurrent());
    }

    @Test public void malformedKillRecordDoesNotInventProgress() {
        for (String state : new String[] {"start", "start;", "start;szczur,0,5,x,0",
                "start;szczur,0,-5,0,0", "other;szczur,0,5,0,0"}) {
            save(sedro, state); assertEquals(State.UNKNOWN, read(sedro).getState());
        }
    }

    @Test public void woodDoesNotRevealHairStageAndLosingItemsRevokesReadiness() {
        save(ouchit, "polano"); carry("polano", 10, "bag");
        QuestProgress progress = read(ouchit);
        assertEquals(State.READY_TO_DELIVER, progress.getState());
        assertFalse(progress.toWire().toString().contains("Karl"));
        player.drop("polano", 1);
        assertEquals(State.ACTIVE, read(ouchit).getState());
    }

    @Test public void hairStageRoutesToKarlOrOuchitAccordingToInventory() {
        save(ouchit, "hair"); assertEquals("Karl", read(ouchit).getNextNPC());
        carry("koński włos", 1, "bag");
        assertEquals("Ouchit", read(ouchit).getNextNPC());
        assertEquals(State.READY_TO_DELIVER, read(ouchit).getState());
    }

    @Test public void carmenAcceptsPartialDeliveryThroughActualDialogue() {
        say("Carmen", "leczniczych"); say("Carmen", "tak");
        carry("arandula", 2, "bag"); say("Carmen", "zapasy"); say("Carmen", "arandula");
        assertEquals(0, player.getNumberOfEquipped("arandula"));
        assertEquals(2, objective(read(carmen), "arandula").getCurrent());
        assertEquals(State.ACTIVE, read(carmen).getState());
    }

    @Test public void carmenListsCanonicalPolishNamesWithActualRemainingCounts() {
        say("Carmen", "leczniczych"); say("Carmen", "tak");
        SpeakerNPC npc = NPCList.get().get("Carmen");
        String speech = lastSpeech(npc);
        for (java.util.Map.Entry<String, Integer> item : HerbsForCarmen.getRequiredItems().entrySet()) {
            assertTrue(speech, speech.contains("#" + item.getKey() + " - " + item.getValue()));
        }
        assertFalse(speech.contains("sprig"));
        assertFalse(speech.contains("spring"));

        carry("arandula", 2, "bag"); carry("jabłko", 3, "bag");
        say("Carmen", "zapasy"); say("Carmen", "arandula"); say("Carmen", "jabłko");
        say("Carmen", "nie");
        String state = player.getQuest(HerbsForCarmen.QUEST_SLOT);
        say("Carmen", "zapasy");
        speech = lastSpeech(npc);
        assertTrue(speech, speech.contains("#arandula - 3"));
        assertFalse(speech, speech.contains("jabłko"));
        assertFalse(speech, speech.contains("sprig"));
        String history = carmen.getHistory(player).toString();
        assertTrue(history, history.contains("arandula - 3"));
        assertFalse(history, history.contains("jabłko"));
        assertFalse(history, history.contains("sprig"));
        assertFalse(history, history.contains("#"));
        assertEquals("Displaying the remaining list must not change the quest save", state,
                player.getQuest(HerbsForCarmen.QUEST_SLOT));
    }

    @Test public void carmenMissingAndAlreadyDeliveredResponsesStayPolishWithoutTakingItems() {
        say("Carmen", "leczniczych"); say("Carmen", "tak"); say("Carmen", "zapasy");
        SpeakerNPC npc = NPCList.get().get("Carmen");
        String state = player.getQuest(HerbsForCarmen.QUEST_SLOT);
        say("Carmen", "arandula");
        assertEquals("Nie masz tego składnika przy sobie. Poszukaj go i wróć, kiedy coś zbierzesz.", lastSpeech(npc));
        assertEquals(state, player.getQuest(HerbsForCarmen.QUEST_SLOT));
        carry("arandula", 5, "bag"); say("Carmen", "arandula");
        state = player.getQuest(HerbsForCarmen.QUEST_SLOT);
        carry("arandula", 1, "bag"); say("Carmen", "arandula");
        assertEquals("Ten składnik mam już w potrzebnej ilości. Dziękuję.", lastSpeech(npc));
        assertEquals(1, player.getNumberOfEquipped("arandula"));
        assertEquals(state, player.getQuest(HerbsForCarmen.QUEST_SLOT));
    }

    @Test public void ouchitKarlRouteAndRewardCannotBeRepeated() {
        say("Ouchit", "zadanie"); say("Ouchit", "tak"); say("Ouchit", "tak");
        carry("polano", 10, "bag"); say("Ouchit", "polano");
        assertEquals("hair", player.getQuest("bows_ouchit"));
        assertEquals(0, player.getNumberOfEquipped("polano"));
        assertEquals("Karl", read(ouchit).getNextNPC());
        say("Karl", "Ouchit");
        assertEquals("Ouchit", read(ouchit).getNextNPC());
        say("Ouchit", "włosie");
        assertEquals(State.COMPLETED, read(ouchit).getState());
        assertEquals(1, player.getNumberOfEquipped("łuk ouchita"));
        assertEquals(500, player.getNumberOfEquipped("strzała"));
        assertEquals(300, player.getNumberOfEquipped("strzała żelazna"));
        int xp = player.getXP();
        NPCList.get().get("Ouchit").getEngine().step(player, "włosie");
        assertEquals(xp, player.getXP());
        assertEquals(1, player.getNumberOfEquipped("łuk ouchita"));
    }

    @Test public void eventRoundTripUsesExistingMarauroaListEncoding() {
        save(carmen, "arandula=3");
        QuestProgress progress = read(carmen);
        ProgressStatusEvent event = new ProgressStatusEvent("Otwarte zadania", "Zioła dla Carmen", "", Collections.<String>emptyList());
        event.setQuestProgress(progress);
        assertEquals(progress.toWire(), QuestProgress.fromWire(event.getList("quest_progress")).toWire());
        assertFalse(event.get("quest_progress").contains("arandula=3"));
        assertFalse(event.get("quest_progress").contains("!quests"));
    }

    @Test public void queryingUndiscoveredQuestEmitsNoDetails() {
        RPAction action = new RPAction();
        action.put("progress_type", "Otwarte zadania"); action.put("item", "Zioła dla Carmen");
        player.clearEvents();
        new ProgressStatusQueryAction().onAction(player, action);
        assertTrue(player.events().isEmpty());
    }

    @Test public void querySendsPrivateProjectionAndHandlesMalformedLegacySave() {
        RPAction action = new RPAction();
        action.put("progress_type", "Otwarte zadania"); action.put("item", "Zioła dla Carmen");
        action.put("passive", "1");
        save(carmen, "arandula=3"); player.clearEvents();
        new ProgressStatusQueryAction().onAction(player, action);
        assertEquals(1, player.events().size());
        RPEvent event = player.events().get(0);
        assertTrue(event.has("passive"));
        assertEquals("HerbsForCarmen", QuestProgress.fromWire(event.getList("quest_progress")).getId());
        save(carmen, "arandula=invalid"); player.clearEvents();
        new ProgressStatusQueryAction().onAction(player, action);
        assertEquals(State.UNKNOWN, QuestProgress.fromWire(player.events().get(0).getList("quest_progress")).getState());
        assertEquals("[]", player.events().get(0).get("data"));
    }

    @Test public void legacyStateCanBeReloadedWithoutMigration() {
        save(sedro, "start;szczur,0,5,0,0"); player.setSoloKill("szczur");
        QuestProgress before = read(sedro);
        Player restored = new Player((RPObject) player.clone());
        assertEquals(before.toWire(), PilotQuestProgress.read(sedro, restored).toWire());
    }

    @Test public void carmenFullDeliveryGrantsOriginalRewardOnlyOnce() {
        say("Carmen", "leczniczych"); say("Carmen", "tak"); say("Carmen", "zapasy");
        for (java.util.Map.Entry<String, Integer> item : HerbsForCarmen.getRequiredItems().entrySet()) {
            carry(item.getKey(), item.getValue(), "bag"); say("Carmen", item.getKey());
        }
        assertEquals(State.COMPLETED, read(carmen).getState());
        assertEquals(500, player.getXP());
        assertEquals(50, player.getNumberOfEquipped("mały eliksir"));
        say("Carmen", "zadanie");
        assertEquals(500, player.getXP());
        assertEquals(50, player.getNumberOfEquipped("mały eliksir"));
    }

    @Test public void sedroFullHuntGrantsOriginalRewardOnlyOnce() {
        say("Sedro", "zadanie"); say("Sedro", "tak");
        for (int i = 0; i < 5; i++) { player.setSharedKill("szczur"); }
        for (int i = 0; i < 3; i++) { player.setSoloKill("szczur jaskiniowy"); }
        player.setSoloKill("gnom"); player.setSharedKill("panna gnom");
        assertEquals(State.READY_TO_DELIVER, read(sedro).getState());
        say("Sedro", "zadanie");
        assertEquals(State.COMPLETED, read(sedro).getState());
        assertEquals(500, player.getXP());
        assertEquals(10, player.getNumberOfEquipped("zwój semos"));
        assertEquals(50, player.getNumberOfEquipped("mięso"));
        say("Sedro", "zadanie");
        assertEquals(500, player.getXP());
        assertEquals(50, player.getNumberOfEquipped("mięso"));
    }

}
