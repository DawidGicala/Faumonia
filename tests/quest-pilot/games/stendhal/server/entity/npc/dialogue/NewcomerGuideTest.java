package games.stendhal.server.entity.npc.dialogue;
import games.stendhal.common.*;
import games.stendhal.server.core.events.NewcomerGuide;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.maps.semos.masterhouse.MasterNPC;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class NewcomerGuideTest {
    JourneyQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){
        f=new JourneyQuestFixture();f.createWorld();f.player.setXP(0);f.player.setLevel(0);NewcomerGuide.newCharacter(f.player);
        for(String name:Arrays.asList("Sedro","Mistrz Faumonii","Miter"))f.originals.put(name,NPCList.get().remove(name));
        new games.stendhal.server.maps.semos.village.SedroNPC().configureZone(f.zone,Collections.<String,String>emptyMap());
        new MasterNPC().configureZone(f.zone,Collections.<String,String>emptyMap());
        new games.stendhal.server.maps.semos.guardhouse.MiterNPC().configureZone(f.zone,Collections.<String,String>emptyMap());
        new HelpSedro().addToWorld();new MainTask1().addToWorld();
        for(String name:Arrays.asList("Sedro","Mistrz Faumonii","Miter"))f.npcs.put(name,NPCList.get().get(name));
    }
    @After public void cleanup(){f.restoreNPCs();}
    String step(){return NewcomerGuide.read(f.player).step;}
    void guided(String id){
        String label=null;
        for(NpcDialogueSnapshot.Reply reply:f.latest().getReplies())if(id.equals(reply.getId()))label=reply.getLabel();
        assertNotNull("Reply "+id+" at "+f.latest().getNpcName(),label);
        TutorialGuideSnapshot guide=NewcomerGuide.read(f.player);
        assertTrue("Guide must name actual reply "+id+" / "+label+": "+guide.text,guide.text.contains(label));
        f.click(id);
    }
    void greetPolish(String name){
        for(SpeakerNPC npc:f.npcs.values())if(npc!=null&&npc.getAttending()==f.player)npc.listenTo(f.player,"dowidzenia");
        SpeakerNPC npc=NPCList.get().get(name);f.player.setPosition(npc.getX()+1,npc.getY());
        JourneyQuestFixture.rules.turn+=34;npc.listenTo(f.player,"cześć");
        assertEquals(name,f.latest().getNpcName());assertTrue(f.latest().isActive());
    }
    void purchase(String name,int quantity){
        NpcDialogueSnapshot.Item item=null;for(NpcDialogueSnapshot.Item row:f.latest().getItems())if(name.equals(row.getName())&&row.isForSale())item=row;
        assertNotNull(name,item);marauroa.common.game.RPAction a=new marauroa.common.game.RPAction();
        a.put("session",f.latest().getSession());a.put("revision",Long.toString(f.latest().getRevision()));a.put("buy",item.getId());a.put("amount",Integer.toString(quantity));
        JourneyQuestFixture.rules.turn+=34;new games.stendhal.server.actions.NpcDialogueAction().onAction(f.player,a);
    }
    @Test public void actualGuideNamesEveryClickThroughAllFiveNpcsAndBarrier(){
        f.carry("money",5);
        greetPolish("Hayunn");guided("quest");guided("know");guided("ingredients");guided("accept");
        assertEquals("farewell",NewcomerGuide.read(f.player).anchor);guided("goodbye");
        greetPolish("Miter");guided("offer");
        assertEquals("shop",NewcomerGuide.read(f.player).anchor);assertTrue(NewcomerGuide.read(f.player).text.contains("„Kup”"));
        purchase("woda",1);assertEquals("purchase",NewcomerGuide.read(f.player).anchor);guided("confirm");guided("goodbye");
        greetPolish("Hayunn");guided("supplies");guided("give_woda");guided("goodbye");
        assertTrue(f.player.isQuestCompleted("task_hayunn"));
        greetPolish("Mistrz Faumonii");guided("stage_1");guided("quest");guided("accept");guided("goodbye");
        kill("szczur",3);assertTrue(NewcomerGuide.read(f.player).text.contains("szczur: 3/3"));
        kill("szczur jaskiniowy",1);assertEquals("master_return",step());
        greetPolish("Mistrz Faumonii");guided("stage_1");guided("quest");guided("goodbye");
        assertTrue(f.player.isQuestCompleted("main_task_1"));
        greetPolish("Sedro");guided("quest");guided("accept");guided("goodbye");
        kill("gnom",1);assertTrue(NewcomerGuide.read(f.player).text.contains("gnom: 1/1"));
        kill("panna gnom",1);greetPolish("Sedro");
        f.click("hints");guided("back");guided("quest");guided("goodbye");
        assertTrue(f.player.isQuestCompleted("help_sedro"));
        greetPolish("Soro");guided("quest");guided("know");guided("ingredients");guided("accept");guided("goodbye");
        assertTrue(NewcomerGuide.read(f.player).text.contains("„Otwórz”"));
        f.carry("ser",1);greetPolish("Soro");guided("supplies");guided("give_ser");guided("goodbye");
        assertEquals("ser=2",f.player.getQuest("cheese_for_soro"));assertEquals("cheese_hunt",step());
        f.carry("ser",2);greetPolish("Soro");guided("supplies");guided("give_ser");guided("goodbye");
        assertEquals(4,NewcomerGuide.completed(f.player));assertEquals("barrier",step());assertTrue(NewcomerGuide.canLeave(f.player));
        assertEquals(0,f.player.getNumberOfEquipped("woda"));assertEquals(0,f.player.getNumberOfEquipped("ser"));
    }
    @Test public void miterGuideCancelsWrongQuantityAndWrongItemWithoutSpending(){
        f.player.setQuest("task_hayunn","woda=1");f.carry("money",500);greetPolish("Miter");guided("offer");
        purchase("woda",2);assertEquals("cancel_purchase",NewcomerGuide.read(f.player).anchor);guided("cancel");
        assertEquals(500,f.player.getNumberOfEquipped("money"));assertEquals(0,f.player.getNumberOfEquipped("woda"));
        String other=null;for(NpcDialogueSnapshot.Item row:f.latest().getItems())if(row.isForSale()&&!"woda".equals(row.getName())){other=row.getName();break;}
        assertNotNull(other);purchase(other,1);assertEquals("cancel_purchase",NewcomerGuide.read(f.player).anchor);guided("cancel");
        assertEquals(500,f.player.getNumberOfEquipped("money"));
        purchase("woda",1);guided("confirm");assertEquals(495,f.player.getNumberOfEquipped("money"));assertEquals("water_return",step());
    }
    @Test public void miterGuideSupportsTypedPolishPurchaseAndExplainsMissingCoin(){
        f.player.setQuest("task_hayunn","woda=1");f.carry("money",4);greetPolish("Miter");
        f.type("kupię 1 woda");assertEquals("cancel_purchase",NewcomerGuide.read(f.player).anchor);
        assertTrue(NewcomerGuide.read(f.player).text.contains("masz 4"));guided("cancel");
        assertEquals(4,f.player.getNumberOfEquipped("money"));
        f.carry("money",1);f.type("kupię 1 woda");assertEquals("purchase",NewcomerGuide.read(f.player).anchor);guided("confirm");
        assertEquals(1,f.player.getNumberOfEquipped("woda"));assertEquals(0,f.player.getNumberOfEquipped("money"));guided("goodbye");
        assertNull(NpcDialogueService.guidePurchase(NPCList.get().get("Miter"),f.player));
    }
    @Test public void polishGreetingAndEveryHayunnReplyLeadToAcceptedRequest(){
        SpeakerNPC npc=NPCList.get().get("Hayunn");f.player.setPosition(npc.getX()+1,npc.getY());
        TutorialGuideSnapshot before=NewcomerGuide.read(f.player);
        assertEquals("greet",before.anchor);assertTrue(before.actionHint().contains("cześć"));
        assertFalse(before.text.contains(" hi"));assertFalse(before.text.contains("/k"));
        npc.listenTo(f.player,"cześć");assertTrue(f.latest().isActive());
        assertEquals("reply",NewcomerGuide.read(f.player).anchor);
        f.click("quest");assertTrue(NewcomerGuide.read(f.player).text.contains("Jasne. Jak to zrobić?"));
        f.click("know");assertTrue(NewcomerGuide.read(f.player).text.contains("Dobrze. Co mam kupić?"));
        f.click("ingredients");assertTrue(NewcomerGuide.read(f.player).text.contains("Jasne, przyniosę ci wodę."));
        assertTrue(f.has("accept"));f.click("accept");
        assertEquals("woda=1",f.player.getQuest("task_hayunn"));assertEquals("water_buy",step());
        assertEquals("farewell",NewcomerGuide.read(f.player).anchor);
        f.type("dowidzenia");assertFalse(f.latest().isActive());
        assertEquals("Miter",NewcomerGuide.read(f.player).waypoint);
        f.greet("Miter");assertTrue(NewcomerGuide.read(f.player).text.contains("Pokaż mi swoją ofertę."));
    }
    @Test public void occupiedNpcExplainsWaitingWithoutTakingOverConversation(){
        SpeakerNPC npc=NPCList.get().get("Hayunn");
        games.stendhal.server.entity.player.Player other=new games.stendhal.server.entity.player.Player(new marauroa.common.game.RPObject(f.player));
        npc.setAttending(other);npc.setCurrentState(ConversationStates.ATTENDING);
        assertEquals("wait",NewcomerGuide.read(f.player).anchor);assertSame(other,npc.getAttending());
        npc.setAttending(null);npc.setCurrentState(ConversationStates.IDLE);
        assertEquals("greet",NewcomerGuide.read(f.player).anchor);
    }
    @Test public void routesDescribeAnEntranceInsteadOfGreetingAPortal(){
        // This fixture zone is outside the starting routes, so no false NPC marker is shown.
        f.player.setQuest("task_hayunn","done");f.player.setQuest("main_task_1","start;szczur,0,3,0,0,szczur jaskiniowy,0,1,0,0");
        TutorialGuideSnapshot s=NewcomerGuide.read(f.player);
        assertEquals("walk",s.anchor);assertEquals(-1,s.x);assertFalse(s.actionHint().contains("cześć"));
    }
    @Test public void legacyLevelTipsDisappearAtSevenAndTwentyWithoutChangingQuests(){
        f.player.setKeyedSlot("!tutorial",NewcomerGuide.KEY,"legacy");
        f.player.setLevel(6);TutorialGuideSnapshot s=NewcomerGuide.read(f.player);
        assertTrue(s.automatic);assertTrue(s.text.contains("/k"));assertTrue(s.text.contains("Mistrz Faumonii"));
        f.player.setLevel(7);s=NewcomerGuide.read(f.player);
        assertTrue(s.automatic);assertFalse(s.text.contains("/k"));assertTrue(s.text.contains("Mistrz Faumonii"));
        f.player.setLevel(19);assertTrue(NewcomerGuide.read(f.player).automatic);
        f.player.setLevel(20);s=NewcomerGuide.read(f.player);
        assertFalse(s.automatic);assertTrue(s.text.contains("/k"));
        assertEquals(0,NewcomerGuide.completed(f.player));
    }
    @Test public void freshStartNamesMasterAndEveryEntranceButDoesNotRepeatCompletedTrial(){
        SpeakerNPC master=NPCList.get().get("Mistrz Faumonii");
        games.stendhal.server.core.engine.StendhalRPZone house=new games.stendhal.server.core.engine.StendhalRPZone(NewcomerGuide.MASTER);
        master.onAdded(house);f.player.setQuest("task_hayunn","done");
        try{
            f.player.onAdded(new games.stendhal.server.core.engine.StendhalRPZone(NewcomerGuide.HOUSE));
            TutorialGuideSnapshot s=NewcomerGuide.read(f.player);
            assertEquals("master_accept",s.step);assertEquals("Poznaj Mistrza Faumonii",s.title);assertEquals(13,s.x);assertEquals(16,s.y);
            f.player.onAdded(new games.stendhal.server.core.engine.StendhalRPZone(NewcomerGuide.CITY));
            s=NewcomerGuide.read(f.player);assertEquals("Dom Mistrza Faumonii",s.waypoint);assertEquals(29,s.x);assertEquals(40,s.y);assertTrue(s.text.contains("pierwsze drzwi po lewej"));
            f.player.onAdded(house);f.player.setPosition(14,2);
            s=NewcomerGuide.read(f.player);assertEquals("Wejście do sali Mistrza",s.waypoint);assertEquals(11,s.x);assertEquals(5,s.y);
            f.player.setPosition(4,5);s=NewcomerGuide.read(f.player);assertEquals("Mistrz Faumonii",s.waypoint);assertEquals("greet",s.anchor);
            f.player.setQuest("main_task_1","done;123");assertEquals("sedro_accept",step());assertEquals("done;123",f.player.getQuest("main_task_1"));
        }finally{master.onAdded(f.zone);f.player.onAdded(f.zone);}
    }
    @Test public void crossingBarrierClosesGuideAndReturningDoesNotReopenIt(){
        f.player.onAdded(new games.stendhal.server.core.engine.StendhalRPZone(NewcomerGuide.CITY));
        try{
            f.player.setPosition(67,45);NewcomerGuide.crossedBarrier(f.player);
            assertEquals("active",f.player.getKeyedSlot("!tutorial",NewcomerGuide.KEY));
            for(String q:NewcomerGuide.QUESTS)f.player.setQuest(q,"done");f.player.setLevel(5);
            f.player.setPosition(66,45);NewcomerGuide.crossedBarrier(f.player);
            assertEquals("active",f.player.getKeyedSlot("!tutorial",NewcomerGuide.KEY));
            assertTrue(NewcomerGuide.read(f.player).automatic);
            f.player.setPosition(67,45);NewcomerGuide.crossedBarrier(f.player);
            assertEquals("done",f.player.getKeyedSlot("!tutorial",NewcomerGuide.KEY));assertEquals("finished",step());
            for(int level:new int[]{5,6,7,19,20}){f.player.setLevel(level);assertFalse(NewcomerGuide.read(f.player).automatic);}
            f.player.setPosition(65,45);NewcomerGuide.tick(f.player);assertFalse(NewcomerGuide.read(f.player).automatic);
            assertEquals(4,NewcomerGuide.completed(f.player));assertTrue(NewcomerGuide.read(f.player).text.contains("Mistrz Faumonii"));
        }finally{f.player.onAdded(f.zone);}
    }
    @Test public void legacyPlayerCanCloseGuideAtBarrierWithoutInventingQuestCompletion(){
        f.player.setKeyedSlot("!tutorial",NewcomerGuide.KEY,"legacy");f.player.setLevel(6);
        f.player.onAdded(new games.stendhal.server.core.engine.StendhalRPZone(NewcomerGuide.CITY));
        try{
            f.player.setPosition(67,45);NewcomerGuide.crossedBarrier(f.player);
            assertEquals("done",f.player.getKeyedSlot("!tutorial",NewcomerGuide.KEY));assertFalse(NewcomerGuide.read(f.player).automatic);
            assertEquals(0,NewcomerGuide.completed(f.player));assertTrue(NewcomerGuide.canLeave(f.player));
        }finally{f.player.onAdded(f.zone);}
    }
    @Test public void miterFirstProductIsTaskWaterAndPurchaseKeepsCorrectPrice(){
        f.player.setQuest("task_hayunn","woda=1");f.carry("money",5);f.greet("Miter");f.click("offer");
        NpcDialogueSnapshot.Item first=f.latest().getItems().get(0);
        assertEquals("woda",first.getName());assertEquals(5,first.getUnitPrice());assertEquals("shop_0",first.getId());
        boolean bottle=false;for(NpcDialogueSnapshot.Item item:f.latest().getItems())if("butelka wody".equals(item.getName()))bottle=true;
        assertTrue("The separate bottle must remain available",bottle);
        purchase(first.getName(),1);f.click("confirm");
        assertEquals(1,f.player.getNumberOfEquipped("woda"));assertEquals(0,f.player.getNumberOfEquipped("butelka wody"));assertEquals(0,f.player.getNumberOfEquipped("money"));
    }
    @Test public void miterBuysStarterLootOnlyAfterConfirmation(){
        f.carry("maczuga",1);f.greet("Miter");f.click("buyback");
        NpcDialogueSnapshot.Reply sell=null;
        for(NpcDialogueSnapshot.Reply r:f.latest().getReplies())if(r.getLabel().contains("Sprzedam: maczuga"))sell=r;
        assertNotNull(sell);f.click(sell.getId());
        assertEquals(1,f.player.getNumberOfEquipped("maczuga"));
        f.click("cancel");assertEquals(1,f.player.getNumberOfEquipped("maczuga"));
        f.type("sprzedam maczuga");f.click("confirm");
        assertEquals(0,f.player.getNumberOfEquipped("maczuga"));assertEquals(1,f.player.getNumberOfEquipped("money"));
        f.type("tak");assertEquals(1,f.player.getNumberOfEquipped("money"));
    }
    @Test public void generatedAndLegacyGameNameCapitalizationCanLogIn() throws Exception {
        marauroa.common.Configuration.setConfigurationPersitance(false);
        marauroa.common.Configuration config=marauroa.common.Configuration.getConfiguration();
        marauroa.common.Configuration.setConfigurationPersitance(true);
        String previous=config.get("server_typeGame","FaumoniaOnline");
        try {
            for(String name:Arrays.asList("FaumoniaOnline","faumoniaonline","faumonia")) {
                config.set("server_typeGame",name);
                assertTrue(JourneyQuestFixture.rules.checkGameVersion("faumoniaonline","7.0"));
                assertTrue(JourneyQuestFixture.rules.checkGameVersion("faumonia","7.0"));
                assertFalse(JourneyQuestFixture.rules.checkGameVersion("anothergame","7.0"));
            }
            config.set("server_typeGame","anothergame");
            assertFalse(JourneyQuestFixture.rules.checkGameVersion("faumonia","7.0"));
        } finally { config.set("server_typeGame",previous); }
    }
    void kill(String name,int count){for(int i=0;i<count;i++)f.player.setSoloKill(name);}
    void acceptCollection(String name){f.greet(name);f.click("quest");f.click("know");f.click("ingredients");f.click("accept");}
    @Test public void realFourQuestStartReachesBarrierWithoutGrinding(){
        assertEquals("hayunn",step());assertFalse(NewcomerGuide.canLeave(f.player));
        acceptCollection("Hayunn");assertEquals("water_buy",step());f.carry("money",5);
        f.greet("Miter");f.click("offer");assertTrue(f.latest().isShop());
        NpcDialogueSnapshot.Item water=null;for(NpcDialogueSnapshot.Item i:f.latest().getItems())if("woda".equals(i.getName()))water=i;
        assertNotNull("Water must be visible in the real shop",water);assertEquals(5,water.getUnitPrice());
        marauroa.common.game.RPAction buy=new marauroa.common.game.RPAction();buy.put("session",f.latest().getSession());buy.put("revision",Long.toString(f.latest().getRevision()));buy.put("buy",water.getId());buy.put("amount","1");
        JourneyQuestFixture.rules.turn+=34;new games.stendhal.server.actions.NpcDialogueAction().onAction(f.player,buy);
        f.click("confirm");assertEquals(1,f.player.getNumberOfEquipped("woda"));assertEquals(0,f.player.getNumberOfEquipped("money"));
        assertEquals("water_return",step());f.greet("Hayunn");f.click("supplies");f.click("give_woda");assertTrue(f.player.isQuestCompleted("task_hayunn"));
        assertEquals("master_accept",step());f.greet("Mistrz Faumonii");f.click("stage_1");f.click("quest");f.click("accept");
        assertEquals("master_hunt",step());kill("szczur",3);kill("szczur jaskiniowy",1);assertEquals("master_return",step());
        assertFalse(NewcomerGuide.canLeave(f.player));f.click("quest");assertEquals("sedro_accept",step());
        f.greet("Sedro");f.click("quest");f.click("accept");assertEquals("sedro_hunt",step());kill("gnom",1);kill("panna gnom",1);
        assertEquals("sedro_return",step());f.click("back");f.click("quest");assertEquals("soro",step());
        acceptCollection("Soro");assertEquals("cheese_hunt",step());f.carry("ser",1);assertEquals("cheese_return",step());
        f.click("supplies");f.click("give_ser");assertEquals("ser=2",f.player.getQuest("cheese_for_soro"));
        f.carry("ser",2);f.type("ser");assertEquals(4,NewcomerGuide.completed(f.player));
        assertEquals(1825,f.player.getXP());assertTrue(f.player.getLevel()>=5);assertTrue(NewcomerGuide.canLeave(f.player));
    }
    @Test public void newLevelFivePlayerStillNeedsActualCompletion(){f.player.setLevel(5);assertFalse(NewcomerGuide.canLeave(f.player));for(String slot:NewcomerGuide.QUESTS)f.player.setQuest(slot,"done");assertTrue(NewcomerGuide.canLeave(f.player));}
    @Test public void allCompletedButLowLevelDoesNotSkipLevelGate(){for(String slot:NewcomerGuide.QUESTS)f.player.setQuest(slot,"done");assertFalse(NewcomerGuide.canLeave(f.player));assertTrue(NewcomerGuide.missing(f.player).contains("poziom 5"));}
    @Test public void oldAdventurerKeepsAccessWithoutFakeQuestCompletion(){f.player.setKeyedSlot("!tutorial",NewcomerGuide.KEY,null);f.player.setLevel(50);assertTrue(NewcomerGuide.canLeave(f.player));assertEquals(0,NewcomerGuide.completed(f.player));assertFalse(NewcomerGuide.read(f.player).automatic);}
    @Test public void migrationKeepsKillBaselinesAndAlreadyPaidCheese(){
        f.player.setKeyedSlot("!tutorial","guide_migrated",null);
        f.player.setQuest("main_task_1","start;szczur,0,10,21,4,szczur jaskiniowy,0,5,9,2");
        f.player.setQuest("help_sedro","start;gnom,0,1,4,5,panna gnom,0,1,3,2,szczur,0,5,8,1,szczur jaskiniowy,0,3,2,1");
        f.player.setQuest("cheese_for_soro","ser=8");int xp=f.player.getXP();NewcomerGuide.initialize(f.player);
        assertEquals("start;szczur,0,3,21,4,szczur jaskiniowy,0,1,9,2",f.player.getQuest("main_task_1"));
        assertEquals("start;gnom,0,1,4,5,panna gnom,0,1,3,2",f.player.getQuest("help_sedro"));assertEquals("ser=1",f.player.getQuest("cheese_for_soro"));
        NewcomerGuide.initialize(f.player);assertEquals("ser=1",f.player.getQuest("cheese_for_soro"));assertEquals(xp,f.player.getXP());
    }
    @Test public void fullyCreditedOldCheeseStillRequiresClaimAndCannotPayTwice(){
        f.player.setKeyedSlot("!tutorial","guide_migrated",null);f.player.setQuest("cheese_for_soro","ser=4");NewcomerGuide.initialize(f.player);
        assertEquals("ser=0",f.player.getQuest("cheese_for_soro"));assertFalse(f.player.isQuestCompleted("cheese_for_soro"));
        f.greet("Soro");f.click("claim");assertTrue(f.player.isQuestCompleted("cheese_for_soro"));int xp=f.player.getXP();f.type("odbiór");assertEquals(xp,f.player.getXP());
    }
    @Test public void freshSoroRequestIsNotMigratedAgain(){acceptCollection("Soro");NewcomerGuide.initialize(f.player);assertEquals("ser=3",f.player.getQuest("cheese_for_soro"));}
    @Test public void completedOldRecordsAreNotRewritten(){f.player.setKeyedSlot("!tutorial","guide_migrated",null);for(String q:NewcomerGuide.QUESTS)f.player.setQuest(q,"done;123");NewcomerGuide.initialize(f.player);for(String q:NewcomerGuide.QUESTS)assertEquals("done;123",f.player.getQuest(q));}
    @Test public void wireRoundTripAndMalformedValues(){TutorialGuideSnapshot s=NewcomerGuide.read(f.player);assertEquals(s.toWire(),TutorialGuideSnapshot.fromWire(s.toWire()).toWire());List<String> invalid=new ArrayList<String>(s.toWire());invalid.set(5,"-2");try{TutorialGuideSnapshot.fromWire(invalid);fail("bad coordinates");}catch(IllegalArgumentException expected){}}
    @Test public void guideDoesNotSendIdenticalPacketEveryTurn(){NewcomerGuide.tick(f.player);int size=f.player.events().size();NewcomerGuide.tick(f.player);assertEquals(size,f.player.events().size());f.player.setQuest("task_hayunn","woda=1");NewcomerGuide.tick(f.player);assertEquals(size+1,f.player.events().size());}
    @Test public void newRealCharacterGetsGuideMarkerAndEnoughMoneyForWater(){
        games.stendhal.server.entity.player.Player p=games.stendhal.server.entity.player.Player.createZeroLevelPlayer("NightNewcomer",new marauroa.common.game.RPObject());
        assertEquals("active",p.getKeyedSlot("!tutorial",NewcomerGuide.KEY));assertTrue(p.getNumberOfEquipped("money")>=5);
    }
    @Test public void privateGuideSerializesForOwnerAndNeverForNearbyPlayers() throws Exception {
        NewcomerGuide.tick(f.player);
        for(marauroa.common.game.DetailLevel detail:marauroa.common.game.DetailLevel.values()){
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();f.player.writeObject(new marauroa.common.net.OutputSerializer(bytes),detail);
            marauroa.common.game.RPObject restored=new marauroa.common.game.RPObject();restored.readObject(new marauroa.common.net.InputSerializer(new java.io.ByteArrayInputStream(bytes.toByteArray())));
            boolean found=false;for(marauroa.common.game.RPEvent event:restored.events())if(TutorialGuideSnapshot.EVENT.equals(event.getName())){found=true;TutorialGuideSnapshot.fromWire(event.getList("data"));}
            assertEquals(detail!=marauroa.common.game.DetailLevel.NORMAL,found);
            if(detail==marauroa.common.game.DetailLevel.FULL){games.stendhal.server.entity.player.Player p=new games.stendhal.server.entity.player.Player(restored);assertEquals("active",p.getKeyedSlot("!tutorial",NewcomerGuide.KEY));}
        }
    }
}
