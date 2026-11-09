package games.stendhal.server.entity.npc.dialogue;
import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.npc.ConversationStates;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CollectionQuestDialogueTest {
    @Parameterized.Parameters public static Collection<Object[]> data(){return Arrays.asList(new Object[][]{{"AdosBadgeQuest"},{"AndrzejCollector"},{"BanditItemQuest"},{"CloakCollector"},{"CloakCollector2"},{"CloaksForBario"},{"DailyGeneralQuest"},{"DailyItemQuest"},{"DailyMonsterQuest"},{"DailyMonsterQuest1"},{"DailyMonsterQuest2"},{"DragonCave"},{"FadoBadgeQuest"},{"KirdnehBadgeQuest"},{"NalworBadgeQuest"},{"SemosBadgeQuest"},{"StuffForBaldemar"},{"StuffForVulcanus"},{"SwordsForNnyddion"},{"ToysCollector"},{"WeaponsCollector"},{"WeaponsCollector2"},{"WeeklyCollectorQuest"},{"WeeklyItemQuest"},{"ememedal"},{"obsmedal"},{"rubmedal"},{"sapmedal"}});}
    final String name;CollectionQuestFixture f;IQuest q;NativeCollectionDetails d;String kind;
    public CollectionQuestDialogueTest(String name){this.name=name;}
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();q=f.quests.get(name);d=CollectionQuestProgress.details(q);kind=d.getCollectionKind();}
    @After public void teardown(){f.restoreNPCs();}
    void greet(){f.greet(f.npcName(q));}
    void begin(){
        for(String slot:d.getCollectionPrerequisites().keySet())f.player.setQuest(slot,"done");
        if(q instanceof CloakCollector2)f.player.setQuest("cloaks_collector","done");
        if(q instanceof WeaponsCollector2)f.player.setQuest("weapons_collector","done");
        greet();if(!f.has("accept"))f.click("quest");f.capture(name+"-offer");if(f.has("accept"))f.click("accept");
        assertTrue(name+" was not started: "+f.latest().getSpeech(),f.player.hasQuest(q.getSlotName()));
        assertFalse(CollectionQuestProgress.done(q,f.player));
    }
    @Test public void nativeDeliveryFromOfferToRealReceiptWithoutDuplicateRewards(){
        if(!CollectionQuestProgress.missingReward(q).isEmpty()){
            greet();assertFalse("Unavailable legacy reward is not offered",f.has("quest"));f.type("zadanie");assertFalse(f.player.hasQuest(q.getSlotName()));
            f.player.setQuest(q.getSlotName(),"start;0;0;0;0");String first=d.getCollectionItems().keySet().iterator().next();f.carry(first,d.getCollectionItems().get(first));String saved=f.player.getQuest(q.getSlotName());
            greet();f.type("dostawa");assertEquals(saved,f.player.getQuest(q.getSlotName()));assertEquals((int)d.getCollectionItems().get(first),f.player.getNumberOfEquipped(first));
            f.player.setQuest(q.getSlotName(),"forging;0");int xp=f.player.getXP();greet();f.type("dostawa");assertEquals("forging;0",f.player.getQuest(q.getSlotName()));assertEquals(xp,f.player.getXP());assertTrue(f.latest().getRewards().isEmpty());f.capture(name+"-unavailable");return;
        }
        begin();int xp=f.player.getXP();String saved=f.player.getQuest(q.getSlotName());
        QuestProgress progress=CollectionQuestProgress.read(q,f.player);assertNotNull(progress);assertEquals(name,QuestProgress.State.ACTIVE,progress.getState());
        assertFalse("An active request must not offer to start again",f.has("quest"));
        f.capture(name+"-active");
        assertFalse("Current item/creature icons expected",f.latest().getItems().isEmpty());
        if("dailykill".equals(kind)){
            String[] record=f.player.getQuest(q.getSlotName(),0).split(",");f.type("zakończone");assertEquals(saved,f.player.getQuest(q.getSlotName()));assertEquals(xp,f.player.getXP());
            f.player.setSharedKill(record[0]);assertEquals(QuestProgress.State.READY_TO_DELIVER,CollectionQuestProgress.read(q,f.player).getState());greet();f.click("deliver");
        }else if("sequence".equals(kind)){
            Map<String,Integer> all=d.getCollectionItems();String first=all.keySet().iterator().next();int amount=Math.max(1,all.get(first)/2);f.carry(first,amount);greet();
            assertEquals("Native partial hand-in records contribution",Integer.toString(amount),f.player.getQuest(q.getSlotName(),1));assertEquals(xp,f.player.getXP());
            for(Map.Entry<String,Integer> e:CollectionQuestProgress.remaining(q,f.player).entrySet())f.carry(e.getKey(),e.getValue());
            if(d.getCollectionSolo())f.player.setSoloKill(d.getCollectionKill());else f.player.setSharedKill(d.getCollectionKill());
            greet();assertEquals("forging",f.player.getQuest(q.getSlotName(),0));f.capture(name+"-waiting");f.click("supplies");assertEquals(xp,f.player.getXP());
            f.player.setQuest(q.getSlotName(),1,"0");greet();if(f.payFeeIfRequested(q.getSlotName()))f.click("supplies");assertTrue(CollectionQuestProgress.done(q,f.player));
            if(q instanceof AndrzejCollector)f.reward("Maksymalne zdrowie",100);
        }else if("random".equals(kind)||"badge".equals(kind)){
            Map<String,Integer> left=CollectionQuestProgress.remaining(q,f.player);assertEquals("Only the recorded target, never the pool",1,left.size());assertEquals(1,f.latest().getItems().size());
            if("badge".equals(kind)){f.click("supplies");f.click("confirm");}else f.type("zakończone");
            assertEquals(saved,f.player.getQuest(q.getSlotName()));assertEquals(xp,f.player.getXP());
            for(Map.Entry<String,Integer> e:left.entrySet())f.carry(e.getKey(),e.getValue());greet();
            if("badge".equals(kind)){f.click("confirm");if(f.payFeeIfRequested(q.getSlotName())){f.click("supplies");f.click("confirm");}}else f.click("deliver");
        }else if("count".equals(kind)){
            String item=d.getCollectionItems().keySet().iterator().next();int n=d.getCollectionItems().get(item);f.carry(item,n);greet();
            for(int i=n;i>0;i--){f.click("give_"+item);if(f.payFeeIfRequested(q.getSlotName())){f.click("supplies");f.click("give_"+item);}if(i>1){assertEquals(Integer.toString(i-1),f.player.getQuest(q.getSlotName()));assertEquals(xp,f.player.getXP());}}
            assertFalse(f.player.isEquipped(item));
        }else{
            Map<String,Integer> left=new LinkedHashMap<String,Integer>(CollectionQuestProgress.remaining(q,f.player));
            for(Map.Entry<String,Integer> e:left.entrySet())f.carry(e.getKey(),e.getValue());greet();
            if(f.has("supplies"))f.click("supplies");else if(f.has("collection_two"))f.click("collection_two");
            for(String item:left.keySet()){f.click("give_"+item);if(f.payFeeIfRequested(q.getSlotName())){if(f.has("supplies"))f.click("supplies");else if(f.has("collection_two"))f.click("collection_two");f.click("give_"+item);}assertFalse("Remove already delivered options",f.has(ResidentDialogueReplies.id("give_"+item)));}
        }
        assertTrue(name+": "+f.player.getQuest(q.getSlotName()),CollectionQuestProgress.done(q,f.player));assertTrue(f.player.getXP()>xp);
        f.reward("XP postaci",f.player.getXP()-xp);assertFalse(f.latest().getRewards().isEmpty());f.capture(name+"-reward");
        String done=f.player.getQuest(q.getSlotName());int after=f.player.getXP();greet();f.type("zakończone");f.type("tak");
        assertEquals("Already claimed stage must not reset",done,f.player.getQuest(q.getSlotName()));assertEquals(after,f.player.getXP());
    }
    @Test public void unknownSaveIsReadOnlyAndNeverProducesInventedRequirements(){
        f.player.setQuest(q.getSlotName(),"unrecognized-save-format");String before=f.player.getQuest(q.getSlotName());
        QuestProgress progress=CollectionQuestProgress.read(q,f.player);assertEquals(QuestProgress.State.UNKNOWN,progress.getState());assertTrue(progress.getObjectives().isEmpty());assertEquals(before,f.player.getQuest(q.getSlotName()));
    }
}
