package games.stendhal.server.entity.npc.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.PilotQuestProgress;
import games.stendhal.server.core.rp.quest.ResidentQuestProgress;
import games.stendhal.server.entity.creature.*;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import org.junit.*;

/** The group uses real NPC definitions, real quests and the same network action as the client. */
public class ResidentQuestDialogueTest {
    ResidentQuestFixture f;
    @BeforeClass public static void init(){ResidentQuestFixture.init();}
    @AfterClass public static void finish(){ResidentQuestFixture.finish();}
    @Before public void setup(){f=new ResidentQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    void begin(String npc){f.greet(npc);f.click("quest");f.click("accept");}
    void collect(String npc,String item,int amount){f.carry(item,amount);f.type("tematy");f.click("supplies");f.click("give_"+item);}
    void completeCollection(String npc,String quest){
        f.greet(npc);f.click("quest");f.click("know");f.click("ingredients");f.click("accept");
        for(Map.Entry<String,Integer> e:ResidentQuestProgress.requirements(f.quests.get(quest)).entrySet())f.carry(e.getKey(),e.getValue());
        f.click("supplies");for(String item:new ArrayList<String>(ResidentQuestProgress.requirements(f.quests.get(quest)).keySet())){f.click("give_"+item);if(f.payFeeIfRequested(f.quests.get(quest).getSlotName())){f.click("supplies");f.click("give_"+item);}}
    }
    @Test public void hayunnTutorialAndWaterReceipt(){
        completeCollection("Hayunn","TaskHayunn");assertEquals("done",f.player.getQuest("task_hayunn"));
        f.reward("XP postaci",200);f.reward("XP ataku",50);f.reward("XP obrony",50);f.reward("Karma",10);f.capture("01-hayunn-complete");
        int xp=f.player.getXP();f.type("tak");assertEquals(xp,f.player.getXP());assertFalse(f.has("give_woda"));
    }
    @Test public void soroPartialDeliveryAndNineItems(){
        f.greet("Soro");f.click("quest");f.click("know");f.click("ingredients");f.click("accept");
        f.carry("ser",1);f.click("supplies");f.click("give_ser");assertEquals("ser=2",f.player.getQuest("cheese_for_soro"));f.capture("02-soro-partial");
        f.carry("ser",2);f.type("ser");assertEquals("done",f.player.getQuest("cheese_for_soro"));
        f.reward("XP postaci",1000);f.reward("XP ataku",500);f.reward("XP obrony",500);f.reward("statuetka aniołka",1);assertEquals(13,f.latest().getRewards().size());f.capture("03-soro-reward");
    }
    @Test public void tadFullChainAndIlisaCoexistence(){
        begin("Tad");f.carry("flasza",1);f.type("flasza");assertEquals("ilisa",f.player.getQuest("introduce_players"));f.reward("XP postaci",100);f.reward("money",10);
        f.greet("Ilisa");assertEquals("corpse&herbs",f.player.getQuest("introduce_players"));f.reward("XP postaci",100);assertEquals(0,f.player.getNumberOfEquipped("flasza"));
        f.carry("arandula",1);f.type("tematy");f.click("give_arandula");f.reward("XP postaci",200);assertEquals("eliksir",f.player.getQuest("introduce_players"));f.capture("04-ilisa-tad");
        f.greet("Tad");assertTrue(f.player.isQuestCompleted("introduce_players"));f.reward("XP postaci",500);f.reward("butelka wody",50);f.reward("zwój semos",10);f.capture("05-tad-reward");
        int xp=f.player.getXP();f.type("lekarstwo tada");assertEquals(xp,f.player.getXP());
    }
    @Test public void newsCannotRestartAndHelmetMatchesNarrative(){
        begin("Hackim Easso");f.type("zadanie");assertFalse(f.has("accept"));assertEquals("start",f.player.getQuest("news_hackim"));
        f.greet("Xin Blanca");assertTrue(f.player.isQuestCompleted("news_hackim"));f.reward("XP postaci",200);f.reward("XP ataku",200);f.reward("XP obrony",300);f.reward("hełm kolczy",1);f.capture("06-xin-reward");
        int xp=f.player.getXP();f.type("wiadomość hackima");assertEquals(xp,f.player.getXP());
    }
    @Test public void karmaLessonAndIndicatorAwardOnce(){
        begin("Sarzina");assertTrue(f.player.hasFeature("karma_indicator"));f.reward("Karma",5);f.reward("Wskaźnik karmy",1);f.capture("07-karma-unlock");
        double karma=f.player.getKarma();f.click("karma");f.click("confirm");assertEquals(karma,f.player.getKarma(),0.00001);assertTrue(f.latest().getRewards().isEmpty());
    }
    @Test public void karmaRefusalKeepsOriginalMinusTenAndUnlock(){
        f.greet("Sarzina");f.click("quest");double before=f.player.getKarma();f.click("decline");
        assertEquals(before-10,f.player.getKarma(),0.00001);assertTrue(f.player.isQuestCompleted("learn_karma"));f.reward("Wskaźnik karmy",1);f.reward("Karma",0);
    }
    @Test public void sheepGiftIsNotACommercialSale(){
        begin("Nishiya");assertTrue(f.player.hasSheep());assertEquals("start",f.player.getQuest("sheep_growing"));f.capture("08-nishiya-sheep");
        Sheep sheep=f.player.getSheep();sheep.setWeight(100);f.greet("Sato");f.click("confirm");assertFalse(f.player.hasSheep());assertEquals("handed_over",f.player.getQuest("sheep_growing"));
        f.greet("Nishiya");f.click("confirm");assertEquals("done",f.player.getQuest("sheep_growing"));f.reward("XP postaci",30);f.reward("Karma",10);f.capture("09-nishiya-reward");
    }
    @Test public void sheepTooSmallAndNoSecondFreeSheep(){
        begin("Nishiya");Sheep first=f.player.getSheep();f.type("zadanie");assertSame(first,f.player.getSheep());assertFalse(f.has("accept"));
        f.greet("Sato");assertFalse(f.has("confirm"));assertTrue(f.player.hasSheep());assertEquals("start",f.player.getQuest("sheep_growing"));
    }
    @Test public void diehelmAllNativeRequirementsAndReward(){
        completeCollection("Diehelm Brui","ResourceForDiehelm");assertTrue(f.player.isQuestCompleted("resource_for_diehelm"));f.reward("XP postaci",1000000);f.reward("XP ataku",200000);f.reward("XP obrony",250000);f.reward("klucz do więzienia",1);f.capture("10-diehelm-reward");
    }
    @Test public void haunchyRandomBoundSteaksAndCooldown(){
        begin("Haunchy Meatoch");f.carry("węgiel",25);f.type("tematy");f.click("give_coal");int steaks=f.player.getNumberOfEquipped("grillowany stek");assertTrue(steaks>=1&&steaks<=20);
        f.reward("grillowany stek",steaks);f.reward("XP postaci",5000);assertFalse(f.quests.get("CoalForHaunchy").isRepeatable(f.player));f.capture("11-haunchy-reward");
        f.greet("Haunchy Meatoch");f.type("zadanie");assertFalse(f.has("accept"));
    }
    @Test public void elisabethNeedsMotherAndOnlyConsumesAfterConfirmation(){
        begin("Elisabeth");f.carry("tabliczka czekolady",1);f.greet("Elisabeth");assertEquals("start",f.player.getQuest("chocolate_for_elisabeth"));assertEquals(1,f.player.getNumberOfEquipped("tabliczka czekolady"));
        f.greet("Carey");assertEquals("mummy",f.player.getQuest("chocolate_for_elisabeth"));f.capture("12-carey-permission");
        f.greet("Elisabeth");f.click("confirm");assertTrue(f.player.isQuestCompleted("chocolate_for_elisabeth"));f.reward("XP postaci",500);f.reward("XP ataku",200);f.reward("XP obrony",250);
        int flowers=0;for(String n:Arrays.asList("stokrotki","bielikrasa","bratek")){flowers+=f.player.getNumberOfEquipped(n);f.reward(n,f.player.getNumberOfEquipped(n));}assertEquals(1,flowers);f.capture("13-elisabeth-reward");
    }
    @Test public void anniePermissionGiftAndCooldown(){
        begin("Annie Jones");f.carry("lody",1);f.greet("Mrs Jones");assertEquals("mummy",f.player.getQuest("icecream_for_annie"));
        f.greet("Annie Jones");f.click("confirm");assertTrue(f.player.isQuestCompleted("icecream_for_annie"));f.reward("prezent",1);f.reward("XP postaci",500);f.capture("14-annie-reward");assertFalse(f.quests.get("IcecreamForAnnie").isRepeatable(f.player));
    }
    void soup(String name,String quest,String item,int xp){
        f.greet(name);f.click("recipe");f.click("ingredients");f.capture(quest+"-recipe");f.click("accept");
        for(String food:ResidentQuestProgress.requirements(f.quests.get(quest)).keySet())f.carry(food,1);
        f.type("składniki");for(String food:ResidentQuestProgress.requirements(f.quests.get(quest)).keySet())f.click("give_"+food);
        assertTrue(f.quests.get(quest).isCompleted(f.player));f.reward("XP postaci",xp);f.reward("Karma",5);f.reward(item,1);
        assertEquals(1,f.zone.dropped.size());assertEquals(f.player.getName(),f.zone.dropped.get(0).getBoundTo());assertTrue(f.latest().getRewards().get(f.latest().getRewards().size()-1).isOnGround());f.capture(quest+"-ready");
    }
    @Test public void helenaPartialRecipeAndGroundPortion(){soup("Old Mother Helena","Soup","zupa",20);}
    @Test public void florenceFishRecipeAndGroundPortion(){soup("Florence Boullabaisse","FishSoup","zupa rybna",50);}
    @Test public void hughieSoupIsForChildAndPotionsForPlayer(){
        begin("Anastasia");f.carry("zupa rybna",1);f.type("zupa dla hughiego");f.click("confirm");assertTrue(f.quests.get("FishSoupForHughie").isCompleted(f.player));
        f.reward("XP postaci",10000);f.reward("wielki eliksir",50);f.reward("zupa rybna",0);assertEquals("Hughie",f.zone.dropped.get(0).getBoundTo());f.capture("17-hughie-reward");
    }
    @Test public void coraliaHintsDoNotConsumeFruitAndPartialDeliveryWorks(){
        begin("Coralia");f.carry("jabłko",4);f.click("hint_jabłko");assertEquals(4,f.player.getNumberOfEquipped("jabłko"));f.click("supplies");f.click("give_jabłko");
        for(Map.Entry<String,Integer> e:ResidentQuestProgress.remaining(f.quests.get("FruitsForCoralia"),f.player).entrySet())f.carry(e.getKey(),e.getValue());
        f.type("zadanie");for(String fruit:new ArrayList<String>(ResidentQuestProgress.remaining(f.quests.get("FruitsForCoralia"),f.player).keySet()))f.type(fruit);
        assertTrue(f.player.isQuestCompleted("fruits_coralia"));f.reward("XP postaci",10000);f.reward("naleśnik",f.player.getNumberOfEquipped("naleśnik"));f.capture("18-coralia-reward");
    }
    @Test public void zaraCreamAndBoundHouseKey(){
        begin("Zara");f.carry("olejek do opalania",1);f.type("olejek");f.click("confirm");assertTrue(f.quests.get("SuntanCreamForZara").isCompleted(f.player));f.reward("XP postaci",1000);f.reward("kluczyk Zary",1);f.capture("19-zara-reward");
    }
    @Test public void zooRandomFoodCannotBeRerolledByAskingAgain(){
        begin("Katinka");String initial=f.player.getQuest("zoo_food");f.type("zadanie");assertEquals(initial,f.player.getQuest("zoo_food"));assertFalse(f.has("accept"));
        String food=f.player.getRequiredItemName("zoo_food",1);int count=f.player.getRequiredItemQuantity("zoo_food",1);f.carry(food,count);f.type("karma dla zwierząt");f.click("confirm");
        assertTrue(f.player.isQuestCompleted("zoo_food"));f.reward("XP postaci",2000);f.reward("XP ataku",1000);f.reward("XP obrony",1000);f.capture("20-katinka-reward");f.greet("Dr. Feelgood");assertTrue(f.has("heal"));
    }
    @Test public void agnusAllChildrenNativeMeetingsAndReportedOnce(){
        begin("Agnus");
        for(String name:Arrays.asList("Avalon","Cody","Mariel","Opal")){
            // These original NPCs finish their greeting immediately; the quest still records the visit.
            for(games.stendhal.server.entity.npc.SpeakerNPC n:f.npcs.values())if(n.getAttending()==f.player)n.listenTo(f.player,"bye");
            games.stendhal.server.entity.npc.SpeakerNPC kid=f.npcs.get(name);f.player.setPosition(kid.getX()+1,kid.getY());ResidentQuestFixture.rules.turn+=34;kid.listenTo(f.player,"hi");
            f.reward("XP postaci",500);if("Avalon".equals(name))f.capture("31-avalon-found");
            assertTrue(f.player.getQuest("find_rat_kids").contains(name.toLowerCase()));
            f.greet("Agnus");f.click("give_"+name.toLowerCase());assertFalse(f.has("give_"+name.toLowerCase()));
        }
        assertTrue(f.player.isQuestCompleted("find_rat_kids"));f.reward("XP postaci",5000);f.reward("Karma",50);f.capture("21-agnus-reward");
    }
    @Test public void yetiWholeChainNativePetAndDelayedReward(){
        begin("Mrs. Yeti");f.greet("Salva Mattori");f.click("yeti");assertEquals("hackim",f.player.getQuest("mrsyeti"));
        f.greet("Hackim Easso");f.click("yeti");assertEquals("pies",f.player.getQuest("mrsyeti"));f.carry("tarta",5);f.type("salva");assertEquals("mieczyk",f.player.getQuest("mrsyeti"));
        f.carry("mieczyk",1);f.greet("Salva Mattori");f.click("give_knife");assertEquals("eliksir",f.player.getQuest("mrsyeti"));
        f.carry("lilia",3);for(String n:Arrays.asList("kokuda","napój z winogron","czarna perła"))f.carry(n,1);f.type("eliksir");assertEquals("gotpotion",f.player.getQuest("mrsyeti"));f.reward("eliksir miłości",1);f.capture("22-salva-potion");
        f.greet("Mrs. Yeti");f.click("give_potion");assertEquals("dragon",f.player.getQuest("mrsyeti"));
        BabyDragon pet=new BabyDragon(f.player);f.zone.add(pet);f.player.setPet(pet);f.type("smoki");assertEquals("reward",f.player.getQuest("mrsyeti",0));assertFalse(f.player.hasPet());f.reward("XP postaci",500);
        f.type("nagroda");assertFalse(f.player.isQuestCompleted("mrsyeti"));f.player.setQuest("mrsyeti","reward;"+(System.currentTimeMillis()-1441L*60000));f.greet("Mrs. Yeti");f.click("reward");assertTrue(f.player.isQuestCompleted("mrsyeti"));f.reward("XP postaci",500000);f.capture("23-yeti-reward");
    }
    @Test public void tomiOldDoneRecordAndStableRepeatReward(){
        f.carry("miecz lodowy",1);f.greet("tomi");f.click("give_sword");assertEquals("done;1",f.player.getQuest("help_tomi"));f.reward("XP postaci",1000);f.reward("Karma",30);
        f.player.setQuest("help_tomi","done");f.carry("miecz lodowy",1);f.greet("tomi");f.click("give_sword");assertEquals("done;2",f.player.getQuest("help_tomi"));f.reward("XP postaci",1000);f.reward("Karma",15);f.capture("24-tomi-reward");
    }
    void pizzaFor(String customer,long age){
        f.player.setQuest("pizza_delivery",customer+";"+(System.currentTimeMillis()-age));Item pizza=SingletonRepository.getEntityManager().getItem("pizza");pizza.setInfoString("Pizza Pasta");pizza.setBoundTo(customer);f.player.getSlot("bag").add(pizza);
    }
    @Test public void pizzaHotDeliveryAndColdDeliveryUseActualTips(){
        pizzaFor("Nishiya",0);f.greet("Nishiya");f.click("give_pizza");assertTrue(f.player.isQuestCompleted("pizza_delivery"));f.reward("money",10);f.reward("XP postaci",25);f.capture("25-pizza-hot");
        pizzaFor("Nishiya",121000);f.greet("Nishiya");f.click("give_pizza");f.reward("money",0);f.reward("XP postaci",12);f.capture("26-pizza-cold");
    }
    @Test public void pizzaWrongRecipientCannotCompleteCurrentDelivery(){
        pizzaFor("Nishiya",0);f.player.setQuest("pizza_delivery","Ouchit;"+System.currentTimeMillis());f.greet("Nishiya");int xp=f.player.getXP();f.type("pizza");assertEquals(xp,f.player.getXP());assertFalse(f.player.isQuestCompleted("pizza_delivery"));assertEquals(1,f.player.getNumberOfEquipped("pizza"));
    }
    @Test public void leanderStartsRealRandomAssignment(){
        begin("Leander");assertNotNull(f.player.getQuest("pizza_delivery"));assertEquals(1,f.player.getNumberOfEquipped("pizza"));assertNotNull(PizzaDelivery.customerHint(f.player.getQuest("pizza_delivery",0)));f.capture("27-leander-route");
    }
    @Test public void unknownCollectionSaveIsReadOnlyAndCannotOfferInventedDeliveries(){
        f.player.setQuest("resource_for_diehelm","bad=4");QuestProgress p=f.progress("ResourceForDiehelm");assertEquals(QuestProgress.State.UNKNOWN,p.getState());assertEquals("bad=4",f.player.getQuest("resource_for_diehelm"));
    }
}
