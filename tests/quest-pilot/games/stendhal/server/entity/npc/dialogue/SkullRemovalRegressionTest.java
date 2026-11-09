package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.Level;
import games.stendhal.common.constants.Events;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.PlayerDieer;
import java.lang.reflect.*;
import java.util.*;
import marauroa.common.game.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Production dialogue engines and real money removal, with no database or live accounts. */
public class SkullRemovalRegressionTest {
    private JourneyQuestFixture f;
    private SpeakerNPC wikary, io;
    private static final long DAY = 24L * 60 * 60 * 1000;

    @BeforeClass public static void init() { JourneyQuestFixture.init(); }
    @AfterClass public static void finish() { JourneyQuestFixture.finish(); }
    @Before public void setup() {
        f = new JourneyQuestFixture(); f.createWorld();
        for (String name : Arrays.asList("Wikary", "Io Flotto")) f.originals.put(name, NPCList.get().remove(name));
        new games.stendhal.server.maps.fado.city.WikaryNPC().configureZone(f.zone, Collections.<String,String>emptyMap());
        new games.stendhal.server.maps.semos.temple.TelepathNPC().configureZone(f.zone, Collections.<String,String>emptyMap());
        wikary = NPCList.get().get("Wikary"); io = NPCList.get().get("Io Flotto");
        f.npcs.put("Wikary", wikary); f.npcs.put("Io Flotto", io);
    }
    @After public void teardown() { f.restoreNPCs(); }
    private void say(SpeakerNPC npc, String text) { assertTrue(npc.getName()+" / "+text, npc.getEngine().step(f.player, text)); }
    private void level(int value) { f.player.setXP(Level.getXP(value)); f.player.setLevel(value); }
    private void mark() { f.player.put("last_player_kill_time", 1); }
    private int money() { return f.player.getNumberOfEquipped("money"); }
    private void offer() { say(wikary,"hi"); say(wikary,"zdejmij"); }
    private void prepareIo(long age, double karma) {
        mark(); f.player.setQuest("meet_io","start");
        f.player.addKarma(karma - f.player.getKarma());
        f.player.put("last_pvp_action_time", System.currentTimeMillis()-age);
    }
    private String speech(SpeakerNPC npc) {
        String text = "";
        for (RPEvent event : npc.events()) if (Events.PUBLIC_TEXT.equals(event.getName())) text = event.get("text");
        return text;
    }

    @Test public void unmarkedPlayerCannotPayEvenByRepeatingTheRequest() {
        f.carry("money", 100000); offer();
        assertEquals(ConversationStates.ATTENDING, wikary.getEngine().getCurrentState());
        assertTrue(speech(wikary).contains("Nie nosisz"));
        for (int i=0;i<5;i++) { wikary.getEngine().step(f.player,"tak"); say(wikary,"zdejmij"); }
        assertEquals(100000,money()); assertFalse(f.player.isBadBoy());
    }

    @Test public void skullRemovedAfterOfferCannotBeChargedAtConfirmation() {
        mark(); f.carry("money",10000); offer(); f.player.rehabilitate(); say(wikary,"tak");
        assertEquals(10000,money()); assertTrue(speech(wikary).contains("Nie nosisz"));
    }

    @Test public void everyActivePriceBoundaryChargesExactlyOnce() {
        int[][] prices={{0,1000},{9,1000},{10,10000},{249,10000},{250,20000},{349,20000},{350,40000},{597,40000}};
        for (int[] price:prices) {
            level(price[0]); mark(); f.carry("money",price[1]); offer();
            assertTrue(speech(wikary),speech(wikary).contains(Integer.toString(price[1])));
            say(wikary,"tak"); assertEquals(0,money()); assertFalse(f.player.isBadBoy());
            f.carry("money",price[1]);
            for (int i=0;i<3;i++) wikary.getEngine().step(f.player,"tak");
            say(wikary,"zdejmij"); assertEquals(price[1],money());
            f.player.drop("money",price[1]); say(wikary,"bye");
        }
    }

    @Test public void insufficientMoneyIsNotPartiallyTakenAndRetryCanSucceed() {
        level(350); mark(); offer(); say(wikary,"tak"); assertTrue(f.player.isBadBoy());
        f.carry("money",39999); say(wikary,"zdejmij"); say(wikary,"tak");
        assertEquals(39999,money()); assertTrue(f.player.isBadBoy());
        f.carry("money",1); say(wikary,"zdejmij"); say(wikary,"tak");
        assertEquals(0,money()); assertFalse(f.player.isBadBoy());
    }

