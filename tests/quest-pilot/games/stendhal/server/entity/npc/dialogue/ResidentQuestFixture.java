package games.stendhal.server.entity.npc.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.Constants;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.NpcDialogueSnapshot.Reply;
import games.stendhal.common.QuestProgress;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.core.engine.*;
import games.stendhal.server.core.rp.quest.PilotQuestProgress;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import marauroa.common.game.*;
import marauroa.server.db.TransactionPool;
import marauroa.server.db.command.DBCommandQueue;
import org.junit.*;

/** Uses production NPC definitions, quest transitions, parser, actions and wire snapshots. */
public class ResidentQuestFixture {
    static LocalRules rules;
    static LocalWorld world;
    LocalZone zone;
    Player player;
    final Map<String,SpeakerNPC> originals = new LinkedHashMap<String,SpeakerNPC>();
    final Map<String,SpeakerNPC> npcs = new LinkedHashMap<String,SpeakerNPC>();
    final Map<String,IQuest> quests = new LinkedHashMap<String,IQuest>();
    static class LocalRules extends StendhalRPRuleProcessor {
        final StendhalRPRuleProcessor previous; int turn=100;
        LocalRules() { previous=instance; instance=this; }
        @Override public int getTurn() { return turn; }
        void restore() { instance=previous; }
    }
    static class LocalWorld extends StendhalRPWorld {
        final StendhalRPWorld previous; LocalZone zone;
        LocalWorld() { previous=instance; instance=this; }
        @Override public StendhalRPZone getZone(String id) { return zone; }
        @Override public RPObject get(RPObject.ID id) { return zone.objects.get(id); }
        void restore() { instance=previous; }
    }
    static class LocalZone extends StendhalRPZone {
        final List<Item> dropped=new ArrayList<Item>();
        final Map<RPObject.ID,RPObject> objects=new HashMap<RPObject.ID,RPObject>();
        LocalZone() { super("resident_quests_test"); }
        @Override public synchronized void add(RPObject object) {
            assignRPObjectID(object); ((Entity)object).onAdded(this);
            objects.put(object.getID(),object);
            if (object instanceof SpeakerNPC) { NPCList.get().add((SpeakerNPC)object); }
            if (object instanceof Item) { dropped.add((Item)object); }
            if (object instanceof games.stendhal.server.entity.creature.DomesticAnimal) { getPlayerAndFriends().add((games.stendhal.server.entity.RPEntity)object); }
            if (object instanceof Player) { getPlayers().add((Player)object); getPlayerAndFriends().add((Player)object); }
        }
        @Override public void add(RPObject object,boolean expire) { add(object); }
        @Override public synchronized void modify(RPObject object) { }
        @Override public boolean collides(Entity entity,double x,double y){return false;}
        @Override public boolean collides(Entity entity,double x,double y,boolean objects){return false;}
        @Override public synchronized RPObject remove(RPObject object){getPlayerAndFriends().remove(object);objects.remove(object.getID());return object;}
        @Override public int getWidth(){return 256;}
        @Override public int getHeight(){return 256;}
    }
    public static void init() {
        assertNull(TransactionPool.get()); new RPClassGenerator().createRPClasses();
        rules=new LocalRules(); world=new LocalWorld();
    }
    public static void finish() {
        world.restore(); rules.restore(); assertNull(TransactionPool.get()); DBCommandQueue.get().finish();
    }
    public void createWorld() {
        rules.turn=100; zone=new LocalZone(); world.zone=zone;
        String[] names={"Fidorea","Eliza","Anastasia","Haizen","Haunchy Meatoch","Florence Boullabaisse","Katinka","Dr. Feelgood","Balduin","Coralia","Martin Farmer","Zara","Sarzina","Old Mother Helena","Annie Jones","Mrs Jones","Elisabeth","Carey","Salva Mattori","Diehelm Brui","tomi","Opal","Mariel","Cody","Avalon","Jynath","Agnus","Leander","Hackim Easso","Sato","Tor'Koom","Hayunn","Tad","Marcus","Jenny","Ouchit","Xin Blanca","Ilisa","Ketteh Wehoh","Nishiya","Soro","Mrs. Yeti"};
        for(String name:names)originals.put(name,NPCList.get().remove(name));
        Map<String,String> attrs=Collections.emptyMap();
        new games.stendhal.server.maps.ados.city.MakeupArtistNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.coast.FerryConveyerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.farmhouse.MotherNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.magician_house.WizardNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.market.BBQGrillmasterNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.market.FishSoupMakerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.outside.AnimalKeeperNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.outside.VeterinarianNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.rock.WeaponsCollectorNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.tavern.BarMaidNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.wall.HolidayingManNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.athor.holiday_area.TouristFromAdosNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.fado.hut.SellerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.fado.tavern.MaidNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.kalavan.citygardens.LittleGirlNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.kalavan.citygardens.MummyNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.kirdneh.city.LittleGirlNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.kirdneh.city.MummyNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.magic.city.HealerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.mithrilbourgh.stores.BuyerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.nalwor.hell.CaptiveNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.orril.dungeon.RatChild1NPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.orril.dungeon.RatChild2NPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.orril.dungeon.RatChildBoy1NPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.orril.dungeon.RatChildBoy2NPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.orril.magician_house.WitchNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ratcity.house1.OldRatWomanNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.bakery.ChefNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.blacksmith.BlacksmithAssistantNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.city.SheepBuyerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.dungeon.SheepBuyerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.guardhouse.HayunnNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.hostel.BoyNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.jail.GuardNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.plains.MillerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.tavern.BowAndArrowSellerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.tavern.TraderNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.temple.HealerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.townhall.DecencyAndMannersWardenNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.village.SheepSellerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.village.SoroNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.yeticave.MrsYetiNPC().configureZone(zone,attrs);
        new LearnAboutOrbs().addToWorld();
        for(IQuest quest:Arrays.<IQuest>asList(new TaskHayunn(),new CheeseForSoro(),new MedicineForTad(),new NewsFromHackim(),new LearnAboutKarma(),new SheepGrowing(),new ResourceForDiehelm(),new CoalForHaunchy(),new ChocolateForElisabeth(),new IcecreamForAnnie(),new Soup(),new FishSoup(),new FishSoupForHughie(),new FruitsForCoralia(),new SuntanCreamForZara(),new ZooFood(),new FindRatChildren(),new HelpMrsYeti(),new HelpTomi(),new PizzaDelivery())){quest.addToWorld();quests.put(quest.getClass().getSimpleName(),quest);}
        for (String name:names) { npcs.put(name,NPCList.get().get(name)); }
        player=new Player(new RPObject()); player.put("name","ResidentLocal");
        player.put("base_hp",100); player.put("hp",100); player.put("atk",10); player.put("def",10);
        player.put("level",11); player.put("atk_xp",0); player.put("def_xp",0);
        for(String slot:Constants.CARRYING_SLOTS) { player.addSlot(slot); }
        for(String slot:new String[]{"!quests","!kills","!tutorial","!visited"}) { player.addSlot(slot); player.getSlot(slot).add(new RPObject()); }
        player.update(); zone.add(player); zone.dropped.clear();
        player.setOutfit(new games.stendhal.server.entity.Outfit(0,0,0,0),false);
        player.setXP(games.stendhal.common.Level.getXP(11));
    }
    public void restoreNPCs() {
        for(SpeakerNPC npc:npcs.values()) { if(npc!=null) { npc.setAttending(null); NPCList.get().remove(npc.getName()); } }
        for(SpeakerNPC original:originals.values()) { if(original!=null) { NPCList.get().add(original); } }
    }
    NpcDialogueSnapshot latest() {
        NpcDialogueSnapshot result=null;
        for(RPEvent event:player.events()) { if(NpcDialogueSnapshot.EVENT.equals(event.getName())) { result=NpcDialogueSnapshot.fromWire(event.getList("data")); } }
        assertNotNull("Missing dialogue snapshot",result); return result;
    }
    NpcDialogueSnapshot greet(String name) {
        for(SpeakerNPC npc:npcs.values()) { if(npc.getAttending()==player) { npc.listenTo(player,"bye"); } }
        SpeakerNPC npc=npcs.get(name); player.setPosition(npc.getX()+1,npc.getY());
        rules.turn+=34; npc.listenTo(player,"hi");
        assertEquals(name,latest().getNpcName()); assertTrue(latest().isActive()); return latest();
    }
    NpcDialogueSnapshot click(String id) {
        id=ResidentDialogueReplies.id(id);
        assertTrue("Missing choice "+id+" at "+latest().getNpcName()+": "+latest().getSpeech()+"; choices="+choiceIds(),has(id));
        NpcDialogueSnapshot snapshot=latest(); RPAction action=new RPAction();
        action.put("session",snapshot.getSession()); action.put("revision",Long.toString(snapshot.getRevision())); action.put("reply",id);
        rules.turn+=34; new NpcDialogueAction().onAction(player,action); return latest();
    }
    NpcDialogueSnapshot type(String text) {
        NpcDialogueSnapshot snapshot=latest(); RPAction action=new RPAction();
        action.put("session",snapshot.getSession()); action.put("revision",Long.toString(snapshot.getRevision())); action.put("text",text);
        rules.turn+=34; new NpcDialogueAction().onAction(player,action); return latest();
    }
    boolean has(String id) { for(Reply r:latest().getReplies()) { if(r.getId().equals(id)) return true; } return false; }
    List<String> choiceIds() { List<String> ids=new ArrayList<String>(); for(Reply r:latest().getReplies()) ids.add(r.getId()); return ids; }
    boolean payFeeIfRequested(String slot) {
        games.stendhal.server.maps.quests.logic.QuestGoldFees.Fee fee=games.stendhal.server.maps.quests.logic.QuestGoldFees.forSlot(slot);
        if(fee==null || !fee.pending(player))return false;
        int price=fee.price(player), before=player.getNumberOfEquipped("money");
        if(before<price){carry("money",price-before);before=price;}
        click("give_fee_"+fee.id+"_"+price);
        assertEquals("Payment must be explicit and exact",before-price,player.getNumberOfEquipped("money"));
        assertTrue("Payment must survive retries",fee.paid(player));
        return true;
    }
    void carry(String name,int quantity) {
        Item item=SingletonRepository.getEntityManager().getItem(name); assertNotNull(name,item);
        if(item instanceof StackableItem) { ((StackableItem)item).setQuantity(quantity); player.getSlot("bag").add(item); }
        else { for(int i=0;i<quantity;i++) { player.getSlot("bag").add(i==0?item:SingletonRepository.getEntityManager().getItem(name)); } }
    }
    void reward(String name,double amount) {
        double actual=0; for(NpcDialogueSnapshot.Reward r:latest().getRewards()) if(name.equals(r.getName())) actual+=r.getAmount();
        assertEquals(name,amount,actual,0.000001);
    }
    QuestProgress progress(String name) { return PilotQuestProgress.read(quests.get(name),player); }
    void start(String name) { greet(name); click("quest"); click("accept"); }
    void capture(String name) {
        try {
            java.nio.file.Path dir=java.nio.file.Paths.get(System.getProperty("quest.pilot.output","."),"resident-scenes");
            java.nio.file.Files.createDirectories(dir);
            try(java.io.DataOutputStream out=new java.io.DataOutputStream(java.nio.file.Files.newOutputStream(dir.resolve(name+".wire")))) {
                List<String> values=latest().toWire(); out.writeInt(values.size()); for(String value:values) out.writeUTF(value);
            }
        } catch(java.io.IOException e) { throw new AssertionError(e); }
    }

}
