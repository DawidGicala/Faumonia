package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.npc.behaviour.impl.RepricedProducerBehaviour;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class RepricedGlovesTest {
    @Parameterized.Parameters public static Collection<Object[]> data(){return Arrays.asList(new Object[][]{
        {"Leatheron","leatheron_gloves","rękawice ze skóry tygrysa","skóra tygrysa"},
        {"Glover","glover_gloves","rękawice ze skóry lwa","skóra lwa"}});}
    final String name,slot,product,skin;
    JourneyQuestFixture f;SpeakerNPC npc;RepricedProducerBehaviour producer;
    public RepricedGlovesTest(String name,String slot,String product,String skin){this.name=name;this.slot=slot;this.product=product;this.skin=skin;}
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){
        f=new JourneyQuestFixture();f.createWorld();npc=new SpeakerNPC(name);
        Map<String,Integer> materials=new LinkedHashMap<String,Integer>();materials.put("money",1500);materials.put(skin,4);materials.put("skóra zwierzęca",8);
        producer=new RepricedProducerBehaviour(slot,Arrays.asList("wykonaj"),product,materials,0,1000);
        producer.installPayment(npc);npc.setCurrentState(ConversationStates.ATTENDING);
    }
    @After public void cleanup(){NPCList.get().remove(name);f.restoreNPCs();}
    @Test public void newOrderUsesFullPriceOnce(){
        f.carry("money",1500);f.carry(skin,4);f.carry("skóra zwierzęca",8);
        assertTrue(producer.transactAgreedDeal(new ItemParserResult(true,product,1,Collections.<String>emptySet()),new EventRaiser(npc),f.player));
        assertEquals(0,f.player.getNumberOfEquipped("money"));assertEquals(0,producer.remainingPayment(f.player));
        producer.giveProduct(new EventRaiser(npc),f.player);assertEquals(1,f.player.getNumberOfEquipped(product));
        assertTrue(f.player.isQuestCompleted(slot));
    }
    @Test public void legacyOrderCreditsOldPaymentAndRejectsInsufficientDifference(){
        f.player.setQuest(slot,"2;"+product+";0");f.carry("money",999);
        producer.giveProduct(new EventRaiser(npc),f.player);assertFalse(f.player.isEquipped(product));
        npc.getEngine().step(f.player,"dopłata");assertEquals(999,f.player.getNumberOfEquipped("money"));
        f.carry("money",1);npc.getEngine().step(f.player,"dopłata");
        assertEquals(0,f.player.getNumberOfEquipped("money"));assertEquals(2,f.player.getNumberOfEquipped(product));
        f.carry("money",500);npc.getEngine().step(f.player,"dopłata");assertEquals(500,f.player.getNumberOfEquipped("money"));
        assertEquals(2,f.player.getNumberOfEquipped(product));
    }
    @Test public void paidLegacyOrderSurvivesNoInventorySpaceAndSaveCopy(){
        String order="1;"+product+";0";
        f.player.setQuest(slot,order);f.carry("money",501);
        for(String carrying:games.stendhal.common.Constants.CARRYING_SLOTS){
            marauroa.common.game.RPSlot inventory=f.player.getSlot(carrying);inventory.setCapacity(1);
            if(inventory.size()==0)inventory.add(new games.stendhal.server.entity.item.StackableItem("filler","test","test",Collections.<String,String>emptyMap()));
        }
        npc.getEngine().step(f.player,"dopłata");
        assertEquals(1,f.player.getNumberOfEquipped("money"));
        assertEquals(order,f.player.getQuest(slot));assertEquals(0,f.player.getNumberOfEquipped(product));
        assertEquals(0,producer.remainingPayment(f.player));assertTrue(f.zone.dropped.isEmpty());
        games.stendhal.server.entity.player.Player restored=new games.stendhal.server.entity.player.Player(new marauroa.common.game.RPObject(f.player));
        assertEquals(0,producer.remainingPayment(restored));restored.getSlot("bag").setCapacity(20);
        producer.giveProduct(new EventRaiser(npc),restored);
        assertTrue(restored.isQuestCompleted(slot));assertEquals(1,restored.getNumberOfEquipped(product));
        assertEquals(1,restored.getNumberOfEquipped("money"));
    }
}
