package games.stendhal.server.entity.npc.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.JourneyQuestProgress;
import games.stendhal.server.entity.item.*;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import org.junit.*;

/** Native parser, inventory, quest records and wire actions, without opening a database. */
public class JourneyQuestDialogueTest {
    JourneyQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new JourneyQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    void begin(String npc){f.greet(npc);f.click("quest");f.click("accept");}
    void done(String slot,String reward,int count){assertEquals("done",f.player.getQuest(slot));f.reward(reward,count);int xp=f.player.getXP();f.type("tak");assertEquals(xp,f.player.getXP());}
    void aged(String slot,String phase,int minutes){f.player.setQuest(slot,phase+";"+(System.currentTimeMillis()-minutes*60000L));for(SpeakerNPC n:f.npcs.values())if(n.getAttending()==f.player){f.type("wątki");break;}}
    void items(Map<String,Integer> list){for(Map.Entry<String,Integer> e:list.entrySet())f.carry(e.getKey(),e.getValue());}

    @Test public void cerylLoanAndRealBook(){
        begin("Ceryl");f.greet("Jynath");assertEquals("jynath",f.player.getQuest("ceryl_book"));f.reward("księga czarna",1);
        f.greet("Ceryl");assertEquals("done",f.player.getQuest("ceryl_book"));f.reward("money",50);f.reward("XP postaci",100);f.capture("01-ceryl-book");
    }
    @Test public void quoteUsesFreeTextAndCorrectNativeAnswer() throws Exception{
        begin("Pequod");String who=f.player.getQuest("get_fishing_rod");
        java.lang.reflect.Field field=LookUpQuote.class.getDeclaredField("quotes");field.setAccessible(true);
        String quote=((Map<String,String>)field.get(null)).get(who);assertNotNull(quote);
        f.type("zadanie");f.click("confirm");assertEquals(1,f.latest().getReplies().size());f.type(quote);
        assertEquals("done",f.player.getQuest("get_fishing_rod"));f.reward("wędka",1);f.reward("XP postaci",750);f.capture("02-pequod-reward");
    }
    @Test public void staffCrossesFourNPCsAndCannotRestart(){
        begin("Samaron");f.greet("Woy'tyk");f.click("quest");assertEquals("first",f.player.getQuest("magicstaff"));
        f.greet("Leander");f.click("give_athor");assertEquals("first1",f.player.getQuest("magicstaff"));f.carry("mięso z kraba",60);f.type("jedzenie");f.reward("paczka jedzenia",1);
        f.greet("Woy'tyk");f.click("give_food");assertEquals("first2",f.player.getQuest("magicstaff"));items(JourneyQuestProgress.items("różdżka",10,"magia ziemi",2500,"magia płomieni",2000));f.click("give_magic");f.reward("uszkodzona różdżka",1);
        f.greet("Ylrik");f.click("give_staff");f.carry("muchomor",40);f.type("różdżka");assertEquals("powrot",f.player.getQuest("magicstaff"));
        f.greet("Samaron");f.click("give_staff");assertEquals("killing",f.player.getQuest("magicstaff"));
        items(JourneyQuestProgress.items("magia deszczu",1000,"magia mroku",500,"magia światła",250,"diament",1,"pazur zielonego smoka",1,"pazur czarnego smoka",3,"pazur złotego smoka",4,"różdżka Swaroga",1));f.type("różdżka");
        assertTrue(f.payFeeIfRequested("magicstaff"));f.type("różdżka");assertEquals("done",f.player.getQuest("magicstaff"));f.reward("różdżka mokoszy",1);f.capture("03-staff-complete");f.greet("Woy'tyk");f.type("jedzenie");assertEquals("done",f.player.getQuest("magicstaff"));
    }
    @Test public void alrakUsesSecondBookAfterCerylLoan() {
        f.player.setQuest("ceryl_book","done");f.player.setQuest("obsidian_knife","seeking_book");f.greet("Ceryl");f.click("alrak");f.click("give_message");assertEquals("got_book",f.player.getQuest("obsidian_knife"));f.reward("księga lazurowa",1);
        f.greet("Alrak");assertTrue(f.player.getQuest("obsidian_knife").startsWith("reading;"));aged("obsidian_knife","reading",4321);f.type("praca alraka");f.player.setXP(Level.getXP(51));f.player.setLevel(51);f.click("knife");assertEquals("knife_offered",f.player.getQuest("obsidian_knife"));
        f.carry("obsydian",1);f.carry("dorsz",1);f.player.setSoloKill("czarny smok");f.type("praca alraka");assertTrue(f.player.getQuest("obsidian_knife").startsWith("forging;"));aged("obsidian_knife","forging",11);f.type("praca alraka");
        assertTrue(f.payFeeIfRequested("obsidian_knife"));f.type("praca alraka");assertEquals("done",f.player.getQuest("obsidian_knife"));f.reward("obsydianowy saks",1);f.reward("XP postaci",100000);f.capture("04-alrak-complete");
    }
    @Test public void esclaraRandomRewardAndCooldown(){
        begin("Princess Esclara");f.carry("napój z oliwką",1);f.click("give_drink");assertTrue(f.player.getQuest("amazon_princess").startsWith("drinking;"));
        assertTrue(f.player.getNumberOfEquipped("tarta z rybnym nadzieniem")>=2);assertTrue(f.player.getNumberOfEquipped("tarta z rybnym nadzieniem")<=7);assertFalse(f.has("quest"));f.capture("05-esclara-drink");
    }
    @Test public void tywysogaFlowerAndReward(){
        begin("Tywysoga");f.greet("Róża Kwiaciarka");assertEquals("got_flower",f.player.getQuest("elf_princess",0));f.reward("orchidea",1);
        f.greet("Tywysoga");f.click("give_flower");assertEquals("flower_brought",f.player.getQuest("elf_princess",0));assertTrue(f.player.getNumberOfEquipped("sztabka złota")>=5);f.reward("XP postaci",5000);f.capture("06-tywysoga-orchid");
    }
    @Test public void ylfliaUsesAcceptedLevelAndAwardsCitizenship(){
        begin("Princess Ylflia");int level=Integer.parseInt(f.player.getQuest("imperial_princess"));f.player.setXP(Level.getXP(20));f.player.setLevel(20);
        items(JourneyQuestProgress.items("arandula",1+level/5,"eliksir",1+level/3,"antidotum",1+level/4,"kokuda",1,"sclaria",1,"kekik",1));f.click("give_herbs");assertEquals("recommended",f.player.getQuest("imperial_princess"));f.reward("XP postaci",level*2000);
        f.greet("King Cozart");assertEquals("done",f.player.getQuest("imperial_princess"));f.reward("Obywatelstwo Kalavan",1);f.capture("07-kalavan-citizen");
    }
    @Test public void lorenzCrossesBothPrincesses(){
        begin("Lorenz");f.carry("kosa",1);f.click("give_kosa");f.greet("Princess Esclara");f.click("lorenz");f.greet("Lorenz");f.click("tunnel");f.carry("jajo",1);f.click("give_jajo");
        f.greet("Princess Ylflia");f.click("lorenz");f.greet("Lorenz");f.click("give_message");f.carry("zbroja barbarzyńcy",1);f.click("give_armor");assertEquals("done",f.player.getQuest("jailedbarb"));f.reward("sztabka złota",33);f.reward("sztabka mithrilu",12);f.capture("08-lorenz-complete");
    }
    @Test public void hunelRetainsKeyAndOpensTrade(){
        f.greet("Hunel");f.carry("klucz do więzienia Kanmararn",1);f.click("give_key");assertEquals("done",f.player.getQuest("jailed_dwarf"));assertTrue(f.player.isEquipped("klucz do więzienia Kanmararn"));f.reward("Handel z Hunelem",1);f.capture("09-hunel-access");
    }
    @Test public void crownCollectionAndSeparateKendraReward(){
        begin("Ivan Abe");items(CrownForTheWannaBeKing.getRequiredItems());
        for(String item:CrownForTheWannaBeKing.getRequiredItems().keySet())f.type(item);
        assertEquals("reward",f.player.getQuest("crown_for_the_wannabe_king"));f.reward("XP postaci",1000000);int before=f.player.getAtkXP();f.greet("Kendra Mattori");f.click("give_message");assertTrue(f.payFeeIfRequested("crown_for_the_wannabe_king"));f.click("give_message");assertEquals("done",f.player.getQuest("crown_for_the_wannabe_king"));f.reward("XP ataku",f.player.getAtkXP()-before);f.capture("10-kendra-reward");
    }
    void tagged(String item,String owner){f.carry(item,1);f.player.getFirstEquipped(item).setInfoString(owner);}
    @Test public void soldiersRequirePersonalItemsAndKeepNote(){
        begin("Henry");tagged("skórzane spodnie","tom");tagged("zbroja łuskowa","peter");tagged("karteczka","charles");f.click("supplies");assertEquals("map",f.player.getQuest("soldier_henry"));assertTrue(f.player.isEquipped("karteczka"));f.reward("mapa",1);
        f.greet("Sergeant James");f.click("give_map");assertEquals("done",f.player.getQuest("soldier_henry"));f.reward("buty żelazne",1);f.reward("XP postaci",5000);f.capture("11-soldiers-complete");
    }
    @Test public void elevenDragonsAreUnique(){
        for(String name:FindDragons.getMeetingNames())f.greet(name);assertTrue(new FindDragons().isCompleted(f.player));f.reward("płaszcz ze smoczych łusek",1);f.capture("12-dragons-complete");int xp=f.player.getXP();f.greet(FindDragons.getMeetingNames().get(0));assertEquals(xp,f.player.getXP());assertTrue(f.latest().getRewards().isEmpty());
    }
    @Test public void ghostsRequireMeetingBeforeReporting(){
        begin("Carena");for(String name:Arrays.asList("Mary","Ben","Zak","Goran"))f.greet(name);f.greet("Carena");
        for(String name:Arrays.asList("mary","ben","zak","goran"))f.click("give_"+name);
        assertEquals("done",f.player.getQuest("find_ghosts"));f.reward("Maksymalne zdrowie",50);f.reward("XP postaci",10000);f.capture("13-carena-complete");
    }
    @Test public void sevenCherubsAndNoRepeatedReward(){
        for(String name:SevenCherubs.getMeetingNames())f.greet(name);assertTrue(new SevenCherubs().isCompleted(f.player));f.reward("anielski pierścień",1);f.reward("XP postaci",10000);f.capture("14-cherubs-complete");int xp=f.player.getXP();f.greet("Cherubiel");assertEquals(xp,f.player.getXP());
    }
    @Test public void wrvilizaKeepsRandomListAndReportsFloorBottles(){
        begin("Wrviliza");Map<String,Integer> needed=JourneyQuestProgress.remaining(new KoboldishTorcibud(),f.player);String saved=f.player.getQuest("koboldish_torcibud");items(needed);assertEquals(saved,f.player.getQuest("koboldish_torcibud"));
        for(String item:needed.keySet())f.type(item);assertTrue(new KoboldishTorcibud().isCompleted(f.player));f.reward("XP postaci",500);assertTrue(f.latest().getRewards().stream().anyMatch(r->r.isOnGround()&&r.getName().equals("leżakowana nalewka litworowa")&&r.getAmount()>=2&&r.getAmount()<=7));f.capture("15-wrviliza-floor");
    }
    @Test public void hogartReceivesFullMaterialsAndWaits(){
        begin("Hogart");f.player.drop("pusta czara");f.carry("czara",1);f.carry("truchło wampira",200);f.player.setSoloKill("lord wampir");f.click("supplies");assertTrue(f.player.getQuest("vs_quest").startsWith("forging;"));
        aged("vs_quest","forging",11);f.type("praca hogarta");assertTrue(f.payFeeIfRequested("vs_quest"));f.type("praca hogarta");assertEquals("done",f.player.getQuest("vs_quest"));f.reward("krwiopijca",1);f.reward("XP postaci",50000);f.capture("16-hogart-complete");
    }
    @Test public void ringPickupPreservesBinding(){
        aged("fix_emerald_ring","forging",16);f.greet("Ognir");f.click("give_ring");assertEquals("done",f.player.getQuest("fix_emerald_ring"));assertEquals(f.player.getName(),f.player.getFirstEquipped("pierścień szmaragdowy").getBoundTo());f.reward("pierścień szmaragdowy",1);f.capture("17-ring-complete");
    }
    @Test public void samanCountsFreshQueenKill(){
        begin("Orc Saman");f.reward("klucz do więzienia Kotoch",1);f.player.setSoloKill("królowa pająków");f.click("supplies");assertEquals("done",f.player.getQuest("club_thorns"));f.reward("młot Thora",1);f.capture("18-saman-complete");
    }
    void craft(IQuest q,String npc,String command,int wait,String item,String capture){
        begin(npc);items(JourneyQuestProgress.requirements(q));f.player.setSoloKill(JourneyQuestProgress.kill(q));f.click("supplies");assertTrue(q.getName()+": "+f.player.getQuest(q.getSlotName()),f.player.getQuest(q.getSlotName()).startsWith("forging;"));
        aged(q.getSlotName(),"forging",wait+1);f.type(command);if(f.payFeeIfRequested(q.getSlotName()))f.type(command);assertEquals("done",f.player.getQuest(q.getSlotName()));f.reward(item,1);f.capture(capture);
    }
    @Test public void fireDagger(){craft(new ElementalDagger1(),"Żywioł Ognia","materiały ognia",90,"sztylet żywiołów","19-fire-dagger");}
    @Test public void waterDagger(){craft(new ElementalDagger2(),"Żywioł Wody","materiały wody",180,"ulepszony sztylet żywiołów","20-water-dagger");}
    @Test public void ludwikAxe(){craft(new Ciupagaz1Wasem(),"Ludwik","materiały ludwika",1440,"złota ciupaga z wąsem","21-ludwik-axe");}
    @Test public void travellerNecklace(){craft(new Podroznik(),"Tajemniczy Wędrowiec","materiały wędrowca",15,"naszyjnik podróżnika","22-traveller-necklace");}
    @Test public void adalbertPartialKeepsakes(){
        f.greet("Adalbert");f.type("pomóc");f.click("accept");f.carry("krwawa suknia",1);f.click("supplies");f.click("give_krwawa suknia");f.capture("23-adalbert-partial");f.carry("krwawa obrączka",1);f.type("krwawa obrączka");assertEquals("done",f.player.getQuest("dead_wife"));f.reward("krwawy pierścień",1);f.capture("24-adalbert-complete");
    }
    @Test public void santiagoQuizIsAnonymousAndPracticalCollectionCannotReset() throws Exception{
        begin("Santiago");java.lang.reflect.Field field=FishermansLicenseQuiz.class.getDeclaredField("fishOnTable");field.setAccessible(true);
        for(int i=0;i<8;i++){
            Item fish=(Item)field.get(f.quests.get("FishermansLicenseQuiz"));assertNotNull(fish);assertFalse(f.latest().getItems().isEmpty());assertEquals("Jaki to gatunek?",f.latest().getItems().get(0).getName());assertEquals(1,f.latest().getReplies().size());assertTrue("Exam fish must not be revealed as a reward",f.latest().getRewards().isEmpty());if(i==0)f.capture("25-santiago-quiz");f.type(fish.getName());
        }
        assertEquals("passed",f.player.getQuest("fishermans_license1"));assertTrue(f.payFeeIfRequested("fishermans_license1"));f.type("egzamin");assertEquals("done",f.player.getQuest("fishermans_license1"));f.reward("XP postaci",500);f.type("egzamin");f.click("confirm");f.carry("pstrąg",1);f.click("supplies");f.click("give_pstrąg");
        f.greet("Santiago");f.type("egzamin");assertTrue(f.player.getQuest("fishermans_license2").contains("pstrąg"));f.click("supplies");
        for(String fish:FishermansLicenseCollector.getRequiredFish())if(!"pstrąg".equals(fish)){f.carry(fish,1);f.type(fish);if(f.payFeeIfRequested("fishermans_license2")){f.click("supplies");f.type(fish);}}
        assertEquals("done",f.player.getQuest("fishermans_license2"));assertEquals("0.5",f.player.getSkill("fishing"));f.reward("Karta wędkarska",1);f.capture("26-santiago-license");
    }
}
