package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.*;
import games.stendhal.server.core.engine.*;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.item.*;
import games.stendhal.server.entity.mapstuff.useable.ExpeditionSurveySpot;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.maps.semos.masterhouse.MasterNPC;
import games.stendhal.server.maps.semos.masterhouse.MasterNPC.MasterSpeaker;
import java.util.*;
import marauroa.common.game.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ExpeditionXXVTest {
    JourneyQuestFixture f; MasterSpeaker master; final Map<String,SpeakerNPC> originals=new HashMap<String,SpeakerNPC>();
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){
        f=new JourneyQuestFixture();f.createWorld();
        for(String name:Arrays.asList("Fag","Thyron",MasterStage.NPC_NAME))originals.put(name,NPCList.get().remove(name));
        new games.stendhal.server.maps.pol.desert.FagNPC().configureZone(f.zone,Collections.<String,String>emptyMap());
        new games.stendhal.server.maps.semos.village.ThyronNPC().configureZone(f.zone,Collections.<String,String>emptyMap());
        new MasterNPC().configureZone(f.zone,Collections.<String,String>emptyMap());
        master=(MasterSpeaker)NPCList.get().get(MasterStage.NPC_NAME);
        for(String name:originals.keySet())f.npcs.put(name,NPCList.get().get(name));
        new KillLast().addToWorld();new FagDeathScout().addToWorld();new ThyronExpeditionFittings().addToWorld();new MainTask25().addToWorld();
        level(320);f.player.setQuest("main_task_24","done");
    }
    @After public void clean(){f.restoreNPCs();for(SpeakerNPC n:originals.values())if(n!=null)NPCList.get().add(n);}
    void level(int n){f.player.setXP(Level.getXP(n));f.player.setLevel(n);}
    void type(String text){assertTrue(f.latest().isActive());f.type(text);}
    void startMaster(){f.greet(MasterStage.NPC_NAME);type("etap 25");f.click("quest");f.click("accept");assertEquals("preparing",f.player.getQuest(ExpeditionXXV.MASTER));}
    void open(String n){f.greet(n);f.click("xxv_open");}
    void startHunt(){open("Fag");f.click("xxv_accept");f.click("accept");}
    void startFittings(){open("Thyron");f.click("xxv_accept");f.click("accept");}
    void kills(){for(Map.Entry<String,marauroa.common.Pair<Integer,Integer>> e:ExpeditionXXV.targets().entrySet())for(int i=0;i<e.getValue().second();i++)f.player.setSoloKill(e.getKey());}
    void prepared(){f.player.setQuest(ExpeditionXXV.HUNT,"done");f.player.setQuest(ExpeditionXXV.FITTINGS,"done");}
    void report(){f.greet(MasterStage.NPC_NAME);type("etap 25");f.click("quest");}
    void fullInventory(){for(String slot:Constants.CARRYING_SLOTS){RPSlot s=f.player.getSlot(slot);s.setCapacity(Math.max(1,s.size()));if(s.size()==0)s.add(new StackableItem("filler","test","test",Collections.<String,String>emptyMap()));}}
    void survey(int i,boolean expected){
        StendhalRPZone zone=new StendhalRPZone(ExpeditionXXV.ZONES[i]){@Override public synchronized void modify(RPObject o){}};
        ExpeditionSurveySpot spot=new ExpeditionSurveySpot(i);spot.onAdded(zone);f.player.onAdded(zone);f.player.setPosition(spot.getX()+1,spot.getY());
        assertEquals(expected,spot.onUsed(f.player));f.player.onAdded(f.zone);
    }
    @Test public void fullChainThroughPublicPanelActionsRewardsExactlyOnce(){
        startMaster();startHunt();f.capture("xxv-fag-hunt");kills();f.click("xxv_report");assertTrue(f.player.isQuestCompleted(ExpeditionXXV.HUNT));
        startFittings();f.carry("żelazo",100);f.carry("polano",200);f.carry("sztabka mithrilu",30);
        open("Hogart");f.capture("xxv-hogart-materials");for(int i=0;i<3;i++)f.click("give_material_"+i);
        assertEquals("ready",f.player.getQuest(ExpeditionXXV.FITTINGS));assertFalse(f.player.isQuestCompleted(ExpeditionXXV.FITTINGS));
        f.click("give_parcel");assertNotNull(ExpeditionXXV.carriedPackage(f.player));open("Thyron");f.click("give_parcel");assertTrue(f.player.isQuestCompleted(ExpeditionXXV.FITTINGS));
        report();assertEquals("survey;0",f.player.getQuest(ExpeditionXXV.MASTER));f.capture("xxv-survey");
        survey(2,true);survey(0,true);survey(1,true);assertEquals("survey;7",f.player.getQuest(ExpeditionXXV.MASTER));
        report();assertEquals("fang",f.player.getQuest(ExpeditionXXV.MASTER));f.carry(ExpeditionXXV.FANG,2);int xp=f.player.getXP();f.click("give_master");
        assertTrue(f.player.isQuestCompleted(ExpeditionXXV.MASTER));assertEquals(7664250,f.player.getXP()-xp);assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.FANG));
        assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.BADGE));f.reward(ExpeditionXXV.BADGE,1);f.reward("XP postaci",7664250);f.capture("xxv-reward");
        int paid=f.player.getXP();type("oddaj kieł");type("zadanie");assertEquals(paid,f.player.getXP());assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.FANG));
    }
    @Test public void level319CannotAcceptAnyQuest(){level(319);f.greet("Fag");assertFalse(f.has("xxv_open"));type("zwiad wyprawy");type("przyjmij zwiad");type("tak");assertFalse(f.player.hasQuest(ExpeditionXXV.HUNT));f.greet(MasterStage.NPC_NAME);type("etap 25");type("zadanie");type("tak");assertFalse(f.player.hasQuest(ExpeditionXXV.MASTER));}
    @Test public void previousStageRequiredEvenWithBadge(){f.player.removeQuest("main_task_24");f.carry("odznaka wojownika XXIV",1);f.greet("Thyron");assertFalse(f.has("xxv_open"));type("okucia wyprawy");type("przyjmij zamówienie");type("tak");assertFalse(f.player.hasQuest(ExpeditionXXV.FITTINGS));}
    @Test public void thyronAcceptsVeteransAboveOldGreetingLimit(){level(650);open("Thyron");f.click("xxv_accept");f.click("accept");assertNotNull(ExpeditionXXV.remaining(f.player));}
    @Test public void onlyNewKillsCountAndOlderFagQuestRemainsIntact(){f.player.setQuest(new KillLast().getSlotName(),"legacy-progress");kills();startHunt();f.click("xxv_report");assertFalse(f.player.isQuestCompleted(ExpeditionXXV.HUNT));for(int[] v:ExpeditionXXV.kills(f.player).values())assertEquals(0,v[0]);kills();f.click("xxv_report");assertTrue(f.player.isQuestCompleted(ExpeditionXXV.HUNT));assertEquals("legacy-progress",f.player.getQuest(new KillLast().getSlotName()));}
    @Test public void sharedKillsCountButOtherSpeciesDoNot(){startHunt();for(int i=0;i<50;i++)f.player.setSoloKill("szczur");for(Map.Entry<String,marauroa.common.Pair<Integer,Integer>> e:ExpeditionXXV.targets().entrySet())for(int i=0;i<e.getValue().second();i++)f.player.setSharedKill(e.getKey());f.click("xxv_report");assertTrue(f.player.isQuestCompleted(ExpeditionXXV.HUNT));}
    @Test public void malformedHuntCannotBeCompleted(){f.player.setQuest(ExpeditionXXV.HUNT,"start;");open("Fag");f.click("xxv_report");assertFalse(f.player.isQuestCompleted(ExpeditionXXV.HUNT));}
    @Test public void partialDeliveryTakesOnlyOutstandingMaterials(){startFittings();f.carry("żelazo",40);open("Hogart");f.click("give_material_0");assertEquals("collect;60;200;30",f.player.getQuest(ExpeditionXXV.FITTINGS));f.carry("żelazo",80);type("stan okuć");f.click("give_material_0");assertEquals(20,f.player.getNumberOfEquipped("żelazo"));assertEquals("collect;0;200;30",f.player.getQuest(ExpeditionXXV.FITTINGS));assertFalse(f.has("give_material_0"));}
    @Test public void materialsStoredInBankAreNotTaken(){startFittings();f.player.addSlot("bank");Item item=SingletonRepository.getEntityManager().getItem("żelazo");((StackableItem)item).setQuantity(100);f.player.getSlot("bank").add(item);open("Hogart");type("przekaż materiał 0");assertEquals("collect;100;200;30",f.player.getQuest(ExpeditionXXV.FITTINGS));assertEquals(100,((StackableItem)f.player.getSlot("bank").iterator().next()).getQuantity());}
    @Test public void malformedMaterialRecordPreservedWithoutTakingAnything(){f.player.setQuest(ExpeditionXXV.FITTINGS,"collect;999;200;30");f.carry("żelazo",100);open("Hogart");type("przekaż materiał 0");assertEquals(100,f.player.getNumberOfEquipped("żelazo"));assertEquals("collect;999;200;30",f.player.getQuest(ExpeditionXXV.FITTINGS));}
    @Test public void parcelIsBoundMarkedAndNotUsableAsAChest(){f.player.setQuest(ExpeditionXXV.FITTINGS,"ready");open("Hogart");f.click("give_parcel");Item parcel=ExpeditionXXV.carriedPackage(f.player);assertNotNull(parcel);assertEquals(f.player.getName(),parcel.getBoundTo());assertEquals(ExpeditionXXV.packageMark(f.player),parcel.getInfoString());assertEquals(Item.class,parcel.getClass());assertEquals(1,parcel.getInt("undroppableondeath"));}
    @Test public void fullBagLeavesParcelReadyWithoutDropping(){f.player.setQuest(ExpeditionXXV.FITTINGS,"ready");fullInventory();open("Hogart");f.click("give_parcel");assertEquals("ready",f.player.getQuest(ExpeditionXXV.FITTINGS));assertNull(ExpeditionXXV.carriedPackage(f.player));assertTrue(f.zone.dropped.isEmpty());}
    @Test public void existingParcelInBankBlocksDuplicate(){f.player.setQuest(ExpeditionXXV.FITTINGS,"ready");open("Hogart");f.click("give_parcel");Item item=ExpeditionXXV.carriedPackage(f.player);f.player.drop(item);f.player.addSlot("bank");f.player.getSlot("bank").add(item);f.click("give_parcel");assertNull(ExpeditionXXV.carriedPackage(f.player));assertEquals(1,f.player.getSlot("bank").size());}
    @Test public void genuinelyMissingParcelCanBeReissuedButNotAfterCompletion(){f.player.setQuest(ExpeditionXXV.FITTINGS,"issued");open("Hogart");f.click("give_parcel");assertNotNull(ExpeditionXXV.carriedPackage(f.player));f.click("give_parcel");assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.PACKAGE));open("Thyron");f.click("give_parcel");open("Hogart");type("odbierz okucia");assertEquals(0,f.player.getNumberOfEquipped(ExpeditionXXV.PACKAGE));}
    @Test public void unmarkedOrWrongOwnerParcelCannotCompleteDelivery(){f.player.setQuest(ExpeditionXXV.FITTINGS,"issued");f.carry(ExpeditionXXV.PACKAGE,1);open("Thyron");f.click("give_parcel");assertEquals("issued",f.player.getQuest(ExpeditionXXV.FITTINGS));Item item=f.player.getAllEquipped(ExpeditionXXV.PACKAGE).get(0);item.setBoundTo("SomeoneElse");item.setInfoString(ExpeditionXXV.packageMark(f.player));f.click("give_parcel");assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.PACKAGE));assertEquals("issued",f.player.getQuest(ExpeditionXXV.FITTINGS));}
    @Test public void explorationRequiresBothCompletedQuestsAndExplicitOrder(){startMaster();survey(0,false);f.player.setQuest(ExpeditionXXV.HUNT,"done");report();assertEquals("preparing",f.player.getQuest(ExpeditionXXV.MASTER));f.player.setQuest(ExpeditionXXV.FITTINGS,"done");survey(0,false);report();survey(0,true);assertEquals("survey;1",f.player.getQuest(ExpeditionXXV.MASTER));}
    @Test public void allSixDiscoveryOrdersAndRepeatsProduceExactlyThreeFlags(){prepared();int[][] orders={{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};for(int[] order:orders){f.player.setQuest(ExpeditionXXV.MASTER,"survey;0");for(int i:order){survey(i,true);String before=f.player.getQuest(ExpeditionXXV.MASTER);survey(i,true);assertEquals(before,f.player.getQuest(ExpeditionXXV.MASTER));}assertEquals(3,ExpeditionXXV.surveyed(f.player));}}
    @Test public void wrongZoneFarDistanceAndDiagonalUseCannotCredit(){prepared();f.player.setQuest(ExpeditionXXV.MASTER,"survey;0");ExpeditionSurveySpot s=new ExpeditionSurveySpot(0);s.onAdded(f.zone);f.player.setPosition(s.getX(),s.getY());assertFalse(s.onUsed(f.player));StendhalRPZone z=new StendhalRPZone(ExpeditionXXV.ZONES[0]);s.onAdded(z);f.player.onAdded(z);f.player.setPosition(s.getX()+10,s.getY());assertFalse(s.onUsed(f.player));f.player.setPosition(s.getX()+1,s.getY()+1);assertFalse(s.onUsed(f.player));f.player.onAdded(f.zone);assertEquals("survey;0",f.player.getQuest(ExpeditionXXV.MASTER));}
    @Test public void earlierOrTradedFangWorksOnlyAfterFullReport(){f.carry(ExpeditionXXV.FANG,1);startMaster();type("oddaj kieł");assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.FANG));prepared();report();survey(0,true);survey(1,true);report();assertEquals("survey;3",f.player.getQuest(ExpeditionXXV.MASTER));type("oddaj kieł");assertFalse(f.player.isQuestCompleted(ExpeditionXXV.MASTER));survey(2,true);report();f.click("give_master");assertTrue(f.player.isQuestCompleted(ExpeditionXXV.MASTER));}
    @Test public void fullInventoryDoesNotConsumeFangOrAwardXp(){prepared();f.player.setQuest(ExpeditionXXV.MASTER,"fang");f.carry(ExpeditionXXV.FANG,1);fullInventory();f.greet(MasterStage.NPC_NAME);type("etap 25");int xp=f.player.getXP();f.click("give_master");assertEquals("fang",f.player.getQuest(ExpeditionXXV.MASTER));assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.FANG));assertEquals(xp,f.player.getXP());assertTrue(f.zone.dropped.isEmpty());}
    @Test public void legacyCompletedOrUnknownRecordsAreNeverReset(){for(String record:Arrays.asList("done;123","start;stary przedmiot;1")){f.player.setQuest(ExpeditionXXV.MASTER,record);int xp=f.player.getXP();f.greet(MasterStage.NPC_NAME);type("etap 25");type("zadanie");type("tak");assertEquals(record,f.player.getQuest(ExpeditionXXV.MASTER));assertEquals(xp,f.player.getXP());}}
    @Test public void journalAndWireShowSeparateObjectivesAndRealSprites(){startMaster();prepared();report();survey(1,true);QuestProgress q=PilotQuestProgress.read(new MainTask25(),f.player);assertEquals(3,q.getObjectives().size());assertEquals(1,q.getObjectives().get(1).getCurrent());assertNotNull(QuestProgress.fromWire(q.toWire()));f.greet(MasterStage.NPC_NAME);type("etap 25");assertEquals(3,f.latest().getItems().size());assertEquals("useable/expedition/lamp",f.latest().getItems().get(0).getSprite());}
    @Test public void goodbyeInNewBranchClosesConversation(){open("Thyron");f.click("goodbye");assertFalse(f.latest().isActive());assertEquals(ConversationStates.IDLE,f.npcs.get("Thyron").getEngine().getCurrentState());}
    @Test public void droppingParcelInWorldDoesNotPermitASecondCopy(){
        f.player.setQuest(ExpeditionXXV.FITTINGS,"ready");open("Hogart");f.click("give_parcel");Item item=ExpeditionXXV.carriedPackage(f.player);f.player.drop(item);
        final List<RPObject> groundObjects=new ArrayList<RPObject>();groundObjects.add(item);
        StendhalRPZone ground=new StendhalRPZone("xxv_parcel_ground"){@Override public Iterator<RPObject> iterator(){return groundObjects.iterator();}};
        JourneyQuestFixture.world.addRPZone("xxv_test",ground);
        try{f.click("give_parcel");assertNull(ExpeditionXXV.carriedPackage(f.player));assertEquals(1,groundObjects.size());}
        finally{JourneyQuestFixture.world.removeZone(ground);}
    }
    @Test public void obsoletePanelActionDoesNotPayTwice(){
        prepared();f.player.setQuest(ExpeditionXXV.MASTER,"fang");f.carry(ExpeditionXXV.FANG,2);f.greet(MasterStage.NPC_NAME);type("etap 25");NpcDialogueSnapshot before=f.latest();
        f.click("give_master");int xp=f.player.getXP();RPAction stale=new RPAction();stale.put("session",before.getSession());stale.put("revision",Long.toString(before.getRevision()));stale.put("reply","give_master");
        JourneyQuestFixture.rules.turn+=35;new games.stendhal.server.actions.NpcDialogueAction().onAction(f.player,stale);
        assertEquals(xp,f.player.getXP());assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.BADGE));assertEquals(1,f.player.getNumberOfEquipped(ExpeditionXXV.FANG));
    }
    @Test public void leavingOfferCancelsItsPendingYes(){open("Thyron");f.click("xxv_accept");f.click("back");type("tak");assertFalse(f.player.hasQuest(ExpeditionXXV.FITTINGS));}
    @Test public void losingEligibilityWhileOfferIsOpenCannotAccept(){open("Fag");f.click("xxv_accept");level(319);type("tak");assertFalse(f.player.hasQuest(ExpeditionXXV.HUNT));}
    @Test public void missingFangKeepsReportAndPaysNothing(){prepared();f.player.setQuest(ExpeditionXXV.MASTER,"fang");f.greet(MasterStage.NPC_NAME);type("etap 25");int xp=f.player.getXP();f.click("give_master");assertEquals("fang",f.player.getQuest(ExpeditionXXV.MASTER));assertEquals(xp,f.player.getXP());assertFalse(f.player.isEquipped(ExpeditionXXV.BADGE));}
}


