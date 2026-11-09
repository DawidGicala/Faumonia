package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.Level;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.core.rp.quest.MasterStage;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.semos.masterhouse.MasterNPC;
import games.stendhal.server.maps.semos.masterhouse.MasterNPC.MasterSpeaker;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class MasterConsolidationTest {
    JourneyQuestFixture f;MasterSpeaker master;SpeakerNPC original;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup() throws Exception {
        f=new JourneyQuestFixture();f.createWorld();original=NPCList.get().remove(MasterStage.NPC_NAME);
        new MasterNPC().configureZone(f.zone,Collections.<String,String>emptyMap());master=(MasterSpeaker)NPCList.get().get(MasterStage.NPC_NAME);
        for(int i=1;i<=24;i++)((IQuest)Class.forName("games.stendhal.server.maps.quests.MainTask"+i).newInstance()).addToWorld();
        f.player.setPosition(4,5);master.setAttending(f.player);say("hi");
    }
    @After public void cleanup(){master.setAttending(null);NPCList.get().remove(MasterStage.NPC_NAME);if(original!=null)NPCList.get().add(original);f.restoreNPCs();}
    boolean say(String text){JourneyQuestFixture.rules.turn+=35;return master.getEngine().step(f.player,text);}
    void level(int n){f.player.setXP(Level.getXP(n));f.player.setLevel(n);}
    void kills(String name,int n){for(int i=0;i<n;i++)f.player.setSoloKill(name);}
    @Test public void oneNpcOwnsAll24StageRegistrations(){assertEquals(24,master.stages().size());assertEquals(MasterStage.NPC_NAME,master.getName());}
    @Test public void selectingTopicDoesNotStartOrAward(){say("etap 1");assertFalse(f.player.hasQuest("main_task_1"));assertFalse(f.player.isEquipped("odznaka wojownika I"));}
    @Test public void stageOneRequiresExactlyTheNewTargetsAndPaysOnlyOnce(){
        say("etap 1");assertTrue(say("zadanie"));assertTrue(say("tak"));
        assertTrue(f.player.getQuest("main_task_1").contains("szczur,0,3,"));
        assertTrue(f.player.getQuest("main_task_1").contains("szczur jaskiniowy,0,1,"));
        kills("szczur",3);say("zadanie");assertFalse(f.player.isQuestCompleted("main_task_1"));
        kills("szczur jaskiniowy",1);int xp=f.player.getXP();say("zadanie");
        assertTrue(f.player.isQuestCompleted("main_task_1"));assertEquals(125,f.player.getXP()-xp);
        assertTrue(f.player.isEquipped("odznaka wojownika I"));xp=f.player.getXP();
        say("tak");say("zadanie");say("etapy");say("etap 1");say("zadanie");assertEquals(xp,f.player.getXP());
    }
    @Test public void badgeAloneDoesNotUnlockLaterStage(){f.carry("odznaka wojownika I",1);say("etap 2");assertNull(master.selected(f.player));assertFalse(f.player.hasQuest("main_task_2"));}
    @Test public void completionUnlocksWithoutCarryingBadge(){f.player.setQuest("main_task_1","done");say("etap 2");assertNotNull(master.selected(f.player));assertEquals(2,master.selected(f.player).number);}
    @Test public void allStageSelectorsKeepTheirNumbers(){
        level(400);for(int i=1;i<=24;i++)f.player.setQuest("main_task_"+i,"done");
        for(int i=1;i<=24;i++){assertTrue(say("etap "+i));assertEquals(i,master.selected(f.player).number);}
    }
    @Test public void levelGateIsCheckedForManuallyTypedCommand(){
        f.player.setQuest("main_task_23","done");level(299);say("etap 24");say("zadanie");say("tak");assertFalse(f.player.hasQuest("main_task_24"));
    }
    @Test public void changingTopicCancelsPendingAcceptance(){
        say("etap 1");say("zadanie");assertEquals(ConversationStates.QUEST_OFFERED,master.getEngine().getCurrentState());
        say("etapy");say("tak");assertFalse(f.player.hasQuest("main_task_1"));
    }
    @Test public void stage24KeepsExistingPrerequisitesAndClawRecord(){
        level(350);f.player.setQuest("main_task_23","done");say("etap 24");say("zadanie");assertFalse(f.player.hasQuest("main_task_24"));
        f.player.setQuest("find_dragons","done");f.player.setQuest("cloaks_collector_2","done");
        say("zadanie");say("tak");String state=f.player.getQuest("main_task_24");assertNotNull("State="+master.getEngine().getCurrentState()+" speech="+f.latest().getSpeech(),state);assertTrue(state.startsWith("start;"));
        say("etapy");say("etap 24");assertEquals(state,f.player.getQuest("main_task_24"));
        MasterStage stage=master.selected(f.player);String name=stage.itemName(f.player);int amount=stage.itemAmount(f.player);
        f.carry(name,amount);int xp=f.player.getXP();say("tak");assertTrue(f.player.isQuestCompleted("main_task_24"));
        assertEquals(MasterStage.rewardXP(300),f.player.getXP()-xp);assertTrue(f.player.isEquipped("odznaka wojownika XXIV"));
        assertTrue(f.player.isEquipped("klucz do tawerny semos"));
    }
    @Test public void completedLegacyStagesHaveNoRetroactiveXp(){
        f.player.setQuest("main_task_18","done;12345");int xp=f.player.getXP();say("etap 18");say("tak");say("zadanie");assertEquals(xp,f.player.getXP());
    }
    @Test public void legacyActiveStageWithEarlierGapIsRetained(){
        f.player.setQuest("main_task_24","start;pazur czarnego smoka;3");say("etap 24");assertEquals(24,master.selected(f.player).number);
        assertEquals("start;pazur czarnego smoka;3",f.player.getQuest("main_task_24"));
    }
    @Test public void rewardFormulaUsesFixedStageLevel(){assertEquals(2990250,MasterStage.rewardXP(200));assertEquals(125,MasterStage.rewardXP(0));}
    @Test public void panelOffersCurrentStageAndActualHuntingPictures(){
        say("etap 1");NpcDialogueSnapshot snapshot=f.latest();assertEquals(MasterStage.NPC_NAME,snapshot.getNpcName());
        assertEquals(2,snapshot.getItems().size());assertFalse(snapshot.getReplies().isEmpty());
        assertEquals(3,snapshot.getItems().get(0).getRequired());
    }
    @Test public void everyStageCompletesThroughNativeActionsAndAwardsFixedXpOnce() throws Exception {
        for(int i=1;i<=24;i++){
            level(400);
            Class<?> type=Class.forName("games.stendhal.server.maps.quests.MainTask"+i);
            for(java.lang.reflect.Field field:type.getDeclaredFields()){
                if(java.lang.reflect.Modifier.isStatic(field.getModifiers())&&field.getType()==String.class&&!field.getName().equals("QUEST_SLOT")){
                    field.setAccessible(true);String value=(String)field.get(null);
                    if(value!=null&&value.matches("[a-zA-Z][a-zA-Z0-9_]+"))f.player.setQuest(value,"done");
                }
            }
            say("etapy");assertTrue("selector "+i,say("etap "+i));say("zadanie");say("tak");
            MasterStage stage=master.selected(f.player);assertNotNull("selected "+i,stage);
            assertTrue("Stage "+i+" not active: "+f.latest().getSpeech(),stage.active(f.player));
            if(stage.hunt()){
                for(Map.Entry<String,marauroa.common.Pair<Integer,Integer>> e:stage.targets().entrySet())kills(e.getKey(),e.getValue().first()+e.getValue().second());
            }else{
                assertTrue("No recorded item "+i,stage.itemAmount(f.player)>0);
                f.carry(stage.itemName(f.player),stage.itemAmount(f.player));
                say("etap "+i);
            }
            int xp=f.player.getXP();say(stage.hunt()?"zadanie":"tak");
            assertTrue("Stage "+i+" not done: "+f.latest().getSpeech(),stage.completed(f.player));
            assertEquals("XP stage "+i,MasterStage.rewardXP(stage.level()),f.player.getXP()-xp);
            int paid=f.player.getXP();say("tak");say("zadanie");say("etap "+i);say("tak");assertEquals("Duplicate XP "+i,paid,f.player.getXP());
        }
    }
}

