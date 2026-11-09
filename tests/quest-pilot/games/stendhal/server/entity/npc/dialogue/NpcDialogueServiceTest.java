package games.stendhal.server.entity.npc.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.Constants;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.NpcDialogueSnapshot.Reply;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.HerbsForCarmen;
import games.stendhal.server.maps.quests.HelpSedro;
import games.stendhal.server.maps.quests.BowsForOuchit;
import games.stendhal.server.maps.semos.city.HealerNPC;
import games.stendhal.server.maps.semos.village.SedroNPC;
import games.stendhal.server.maps.semos.tavern.BowAndArrowSellerNPC;
import games.stendhal.server.maps.ados.forest.FarmerNPC;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.server.db.TransactionPool;
import marauroa.server.db.command.DBCommandQueue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Real map NPCs, quest actions and parser, with only world storage and turn clock isolated. */
public class NpcDialogueServiceTest {
    private static LocalRules rules;
    private LocalZone zone;
    private LocalPlayer player;
    private SpeakerNPC carmen;
    private SpeakerNPC sedro;
    private SpeakerNPC ouchit;
    private SpeakerNPC karl;
    private final Map<String, SpeakerNPC> originals = new HashMap<String, SpeakerNPC>();

    private static final class LocalRules extends StendhalRPRuleProcessor {
        private final StendhalRPRuleProcessor previous;
        int turn = 100;
        LocalRules() { previous = instance; instance = this; }
        @Override public int getTurn() { return turn; }
        void restore() { instance = previous; }
    }
    private static final class LocalZone extends StendhalRPZone {
        final List<games.stendhal.server.entity.item.Item> dropped = new ArrayList<games.stendhal.server.entity.item.Item>();
        LocalZone() { super("dialogue_test"); }
        @Override public synchronized void add(RPObject object) {
            assignRPObjectID(object);
            ((Entity) object).onAdded(this);
            if (object instanceof games.stendhal.server.entity.item.Item) { dropped.add((games.stendhal.server.entity.item.Item) object); }
            if (object instanceof SpeakerNPC) { NPCList.get().add((SpeakerNPC) object); }
            if (object instanceof Player) {
                getPlayers().add((Player) object);
                getPlayerAndFriends().add((Player) object);
            }
        }
        @Override public synchronized void modify(RPObject object) { }
    }
    private static final class LocalPlayer extends Player {
        boolean disconnectedForTest;
        LocalPlayer() { super(new RPObject()); }
        @Override public boolean isDisconnected() { return disconnectedForTest; }
    }
    @BeforeClass public static void init() {
        assertNull(TransactionPool.get());
        new RPClassGenerator().createRPClasses();
        rules = new LocalRules();
    }
    @Before public void createWorld() {
        rules.turn = 100;
        for (String name : new String[] {"Carmen", "Sedro", "Ouchit", "Karl"}) {
            originals.put(name, NPCList.get().remove(name));
        }
        zone = new LocalZone();
        new HealerNPC().configureZone(zone, Collections.<String, String>emptyMap());
        new SedroNPC().configureZone(zone, Collections.<String, String>emptyMap());
        new BowAndArrowSellerNPC().configureZone(zone, Collections.<String, String>emptyMap());
        new FarmerNPC().configureZone(zone, Collections.<String, String>emptyMap());
        carmen = NPCList.get().get("Carmen");
        sedro = NPCList.get().get("Sedro");
        ouchit = NPCList.get().get("Ouchit");
        karl = NPCList.get().get("Karl");
        new HerbsForCarmen().addToWorld();
        new HelpSedro().addToWorld();
        new BowsForOuchit().addToWorld();
        player = player("DialogueLocal");
    }
    private LocalPlayer player(String name) {
        LocalPlayer result = new LocalPlayer();
        result.put("name", name);
        result.put("base_hp", 100); result.put("hp", 100);
        result.put("atk", 10); result.put("def", 10);
        result.put("level", 3); result.put("atk_xp", 0); result.put("def_xp", 0);
        for (String slot : Constants.CARRYING_SLOTS) { result.addSlot(slot); }
        for (String slot : new String[] {"!quests", "!kills", "!tutorial", "!visited"}) {
            result.addSlot(slot); result.getSlot(slot).add(new RPObject());
        }
        result.update();
        zone.add(result);
        result.setPosition(carmen.getX() + 1, carmen.getY());
        return result;
    }
    @After public void restoreNPCs() {
        for (SpeakerNPC npc : new SpeakerNPC[] {carmen, sedro, ouchit, karl}) {
            if (npc != null) { npc.setAttending(null); NPCList.get().remove(npc.getName()); }
        }
        for (SpeakerNPC original : originals.values()) {
            if (original != null) { NPCList.get().add(original); }
        }
    }
    @AfterClass public static void finish() {
        rules.restore();
        assertNull(TransactionPool.get());
        DBCommandQueue.get().finish();
    }
    private NpcDialogueSnapshot latest(Player target) {
        NpcDialogueSnapshot result = null;
        for (RPEvent event : target.events()) {
            if (NpcDialogueSnapshot.EVENT.equals(event.getName())) {
                result = NpcDialogueSnapshot.fromWire(event.getList("data"));
            }
        }
        assertNotNull("No valid private snapshot", result);
        return result;
    }
    private List<NpcDialogueSnapshot> snapshots(Player target) {
        List<NpcDialogueSnapshot> result = new ArrayList<NpcDialogueSnapshot>();
        for (RPEvent event : target.events()) {
            if (NpcDialogueSnapshot.EVENT.equals(event.getName())) {
                result.add(NpcDialogueSnapshot.fromWire(event.getList("data")));
            }
        }
        return result;
    }
    private RPAction reply(NpcDialogueSnapshot snapshot, String id) {
        RPAction action = new RPAction();
        action.put("type", NpcDialogueSnapshot.ACTION);
        action.put("session", snapshot.getSession());
        action.put("revision", Long.toString(snapshot.getRevision()));
        action.put("reply", id);
        return action;
    }
    private void openCarmenOffer() {
        greet(carmen); click("quest"); click("know"); click("ingredients");
        rules.turn += 34;
    }
    private NpcDialogueSnapshot click(String id) {
        assertTrue("Unavailable choice: " + id, has(latest(player), id));
        new NpcDialogueAction().onAction(player, reply(latest(player), id));
        return latest(player);
    }
    private NpcDialogueSnapshot type(String text) {
        NpcDialogueSnapshot snapshot = latest(player);
        RPAction action = new RPAction();
        action.put("session", snapshot.getSession()); action.put("revision", Long.toString(snapshot.getRevision()));
        action.put("text", text);
        new NpcDialogueAction().onAction(player, action);
        return latest(player);
    }
    private boolean has(NpcDialogueSnapshot snapshot, String id) {
        for (Reply reply : snapshot.getReplies()) { if (id.equals(reply.getId())) { return true; } }
        return false;
    }
    private String topic(NpcDialogueSnapshot snapshot, String id) {
        for (Reply reply : snapshot.getReplies()) { if (id.equals(reply.getId())) { return reply.getTopic(); } }
        throw new AssertionError("No reply " + id);
    }
    private void greet(SpeakerNPC npc) {
        player.setPosition(npc.getX() + 1, npc.getY());
        for (SpeakerNPC previous : new SpeakerNPC[] {carmen, sedro, ouchit, karl}) {
            if (previous != npc && previous.getAttending() == player) { previous.preLogic(); }
        }
        npc.listenTo(player, "hi");
    }
    private void carry(String itemName, int quantity) {
        StackableItem item = new StackableItem(itemName, "test", "test", Collections.<String, String>emptyMap());
        item.setQuantity(quantity); player.getSlot("bag").add(item);
    }
    private void assertReward(String name, double amount) {
        double actual = 0;
        for (NpcDialogueSnapshot.Reward reward : latest(player).getRewards()) {
            if (name.equals(reward.getName())) { actual += reward.getAmount(); assertFalse(reward.getSprite().isEmpty()); }
        }
        assertEquals(name, amount, actual, 0.000001);
    }

