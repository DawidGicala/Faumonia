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
public class AdditionalQuestDialogueTest {
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
        void restore() { instance=previous; }
    }
    static class LocalZone extends StendhalRPZone {
        final List<Item> dropped=new ArrayList<Item>();
        LocalZone() { super("seven_quests_test"); }
        @Override public synchronized void add(RPObject object) {
            assignRPObjectID(object); ((Entity)object).onAdded(this);
            if (object instanceof SpeakerNPC) { NPCList.get().add((SpeakerNPC)object); }
            if (object instanceof Item) { dropped.add((Item)object); }
            if (object instanceof Player) { getPlayers().add((Player)object); getPlayerAndFriends().add((Player)object); }
        }
        @Override public void add(RPObject object,boolean expire) { add(object); }
        @Override public synchronized void modify(RPObject object) { }
    }
    @BeforeClass public static void init() {
        assertNull(TransactionPool.get()); new RPClassGenerator().createRPClasses();
        rules=new LocalRules(); world=new LocalWorld();
    }
    @AfterClass public static void finish() {
        world.restore(); rules.restore(); assertNull(TransactionPool.get()); DBCommandQueue.get().finish();
    }
    @Before public void createWorld() {
        rules.turn=100; zone=new LocalZone(); world.zone=zone;
        String[] names={"Plink","Ilisa","Xoderos","Joshua","Jef","Amber","Eonna","Sally","Lupos","Tad","Ketteh Wehoh"};
        for (String name:names) { originals.put(name,NPCList.get().remove(name)); }
        Map<String,String> attrs=Collections.emptyMap();
        new games.stendhal.server.maps.semos.plains.LittleBoyNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.temple.HealerNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.blacksmith.BlacksmithNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.ados.goldsmith.GoldsmithNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.kirdneh.city.GossipNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.fado.forest.OldWomanNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.semos.storage.HousewifeNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.orril.river.CampingGirlNPC().configureZone(zone,attrs);
        new games.stendhal.server.maps.fado.forest.CraftsmanNPC().configureZone(zone,attrs);
        zone.add(new SpeakerNPC("Tad")); zone.add(new SpeakerNPC("Ketteh Wehoh"));
        // Ilisa is shared with an existing quest outside this batch.
        new MedicineForTad().addToWorld();
        for (IQuest quest:Arrays.<IQuest>asList(new PlinksToy(),new LearnAboutOrbs(),new HungryJoshua(),
                new FindJefsMom(),new CleanStorageSpace(),new Campfire(),new ElvishArmor())) {
            quest.addToWorld(); quests.put(quest.getName(),quest);
        }
        for (String name:names) { npcs.put(name,NPCList.get().get(name)); }
        player=new Player(new RPObject()); player.put("name","SevenLocal");
        player.put("base_hp",100); player.put("hp",100); player.put("atk",10); player.put("def",10);
        player.put("level",11); player.put("atk_xp",0); player.put("def_xp",0);
        for(String slot:Constants.CARRYING_SLOTS) { player.addSlot(slot); }
        for(String slot:new String[]{"!quests","!kills","!tutorial","!visited"}) { player.addSlot(slot); player.getSlot(slot).add(new RPObject()); }
        player.update(); zone.add(player); zone.dropped.clear();
        player.setXP(games.stendhal.common.Level.getXP(11));
    }
    @After public void restoreNPCs() {
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
            java.nio.file.Path dir=java.nio.file.Paths.get(System.getProperty("quest.pilot.output","."),"next-seven-scenes");
            java.nio.file.Files.createDirectories(dir);
            try(java.io.DataOutputStream out=new java.io.DataOutputStream(java.nio.file.Files.newOutputStream(dir.resolve(name+".wire")))) {
                List<String> values=latest().toWire(); out.writeInt(values.size()); for(String value:values) out.writeUTF(value);
            }
        } catch(java.io.IOException e) { throw new AssertionError(e); }
    }

    @Test public void plinkFullFlowHintsRemainAnOfferAndRewardOnlyOnce() {
        greet("Plink"); capture("01-plink-offer"); click("hints"); assertTrue(has("accept")); click("accept");
        assertEquals("start",player.getQuest("plinks_toy")); assertFalse(has("quest"));
        greet("Plink"); assertFalse(has("accept")); assertEquals(QuestProgress.State.ACTIVE,progress("PlinksToy").getState());
        carry("pluszowy miś",1); greet("Plink");
        assertEquals("done",player.getQuest("plinks_toy")); reward("XP postaci",200); reward("XP ataku",100); reward("XP obrony",150); reward("Karma",30); capture("02-plink-reward");
        assertEquals(0,player.getNumberOfEquipped("pluszowy miś")); int xp=player.getXP(); greet("Plink"); assertEquals(xp,player.getXP()); assertTrue(latest().getRewards().isEmpty());
    }
    @Test public void plinkCanReceiveAlreadyFoundToyWithoutAcceptingFirst() {
        carry("pluszowy miś",1); greet("Plink"); assertTrue(player.isQuestCompleted("plinks_toy")); reward("XP postaci",200);
    }
    @Test public void plinkAcceptThenHandInDuringSameConversation() {
        greet("Plink"); click("accept"); carry("pluszowy miś",1); type("tematy"); click("give_teddy");
        assertTrue(player.isQuestCompleted("plinks_toy")); reward("XP postaci",200);
    }
    @Test public void ilisaTeachesAndReminderDoesNotAwardAgain() {
        greet("Ilisa"); click("quest"); click("instructions"); capture("03-ilisa-lesson"); click("cancel"); assertFalse(player.hasQuest("learn_scrying"));
        click("confirm"); assertTrue(player.isQuestCompleted("learn_scrying")); reward("XP postaci",50);
        greet("Ilisa"); click("instructions"); assertTrue(latest().getRewards().isEmpty()); assertFalse(has("quest"));
    }
    @Test public void ilisaLevelTenCannotFinishLesson() {
        player.setXP(games.stendhal.common.Level.getXP(10)); player.setLevel(10);
        greet("Ilisa"); click("quest"); click("instructions");
        assertFalse("Lesson must not be completed below level 11",player.hasQuest("learn_scrying"));
        assertFalse("Lesson confirmation must not be available",has("confirm")); assertTrue(latest().getSpeech().contains("11."));
    }
    @Test public void ilisaExistingTadQuestStillConsumesFlaskOnGreeting() {
        player.setQuest("introduce_players","ilisa"); carry("flasza",1); greet("Ilisa");
        assertEquals("corpse&herbs",player.getQuest("introduce_players")); assertEquals(0,player.getNumberOfEquipped("flasza"));
        click("quest"); click("instructions"); click("confirm");
        assertEquals("corpse&herbs",player.getQuest("introduce_players")); assertTrue(player.isQuestCompleted("learn_scrying"));
    }
    @Test public void joshuaBothBrothersAndKeyring() {
        greet("Xoderos"); click("quest"); assertFalse(has("quest")); click("food"); capture("04-xoderos-offer"); click("accept");
        assertEquals("start",player.getQuest("hungry_joshua")); click("hints"); assertTrue(latest().getSpeech().contains("sałaty"));
        carry("kanapka",5); greet("Joshua"); click("give_food"); click("confirm");
        assertEquals("joshua",player.getQuest("hungry_joshua")); reward("XP postaci",150); reward("Karma",15); assertEquals(0,player.getNumberOfEquipped("kanapka")); capture("05-joshua-message");
        greet("Xoderos"); click("give_message"); assertEquals("done",player.getQuest("hungry_joshua")); assertTrue(player.hasFeature("keyring"));
        reward("XP postaci",50); reward("Rzemyk na klucze",1); capture("06-xoderos-keyring"); int xp=player.getXP(); type("Joshua"); assertEquals(xp,player.getXP());
    }
    @Test public void missingSandwichesAfterConfirmationAreNotConsumed() {
        player.setQuest("hungry_joshua","start"); carry("kanapka",5); greet("Joshua"); click("give_food"); player.drop("kanapka",1); type("tak");
        assertEquals("start",player.getQuest("hungry_joshua")); assertEquals(4,player.getNumberOfEquipped("kanapka")); assertTrue(latest().getRewards().isEmpty());
    }
    @Test public void rejectionCanBeReconsideredWithoutRestartLoop() {
        greet("Xoderos"); click("quest"); click("food"); click("decline");
        assertEquals("rejected",player.getQuest("hungry_joshua")); click("back"); click("quest"); click("food"); click("accept");
        assertEquals("start",player.getQuest("hungry_joshua")); assertFalse(has("quest"));
    }
    @Test public void jefAmberRoundTripAndRandomReceipt() {
        start("Jef"); click("hints"); assertTrue(latest().getSpeech().contains("Fado")); capture("07-jef-hint");
        greet("Amber"); click("give_message"); assertEquals("found_mom",player.getQuest("find_jefs_mom",0));
        assertTrue(latest().isActive()); assertEquals(1,player.getNumberOfEquipped("bielikrasa")); reward("bielikrasa",1); capture("08-amber-flower");
        greet("Jef"); click("give_flower"); assertEquals("done",player.getQuest("find_jefs_mom",0));
        int fish=player.getNumberOfEquipped("pokolec"); assertTrue(fish>=1&&fish<=6); reward("pokolec",fish);
        reward("XP postaci",5000); reward("XP ataku",1000); reward("XP obrony",1500); reward("Karma",25); capture("09-jef-reward");
        assertEquals(0,player.getNumberOfEquipped("bielikrasa")); assertEquals("1",player.getQuest("find_jefs_mom",2));
        assertFalse(quests.get("FindJefsMom").isRepeatable(player));
    }
    @Test public void amberFullBagDoesNotLoseFlowerOrAdvanceQuest() {
        player.setQuest("find_jefs_mom","start");
        for(String slot:Constants.CARRYING_SLOTS) { player.getSlot(slot).setCapacity(1); player.getSlot(slot).add(new StackableItem("filler","test","test",Collections.<String,String>emptyMap())); }
        greet("Amber"); click("give_message");
        assertEquals("start",player.getQuest("find_jefs_mom")); assertEquals(0,player.getNumberOfEquipped("bielikrasa")); assertTrue(latest().isActive()); assertTrue(latest().getRewards().isEmpty());
    }
    @Test public void jefCooldownAndOldCounterSurviveRepeat() {
        player.setQuest("find_jefs_mom","done;"+System.currentTimeMillis()+";4"); greet("Jef"); click("cooldown"); assertFalse(has("accept"));
        player.setQuest("find_jefs_mom","done;"+(System.currentTimeMillis()-7201L*60000)+";4");
        greet("Jef"); click("quest"); click("accept"); assertEquals("start",player.getQuest("find_jefs_mom",0)); assertEquals("4",player.getQuest("find_jefs_mom",2));
    }
    @Test public void eonnaCountsOnlyNewKillsAndShowsRealMonsterImages() {
        player.setSoloKill("szczur"); player.setSoloKill("szczur jaskiniowy"); player.setSoloKill("wąż");
        start("Eonna"); QuestProgress p=progress("CleanStorageSpace"); assertEquals(QuestProgress.State.ACTIVE,p.getState()); assertEquals(3,p.getObjectives().size());
        for(QuestProgress.Objective o:p.getObjectives()) assertEquals(0,o.getCurrent());
        assertEquals(3,latest().getItems().size()); for(NpcDialogueSnapshot.Item i:latest().getItems()) assertTrue(i.getSprite().startsWith("monsters/")); capture("10-eonna-hunt");
        player.setSharedKill("szczur"); player.setSoloKill("szczur jaskiniowy"); player.setSharedKill("wąż");
        assertEquals(QuestProgress.State.READY_TO_DELIVER,progress("CleanStorageSpace").getState());
        click("quest"); assertTrue(player.isQuestCompleted("clean_storage")); reward("XP postaci",1000); reward("Karma",25);
    }
    @Test public void eonnaLegacyGreetingStillCompletesAndCannotRepeatReward() {
        start("Eonna"); for(String n:Arrays.asList("szczur","szczur jaskiniowy","wąż")) player.setSharedKill(n);
        greet("Eonna"); assertTrue(player.isQuestCompleted("clean_storage")); reward("XP postaci",1000);
        int xp=player.getXP(); greet("Eonna"); type("postęp"); assertEquals(xp,player.getXP());
    }
    @Test public void sallyRandomFoodStatsAndCooldown() {
        start("Sally"); capture("11-sally-active"); carry("polano",10); type("tematy"); click("give_wood"); click("confirm");
        assertTrue(quests.get("Campfire").isCompleted(player)); assertFalse(quests.get("Campfire").isRepeatable(player));
        reward("XP postaci",500); reward("XP ataku",500); reward("XP obrony",500); reward("Karma",10);
        int meat=player.getNumberOfEquipped("mięso"), ham=player.getNumberOfEquipped("szynka"); assertEquals(10,meat+ham); reward(meat>0?"mięso":"szynka",10); capture("12-sally-reward");
        greet("Sally"); click("cooldown"); assertFalse(has("accept"));
        player.setQuest("campfire",Long.toString(System.currentTimeMillis()-61L*60000)); greet("Sally"); click("quest"); click("accept"); assertEquals("start",player.getQuest("campfire"));
    }
    @Test public void sallyNoWoodAfterConfirmationDoesNotAward() {
        player.setQuest("campfire","start"); carry("polano",10); greet("Sally"); player.drop("polano",1); type("tak");
        assertEquals("start",player.getQuest("campfire")); assertEquals(9,player.getNumberOfEquipped("polano")); assertTrue(latest().getRewards().isEmpty());
    }
    @Test public void luposPartialDeliveryThenCompletionAndShopUnlock() {
        start("Lupos"); assertEquals("",player.getQuest("elvish_armor")); assertTrue(latest().isActive());
        List<String> needed=((ElvishArmor)quests.get("ElvishArmor")).getNeededItems();
        carry(needed.get(0),1); click("supplies"); capture("13-lupos-samples"); click("give_0");
        assertEquals(";"+needed.get(0),player.getQuest("elvish_armor")); assertFalse(has("give_0")); assertTrue(latest().getRewards().isEmpty());
        click("later"); assertTrue(latest().isActive()); greet("Lupos");
        for(int i=1;i<needed.size();i++) carry(needed.get(i),1);
        assertEquals(QuestProgress.State.READY_TO_DELIVER,progress("ElvishArmor").getState());
        click("supplies"); for(int i=1;i<needed.size();i++) click("give_"+i);
        assertTrue(player.isQuestCompleted("elvish_armor")); reward("XP postaci",20000); reward("Karma",20); reward("Skup u Luposa",1); assertTrue(has("offer")); capture("14-lupos-reward");
        carry("zbroja elficka",1); click("offer"); boolean sale=false; for(String id:choiceIds()) if(id.startsWith("sell_")) sale=true; assertTrue(sale);
    }
    @Test public void luposMissingAndDuplicateItemsCannotAwardOrConsumeOthers() {
        start("Lupos"); click("supplies"); type("zbroja elficka"); assertEquals("",player.getQuest("elvish_armor"));
        carry("zbroja elficka",2); type("zbroja elficka"); assertEquals(1,player.getNumberOfEquipped("zbroja elficka"));
        type("zbroja elficka"); assertEquals(1,player.getNumberOfEquipped("zbroja elficka")); assertTrue(latest().getRewards().isEmpty());
    }
    @Test public void oldPartialSavesAreReadWithoutMutation() {
        Map<String,String> saves=new LinkedHashMap<String,String>();
        saves.put("plinks_toy","start"); saves.put("hungry_joshua","joshua"); saves.put("find_jefs_mom","found_mom;123;6");
        saves.put("campfire","start"); saves.put("elvish_armor",";zbroja elficka;miecz elficki"); saves.put("learn_scrying","done");
        for(Map.Entry<String,String> e:saves.entrySet()) player.setQuest(e.getKey(),e.getValue());
        for(IQuest quest:quests.values()) { if(player.hasQuest(quest.getSlotName())) assertNotNull(PilotQuestProgress.read(quest,player)); }
        for(Map.Entry<String,String> e:saves.entrySet()) assertEquals(e.getValue(),player.getQuest(e.getKey()));
    }
    @Test public void unknownCollectionSaveNeverInventsProgress() {
        player.setQuest("elvish_armor","future_state"); assertEquals(QuestProgress.State.UNKNOWN,progress("ElvishArmor").getState()); assertEquals("future_state",player.getQuest("elvish_armor"));
    }
    @Test public void ilisaAndXoderosCatalogsUseRealItemsAndBalances() {
        carry("money",1000);
        for(String name:Arrays.asList("Ilisa","Xoderos")) {
            greet(name); click("offer"); assertTrue(latest().isShop()); assertFalse(latest().getItems().isEmpty()); assertEquals(1000,latest().getMoney());
            for(NpcDialogueSnapshot.Item item:latest().getItems()) { assertTrue(item.isForSale()); assertTrue(item.getSprite().startsWith("items/")); }
        }
    }
    @Test public void closingEveryNewNpcUsesGoodbyeAndReleasesConversation() {
        for(String name:Arrays.asList("Plink","Ilisa","Xoderos","Joshua","Jef","Amber","Eonna","Sally","Lupos")) {
            greet(name); click("goodbye"); assertFalse(latest().isActive()); assertFalse(npcs.get(name).isTalking());
        }
    }
    @Test public void luposSkupKeepsExistingPricesAndActuallySellsOneItem() {
        player.setQuest("elvish_armor","done"); carry("zbroja elficka",1); greet("Lupos"); click("offer");
        String sale=null; for(Reply r:latest().getReplies()) if(r.getLabel().contains("zbroja elficka")) sale=r.getId();
        assertNotNull(sale); click(sale); click("confirm");
        assertEquals(0,player.getNumberOfEquipped("zbroja elficka"));
        assertEquals(SingletonRepository.getShopList().get("buyelvish").get("zbroja elficka").intValue(),player.getNumberOfEquipped("money"));
        assertEquals("done",player.getQuest("elvish_armor"));
    }
    @Test public void randomFoodPlacedOnGroundIsReportedAsSuch() {
        start("Sally");
        for(String slot:Constants.CARRYING_SLOTS) {
            player.getSlot(slot).setCapacity(1);
            if(!"bag".equals(slot)) player.getSlot(slot).add(new StackableItem("filler","test","test",Collections.<String,String>emptyMap()));
        }
        // One wood remains occupying the only bag cell after the ten required pieces are removed.
        carry("polano",11); greet("Sally"); click("confirm");
        assertEquals(1,player.getNumberOfEquipped("polano")); assertEquals(1,zone.dropped.size());
        Item food=zone.dropped.get(0); reward(food.getName(),10);
        boolean ground=false; for(NpcDialogueSnapshot.Reward r:latest().getRewards()) if(r.getName().equals(food.getName())) ground=r.isOnGround(); assertTrue(ground);
    }
    @Test public void existingCraftingServicesStillAcceptMaterials() {
        carry("polano",3); carry("ruda żelaza",1); carry("bryłka złota",1);
        greet("Xoderos"); type("odlej 1 żelazo"); click("confirm");
        assertTrue(player.hasQuest("xoderos_cast_iron")); assertEquals(0,player.getNumberOfEquipped("ruda żelaza"));
        greet("Joshua"); type("odlej 1 sztabka złota"); click("confirm");
        assertTrue(player.hasQuest("joshua_cast_gold")); assertEquals(0,player.getNumberOfEquipped("bryłka złota"));
        assertEquals(0,player.getNumberOfEquipped("polano")); assertFalse(player.hasQuest("hungry_joshua"));
    }
}