    @Test public void cancelAndGoodbyeNeverAcceptAnOldOffer() {
        mark(); f.carry("money",10000); offer(); say(wikary,"nie");
        wikary.getEngine().step(f.player,"tak"); assertEquals(10000,money());
        say(wikary,"zdejmij"); say(wikary,"bye");
        say(wikary,"hi"); wikary.getEngine().step(f.player,"tak");
        assertEquals(10000,money()); assertTrue(f.player.isBadBoy());
        say(wikary,"zdejmij"); say(wikary,"tak"); assertEquals(0,money());
    }

    @Test public void crossingALevelBoundaryRequiresConfirmationOfTheNewPrice() {
        level(249); mark(); f.carry("money",20000); offer(); level(250); say(wikary,"tak");
        assertEquals(20000,money()); assertTrue(f.player.isBadBoy());
        assertEquals(ConversationStates.SERVICE_OFFERED,wikary.getEngine().getCurrentState());
        assertTrue(speech(wikary).contains("20000"));
        say(wikary,"tak"); assertEquals(0,money()); assertFalse(f.player.isBadBoy());
    }

    @Test public void aDifferentPlayerCannotAcceptAnotherPlayersQuote() {
        mark(); f.carry("money",10000); offer();
        Player second = new Player(new RPObject(f.player)); second.setName("SkullSecond"); second.remove("id");
        f.zone.add(second); wikary.getEngine().step(second,"tak");
        assertEquals(10000,second.getNumberOfEquipped("money")); assertTrue(second.isBadBoy());
        assertEquals(10000,money()); assertTrue(f.player.isBadBoy());
    }

    @Test public void dormantWikaryKeepsHisOwnPricesButRejectsUnmarkedPayments() {
        NPCList.get().remove("Wikary");
        new games.stendhal.server.maps.pol.zakopane.church.WikaryNPC().configureZone(f.zone,Collections.<String,String>emptyMap());
        wikary=NPCList.get().get("Wikary"); f.npcs.put("Wikary",wikary);
        level(500); f.carry("money",500000); offer();
        assertEquals(ConversationStates.ATTENDING,wikary.getEngine().getCurrentState()); assertEquals(500000,money());
        say(wikary,"bye"); mark(); offer(); say(wikary,"tak");
        assertEquals(0,money()); assertFalse(f.player.isBadBoy());
    }

    @Test public void donationHelpDoesNotFalselyClaimThePlayerHasNoMoney() {
        f.carry("money",1000); say(wikary,"hi"); say(wikary,"datek");
        assertTrue(speech(wikary),speech(wikary).contains("datek 1000 money")); assertEquals(1000,money());
        say(wikary,"oferta"); assertTrue(speech(wikary),speech(wikary).contains("czekolady"));
        say(wikary,"datek 1000 money"); assertEquals(0,money());
        assertEquals(1,f.player.getNumberOfEquipped("tabliczka czekolady"));
        assertFalse(f.player.isEquipped("gigantyczny eliksir"));
    }

    @Test public void highlightedRemovalWordLeadsToTheSameSafeOffer() {
        mark(); f.carry("money",10000); say(wikary,"hi"); say(wikary,"oferta");
        assertTrue(speech(wikary).contains("#zdjąć")); say(wikary,"zdjąć");
        assertEquals(ConversationStates.SERVICE_OFFERED,wikary.getEngine().getCurrentState());
        assertEquals(10000,money()); say(wikary,"tak"); assertEquals(0,money()); assertFalse(f.player.isBadBoy());
    }

    @Test public void aFullBagDoesNotPreventPaidRehabilitation() {
        mark(); f.player.getSlot("bag").setCapacity(1); f.carry("money",10000);
        assertEquals(1,f.player.getSlot("bag").size()); offer(); say(wikary,"tak");
        assertEquals(0,money()); assertFalse(f.player.isBadBoy()); assertTrue(f.zone.dropped.isEmpty());
    }

    @Test public void realNpcListeningKeepsTheSecondPlayersMoneyAndConversationSeparate() {
        mark(); f.carry("money",10000);
        Player second=new Player(new RPObject(f.player)); second.setName("SkullVisitor"); second.remove("id"); f.zone.add(second);
        assertFalse(f.player.getID().equals(second.getID()));
        f.player.setPosition(wikary.getX()+1,wikary.getY()); second.setPosition(wikary.getX()+1,wikary.getY()+1);
        wikary.listenTo(f.player,"hi"); wikary.listenTo(f.player,"zdejmij");
        assertEquals(f.player,wikary.getAttending());
        wikary.listenTo(second,"hi"); wikary.listenTo(second,"tak");
        assertEquals(f.player,wikary.getAttending()); assertEquals(10000,money());
        assertEquals(10000,second.getNumberOfEquipped("money")); assertTrue(second.isBadBoy());
        wikary.listenTo(f.player,"tak"); assertEquals(0,money()); assertFalse(f.player.isBadBoy());
        assertEquals(10000,second.getNumberOfEquipped("money")); assertTrue(second.isBadBoy());
    }