    @Test public void rewardReceiptDistinguishesItemsPlacedOnGroundWhenInventoryIsFull() {
        for (String slot : Constants.CARRYING_SLOTS) {
            player.getSlot(slot).setCapacity(1);
            StackableItem filler = new StackableItem("filler", "test", "test", Collections.<String, String>emptyMap());
            player.getSlot(slot).add(filler);
        }
        carmen.add(ConversationStates.ATTENDING, "receiptpilot", null, ConversationStates.ATTENDING, "Proszę.",
                games.stendhal.server.entity.npc.action.DialogueRewardAction.item("mały eliksir", 50));
        greet(carmen); type("receiptpilot");
        assertEquals(0, player.getNumberOfEquipped("mały eliksir"));
        assertEquals(1, zone.dropped.size());
        assertEquals(50, ((StackableItem) zone.dropped.get(0)).getQuantity());
        assertReward("mały eliksir", 50); assertTrue(latest(player).getRewards().get(0).isOnGround());
        type("praca"); assertTrue(latest(player).getRewards().isEmpty());
    }

    @Test public void refusedExperienceAtNumericLimitIsNotFalselyReportedAsAwarded() {
        player.setXP(Integer.MAX_VALUE - 100);
        player.put("atk_xp", Integer.MAX_VALUE - 100); player.put("def_xp", Integer.MAX_VALUE - 100); player.update();
        carmen.add(ConversationStates.ATTENDING, "receiptpilot", null, ConversationStates.ATTENDING, "Proszę.",
                new games.stendhal.server.entity.npc.action.MultipleActions(
                        games.stendhal.server.entity.npc.action.DialogueRewardAction.xp(500),
                        games.stendhal.server.entity.npc.action.DialogueRewardAction.attackXP(500),
                        games.stendhal.server.entity.npc.action.DialogueRewardAction.defenseXP(500)));
        greet(carmen); type("receiptpilot");
        assertEquals(Integer.MAX_VALUE - 100, player.getXP()); assertTrue(latest(player).getRewards().isEmpty());
    }

    @Test public void sourceMetadataAndSnapshotFollowActualCarmenActions() {
        greet(carmen);
        NpcDialogueSnapshot initial = latest(player);
        assertTrue(initial.isOpened()); assertEquals(carmen.getDescription(), initial.getDescription());
        assertEquals("npc/welcomernpc", initial.getSprite());
        assertFalse(has(initial, "ingredients"));
        click("quest"); click("know"); click("ingredients");
        NpcDialogueSnapshot accepted = click("accept");
        assertFalse(accepted.isOpened());
        assertTrue(player.hasQuest("herbs_for_carmen"));
        assertTrue(accepted.getSpeech().contains("zapasy"));
        assertTrue(has(accepted, "supplies")); assertFalse(has(accepted, "accept"));
        assertEquals("Przyniosę ci składniki.", accepted.getPlayerText());
    }

    @Test public void replayCannotAcceptTwiceAndRefreshesRevision() {
        openCarmenOffer();
        RPAction accept = reply(latest(player), "accept");
        new NpcDialogueAction().onAction(player, accept);
        double karma = player.getKarma();
        long revision = latest(player).getRevision();
        new NpcDialogueAction().onAction(player, accept);
        assertEquals(karma, player.getKarma(), 0.001);
        assertTrue(latest(player).getRevision() > revision);
        assertFalse(latest(player).getFeedback().isEmpty());
        assertEquals("", latest(player).getPlayerText());
    }

