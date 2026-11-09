package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.maps.quests.SevenCherubs;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** Saved progress must count distinct known angels, independent of separators. */
public class SevenCherubsLegacyTest {
    private JourneyQuestFixture f;
    @BeforeClass public static void init() throws Exception { JourneyQuestFixture.init(); }
    @AfterClass public static void finish() { JourneyQuestFixture.finish(); }
    @Before public void setup() { f = new JourneyQuestFixture(); f.createWorld(); }
    @After public void cleanup() { f.restoreNPCs(); }

    @Test public void explicitCompletedSaveNeverRestartsOrRewards() {
        f.player.setQuest("seven_cherubs", "done");
        int xp = f.player.getXP();
        assertTrue(new SevenCherubs().isCompleted(f.player));
        f.greet("Cherubiel");
        assertEquals(xp, f.player.getXP());
        assertEquals("done", f.player.getQuest("seven_cherubs"));
        assertTrue(f.latest().getRewards().isEmpty());
    }

    @Test public void sixNamesWithoutLeadingSeparatorCanFinish() {
        f.player.setQuest("seven_cherubs", "Cherubiel;Ophaniel;Gabriel;Raphael;Zophiel;Azazel");
        f.greet("Uriel");
        assertTrue(new SevenCherubs().isCompleted(f.player));
        f.reward("anielski pierścień", 1);
        f.reward("XP postaci", 10000);
        int xp = f.player.getXP();
        f.greet("Cherubiel");
        assertEquals(xp, f.player.getXP());
    }

    @Test public void DuplicatesAndUnknownNamesDoNotCompleteQuest() {
        f.player.setQuest("seven_cherubs", ";Cherubiel;Cherubiel;Cherubiel;Cherubiel;Cherubiel;Cherubiel;unknown");
        assertFalse(new SevenCherubs().isCompleted(f.player));
        f.greet("Uriel");
        assertFalse(new SevenCherubs().isCompleted(f.player));
        assertFalse(f.player.isEquipped("anielski pierścień"));
        assertTrue(f.latest().getSpeech().contains("5"));
    }

    @Test public void historyForCompletedOrUnknownTokensHasNoNullEntries() {
        SevenCherubs quest = (SevenCherubs) f.quests.get("SevenCherubs");
        f.player.setQuest("seven_cherubs", "done");
        assertFalse(quest.getHistory(f.player).contains(null));
        f.player.setQuest("seven_cherubs", ";Cherubiel;unknown;Cherubiel");
        assertFalse(quest.getHistory(f.player).contains(null));
    }
}
