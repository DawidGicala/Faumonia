package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.impl.DropItem;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import java.lang.reflect.Method;
import java.util.*;
import marauroa.common.game.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Internal weapons must remain usable by creatures without becoming player loot. */
public class InternalItemAccessTest {
    private static final Set<String> INTERNAL = new HashSet<String>(Arrays.asList(
        "luk waga 1", "luk waga 2", "luk waga 3", "luk waga 5", "luk waga 6", "luk waga 712", "luk waga 14", "luk waga 16",
        "sztylet waga 1", "sztylet waga 2", "sztylet waga 3", "sztylet waga 5"));
    private static final String[][] EQUIPPED = {
        {"serafin","luk waga 16"},{"azazel","luk waga 16"},{"żabiczłek","sztylet waga 2"},
        {"żabiczłek elitarny","sztylet waga 2"},{"żabiczłek czarodziej","sztylet waga 2"},
        {"risecia łucznik","luk waga 3"},{"wampir szaman","luk waga 712"},{"czarownica z Aenye","luk waga 14"}};
    private CollectionQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}

    @Test public void internalDefinitionsExistButNormalShopCatalogsDoNotOfferThem(){
        for(String name:INTERNAL)assertNotNull(name,SingletonRepository.getEntityManager().getItem(name));
        int products=0;
        for(SpeakerNPC npc:f.npcs.values())if(npc!=null){
            for(NpcDialogueSnapshot.Item item:NpcDialogueItems.read(npc,f.player,true))if(item.isForSale()){
                products++;assertFalse(npc.getName()+" offers "+item.getName(),INTERNAL.contains(item.getName()));
            }
        }
        assertTrue("Shop coverage",products>=20);
    }

    @Test public void equippedInternalWeaponsDoNotFallIntoCreatureCorpses() throws Exception {
        Method drop=Creature.class.getDeclaredMethod("dropItemsOn",Corpse.class);drop.setAccessible(true);
        int corpses=0;
        for(String[] entry:EQUIPPED){
            Creature creature=SingletonRepository.getEntityManager().getCreature(entry[0]);assertNotNull(entry[0],creature);
            assertTrue(entry[0]+" must retain its combat weapon",hasWeapon(creature,entry[1]));
            f.zone.add(creature);
            f.player.setPosition(creature.getX()+1,creature.getY());
            for(RPObject weapon:creature.getSlot("rhand")){
                RPAction steal=new RPAction();steal.put("type","equip");
                steal.put("source_path",Arrays.asList(Integer.toString(creature.getID().getObjectID()),"rhand",Integer.toString(weapon.getID().getObjectID())));
                steal.put("targetobject",f.player.getID().getObjectID());steal.put("targetslot","bag");
                new games.stendhal.server.actions.equip.EquipAction().onAction(f.player,steal);
                assertFalse("Monster equipment must not be lootable while alive",f.player.isEquipped(entry[1]));
            }
            List<DropItem> loot=new ArrayList<DropItem>(SingletonRepository.getEntityManager().getDefaultCreature(entry[0]).getDropItems());
            for(int i=0;i<25;i++){assertCorpse(drop,creature);corpses++;}
            // Force every configured drop separately: no reliance on a rare random outcome or corpse capacity.
            for(DropItem item:loot){
                assertFalse(entry[0]+" loot table",INTERNAL.contains(item.name));
                creature.clearDropItemList();creature.addDropItem(item.name,Double.MAX_VALUE,item.min,item.max);
                assertCorpse(drop,creature);corpses++;
            }
            creature.clearDropItemList();assertCorpse(drop,creature);corpses++;
            assertTrue("Loot must not unequip monster weapon",hasWeapon(creature,entry[1]));
        }
        System.out.println("INTERNAL_ITEM_CORPSES creatures="+EQUIPPED.length+" corpse_checks="+corpses);
    }
    private void assertCorpse(Method drop,Creature creature)throws Exception{
        Corpse corpse=new Corpse("player",0,0);drop.invoke(creature,corpse);
        for(RPObject item:corpse.getSlot("content"))assertFalse(creature.getName()+" leaked "+item,INTERNAL.contains(((Item)item).getName()));
    }
    private boolean hasWeapon(Creature creature,String name){
        if(!creature.hasSlot("rhand"))return false;
        for(RPObject object:creature.getSlot("rhand"))if(object instanceof Item&&name.equals(((Item)object).getName()))return true;
        return false;
    }

    @Test public void ordinaryPlayerCannotCreateInternalItemsUsingAdminActions(){
        assertEquals(0,f.player.getAdminLevel());
        for(String name:INTERNAL)for(String command:new String[]{"summon","summonat"}){
            RPAction action=new RPAction();action.put("type",command);action.put("creature",name);action.put("item",name);
            action.put("target",f.player.getName());action.put("slot","bag");action.put("x",2);action.put("y",2);
            int objects=f.zone.objects.size();assertFalse(command+" "+name,CommandCenter.execute(f.player,action));
            assertEquals(objects,f.zone.objects.size());assertFalse(f.player.isEquipped(name));
        }
    }
}