    @Test public void unknownTypedInputAndLegacyChatRetainSpeechAndStayUsable() {
        greet(carmen);
        String speech = latest(player).getSpeech();
        NpcDialogueSnapshot unknown = type("xyzzy 123 qqq");
        assertEquals(speech, unknown.getSpeech());
        assertTrue(unknown.getFeedback().startsWith("Brak odpowiedzi"));
        assertTrue(unknown.isActive());
        carmen.listenTo(player, "praca");
        assertFalse(latest(player).getSpeech().equals(speech));
        assertEquals("praca", latest(player).getPlayerText());
        assertFalse(latest(player).isOpened());
    }

    @Test public void validSilentTransitionHasNeutralStatus() {
        carmen.add(ConversationStates.ATTENDING, "ciszapilota", null, ConversationStates.ATTENDING, null, null);
        greet(carmen);
        String speech = latest(player).getSpeech();
        NpcDialogueSnapshot result = type("ciszapilota");
        assertEquals(speech, result.getSpeech());
        assertEquals("Przyjęto odpowiedź.", result.getFeedback());
    }

    @Test public void secondPlayerCannotReadOrControlFirstPlayersConversation() {
        openCarmenOffer();
        LocalPlayer other = player("OtherLocal");
        carmen.listenTo(other, "hi");
        assertSame(player, carmen.getAttending());
        assertTrue(snapshots(other).isEmpty());
        new NpcDialogueAction().onAction(other, reply(latest(player), "accept"));
        assertFalse(player.hasQuest("herbs_for_carmen"));
        assertFalse(other.hasQuest("herbs_for_carmen"));
        assertTrue(snapshots(other).isEmpty());
    }

    @Test public void changedStateAndQuestInvalidateOldChoicesBeforeExecution() {
        openCarmenOffer();
        RPAction accept = reply(latest(player), "accept");
        player.setQuest("herbs_for_carmen", "done");
        new NpcDialogueAction().onAction(player, accept);
        assertEquals("done", player.getQuest("herbs_for_carmen"));
        assertFalse(has(latest(player), "accept"));
        assertFalse(has(latest(player), "decline"));
        // A fresh revision must not make the old unconditional Engine accept safe again.
        new NpcDialogueAction().onAction(player, reply(latest(player), "accept"));
        assertEquals("done", player.getQuest("herbs_for_carmen"));
        carmen.setCurrentState(ConversationStates.ATTENDING);
        new NpcDialogueAction().onAction(player, reply(latest(player), "accept"));
        assertEquals("done", player.getQuest("herbs_for_carmen"));
        assertFalse(has(latest(player), "accept"));
    }

    @Test public void unavailableAndMixedActionCannotDispatchACommand() {
        greet(carmen);
        RPAction invalid = reply(latest(player), "accept");
        new NpcDialogueAction().onAction(player, invalid);
        assertFalse(player.hasQuest("herbs_for_carmen"));
        RPAction mixed = reply(latest(player), "ingredients"); mixed.put("text", "tak");
        new NpcDialogueAction().onAction(player, mixed);
        assertEquals(ConversationStates.ATTENDING, carmen.getEngine().getCurrentState());
        assertFalse(latest(player).getFeedback().isEmpty());
    }

    @Test public void deliveryUsesRealInventoryAndCannotReplayOrReuseHint() {
        openCarmenOffer(); click("accept");
        carry("arandula", 2); click("hints");
        RPAction oldHint = reply(latest(player), "arandula");
        click("supplies");
        new NpcDialogueAction().onAction(player, oldHint);
        assertEquals(2, player.getNumberOfEquipped("arandula"));
        RPAction delivery = reply(latest(player), "give_arandula");
        new NpcDialogueAction().onAction(player, delivery);
        assertEquals(0, player.getNumberOfEquipped("arandula"));
        assertTrue(player.getQuest("herbs_for_carmen").contains("arandula=3"));
        new NpcDialogueAction().onAction(player, delivery);
        assertTrue(player.getQuest("herbs_for_carmen").contains("arandula=3"));
    }

    @Test public void fullCarmenDeliveryPublishesFinalStateAfterOriginalReward() {
        openCarmenOffer(); click("accept");
        for (Map.Entry<String, Integer> entry : HerbsForCarmen.getRequiredItems().entrySet()) {
            carry(entry.getKey(), entry.getValue());
        }
        click("supplies");
        RPAction last = null;
        for (String id : new String[] {"give_arandula", "give_borowik", "give_apple", "give_polano", "give_pieczarka"}) {
        assertEquals("Deliveries must require their explicit button, never an informational link", "", topic(latest(player), id));
            last = reply(latest(player), id);
            new NpcDialogueAction().onAction(player, last);
        }
        assertEquals("done", player.getQuest(HerbsForCarmen.QUEST_SLOT));
        assertEquals(50, player.getNumberOfEquipped("mały eliksir"));
        assertEquals(500, player.getXP());
        assertFalse(has(latest(player), "supplies"));
        assertFalse(has(latest(player), "give_pieczarka"));
        assertEquals(5, latest(player).getRewards().size());
        assertReward("mały eliksir", 50); assertReward("XP postaci", 500); assertReward("XP ataku", 500);
        assertReward("XP obrony", 500); assertReward("Karma", 10);
        new NpcDialogueAction().onAction(player, last);
        assertEquals(50, player.getNumberOfEquipped("mały eliksir"));
        assertEquals(500, player.getXP());
        rules.turn += 34; click("back"); assertTrue(latest(player).getRewards().isEmpty());
    }