    @Test public void recentPvpShowsATwoDayRuleAndOneDayRemaining() {
        prepareIo(DAY,5); say(io,"hi"); say(io,"tak");
        assertTrue(f.player.isBadBoy()); assertEquals(ConversationStates.ATTENDING,io.getEngine().getCurrentState());
        String text=speech(io); assertTrue(text,text.contains("dwa dni")); assertFalse(text,text.contains("tygod"));
        assertFalse(text,text.contains("13 dni"));
        assertTrue(text,text.contains("1 d") || text.contains("24 godzin") || text.contains("23 godzin"));
    }

    @Test public void threeDaysAndExactlyFiveKarmaRemoveTheSkullForFree() {
        prepareIo(3*DAY,5); f.carry("money",1234); say(io,"hi"); say(io,"tak");
        assertEquals(ConversationStates.QUESTION_2,io.getEngine().getCurrentState());
        say(io,"tak"); assertFalse(f.player.isBadBoy()); assertEquals(1234,money());
        assertTrue(speech(io).contains("Zdjęłam"));
        io.getEngine().step(f.player,"tak"); assertEquals(1234,money());
    }

    @Test public void renewedPvpBetweenQuestionsBlocksTheFinalRemoval() {
        prepareIo(3*DAY,10); say(io,"hi"); say(io,"tak");
        f.player.put("last_pvp_action_time",System.currentTimeMillis()); say(io,"tak");
        assertTrue(f.player.isBadBoy()); assertTrue(speech(io),speech(io).contains("dwa dni"));
    }

    @Test public void losingKarmaBetweenQuestionsBlocksTheFinalRemoval() {
        prepareIo(3*DAY,5); say(io,"hi"); say(io,"tak"); f.player.addKarma(-1); say(io,"tak");
        assertTrue(f.player.isBadBoy()); assertTrue(speech(io).contains("karma"));
        assertEquals(ConversationStates.ATTENDING,io.getEngine().getCurrentState());
    }

    @Test public void insufficientKarmaIsExplainedBeforeTheFinalQuestion() {
        prepareIo(3*DAY,4); say(io,"hi"); say(io,"tak"); assertTrue(f.player.isBadBoy());
        assertEquals(ConversationStates.ATTENDING,io.getEngine().getCurrentState());
        assertTrue(speech(io),speech(io).contains("nie jest jeszcze wystarczająca"));
    }

    @Test public void skullRemovedElsewhereDoesNotProduceASecondSuccess() {
        prepareIo(3*DAY,5); say(io,"hi"); say(io,"tak"); f.player.rehabilitate(); say(io,"tak");
        assertFalse(f.player.isBadBoy()); assertTrue(speech(io),speech(io).contains("Nie nosisz"));
    }

    @Test public void ioCancellationDoesNotRemoveTheSkull() {
        prepareIo(3*DAY,5); say(io,"hi"); say(io,"nie"); assertTrue(f.player.isBadBoy());
        say(io,"bye"); say(io,"hi"); say(io,"tak"); say(io,"nie"); assertTrue(f.player.isBadBoy());
    }

    @Test public void tutorialIsSentByTheActualKillMarkAndNotByParticipation() throws Exception {
        Player victim=new Player(new RPObject(f.player)); victim.setName("SkullVictim"); victim.remove("id"); f.zone.add(victim);
        JourneyQuestFixture.rules.getOnlinePlayers().add(f.player);
        try {
            Field participants=RPEntity.class.getDeclaredField("playersToReward"); participants.setAccessible(true);
            @SuppressWarnings("unchecked") Set<String> names=(Set<String>)participants.get(victim); names.add(f.player.getName());
            Method reward=Player.class.getDeclaredMethod("rewardKillers",int.class); reward.setAccessible(true); reward.invoke(victim,0);
            assertNull(f.player.getKeyedSlot("!tutorial","first_player_kill")); assertFalse(f.player.isBadBoy());
            Method kill=PlayerDieer.class.getDeclaredMethod("handlePlayerKiller",Player.class); kill.setAccessible(true);
            kill.invoke(new PlayerDieer(victim),f.player);
            assertTrue(f.player.isBadBoy()); assertEquals("1",f.player.getKeyedSlot("!tutorial","first_player_kill"));
            assertNull(victim.getKeyedSlot("!tutorial","first_player_kill"));
        } finally { JourneyQuestFixture.rules.getOnlinePlayers().remove(f.player); }
    }

    @Test public void suicideDoesNotMarkOrNotifyThePlayer() throws Exception {
        Method kill=PlayerDieer.class.getDeclaredMethod("handlePlayerKiller",Player.class); kill.setAccessible(true);
        kill.invoke(new PlayerDieer(f.player),f.player);
        assertFalse(f.player.isBadBoy()); assertNull(f.player.getKeyedSlot("!tutorial","first_player_kill"));
    }
}
