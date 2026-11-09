package games.stendhal.server.entity.npc.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.core.rp.quest.JourneyQuestProgress;
import games.stendhal.server.entity.item.*;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import org.junit.*;

/** Re-entry, missing resources and legacy saves exercise native server guards. */
public class JourneyQuestBoundaryTest {
    JourneyQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new JourneyQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    void begin(String npc){f.start(npc);}
    void stock(Map<String,Integer> items){for(Map.Entry<String,Integer> e:items.entrySet())f.carry(e.getKey(),e.getValue());}
    void ring(){f.carry("pierścień szmaragdowy",1);((RingOfLife)f.player.getFirstEquipped("pierścień szmaragdowy")).damage();}
    void costs(){stock(JourneyQuestProgress.items("money",100000,"sztabka złota",40,"szmaragd",10,"diament",1));}
    @Test public void unknownRecordsAreReadOnly(){
        for(IQuest q:Arrays.<IQuest>asList(new ElementalDagger1(),new ElementalDagger2(),new Ciupagaz1Wasem(),new Podroznik(),new DeadWife(),new CrownForTheWannaBeKing(),new KoboldishTorcibud(),new MagicStaff(),new ImperialPrincess(),new FindGhosts())){
            f.player.setQuest(q.getSlotName(),"unfamiliar;save");assertEquals(q.getName(),QuestProgress.State.UNKNOWN,JourneyQuestProgress.read(q,f.player).getState());assertEquals("unfamiliar;save",f.player.getQuest(q.getSlotName()));
        }
    }
    @Test public void fullStaffCostIsRequired(){
        f.player.setQuest("magicstaff","killing");stock(JourneyQuestProgress.items("magia deszczu",1000,"magia mroku",500,"magia światła",250,"diament",1,"pazur zielonego smoka",1,"pazur czarnego smoka",2,"pazur złotego smoka",4,"różdżka Swaroga",1,"kryształ mocy",1));
        f.greet("Samaron");f.click("give_staff");assertEquals("killing",f.player.getQuest("magicstaff"));assertEquals(2,f.player.getNumberOfEquipped("pazur czarnego smoka"));assertFalse(f.player.isEquipped("różdżka mokoszy"));f.capture("27-staff-missing-claw");
    }
    @Test public void woyReminderCannotAdvanceOrReset(){
        f.player.setQuest("magicstaff","first");f.greet("Woy'tyk");f.click("give_food");assertEquals("first",f.player.getQuest("magicstaff"));f.type("jedzenie");assertEquals("first",f.player.getQuest("magicstaff"));f.capture("28-athor-food");
    }
    @Test public void alrakFullFoodAndBookStages(){
        f.greet("Alrak");f.click("quest");f.click("supplies");f.click("accept");String food=f.player.getQuest("obsidian_knife");assertTrue(Arrays.asList("szynka","mięso","ser").contains(food));f.carry(food,99);f.type(food);assertEquals(food,f.player.getQuest("obsidian_knife"));assertEquals(99,f.player.getNumberOfEquipped(food));f.carry(food,1);f.type(food);assertEquals("food_brought",f.player.getQuest("obsidian_knife"));f.capture("29-alrak-food");f.click("book");f.click("confirm");assertEquals("seeking_book",f.player.getQuest("obsidian_knife"));
    }
    @Test public void alrakReadingAndLevelGateRemain(){
        f.player.setQuest("obsidian_knife","reading;"+System.currentTimeMillis());f.greet("Alrak");assertTrue(f.player.getQuest("obsidian_knife").startsWith("reading;"));assertFalse(f.has("knife"));
        f.player.setQuest("obsidian_knife","book_read");f.greet("Alrak");f.click("knife");assertEquals("book_read",f.player.getQuest("obsidian_knife"));assertFalse(f.player.isEquipped("obsydianowy saks"));
    }
    @Test public void lostOrchidRequiresDelayAndConfirmation(){
        f.player.setQuest("elf_princess","got_flower;"+(System.currentTimeMillis()-13L*7*24*60*60000));f.greet("Róża Kwiaciarka");assertTrue(f.has("confirm"));f.click("confirm");assertTrue(f.player.isEquipped("orchidea"));f.reward("orchidea",1);
    }
    @Test public void orchidCannotBeDuplicatedSoonAfterPickup(){
        begin("Tywysoga");f.greet("Róża Kwiaciarka");f.player.drop("orchidea");f.greet("Róża Kwiaciarka");f.type("kwiat księżniczki");assertFalse(f.player.isEquipped("orchidea"));assertFalse(f.has("confirm"));
    }
    @Test public void ordinarySoldierItemsAreNotEvidence(){
        begin("Henry");stock(JourneyQuestProgress.items("skórzane spodnie",1,"zbroja łuskowa",1,"karteczka",1));f.click("supplies");assertEquals("start",f.player.getQuest("soldier_henry"));assertTrue(f.player.isEquipped("zbroja łuskowa"));assertFalse(f.player.isEquipped("mapa"));f.capture("30-henry-personal-evidence");
    }
    @Test public void activeSoldierQuestCannotRestart(){
        begin("Henry");String saved=f.player.getQuest("soldier_henry");f.type("zadanie");f.type("tak");assertEquals(saved,f.player.getQuest("soldier_henry"));assertFalse(f.has("quest"));
    }
    @Test public void benCannotGrantMeetingTwiceAfterAnotherGhost(){
        f.greet("Ben");f.greet("Mary");int xp=f.player.getXP();double karma=f.player.getKarma();f.greet("Ben");assertEquals(xp,f.player.getXP());assertEquals(karma,f.player.getKarma(),0.00001);f.capture("31-ben-return");
    }
    @Test public void carenaDoesNotAcceptUnseenGhost(){
        begin("Carena");f.type("wieści duchów");String saved=f.player.getQuest("find_ghosts");f.type("mary");assertEquals(saved,f.player.getQuest("find_ghosts"));assertFalse(f.has("give_mary"));f.capture("32-carena-no-news");
    }
    @Test public void meetingsRenderActualNPCSprites(){
        f.greet("Gorath");assertFalse(f.latest().getItems().isEmpty());for(NpcDialogueSnapshot.Item i:f.latest().getItems())assertTrue(i.getSprite().startsWith("npc/"));f.capture("33-dragon-first");f.greet("Cherubiel");f.capture("34-cherub-first");
    }
    @Test public void markovichNativeProductionAndPickup(){
        f.player.setQuest("vs_quest","start");stock(JourneyQuestProgress.items("pusta czara",1,"truchło wampira",7,"truchło nietoperza",7,"pierścień z czaszką",1));f.greet("Markovich");f.click("fill");f.click("confirm");String saved=f.player.getQuest("sicky_fill_goblet");assertNotNull(saved);assertFalse(f.player.isEquipped("pusta czara"));f.capture("35-markovich-production");
        f.greet("Markovich");assertFalse(f.player.isEquipped("czara"));f.player.setQuest("sicky_fill_goblet","1;czara;"+(System.currentTimeMillis()-6*60000L));f.greet("Markovich");assertTrue(f.player.isEquipped("czara"));f.reward("czara",1);assertEquals(f.player.getName(),f.player.getFirstEquipped("czara").getBoundTo());assertFalse(f.has("fill"));assertTrue(f.latest().getItems().isEmpty());f.capture("36-markovich-goblet");
    }
    @Test public void hogartRequiresLordKill(){
        begin("Hogart");f.carry("czara",1);f.carry("truchło wampira",200);f.type("praca hogarta");assertEquals("start",f.player.getQuest("vs_quest"));assertEquals(200,f.player.getNumberOfEquipped("truchło wampira"));
    }
    @Test public void healthyRingDoesNotOfferPaidRepair(){
        f.carry("pierścień szmaragdowy",1);f.greet("Ognir");f.click("give_ring");assertFalse(f.has("confirm"));assertFalse(f.player.hasQuest("fix_emerald_ring"));
    }
    @Test public void missingRingAtConfirmationDoesNotConsumePayment(){
        ring();costs();f.greet("Ognir");f.click("give_ring");f.capture("37-ring-cost");f.player.drop("pierścień szmaragdowy");f.type("tak");assertFalse(f.player.hasQuest("fix_emerald_ring"));assertEquals(100000,f.player.getNumberOfEquipped("money"));f.type("tak");assertFalse(f.player.hasQuest("fix_emerald_ring"));
    }
    @Test public void unboundRingRemainsUnboundAfterWholeRepair(){
        ring();costs();f.greet("Ognir");f.click("give_ring");f.click("confirm");assertTrue(f.player.getQuest("fix_emerald_ring").startsWith("forgingunbound;"));assertFalse(f.player.isEquipped("money"));f.type("szmaragd");assertFalse(f.player.isEquipped("pierścień szmaragdowy"));
        f.player.setQuest("fix_emerald_ring","forgingunbound;"+(System.currentTimeMillis()-16*60000L));f.greet("Ognir");f.click("give_ring");assertFalse(f.player.getFirstEquipped("pierścień szmaragdowy").isBound());f.reward("XP postaci",500);
    }
    @Test public void partialCraftDeliveryPersistsAndLoreDoesNotChangeSave(){
        begin("Żywioł Ognia");f.carry("żelazo",4);f.click("supplies");String saved=f.player.getQuest("elementalsword_quest");assertNotNull(saved);assertFalse(f.player.isEquipped("żelazo"));f.greet("Żywioł Ognia");f.click("hints");assertEquals(saved,f.player.getQuest("elementalsword_quest"));f.capture("38-fire-partial");
    }
    @Test public void oldQueenKillIsNotFreshQuestProgress(){
        f.player.setSoloKill("królowa pająków");begin("Orc Saman");f.click("supplies");assertTrue(f.player.getQuest("club_thorns").startsWith("start"));assertFalse(f.player.isEquipped("młot Thora"));f.capture("39-saman-new-kill-needed");
    }
    @Test public void quizFailureKeepsCooldownAndDoesNotGrantLicense(){
        begin("Santiago");f.type("nieistniejąca ryba");String time=f.player.getQuest("fishermans_license1");assertNotNull(time);assertFalse(f.player.isQuestCompleted("fishermans_license1"));f.greet("Santiago");f.type("egzamin");assertEquals(time,f.player.getQuest("fishermans_license1"));f.capture("40-santiago-cooldown");
    }
    @Test public void allJourneyNPCsAcceptGoodbye(){
        for(games.stendhal.server.entity.npc.SpeakerNPC npc:f.npcs.values())if(JourneyDialogueReplies.supports(npc)){
            f.greet(npc.getName());assertTrue("Missing goodbye: "+npc.getName(),f.has("goodbye"));
            f.click("goodbye");assertFalse("Still active: "+npc.getName(),f.latest().isActive());assertFalse("Still talking: "+npc.getName(),npc.isTalking());
        }
    }
}