    @Test public void departureInvalidatesSessionWithoutRepeatingPriorPlayerEcho() {
        greet(carmen); click("job");
        RPAction old = reply(latest(player), "quest");
        player.setPosition(carmen.getX() + 20, carmen.getY());
        carmen.preLogic();
        assertFalse(latest(player).isActive()); assertEquals("", latest(player).getPlayerText());
        assertNull(carmen.getAttending());
        new NpcDialogueAction().onAction(player, old);
        assertFalse(player.hasQuest("herbs_for_carmen"));
    }

    @Test public void expiryAndDisconnectRejectClickBeforeNextNpcTick() {
        openCarmenOffer();
        RPAction expired = reply(latest(player), "accept");
        rules.turn += 101;
        new NpcDialogueAction().onAction(player, expired);
        assertFalse(latest(player).isActive()); assertFalse(player.hasQuest("herbs_for_carmen"));
        carmen.preLogic();
        openCarmenOffer();
        player.disconnectedForTest = true;
        new NpcDialogueAction().onAction(player, reply(latest(player), "accept"));
        assertFalse(latest(player).isActive()); assertFalse(player.hasQuest("herbs_for_carmen"));
    }

    @Test public void sedroRealAcceptanceAndCompletionRemainOneReward() {
        greet(sedro); click("quest"); click("accept");
        assertTrue(player.getQuest("help_sedro").startsWith("start;"));
        for (int i = 0; i < 5; i++) { player.setSharedKill("szczur"); }
        for (int i = 0; i < 3; i++) { player.setSharedKill("szczur jaskiniowy"); }
        player.setSoloKill("gnom"); player.setSharedKill("panna gnom");
        click("back");
        RPAction finish = reply(latest(player), "quest");
        new NpcDialogueAction().onAction(player, finish);
        assertTrue(player.getQuest("help_sedro").startsWith("done;"));
        int xp = player.getXP();
        assertEquals(50, player.getNumberOfEquipped("mięso"));
        assertEquals(12, latest(player).getRewards().size());
        for (String name : new String[] {"sztylecik", "maczeta", "pałasz", "drewniana tarcza"}) { assertReward(name, 1); }
        assertReward("zwój semos", 10); assertReward("eliksir", 10); assertReward("duży eliksir", 5);
        assertReward("butelka wody", 20); assertReward("mięso", 50);
        assertReward("XP postaci", 500); assertReward("XP ataku", 500); assertReward("XP obrony", 500);
        new NpcDialogueAction().onAction(player, finish);
        assertEquals(xp, player.getXP()); assertEquals(50, player.getNumberOfEquipped("mięso"));
    }

    @Test public void hearingRangeIsPreservedWhileConversationRemainsOpen() {
        openCarmenOffer();
        player.setPosition(carmen.getX() + 6, carmen.getY());
        NpcDialogueSnapshot before = latest(player);
        new NpcDialogueAction().onAction(player, reply(before, "accept"));
        assertFalse(player.hasQuest("herbs_for_carmen"));
        assertTrue(latest(player).isActive());
        assertEquals("Podejdź bliżej, aby odpowiedzieć.", latest(player).getFeedback());
        assertTrue(latest(player).getRevision() > before.getRevision());
        player.setPosition(carmen.getX() + 1, carmen.getY());
        click("accept");
        assertTrue(player.hasQuest("herbs_for_carmen"));
    }

    @Test public void chatBucketAndMuteAlsoApplyToPanelWithFreshFeedback() {
        openCarmenOffer();
        while (player.getChatBucket().checkAndAdd(50)) { /* fill existing rate limiter */ }
        NpcDialogueSnapshot before = latest(player);
        new NpcDialogueAction().onAction(player, reply(before, "accept"));
        assertFalse(player.hasQuest("herbs_for_carmen"));
        assertTrue(latest(player).getFeedback().startsWith("Zwolnij"));
        assertTrue(latest(player).getRevision() > before.getRevision());
        rules.turn += 34;
        player.setQuest("gag", Long.toString(System.currentTimeMillis() + 60000));
        new NpcDialogueAction().onAction(player, reply(latest(player), "accept"));
        assertFalse(player.hasQuest("herbs_for_carmen"));
        assertTrue(latest(player).getFeedback().startsWith("Zostałeś uciszony"));
    }

    @Test public void wireRoundTripRejectsMalformedChoicesAndVersion() {
        greet(carmen);
        NpcDialogueSnapshot snapshot = latest(player);
        assertEquals(snapshot.toWire(), NpcDialogueSnapshot.fromWire(snapshot.toWire()).toWire());
        List<String> data = snapshot.toWire(); data.set(0, "unknown");
        assertNull(NpcDialogueSnapshot.fromWire(data));
        data = snapshot.toWire(); data.set(13, "999999");
        assertNull(NpcDialogueSnapshot.fromWire(data));
        data = snapshot.toWire(); data.set(2, "-1");
        assertNull(NpcDialogueSnapshot.fromWire(data));
    }

    private void startWoodQuest() {
        greet(ouchit);
        assertFalse(has(latest(player), "karl")); assertFalse(has(latest(player), "hair"));
        click("quest"); click("interest"); click("accept");
        assertEquals("polano", player.getQuest(BowsForOuchit.QUEST_SLOT));
        assertFalse(has(latest(player), "karl")); assertFalse(has(latest(player), "hair"));
        assertEquals("polano", topic(latest(player), "wood"));
    }
    private void reachHairStage() {
        startWoodQuest(); carry("polano", 10); type("zadanie");
        assertEquals("", topic(latest(player), "wood"));
        click("wood");
        assertEquals("hair", player.getQuest(BowsForOuchit.QUEST_SLOT));
        assertEquals(0, player.getNumberOfEquipped("polano"));
        assertTrue(has(latest(player), "karl")); assertFalse(has(latest(player), "hair"));
    }

