package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.Constants;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import java.util.*;
import marauroa.common.game.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Executes every available catalog row through the real request and confirmation handlers. */
public class ShopCatalogAuditTest {
    private CollectionQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    @Test public void ordinaryCatalogPricesMatchCompletedTransactions(){catalog(false);}
    @Test public void penaltyCatalogPricesMatchCompletedTransactions(){catalog(true);}
    private void clearInventory(){
        for(String slot:Constants.CARRYING_SLOTS){
            List<RPObject> objects=new ArrayList<RPObject>();
            for(RPObject item:f.player.getSlot(slot))objects.add(item);
            for(RPObject item:objects)f.player.getSlot(slot).remove(item.getID());
        }
    }
    private RPAction action(String key,String value){
        NpcDialogueSnapshot s=f.latest();RPAction a=new RPAction();a.put("session",s.getSession());
        a.put("revision",Long.toString(s.getRevision()));a.put(key,value);return a;
    }
    private void send(RPAction a){JourneyQuestFixture.rules.turn+=34;new NpcDialogueAction().onAction(f.player,a);}
    private NpcDialogueSnapshot.Item product(String name){
        for(NpcDialogueSnapshot.Item item:f.latest().getItems())if(item.isForSale()&&name.equals(item.getName()))return item;
        throw new AssertionError("Missing product "+name+" at "+f.latest().getNpcName()+": "+f.latest().getSpeech());
    }
    private void catalog(boolean penalty){
        f.player.setLevel(350);
        if(penalty)f.player.put("last_player_kill_time",Long.toString(System.currentTimeMillis()));
        assertEquals(penalty,f.player.isBadBoy());
        f.player.setQuest("zoo_food","done;"+System.currentTimeMillis());
        // Helena first introduces her soup quest. Its cooldown leaves the normal shop accessible.
        f.player.setQuest("soup_maker","done;"+System.currentTimeMillis());
        f.player.setQuest("chaos_tunnels","done");f.player.setQuest("mrsyeti","done");
        List<String> names=new ArrayList<String>(f.npcs.keySet());Collections.sort(names);
        int shops=0,products=0,completed=0,refused=0;
        for(String name:names){
            SpeakerNPC npc=f.npcs.get(name);
            List<NpcDialogueSnapshot.Item> rows=NpcDialogueItems.read(npc,f.player,true);
            List<String> goods=new ArrayList<String>();
            for(NpcDialogueSnapshot.Item item:rows)if(item.isForSale())goods.add(item.getName());
            if(goods.isEmpty())continue;
            shops++;
            for(String good:goods){
                products++;
                for(int requested:new int[]{1,1000}){
                    clearInventory();f.player.clearEvents();f.greet(name);f.type("oferta");
                    NpcDialogueSnapshot.Item item=product(good);int count=Math.min(requested,item.getMaxAmount());
                    assertTrue(name+" "+good,count>0);long price=item.priceFor(count);
                    assertTrue("Reachable catalog price within money range: "+name+" "+good,price>=0&&price<Integer.MAX_VALUE);
                    f.carry("money",(int)price+1);
                    RPAction buy=action("buy",item.getId());buy.put("amount",Integer.toString(count));send(buy);
                    assertEquals("No debit before confirmation",price+1,f.player.getNumberOfEquipped("money"));
                    assertEquals("No goods before confirmation",0,f.player.getNumberOfEquipped(good));
                    RPAction confirm=action("reply","confirm");send(confirm);send(confirm);send(buy);
                    assertEquals(name+" / "+good+" / "+count+" / penalty="+penalty+" speech="+f.latest().getSpeech(),1,f.player.getNumberOfEquipped("money"));
                    assertEquals(name+" / "+good,count,f.player.getNumberOfEquipped(good));completed++;

                    // Empty the purse after a valid offer, then confirm through ordinary text.
                    clearInventory();f.greet(name);f.type("oferta");item=product(good);
                    f.carry("money",(int)price+1);buy=action("buy",item.getId());buy.put("amount",Integer.toString(count));send(buy);
                    f.player.drop("money",(int)price+1);JourneyQuestFixture.rules.turn+=34;npc.listenTo(f.player,"tak");
                    assertEquals(name+" unexpected debit",0,f.player.getNumberOfEquipped("money"));
                    assertEquals(name+" unpaid goods "+good,price==0?count:0,f.player.getNumberOfEquipped(good));refused++;
                }
            }
        }
        assertTrue("Catalog coverage",shops>=5&&products>=20);
        System.out.println("SHOP_CATALOG_AUDIT penalty="+penalty+" shops="+shops+" products="+products+" completed="+completed+" insufficientFunds="+refused);
    }
}
