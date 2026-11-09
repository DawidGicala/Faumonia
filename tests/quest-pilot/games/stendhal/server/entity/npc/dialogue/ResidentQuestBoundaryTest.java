package games.stendhal.server.entity.npc.dialogue;
import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.ResidentQuestProgress;
import games.stendhal.server.entity.item.*;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import marauroa.common.game.*;
import org.junit.*;

public class ResidentQuestBoundaryTest {
    ResidentQuestFixture f;
    @BeforeClass public static void init(){ResidentQuestFixture.init();}
    @AfterClass public static void finish(){ResidentQuestFixture.finish();}
    @Before public void setup(){f=new ResidentQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    void offer(String npc){f.greet(npc);if(f.has("quest"))f.click("quest");if(f.has("know")){f.click("know");f.click("ingredients");}}
    RPAction action(String key,String value){NpcDialogueSnapshot s=f.latest();RPAction a=new RPAction();a.put("session",s.getSession());a.put("revision",Long.toString(s.getRevision()));a.put(key,value);return a;}
    void send(RPAction action){ResidentQuestFixture.rules.turn+=34;new NpcDialogueAction().onAction(f.player,action);}
    @Test public void allNewRefusableRequestsCanResume(){
        for(String name:Arrays.asList("Hayunn","Soro","Tad","Hackim Easso","Nishiya","Diehelm Brui","Haunchy Meatoch","Elisabeth","Annie Jones","Anastasia","Coralia","Zara","Katinka","Agnus","Mrs. Yeti")){
            offer(name);f.click("decline");offer(name);f.click("accept");assertTrue(name,f.latest().isActive());assertFalse(name,f.has("accept"));
        }
    }
    @Test public void staleSharedIlisaQuestDoesNotConsumeHerb(){
        f.player.setQuest("introduce_players","corpse&herbs");f.greet("Ilisa");f.carry("arandula",1);f.type("tematy");RPAction a=action("reply","give_arandula");
        f.player.setQuest("learn_scrying","done");send(a);assertEquals("corpse&herbs",f.player.getQuest("introduce_players"));assertEquals(1,f.player.getNumberOfEquipped("arandula"));assertFalse(f.latest().getFeedback().isEmpty());
    }
    @Test public void staleHackimOtherStoryDoesNotConsumeTarts(){
        f.player.setQuest("mrsyeti","pies");f.carry("tarta",5);f.greet("Hackim Easso");RPAction a=action("reply","give_tarts");f.player.setQuest("news_hackim","done");send(a);assertEquals(5,f.player.getNumberOfEquipped("tarta"));assertEquals("pies",f.player.getQuest("mrsyeti"));
    }
    @Test public void closeUsesByeEvenAfterQuestChanges(){
        offer("Tad");RPAction close=action("close","1");f.player.setQuest("introduce_players","start");send(close);assertFalse(f.latest().isActive());assertFalse(f.npcs.get("Tad").isTalking());
    }
    @Test public void ordinaryChatAndPanelShowSameQuestTransition(){
        f.greet("Soro");ResidentQuestFixture.rules.turn+=34;f.npcs.get("Soro").listenTo(f.player,"zadanie");assertTrue(f.has("know"));f.click("know");f.click("ingredients");f.click("accept");assertEquals("ser=3",f.player.getQuest("cheese_for_soro"));
    }
    @Test public void confirmationAfterLosingIcecreamDoesNotAwardGift(){
        f.player.setQuest("icecream_for_annie","mummy");f.carry("lody",1);f.greet("Annie Jones");f.player.drop("lody");f.type("tak");assertEquals("mummy",f.player.getQuest("icecream_for_annie"));assertEquals(0,f.player.getNumberOfEquipped("prezent"));assertTrue(f.latest().getRewards().isEmpty());
    }
    @Test public void fullBagFlowerIsReportedOnGround(){
        f.player.setQuest("chocolate_for_elisabeth","mummy");f.carry("tabliczka czekolady",2);
        for(String slot:Constants.CARRYING_SLOTS){f.player.getSlot(slot).setCapacity(1);if(f.player.getSlot(slot).size()==0)f.player.getSlot(slot).add(new StackableItem("filler","test","test",Collections.<String,String>emptyMap()));}
        f.greet("Elisabeth");f.click("confirm");assertEquals(1,f.zone.dropped.size());Item flower=f.zone.dropped.get(0);f.reward(flower.getName(),1);boolean ground=false;for(NpcDialogueSnapshot.Reward r:f.latest().getRewards())if(r.getName().equals(flower.getName()))ground=r.isOnGround();assertTrue(ground);f.capture("28-elisabeth-ground-reward");
    }
    void allSoup(String npc,String quest,int xp){f.greet(npc);f.click("recipe");f.click("accept");for(String name:ResidentQuestProgress.requirements(f.quests.get(quest)).keySet())f.carry(name,1);double karma=f.player.getKarma();f.type("wszystko");f.reward("XP postaci",xp);assertEquals(karma,f.player.getKarma(),0.00001);assertTrue(f.quests.get(quest).isCompleted(f.player));}
    @Test public void soupLegacyAllAtOnceKeepsItsOwnKarmaRule(){allSoup("Old Mother Helena","Soup",20);}
    @Test public void fishSoupLegacyAllAtOnceKeepsThirtyXP(){allSoup("Florence Boullabaisse","FishSoup",30);}
    @Test public void soupCooldownDoesNotDiscardExistingProgress(){
        f.greet("Old Mother Helena");f.click("recipe");f.click("accept");f.carry("marchew",1);f.type("marchew");String saved=f.player.getQuest("soup_maker");f.greet("Old Mother Helena");assertEquals(saved,f.player.getQuest("soup_maker"));assertFalse(f.has("accept"));assertFalse(f.has("give_marchew"));
        f.player.setQuest("soup_maker","done;"+System.currentTimeMillis());f.greet("Old Mother Helena");assertFalse(f.has("accept"));f.type("zupa");assertFalse(f.has("accept"));
    }
    @Test public void expiredSoupCanBeCookedAgain(){
        f.player.setQuest("fishsoup_maker","done;"+(System.currentTimeMillis()-121000));f.greet("Florence Boullabaisse");f.click("accept");assertEquals("",f.player.getQuest("fishsoup_maker"));
    }
    @Test public void zooLegacyStartUsesRealPolishHamItem(){
        f.player.setQuest("zoo_food","start");f.greet("Katinka");assertEquals("szynka",f.player.getRequiredItemName("zoo_food",1));assertEquals(10,f.player.getRequiredItemQuantity("zoo_food",1));f.carry("szynka",10);f.type("tak");assertTrue(f.player.isQuestCompleted("zoo_food"));f.reward("XP postaci",2000);
    }
    @Test public void doctorOfferRequiresCurrentWeekAndUsesDiscountedPrice(){
        f.player.setQuest("zoo_food","done;"+System.currentTimeMillis());f.greet("Dr. Feelgood");f.click("offer");assertTrue(f.latest().isShop());assertFalse(f.latest().getItems().isEmpty());
        for(NpcDialogueSnapshot.Item row:f.latest().getItems())assertEquals((int)(.8f*SingletonRepository.getShopList().get("healing").get(row.getName())),row.getUnitPrice());f.capture("29-feelgood-shop");
        f.player.setQuest("zoo_food","done;"+(System.currentTimeMillis()-10081L*60000));assertTrue(NpcDialogueItems.read(f.npcs.get("Dr. Feelgood"),f.player,true).isEmpty());
    }
    @Test public void yetiShopLockedBeforeAndAvailableAfterRealQuestState(){
        f.greet("Mrs. Yeti");f.click("offer");assertTrue(f.latest().getItems().isEmpty());
        f.player.setQuest("mrsyeti","done");f.greet("Mrs. Yeti");f.click("offer");assertEquals(1,f.latest().getItems().size());assertEquals("płotka",f.latest().getItems().get(0).getName());assertEquals(100,f.latest().getItems().get(0).getUnitPrice());f.capture("30-yeti-shop");
    }
    @Test public void agnusCannotAcceptUnmetChildOrPayTwice(){
        offer("Agnus");f.click("accept");f.greet("Agnus");int xp=f.player.getXP();f.type("avalon");assertEquals(xp,f.player.getXP());assertEquals("looking:said",f.player.getQuest("find_rat_kids"));assertFalse(f.has("give_avalon"));
    }
    @Test public void allNativeCooldownsRespectStoredTimestamp(){
        String[][] rows={{"CoalForHaunchy","waiting","2880"},{"ChocolateForElisabeth","done","60"},{"IcecreamForAnnie","done","30"},{"Soup","done","10"},{"FishSoup","done","2"},{"FruitsForCoralia","done","1440"},{"ZooFood","done","10080"},{"FindRatChildren","done","14400"}};
        for(String[] r:rows){IQuest q=f.quests.get(r[0]);f.player.setQuest(q.getSlotName(),r[1]+";"+System.currentTimeMillis());assertFalse(r[0],q.isRepeatable(f.player));f.player.setQuest(q.getSlotName(),r[1]+";"+(System.currentTimeMillis()-(Long.parseLong(r[2])+1)*60000));assertTrue(r[0],q.isRepeatable(f.player));}
    }
    @Test public void pizzaFullBagDoesNotStartOrOverwriteDelivery(){
        for(String slot:Constants.CARRYING_SLOTS){f.player.getSlot(slot).setCapacity(1);f.player.getSlot(slot).add(new StackableItem("filler","test","test",Collections.<String,String>emptyMap()));}
        offer("Leander");f.click("accept");assertFalse(f.player.hasQuest("pizza_delivery"));assertTrue(f.latest().getSpeech().contains("miejsca"));
    }
    @Test public void repeatedDeliveryPacketPaysOnlyOnce(){
        f.player.setQuest("news_hackim","start");f.greet("Hackim Easso");f.greet("Xin Blanca");int xp=f.player.getXP();RPAction repeat=action("text","wiadomość hackima");send(repeat);send(repeat);assertEquals(xp,f.player.getXP());assertEquals(1,f.player.getNumberOfEquipped("hełm kolczy"));
    }
    @Test public void yetiRemindersAndHistoryRespectPolishSaveStates(){
        for(String step:Arrays.asList("hackim","eliksir")){
            f.player.setQuest("mrsyeti",step);f.greet("Mrs. Yeti");assertTrue(step,f.has("hints"));f.click("hints");assertTrue(f.latest().getSpeech().contains("Salvy"));assertEquals(step,f.player.getQuest("mrsyeti"));
        }
        f.player.setQuest("mrsyeti","mieczyk");assertFalse(new HelpMrsYeti().getHistory(f.player).toString().contains("Do eliksiru potrzeba"));
        f.player.setQuest("mrsyeti","eliksir");assertFalse(new HelpMrsYeti().getHistory(f.player).toString().contains("Muszę zanieść eliksir"));
    }
}