    @Test public void ouchitKarlFullRouteRecoversLostHairAndCannotDuplicateReward() {
        reachHairStage();
        greet(karl);
        assertEquals(karl.getDescription(), latest(player).getDescription());
        assertTrue(has(latest(player), "ouchit"));
        click("ouchit");
        assertEquals(1, player.getNumberOfEquipped("koński włos"));
        assertFalse(has(latest(player), "ouchit"));
        player.drop("koński włos");
        rules.turn += 34;
        type("praca");
        assertTrue(has(latest(player), "ouchit"));
        click("ouchit");
        assertEquals(1, player.getNumberOfEquipped("koński włos"));
        greet(ouchit);
        assertEquals("", topic(latest(player), "hair"));
        RPAction delivery = reply(latest(player), "hair");
        int beforeXP = player.getXP();
        new NpcDialogueAction().onAction(player, delivery);
        assertEquals("done", player.getQuest(BowsForOuchit.QUEST_SLOT));
        assertEquals(0, player.getNumberOfEquipped("koński włos"));
        assertEquals(1, player.getNumberOfEquipped("łuk ouchita"));
        assertEquals(500, player.getNumberOfEquipped("strzała"));
        assertEquals(300, player.getNumberOfEquipped("strzała żelazna"));
        assertEquals(beforeXP + 1000, player.getXP());
        assertEquals(5, latest(player).getRewards().size());
        assertReward("łuk ouchita", 1); assertReward("strzała", 500); assertReward("strzała żelazna", 300);
        assertReward("XP postaci", 1000); assertReward("Karma", 10);
        new NpcDialogueAction().onAction(player, delivery);
        assertEquals(beforeXP + 1000, player.getXP());
        assertEquals(1, player.getNumberOfEquipped("łuk ouchita"));
        assertFalse(has(latest(player), "hair"));
        greet(karl); assertFalse(has(latest(player), "ouchit"));
    }

    @Test public void karlOnlyOffersHairInTheActualStageAndRejectsAnotherPlayer() {
        greet(karl); assertFalse(has(latest(player), "ouchit"));
        player.setQuest(BowsForOuchit.QUEST_SLOT, "polano");
        type("praca"); assertFalse(has(latest(player), "ouchit"));
        player.setQuest(BowsForOuchit.QUEST_SLOT, "hair");
        type("praca"); assertTrue(has(latest(player), "ouchit"));
        LocalPlayer other = player("KarlOther");
        other.setPosition(karl.getX() + 1, karl.getY());
        other.setQuest(BowsForOuchit.QUEST_SLOT, "hair");
        karl.listenTo(other, "hi");
        assertSame(player, karl.getAttending());
        new NpcDialogueAction().onAction(other, reply(latest(player), "ouchit"));
        assertEquals(0, other.getNumberOfEquipped("koński włos"));
        assertTrue(snapshots(other).isEmpty());
        click("ouchit"); assertEquals(1, player.getNumberOfEquipped("koński włos"));
    }

    @Test public void ouchitInventoryChangeCannotTurnAnOldHintIntoAnItemDelivery() {
        startWoodQuest();
        RPAction hint = reply(latest(player), "wood");
        carry("polano", 10);
        new NpcDialogueAction().onAction(player, hint);
        assertEquals("polano", player.getQuest(BowsForOuchit.QUEST_SLOT));
        assertEquals(10, player.getNumberOfEquipped("polano"));
        RPAction delivery = reply(latest(player), "wood");
        player.drop("polano", 1);
        new NpcDialogueAction().onAction(player, delivery);
        assertEquals("polano", player.getQuest(BowsForOuchit.QUEST_SLOT));
        assertEquals(9, player.getNumberOfEquipped("polano"));
        carry("polano", 1); type("zadanie"); click("wood");
        assertEquals("hair", player.getQuest(BowsForOuchit.QUEST_SLOT));
        click("back");
        RPAction hairHint = reply(latest(player), "hair");
        carry("koński włos", 1);
        new NpcDialogueAction().onAction(player, hairHint);
        assertEquals("hair", player.getQuest(BowsForOuchit.QUEST_SLOT));
        assertEquals(1, player.getNumberOfEquipped("koński włos"));
    }

