package games.stendhal.server.entity.npc.dialogue;
import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.core.engine.SingletonRepository;
import java.util.*;
import org.junit.*;

public class CollectionQuestBoundaryTest {
    CollectionQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    @Test public void goldParcelUsesTwoNpcsAndDoesNotSuppressLorithienHunt(){
        f.greet("Grafindle");f.click("quest");f.click("accept");assertEquals("start",f.player.getQuest("grafindle_gold"));
        f.greet("Lorithien");if("start".equals(f.player.getQuest("grafindle_gold")))f.click("gold_delivery");
        assertEquals("lorithien",f.player.getQuest("grafindle_gold"));assertEquals(25,f.player.getNumberOfEquipped("sztabka złota"));
        assertTrue("Postal hunt remains offered",f.has("quest"));f.capture("gold-lorithien");
        f.type("przesyłka");assertEquals(25,f.player.getNumberOfEquipped("sztabka złota"));
        f.greet("Grafindle");f.payFeeIfRequested("grafindle_gold");if(!f.player.isQuestCompleted("grafindle_gold"))f.click("gold_delivery");
        assertTrue(f.player.isQuestCompleted("grafindle_gold"));assertEquals(0,f.player.getNumberOfEquipped("sztabka złota"));f.reward("klucz do banku Nalwor",1);f.reward("XP postaci",5000);f.capture("gold-reward");
        int xp=f.player.getXP();f.greet("Grafindle");f.type("przesyłka");assertEquals(xp,f.player.getXP());
    }
    @Test public void phalkRejectsGenericClothesAndReceivesMarkedOrdersFromBothSuppliers(){
        f.greet("Phalk");f.click("quest");f.click("accept");f.click("supplies");f.click("confirm");assertEquals("start",f.player.getQuest("supplies_for_phalk"));
        f.carry("kanapka",3);f.carry("sok z chmielu",3);f.carry("napój z winogron",3);f.greet("Phalk");f.click("supplies");f.click("confirm");assertEquals("clothes;none;none",f.player.getQuest("supplies_for_phalk"));f.capture("phalk-food");
        f.carry("płaszcz krasnoludzki",1);f.carry("zbroja krasnoludzka",1);f.greet("Phalk");f.click("supplies");assertEquals("clothes;none;none",f.player.getQuest("supplies_for_phalk"));assertTrue(f.player.isEquipped("płaszcz krasnoludzki"));
        f.carry("strzała żelazna",20);f.greet("Wrvil");f.click("phalk");f.click("confirm");assertEquals("cloak",f.player.getQuest("supplies_for_phalk",1));assertEquals(1,JourneyQuestProgress.personalItemCount(f.player,"płaszcz krasnoludzki","Phalk"));f.capture("phalk-cloak");
        f.carry("żelazo",10);f.greet("Mrotho");f.click("phalk");f.click("confirm");assertEquals("armor",f.player.getQuest("supplies_for_phalk",2));assertEquals(1,JourneyQuestProgress.personalItemCount(f.player,"zbroja krasnoludzka","Phalk"));f.capture("phalk-armor");
        f.greet("Phalk");f.click("supplies");assertTrue(f.payFeeIfRequested("supplies_for_phalk"));f.click("supplies");assertTrue(f.player.isQuestCompleted("supplies_for_phalk"));f.reward("XP postaci",50000);f.capture("phalk-reward");
        assertEquals(0,JourneyQuestProgress.personalItemCount(f.player,"płaszcz krasnoludzki","Phalk"));assertTrue("Unmarked clothes are not taken",f.player.isEquipped("płaszcz krasnoludzki"));
    }
    @Test public void badgeShowsAllThreeMissingPrerequisitesAndDoesNotRollARequestEarly(){
        for(IQuest q:CollectionQuestProgress.all())if("badge".equals(CollectionQuestProgress.kind(q))){
            f.greet(f.npcName(q));f.click("quest");assertFalse(f.player.hasQuest(q.getSlotName()));
            String speech=f.latest().getSpeech();for(String label:CollectionQuestProgress.details(q).getCollectionPrerequisites().values())assertTrue(label,speech.contains(label));
            assertTrue("No random pool pictured before assignment",f.latest().getItems().isEmpty());
        }
    }
    @Test public void markedStackConsumptionIsExactAndInsufficientDeliveryIsAtomic(){
        f.carry("strzała żelazna",20);
        Item marked=SingletonRepository.getEntityManager().getItem("strzała żelazna");
        ((games.stendhal.server.entity.item.StackableItem)marked).setQuantity(7);
        marked.setInfoString("Phalk");f.player.equipToInventoryOnly(marked);
        new games.stendhal.server.entity.npc.action.DropInfostringItemAction("strzała żelazna",8,"Phalk").fire(f.player,null,null);
        assertEquals(27,f.player.getNumberOfEquipped("strzała żelazna"));assertEquals(7,marked.getQuantity());
        new games.stendhal.server.entity.npc.action.DropInfostringItemAction("strzała żelazna",4,"Phalk").fire(f.player,null,null);
        assertEquals(23,f.player.getNumberOfEquipped("strzała żelazna"));assertEquals(3,marked.getQuantity());
        new games.stendhal.server.entity.npc.action.DropInfostringItemAction("strzała żelazna",3,"Phalk").fire(f.player,null,null);
        assertEquals(20,f.player.getNumberOfEquipped("strzała żelazna"));
    }
    @Test public void terryDirectTopicRespectsNativeLevelGateAndRejectedSave(){
        f.player.setLevel(99);f.greet("Terry");f.type("przedmiotów");
        assertFalse(f.has("accept"));assertFalse(f.player.hasQuest("dragon_cave"));assertTrue(f.latest().getSpeech().contains("100"));
        f.player.setQuest("dragon_cave","rejected");f.type("zadanie");assertFalse(f.has("accept"));assertEquals("rejected",f.player.getQuest("dragon_cave"));
        f.player.setLevel(100);f.type("przedmiotów");assertTrue(f.has("accept"));f.click("accept");assertTrue(f.player.getQuest("dragon_cave").contains("skóra zielonego smoka"));
    }
    @Test public void randomRequestsRespectExpiryAndCooldownWithoutReplacingCurrentOrder(){
        for(IQuest q:CollectionQuestProgress.all())if("random".equals(CollectionQuestProgress.kind(q))){
            NativeCollectionDetails d=CollectionQuestProgress.details(q);f.greet(f.npcName(q));f.click("quest");String first=f.player.getQuest(q.getSlotName());
            assertFalse(f.has("replace"));f.type("inny");assertEquals(first,f.player.getQuest(q.getSlotName()));
            f.player.setQuest(q.getSlotName(),1,"0");f.greet(f.npcName(q));assertTrue("Expired replacement must be reachable",f.has("replace"));f.click("replace");assertTrue(Long.parseLong(f.player.getQuest(q.getSlotName(),1))>0);
            f.player.setQuest(q.getSlotName(),"done;"+System.currentTimeMillis()+";5");f.greet(f.npcName(q));assertFalse(f.has("quest"));assertTrue(f.has("cooldown"));f.click("cooldown");assertEquals("done",f.player.getQuest(q.getSlotName(),0));
            f.player.setQuest(q.getSlotName(),1,"0");f.greet(f.npcName(q));assertTrue(f.has("quest"));f.click("quest");assertFalse(q.isCompleted(f.player));assertEquals("5",f.player.getQuest(q.getSlotName(),2));
        }
    }
    @Test public void dailyHuntSharedRecordKeepsBaselineWhenChangingOfficial(){
        f.greet("Mayor Sakhs");f.click("quest");String record=f.player.getQuest("daily");String creature=record.split(",")[0];
        f.greet("Urzędnik Hans");f.type("zadanie");assertEquals(record,f.player.getQuest("daily"));
        f.player.setSharedKill(creature);f.greet("Urzędnik Bill");f.click("deliver");assertTrue(f.player.isQuestCompleted("daily"));f.reward("XP ataku",2500);f.reward("XP obrony",3500);
        String done=f.player.getQuest("daily");f.player.setQuest("daily",1,"0");f.type("inny");assertEquals("done",f.player.getQuest("daily",0));
    }
    @Test public void josephineLegacyUnclaimedBootsArePaidOnceWithoutRepeatingOtherRewards(){
        f.player.setQuest("cloaks_collector","done");f.player.setQuest("cloaks_collector_2","done");int xp=f.player.getXP();
        f.greet("Josephine");assertTrue(f.payFeeIfRequested("cloaks_collector_2"));f.type("druga kolekcja");assertEquals("done;rewarded",f.player.getQuest("cloaks_collector_2"));f.reward("buty zabójcy",1);assertEquals(xp,f.player.getXP());f.capture("josephine-old-save-boots");
        f.greet("Josephine");assertEquals(1,f.player.getNumberOfEquipped("buty zabójcy"));assertTrue(f.latest().getRewards().isEmpty());
    }
    @Test public void baldmarHistoricalSoloConditionCannotBeSatisfiedBySharedKill(){
        IQuest q=f.quests.get("StuffForBaldemar");f.greet("Baldemar");f.click("quest");f.click("accept");
        for(Map.Entry<String,Integer> e:CollectionQuestProgress.details(q).getCollectionItems().entrySet())f.carry(e.getKey(),e.getValue());
        f.player.setSharedKill("latający złoty smok");f.greet("Baldemar");assertEquals("start",f.player.getQuest(q.getSlotName(),0));assertTrue(CollectionQuestProgress.remaining(q,f.player).isEmpty());
        f.player.setSoloKill("latający złoty smok");f.click("supplies");assertEquals("forging",f.player.getQuest(q.getSlotName(),0));
    }
}
