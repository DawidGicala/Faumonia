package games.stendhal.server.entity.npc.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.item.*;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import marauroa.common.game.RPAction;
import org.junit.*;

/** Extra acceptance boundaries using real NPCs, items and native actions, without a database. */
public class DeepReviewBoundaryTest {
    CollectionQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    RPAction action(String key,String value){NpcDialogueSnapshot s=f.latest();RPAction a=new RPAction();a.put("session",s.getSession());a.put("revision",Long.toString(s.getRevision()));a.put(key,value);return a;}
    void send(RPAction a){JourneyQuestFixture.rules.turn+=34;new NpcDialogueAction().onAction(f.player,a);}
    void buyFirst(int count){NpcDialogueSnapshot.Item item=f.latest().getItems().get(0);RPAction a=action("buy",item.getId());a.put("amount",Integer.toString(count));send(a);}
    void fillRemainingSlots(){
        for(String slot:Constants.CARRYING_SLOTS){f.player.getSlot(slot).setCapacity(Math.max(1,f.player.getSlot(slot).size()));if(f.player.getSlot(slot).size()==0)f.player.getSlot(slot).add(new StackableItem("filler","test","test",Collections.<String,String>emptyMap()));}
    }
    @Test public void unlockedShopRechecksAccessAtConfirmationIncludingPlainChat(){
        f.player.setQuest("chaos_tunnels","done");f.carry("money",100000);f.greet("Thorgal");f.click("offer");buyFirst(1);
        assertEquals(ConversationStates.BUY_PRICE_OFFERED,f.npcs.get("Thorgal").getEngine().getCurrentState());
        f.player.setQuest("chaos_tunnels","start");
        // Plain chat reaches the same native seller; the confirmation must recheck access.
        f.npcs.get("Thorgal").listenTo(f.player,"tak");
        assertEquals(100000,f.player.getNumberOfEquipped("money"));assertEquals(0,f.player.getNumberOfEquipped("zwój chaosu"));
        assertFalse(f.has("confirm"));
    }
    @Test public void doctorDiscountActuallyChargesTheDisplayedPrice(){
        f.player.setQuest("zoo_food","done;"+System.currentTimeMillis());f.carry("money",100000);f.greet("Dr. Feelgood");f.click("offer");
        NpcDialogueSnapshot.Item item=f.latest().getItems().get(0);long cost=item.priceFor(3);buyFirst(3);f.click("confirm");
        assertEquals(100000-cost,f.player.getNumberOfEquipped("money"));assertEquals(3,f.player.getNumberOfEquipped(item.getName()));
        assertTrue(f.latest().isShop());
    }
    @Test public void doctorExpiredPermissionCannotBeUsedByTypingThePurchase(){
        f.player.setQuest("zoo_food","done;"+(System.currentTimeMillis()-10081L*60000));f.carry("money",100000);
        // An old open encounter may outlive the weekly permission.
        SpeakerNPC doctor=f.npcs.get("Dr. Feelgood");f.player.setPosition(doctor.getX()+1,doctor.getY());doctor.setAttending(f.player);doctor.setCurrentState(ConversationStates.ATTENDING);
        JourneyQuestFixture.rules.turn+=34;doctor.listenTo(f.player,"buy 1 mały eliksir");
        assertNotEqualsForOldJUnit(ConversationStates.BUY_PRICE_OFFERED,doctor.getEngine().getCurrentState());
        JourneyQuestFixture.rules.turn+=34;doctor.listenTo(f.player,"tak");
        assertEquals(100000,f.player.getNumberOfEquipped("money"));assertEquals(0,f.player.getNumberOfEquipped("mały eliksir"));
    }
    @Test public void doctorWeeklyAccessIsRecheckedAfterTheQuote(){
        f.player.setQuest("zoo_food","done;"+System.currentTimeMillis());f.carry("money",100000);f.greet("Dr. Feelgood");f.click("offer");
        String item=f.latest().getItems().get(0).getName();buyFirst(2);
        f.player.setQuest("zoo_food","done;"+(System.currentTimeMillis()-10081L*60000));
        f.npcs.get("Dr. Feelgood").listenTo(f.player,"tak");
        assertEquals(100000,f.player.getNumberOfEquipped("money"));assertEquals(0,f.player.getNumberOfEquipped(item));
    }
    @Test public void doctorHealsPetsOnlyWithCurrentWeeklyAccess(){
        games.stendhal.server.entity.creature.BabyDragon pet=new games.stendhal.server.entity.creature.BabyDragon(f.player);
        f.zone.add(pet);f.player.setPet(pet);pet.setHP(1);
        f.player.setQuest("zoo_food","done;"+System.currentTimeMillis());f.greet("Dr. Feelgood");
        assertSame(pet,f.player.getPet());
        f.player.setQuest("zoo_food","done;"+(System.currentTimeMillis()-10081L*60000));f.npcs.get("Dr. Feelgood").listenTo(f.player,"ulecz");assertEquals(1,pet.getHP());
        f.player.setQuest("zoo_food","done;"+System.currentTimeMillis());f.npcs.get("Dr. Feelgood").listenTo(f.player,"ulecz");assertEquals(pet.getBaseHP(),pet.getHP());
    }
    void assertNotEqualsForOldJUnit(Object a,Object b){assertFalse("Unexpected state: "+b,a.equals(b));}
    @Test public void collectionExcessItemsRemainAndCompletedRewardCannotRepeat(){
        IQuest q=f.quests.get("SwordsForNnyddion");String item=CollectionQuestProgress.details(q).getCollectionItems().keySet().iterator().next();int required=CollectionQuestProgress.details(q).getCollectionItems().get(item);
        f.greet("Nnyddion");f.click("quest");f.click("accept");f.carry(item,required+1);f.greet("Nnyddion");
        for(int i=0;i<required;i++)f.click("give_"+item);
        assertTrue(q.isCompleted(f.player));assertEquals(1,f.player.getNumberOfEquipped(item));
        int xp=f.player.getXP();f.greet("Nnyddion");f.type("dostawa");f.type("tak");assertEquals(xp,f.player.getXP());assertEquals(1,f.player.getNumberOfEquipped(item));
    }
    @Test public void finalCollectionRewardOnGroundIsReportedWithoutCountingOldFloorItems(){
        f.player.setQuest("cloaks_collector","done");f.player.setQuest("cloaks_collector_2","done");
        f.carry("money",800001);Item old=SingletonRepository.getEntityManager().getItem("buty zabójcy");f.zone.add(old);fillRemainingSlots();
        f.greet("Josephine");assertTrue(f.payFeeIfRequested("cloaks_collector_2"));f.type("druga kolekcja");f.reward("buty zabójcy",1);assertEquals(2,f.zone.dropped.size());
        boolean onGround=false;for(NpcDialogueSnapshot.Reward r:f.latest().getRewards())if("buty zabójcy".equals(r.getName())){assertEquals(1,r.getAmount(),0);onGround=r.isOnGround();}assertTrue(onGround);
        f.greet("Josephine");assertEquals(2,f.zone.dropped.size());assertTrue(f.latest().getRewards().isEmpty());
    }
    @Test public void partialCollectorSaveSurvivesPlayerObjectCopy(){
        IQuest q=f.quests.get("DragonCave");f.greet("Terry");f.click("quest");f.click("accept");
        String first=CollectionQuestProgress.remaining(q,f.player).keySet().iterator().next();int required=CollectionQuestProgress.remaining(q,f.player).get(first);f.carry(first,1);f.greet("Terry");f.click("supplies");f.click("give_"+first);
        String saved=f.player.getQuest(q.getSlotName());marauroa.common.game.RPObject copy=new marauroa.common.game.RPObject(f.player);
        games.stendhal.server.entity.player.Player restored=new games.stendhal.server.entity.player.Player(copy);
        assertEquals(saved,restored.getQuest(q.getSlotName()));assertEquals(Integer.valueOf(required-1),CollectionQuestProgress.remaining(q,restored).get(first));
        assertEquals(CollectionQuestProgress.remaining(q,f.player),CollectionQuestProgress.remaining(q,restored));
    }
    @Test public void newHuntNpcCanBeReleasedWithWindowCloseDuringAnOffer(){
        for(String name:Arrays.asList("Walt","Octavio","Linda","Mayor Sakhs","Urzędnik Hans","Urzędnik Bill","Grafindle","Phalk","Josephine")){
            f.greet(name);if(f.has("quest"))f.click("quest");send(action("close","1"));assertFalse(name,f.npcs.get(name).isTalking());assertFalse(name,f.latest().isActive());
        }
    }
    @Test public void huntReminderPreservesCountersAndWireProgress(){
        f.start("Walt");f.player.setSoloKill("arachne");String saved=f.player.getQuest("kill_ados_spiders");
        QuestProgress before=f.progress("KillAdosSpiders");String wire=String.valueOf(before.toWire());f.greet("Walt");f.click("supplies");
        assertEquals(saved,f.player.getQuest("kill_ados_spiders"));assertEquals(wire,String.valueOf(f.progress("KillAdosSpiders").toWire()));
    }
}