    private List<String> publicWords() {
        List<String> result = new ArrayList<String>();
        for (RPEvent event : player.events()) {
            if (games.stendhal.common.constants.Events.PLAYER_DIALOGUE_TEXT.equals(event.getName())) { result.add(event.get("text")); }
        }
        return result;
    }
    private NpcDialogueSnapshot.Item item(String name) {
        for (NpcDialogueSnapshot.Item item : latest(player).getItems()) {
            if (name.equals(item.getName())) { return item; }
        }
        throw new AssertionError("Missing item: " + name + " in " + latest(player).getSpeech());
    }
    private RPAction buy(String id, int amount) {
        RPAction action = reply(latest(player), "unused"); action.remove("reply");
        action.put("buy", id); action.put("amount", Integer.toString(amount)); return action;
    }
    private RPAction closeAction() {
        RPAction action = reply(latest(player), "unused"); action.remove("reply"); action.put("close", "1"); return action;
    }
    @Test public void closingUsesNormalFarewellEvenAfterRevisionChangeAndOutsideHearingRange() {
        greet(carmen); RPAction close = closeAction(); click("quest");
        player.setPosition(carmen.getX() + carmen.getPerceptionRange(), carmen.getY());
        new NpcDialogueAction().onAction(player, close);
        assertEquals(ConversationStates.IDLE, carmen.getEngine().getCurrentState());
        assertFalse(latest(player).isActive()); assertFalse(player.hasQuest(HerbsForCarmen.QUEST_SLOT));
        int words = publicWords().size(); new NpcDialogueAction().onAction(player, close); assertEquals(words, publicWords().size());
    }
    @Test public void closingPurchaseDoesNotConfirmSpendMoneyOrAcceptMixedCommands() {
        carry("money", 1000); greet(carmen); click("offer");
        new NpcDialogueAction().onAction(player, buy(item("mały eliksir").getId(), 2));
        RPAction mixed = closeAction(); mixed.put("reply", "confirm");
        new NpcDialogueAction().onAction(player, mixed); assertTrue(latest(player).isActive());
        new NpcDialogueAction().onAction(player, closeAction());
        assertEquals(1000, player.getNumberOfEquipped("money")); assertEquals(0, player.getNumberOfEquipped("mały eliksir"));
        assertFalse(latest(player).isActive()); assertEquals(ConversationStates.IDLE, carmen.getEngine().getCurrentState());
    }
    @Test public void closingStillReleasesNpcWhenChatBucketIsFull() {
        greet(carmen);
        while (player.getChatBucket().checkAndAdd(50)) { }
        new NpcDialogueAction().onAction(player, closeAction());
        assertFalse(latest(player).isActive()); assertTrue(publicWords().isEmpty());
        assertEquals(ConversationStates.IDLE, carmen.getEngine().getCurrentState());
    }
    @Test public void closeMustBelongToCurrentPlayersEncounter() {
        greet(carmen); RPAction foreign = closeAction(); foreign.put("session", "not-this-session");
        new NpcDialogueAction().onAction(player, foreign);
        assertTrue(latest(player).isActive()); assertEquals(ConversationStates.ATTENDING, carmen.getEngine().getCurrentState());
        assertTrue(publicWords().isEmpty());
    }
    @Test public void sedroMonsterCardsUseRecordedQuestCountsAndRealSprites() {
        greet(sedro); click("quest"); click("accept");
        assertEquals(2, latest(player).getItems().size());
        NpcDialogueSnapshot.Item rat = item("gnom");
        assertEquals("monsters/gnome/gnome", rat.getSprite()); assertEquals(1, rat.getRequired());
        assertEquals(0, rat.getOwned()); assertEquals(-1, rat.getUnitPrice());
        player.setSoloKill("gnom");
        click("back"); click("quest"); assertEquals(1, item("gnom").getOwned());
    }
    @Test public void panelSpeechIsPublicOnceAndNeverBecomesAnotherParserInput() {
        greet(carmen); RPAction request = reply(latest(player), "quest");
        new NpcDialogueAction().onAction(player, request);
        assertEquals(Collections.singletonList("Czy mogę ci w czymś pomóc?"), publicWords());
        assertFalse(player.has("text"));
        carmen.preLogic(); assertEquals(ConversationStates.QUESTION_1, carmen.getEngine().getCurrentState());
        new NpcDialogueAction().onAction(player, request); assertEquals(1, publicWords().size());
        click("know");
        assertFalse(has(latest(player), "quest")); assertTrue(has(latest(player), "ingredients"));
        type("Moja własna wypowiedź.");
        assertEquals("Moja własna wypowiedź.", publicWords().get(2)); assertFalse(player.has("text"));
        player.setQuest(HerbsForCarmen.QUEST_SLOT, "done"); type("praca");
        assertFalse(has(latest(player), "quest"));
    }
    @Test public void actualCarmenOfferBuysOnlyAfterConfirmationAndReplayCannotChargeAgain() {
        carry("money", 1000); carry("mały eliksir", 54); greet(carmen); assertFalse(latest(player).isShop()); click("offer");
        assertTrue(latest(player).isShop()); assertEquals(4, latest(player).getItems().size());
        assertEquals(1000, latest(player).getMoney()); assertEquals(54, item("mały eliksir").getOwned());
        NpcDialogueSnapshot.Item potion = item("mały eliksir");
        assertTrue(potion.getSprite().startsWith("items/")); assertEquals(1000, potion.getMaxAmount());
        int price = (int) potion.priceFor(2);
        new NpcDialogueAction().onAction(player, buy(potion.getId(), 2));
        assertEquals(ConversationStates.BUY_PRICE_OFFERED, carmen.getEngine().getCurrentState());
        assertTrue(has(latest(player), "confirm")); assertEquals(1000, player.getNumberOfEquipped("money"));
        assertEquals(54, player.getNumberOfEquipped("mały eliksir"));
        RPAction confirm = reply(latest(player), "confirm"); new NpcDialogueAction().onAction(player, confirm);
        assertEquals(1000 - price, player.getNumberOfEquipped("money"));
        assertEquals(56, player.getNumberOfEquipped("mały eliksir")); assertTrue(latest(player).isShop());
        assertEquals(1000 - price, latest(player).getMoney()); assertEquals(56, item("mały eliksir").getOwned());
        new NpcDialogueAction().onAction(player, confirm);
        assertEquals(1000 - price, player.getNumberOfEquipped("money"));
        assertEquals(56, player.getNumberOfEquipped("mały eliksir"));
    }
    @Test public void purchaseRejectsForgedQuantityAndMixedActionsWithoutPublicEchoOrMoneyChange() {
        carry("money", 1000); greet(carmen);
        new NpcDialogueAction().onAction(player, buy("shop_0", 1)); assertTrue(publicWords().isEmpty());
        type("sklep"); assertTrue(latest(player).isShop()); int words = publicWords().size();
        String id = item("mały eliksir").getId();
        for (int count : new int[] {0, -1, 1001, Integer.MAX_VALUE}) {
            new NpcDialogueAction().onAction(player, buy(id, count));
            assertEquals(words, publicWords().size()); assertFalse(latest(player).getFeedback().isEmpty());
        }
        new NpcDialogueAction().onAction(player, buy("invented", 1));
        RPAction mixed = buy(id, 1); mixed.put("text", "tak"); new NpcDialogueAction().onAction(player, mixed);
        assertEquals(words, publicWords().size()); assertEquals(1000, player.getNumberOfEquipped("money"));
        assertEquals(ConversationStates.ATTENDING, carmen.getEngine().getCurrentState());
    }
    @Test public void cancelAndInsufficientFundsRetainExistingTransactionRules() {
        greet(carmen); click("offer"); String id = item("mały eliksir").getId();
        new NpcDialogueAction().onAction(player, buy(id, 2)); click("cancel");
        assertEquals(0, player.getNumberOfEquipped("mały eliksir")); assertTrue(latest(player).isShop());
        new NpcDialogueAction().onAction(player, buy(id, 2)); click("confirm");
        assertEquals(0, player.getNumberOfEquipped("mały eliksir")); assertEquals(0, player.getNumberOfEquipped("money"));
        assertEquals(0, latest(player).getMoney()); assertEquals(0, item("mały eliksir").getOwned());
        assertTrue(latest(player).isShop());
    }
    @Test public void actualSellerOffersRespectStackabilityAndNpcIdentity() {
        greet(ouchit); click("offer"); assertTrue(latest(player).isShop());
        boolean single = false, stack = false;
        for (NpcDialogueSnapshot.Item row : latest(player).getItems()) {
            if (row.getMaxAmount() == 1) { single = true; } else if (row.getMaxAmount() == 1000) { stack = true; }
        }
        assertTrue(single); assertTrue(stack);
        greet(karl); click("offer"); assertTrue(latest(player).isShop()); assertEquals(10, item("pusty worek").getUnitPrice());
        greet(sedro); assertFalse(has(latest(player), "offer")); type("oferta"); assertFalse(latest(player).isShop());
    }
    @Test public void itemMetadataTracksRemainingDeliveryWithoutChangingQuest() {
        openCarmenOffer(); click("accept");
        String quest = player.getQuest(HerbsForCarmen.QUEST_SLOT);
        assertEquals(5, latest(player).getItems().size()); assertEquals(5, item("arandula").getRequired());
        assertEquals(quest, player.getQuest(HerbsForCarmen.QUEST_SLOT));
        carry("arandula", 2); click("supplies");
        assertEquals(2, item("arandula").getOwned()); assertEquals("give_arandula", item("arandula").getReplyId());
        click("give_arandula"); assertEquals(3, item("arandula").getRequired());
        assertEquals(0, item("arandula").getOwned());
    }

