package games.stendhal.server.entity.npc.dialogue;
import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import marauroa.common.Pair;

@RunWith(Parameterized.class)
public class HuntQuestDialogueTest {
    @Parameterized.Parameters public static Collection<Object[]> data(){return Arrays.asList(new Object[][]{{"KillAdosSpiders"},{"KillAlbinos"},{"KillAmazon"},{"KillAngels"},{"KillBarbarians"},{"KillChaos"},{"KillChaosMonsters"},{"KillDragons"},{"KillDwarfs"},{"KillEasyMonsters"},{"KillElfes"},{"KillFairy"},{"KillForVerdan"},{"KillForWod"},{"KillGiants"},{"KillGnomes"},{"KillHigh"},{"KillLast"},{"KillMadaram"},{"KillMag"},{"KillMediumMonsters"},{"KillMonks"},{"KillSausid"},{"KillStone"},{"KillXeno"},{"CleanAthorsUnderground"}});}
    final String name;HuntQuestFixture f;IQuest q;NativeHuntDetails h;
    public HuntQuestDialogueTest(String name){this.name=name;}
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new HuntQuestFixture();f.createWorld();q=f.quests.get(name);h=(NativeHuntDetails)q;}
    @After public void teardown(){f.restoreNPCs();}
    void begin(){f.greet(q.getNPCName());f.click("quest");if(f.has("accept")){f.capture(name+"-offer");f.click("accept");}assertEquals("start",f.player.getQuest(q.getSlotName(),0));}
    void kill(String creature,int solo,int shared){for(int i=0;i<solo;i++)f.player.setSoloKill(creature);for(int i=0;i<shared;i++)f.player.setSharedKill(creature);}
    @Test public void freshAndSharedKillsProgressRewardAndNoDoubleClaim(){
        for(String creature:h.getDialogueHuntTargets(f.player).keySet())kill(creature,2,3);
        begin();String saved=f.player.getQuest(q.getSlotName());QuestProgress progress=f.progress(name);
        assertNotNull(progress);assertEquals(QuestProgress.State.ACTIVE,progress.getState());
        for(QuestProgress.Objective o:progress.getObjectives())assertEquals(o.getLabel(),0,o.getCurrent());
        assertFalse("No offer to restart an active hunt",f.has("quest"));
        int before=f.player.getXP();f.greet(q.getNPCName());f.click("supplies");assertEquals(saved,f.player.getQuest(q.getSlotName()));assertEquals(before,f.player.getXP());
        f.click("hints");assertEquals(saved,f.player.getQuest(q.getSlotName()));f.capture(name+"-progress");
        assertFalse("Creature icons expected",f.latest().getItems().isEmpty());
        int total=h.getDialogueHuntTotal(f.player);
        if(total>0)kill(h.getDialogueHuntTargets(f.player).keySet().iterator().next(),0,total);
        else for(Map.Entry<String,Pair<Integer,Integer>> e:h.getDialogueHuntTargets(f.player).entrySet())kill(e.getKey(),e.getValue().first(),e.getValue().second());
        assertEquals(QuestProgress.State.READY_TO_DELIVER,f.progress(name).getState());f.greet(q.getNPCName());f.click("supplies");
        assertTrue(q.getName()+": "+f.player.getQuest(q.getSlotName()),q.isCompleted(f.player));
        assertTrue(f.player.getXP()>before);f.reward("XP postaci",f.player.getXP()-before);assertFalse(f.latest().getRewards().isEmpty());f.capture(name+"-reward");
        String completed=f.player.getQuest(q.getSlotName());int xp=f.player.getXP();f.type("zadanie");f.type("tak");
        assertEquals("Do not repeat before cooldown",completed,f.player.getQuest(q.getSlotName()));assertEquals(xp,f.player.getXP());
    }
    @Test public void reentryDoesNotResetCountersAndUnknownSavesAreReadOnly(){
        begin();String saved=f.player.getQuest(q.getSlotName());
        String creature=h.getDialogueHuntTargets(f.player).keySet().iterator().next();kill(creature,1,0);
        f.greet(q.getNPCName());assertEquals(saved,f.player.getQuest(q.getSlotName()));assertFalse(f.has("quest"));
        f.player.setQuest(q.getSlotName(),h.getDialogueKillIndex(),"unrecognized baseline");String bad=f.player.getQuest(q.getSlotName());
        assertEquals(QuestProgress.State.UNKNOWN,HuntQuestProgress.read(q,f.player).getState());assertEquals(bad,f.player.getQuest(q.getSlotName()));
    }
}
