package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.Level;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.quests.CheeseForSoro;
import java.util.*;
import java.util.regex.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Exercises the ordinary text routes as well as the recommended panel replies. */
public class EditorialDialogueAuditTest {
    private CollectionQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}


    @Test public void completedSoroGuidanceUsesActualSedroStateAndStaysReadOnly(){
        f.player.setQuest(CheeseForSoro.QUEST_SLOT, "done");
        f.player.setQuest("help_sedro", "done");
        int xp=f.player.getXP();f.greet("Soro");
        String text=f.type("zadanie").getSpeech();
        assertTrue(text,text.contains("Sedro też otrzymał pomoc"));
        assertFalse(text,text.contains("odwiedź Sedro"));
        assertEquals(xp,f.player.getXP());assertTrue(f.player.isQuestCompleted("help_sedro"));
        f.player.setQuest("help_sedro", "rejected");f.greet("Soro");
        text=f.type("zadanie").getSpeech();assertTrue(text,text.contains("Odwiedź Sedro"));
        assertEquals("rejected",f.player.getQuest("help_sedro"));
    }
    @Test public void weeklyFirstAndRepeatLabelsKeepSameNativeCommand(){
        games.stendhal.server.maps.quests.WeeklyCollectorQuest q=new games.stendhal.server.maps.quests.WeeklyCollectorQuest();
        NpcDialogueSnapshot fresh=f.greet("Wandy");
        String initial=fresh.getReplies().stream().filter(r->r.getId().equals("quest")).findFirst().get().getLabel();
        assertFalse(initial,initial.contains("kolejne"));
        f.click("quest");assertTrue(f.player.hasQuest(q.getSlotName()));
        f.player.setQuest(q.getSlotName(),"done;0");
        String repeat=f.greet("Wandy").getReplies().stream().filter(r->r.getId().equals("quest")).findFirst().get().getLabel();
        assertTrue(repeat,repeat.contains("kolejnym"));
        assertEquals("done;0",f.player.getQuest(q.getSlotName()));
    }
    @Test public void collectorsWaitingReplyDoesNotPretendToForgeAnItem(){
        games.stendhal.server.maps.quests.AndrzejCollector q=new games.stendhal.server.maps.quests.AndrzejCollector();
        String saved="forging;"+System.currentTimeMillis();f.player.setQuest(q.getSlotName(),saved);
        NpcDialogueSnapshot page=f.greet("Kolekcjoner Andrzej");
        String label=page.getReplies().stream().filter(r->r.getId().equals("supplies")).findFirst().get().getLabel();
        assertTrue(label,label.contains("kolekcję"));assertFalse(label,label.contains("wyrób"));
        int xp=f.player.getXP();f.click("supplies");assertEquals(saved,f.player.getQuest(q.getSlotName()));assertEquals(xp,f.player.getXP());
    }

    @Test public void freshSoroGreetingDoesNotInventLevelGate(){
        f.player.setXP(Level.getXP(1));f.player.setLevel(1);
        String text=f.greet("Soro").getSpeech();
        assertFalse(text,text.contains("5 poziom"));
        f.click("quest");f.click("know");f.click("ingredients");f.click("accept");
        assertEquals("ser=3",f.player.getQuest(CheeseForSoro.QUEST_SLOT));
    }
    @Test public void typedSoroHintMatchesNewAndPartDeliveredOrder(){
        f.greet("Soro");String text=f.type("wskazówki").getSpeech();
        assertFalse(text,text.contains("dziesięć"));assertTrue(text,text.contains("trzech"));
        f.player.setQuest(CheeseForSoro.QUEST_SLOT,"ser=2");
        f.greet("Soro");f.type("wskazówki");
        assertEquals("ser=2",f.player.getQuest(CheeseForSoro.QUEST_SLOT));
        f.carry("ser",3);f.click("supplies");f.click("give_ser");
        assertEquals("done",f.player.getQuest(CheeseForSoro.QUEST_SLOT));
        assertEquals(1,f.player.getNumberOfEquipped("ser"));
    }
    @Test public void rejectedSoroOfferActuallyAsksForRenewedAgreement(){
        f.player.setQuest(CheeseForSoro.QUEST_SLOT,"rejected");f.greet("Soro");
        NpcDialogueSnapshot offered=f.click("quest");
        assertTrue(offered.getSpeech(),offered.getSpeech().contains("trzech"));
        assertTrue(f.has("accept"));f.click("accept");
        assertEquals("ser=3",f.player.getQuest(CheeseForSoro.QUEST_SLOT));
    }
    @Test public void revisedHelpKeywordsHaveNativeConversationRoutes(){
        String[] names={"Mrotho","Tajemniczy Wędrowiec","Andy","Walt","Wandy","Pequod","Santiago","Carena","Samaron","Bario","Mayor Chalmers","Urzędnik Ados","Kolekcjoner Andrzej","Josephine","Sausid","Ognir","Adalbert","Hazel","Orc Saman","Vulcanus","Morgrin","Baldemar","Lorithien","Maerion","Tywysoga","Hogart","Leander","Ceryl","Thorgal","Natan","Ilisa","Mayor Sakhs","Urzędnik Hans","Urzędnik Bill","Wrviliza","Wrvil","Florence Boullabaisse"};
        Pattern topic=Pattern.compile("#(?:'([^']+)'|\"([^\"]+)\"|([\\p{L}\\p{N}_-]+))");
        List<String> failures=new ArrayList<String>();int checked=0;
        f.player.setXP(Level.getXP(350));f.player.setLevel(350);
        for(String name:names){
            f.greet(name);SpeakerNPC npc=f.npcs.get(name);
            if("Florence Boullabaisse".equals(name)){f.click("recipe");f.click("decline");}
            if(!npc.getEngine().canHandle(f.player,"pomoc")){failures.add(name+": missing help route");continue;}
            NpcDialogueSnapshot help=f.type("pomoc");Matcher matcher=topic.matcher(help.getSpeech());
            f.capture("editorial-help-"+ResidentDialogueReplies.id(name));
            while(matcher.find()){
                String command=matcher.group(1)!=null?matcher.group(1):matcher.group(2)!=null?matcher.group(2):matcher.group(3);
                checked++;
                if(!npc.getEngine().canHandle(f.player,command))failures.add(name+": #"+command+" in "+help.getSpeech());
            }
        }
        assertTrue("Keyword coverage: "+checked,checked>=25);
        assertTrue(failures.toString(),failures.isEmpty());
        System.out.println("EDITORIAL_HELP_ROUTES NPCs="+names.length+" keywords="+checked);
    }
    @Test public void askingAboutDeliveryDoesNotConsumeExistingMaterialsOrPayment(){
        f.player.setQuest(CheeseForSoro.QUEST_SLOT,"ser=2");f.carry("ser",3);f.carry("money",5000);
        int xp=f.player.getXP();double karma=f.player.getKarma();
        f.greet("Soro");
        for(String command:Arrays.asList("wskazówki","praca","oferta","wskazówki"))f.type(command);
        assertEquals("ser=2",f.player.getQuest(CheeseForSoro.QUEST_SLOT));
        assertEquals(3,f.player.getNumberOfEquipped("ser"));assertEquals(5000,f.player.getNumberOfEquipped("money"));
        assertEquals(xp,f.player.getXP());assertEquals(karma,f.player.getKarma(),0.00001);
    }
}