    private int examineEvents() {
        int count = 0;
        for (RPEvent event : player.events()) { if ("examine".equals(event.getName())) { count++; } }
        return count;
    }

    @Test public void carmenGivesFullHintsInOneClickAndNeverRestartsTheQuestion() {
        openCarmenOffer(); click("accept"); click("hints");
        String quest = player.getQuest(HerbsForCarmen.QUEST_SLOT);
        int xp = player.getXP(); double karma = player.getKarma();
        String[][] hints = {{"arandula", "wilków"}, {"mushrooms", "południe"},
                {"champignons", "południe"}, {"apples", "tawernie"}, {"wood", "dwie sztuki"}};
        for (String[] hint : hints) {
            rules.turn += 34;
            if ("champignons".equals(hint[0])) { click("back"); click("hints"); }
            click(hint[0]); assertTrue(latest(player).getSpeech().contains(hint[1]));
            assertFalse(has(latest(player), hint[0])); assertFalse(has(latest(player), "quest"));
            for (Reply reply : latest(player).getReplies()) { assertFalse(reply.getId().startsWith("detail_")); }
            assertEquals(quest, player.getQuest(HerbsForCarmen.QUEST_SLOT));
            assertEquals(xp, player.getXP()); assertEquals(karma, player.getKarma(), 0.001);
        }
        assertEquals(0, examineEvents()); // Inline sprites do not open a second window.
    }

    @Test public void currentBranchSurvivesUnknownInputAndBackReturnsToMainTopics() {
        openCarmenOffer(); click("accept"); click("hints"); click("arandula");
        List<String> before = ids(latest(player));
        type("xyzzy 123 qqq"); assertEquals(before, ids(latest(player)));
        click("back"); assertTrue(has(latest(player), "offer")); assertFalse(has(latest(player), "arandula"));
        click("hints"); assertTrue(has(latest(player), "arandula"));
        player.setPosition(carmen.getX() + 20, carmen.getY()); carmen.preLogic();
        assertFalse(latest(player).isActive()); assertEquals("", latest(player).getNarration());
        greet(carmen); assertFalse(has(latest(player), "arandula"));
    }

    private List<String> ids(NpcDialogueSnapshot value) {
        List<String> result = new ArrayList<String>();
        for (Reply reply : value.getReplies()) { result.add(reply.getId()); }
        return result;
    }

    @Test public void sedroHasStageSpecificChoicesAndDirectDetailedDirections() {
        greet(sedro); assertFalse(has(latest(player), "hints")); click("quest");
        assertEquals(java.util.Arrays.asList("accept", "decline", "goodbye"), ids(latest(player)));
        click("accept"); assertFalse(has(latest(player), "quest"));
        assertEquals(java.util.Arrays.asList("hints", "back", "goodbye"), ids(latest(player)));
        String quest = player.getQuest("help_sedro"); click("hints");
        assertTrue(latest(player).getSpeech().contains("czerwonym"));
        assertFalse(has(latest(player), "rats")); assertFalse(has(latest(player), "rat_detail"));
        click("gnomes"); assertTrue(latest(player).getSpeech().contains("czerwonym"));
        assertFalse(has(latest(player), "gnomes")); assertFalse(has(latest(player), "gnome_detail"));
        assertEquals(quest, player.getQuest("help_sedro"));
        rules.turn += 34; click("back"); click("quest"); assertFalse(has(latest(player), "quest"));
    }

    @Test public void ouchitMovesFromWoodToKarlWithoutRepeatedIntroductions() {
        reachHairStage(); assertFalse(has(latest(player), "wood")); assertFalse(has(latest(player), "quest"));
        click("karl"); assertTrue(latest(player).getSpeech().contains("wschód od Semos"));
        assertEquals(java.util.Arrays.asList("back", "goodbye"), ids(latest(player)));
        assertEquals("hair", player.getQuest(BowsForOuchit.QUEST_SLOT));
        click("back"); assertTrue(has(latest(player), "quest"));
    }

    @Test public void carmenIntroCompletionAndDeclineDoNotOfferTheSameQuestionAgain() {
        greet(carmen); assertFalse(has(latest(player), "arandula")); assertFalse(has(latest(player), "ingredients"));
        click("quest"); click("know");
        assertEquals(java.util.Arrays.asList("ingredients", "back", "goodbye"), ids(latest(player)));
        click("ingredients"); click("decline");
        assertEquals(java.util.Arrays.asList("back", "goodbye"), ids(latest(player)));
        click("back"); assertTrue(has(latest(player), "quest"));
        player.setQuest(HerbsForCarmen.QUEST_SLOT, "done");
        player.setPosition(carmen.getX() + 20, carmen.getY()); carmen.preLogic(); greet(carmen);
        assertFalse(has(latest(player), "quest")); assertFalse(has(latest(player), "supplies"));
        assertTrue(latest(player).getSpeech().contains("pełne zapasy"));
    }

    @Test public void changedPenaltyRefreshesPriceBeforePurchaseAndKeepsOriginalCharge() {
        carry("money", 1000); greet(carmen); click("offer");
        RPAction stale = buy(item("mały eliksir").getId(), 3);
        player.put("last_player_kill_time", "1234");
        int words = publicWords().size(); new NpcDialogueAction().onAction(player, stale);
        assertEquals(words, publicWords().size()); assertEquals(1000, player.getNumberOfEquipped("money"));
        NpcDialogueSnapshot.Item potion = item("mały eliksir"); assertEquals(225, potion.priceFor(3));
        new NpcDialogueAction().onAction(player, buy(potion.getId(), 3)); click("confirm");
        assertEquals(775, player.getNumberOfEquipped("money")); assertEquals(3, player.getNumberOfEquipped("mały eliksir"));
    }

    @Test public void changedPriceAfterQuoteRequiresFreshConsentWithoutCharging() {
        carry("money", 1000); greet(carmen); click("offer");
        new NpcDialogueAction().onAction(player, buy(item("mały eliksir").getId(), 2));
        assertTrue(latest(player).getSpeech().contains("100 monet"));
        player.put("last_player_kill_time", "1234");
        click("confirm");
        assertEquals("A quote for 100 must not charge 150", 1000, player.getNumberOfEquipped("money"));
        assertEquals(0, player.getNumberOfEquipped("mały eliksir"));
        assertFalse(has(latest(player), "confirm"));
        new NpcDialogueAction().onAction(player, buy(item("mały eliksir").getId(), 2));
        assertTrue(latest(player).getSpeech().contains("150 monet")); click("confirm");
        assertEquals(850, player.getNumberOfEquipped("money"));
        assertEquals(2, player.getNumberOfEquipped("mały eliksir"));
    }

    @Test public void fullInventoryPurchaseNeitherChargesNorDropsThePurchasedItem() {
        carry("money", 1000);
        for (String slot : Constants.CARRYING_SLOTS) {
            player.getSlot(slot).setCapacity(Math.max(1, player.getSlot(slot).size()));
            if (player.getSlot(slot).size() == 0) {
                player.getSlot(slot).add(new StackableItem("filler", "test", "test", Collections.<String,String>emptyMap()));
            }
        }
        greet(carmen); click("offer");
        new NpcDialogueAction().onAction(player, buy(item("mały eliksir").getId(), 2)); click("confirm");
        assertEquals(1000, player.getNumberOfEquipped("money"));
        assertEquals(0, player.getNumberOfEquipped("mały eliksir"));
        assertTrue(zone.dropped.isEmpty()); assertTrue(latest(player).isShop());
    }

    @Test public void fullInventoryStillAllowsPurchaseIntoAnExistingStack() {
        carry("money", 1000);
        StackableItem existing = (StackableItem) games.stendhal.server.core.engine.SingletonRepository.getEntityManager().getItem("mały eliksir");
        existing.setQuantity(54); player.getSlot("bag").add(existing);
        for (String slot : Constants.CARRYING_SLOTS) {
            player.getSlot(slot).setCapacity(Math.max(1, player.getSlot(slot).size()));
            if (player.getSlot(slot).size() == 0) {
                player.getSlot(slot).add(new StackableItem("filler", "test", "test", Collections.<String,String>emptyMap()));
            }
        }
        greet(carmen); click("offer");
        new NpcDialogueAction().onAction(player, buy(item("mały eliksir").getId(), 2)); click("confirm");
        assertEquals(900, player.getNumberOfEquipped("money"));
        assertEquals(56, player.getNumberOfEquipped("mały eliksir"));
        assertTrue(zone.dropped.isEmpty());
    }
}
