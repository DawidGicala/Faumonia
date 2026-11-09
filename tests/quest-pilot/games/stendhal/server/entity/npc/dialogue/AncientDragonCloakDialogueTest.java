package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.maps.quests.AncientDragonCloak;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;

public class AncientDragonCloakDialogueTest {
    private CollectionQuestFixture f;
    private static final String SLOT="ancient_dragon_cloak";
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();new AncientDragonCloak().addToWorld();}
    @After public void cleanup(){f.restoreNPCs();}

    @Test public void requirementsAndLegacyDonePermitOnlyEligibleStart(){
        f.player.setLevel(349);f.player.setQuest("find_dragons","done");f.greet("Terry");f.click("ancient_cloak");
        assertFalse(f.player.hasQuest(SLOT));assertTrue(f.latest().getSpeech().contains("350"));
        f.player.setLevel(350);f.type("pradawny płaszcz");assertEquals("green_cloaks",f.player.getQuest(SLOT));
        assertTrue(f.latest().getSpeech().contains("300000"));
        f.click("ancient_cloak");assertEquals("green_cloaks",f.player.getQuest(SLOT));
        assertTrue(f.latest().getItems().stream().anyMatch(i->i.getName().equals("szmaragdowy płaszcz smoczy")));
    }

    @Test public void fullRecipeTimeFeeRewardAndRepeatedRequest(){
        f.player.setLevel(350);f.player.setQuest("find_dragons","done");f.greet("Terry");f.click("ancient_cloak");
        String[] stages={"green_cloaks","bone_cloaks","blue_cloaks","red_cloaks","black_cloaks","skins"};
        for(String stage:stages){
            assertEquals(stage,f.player.getQuest(SLOT));
            Map<String,Integer> required=AncientDragonCloak.currentRequirements(f.player,"Terry");
            for(Map.Entry<String,Integer> need:required.entrySet())f.carry(need.getKey(),need.getValue());
            f.type("pradawny płaszcz");
            for(String item:required.keySet())assertFalse(f.player.isEquipped(item));
        }
        assertEquals("josephine",f.player.getQuest(SLOT));f.greet("Josephine");f.click("ancient_cloak");
        assertEquals("josephine",f.player.getQuest(SLOT));
        f.carry("płaszcz ze smoczych łusek",1);f.greet("Josephine");f.click("ancient_cloak");
        assertEquals("sewing",f.player.getQuest(SLOT,0));assertFalse(f.player.isEquipped("płaszcz ze smoczych łusek"));
        f.click("ancient_cloak");assertFalse(f.player.isEquipped("płaszcz pradawnych smoków"));
        f.player.setQuest(SLOT,"sewing;0");f.greet("Josephine");f.click("ancient_cloak");
        assertEquals("sewing;0",f.player.getQuest(SLOT));assertTrue(f.latest().getRewards().isEmpty());
        f.carry("money",299999);f.type("oplata16");assertEquals(299999,f.player.getNumberOfEquipped("money"));
        f.carry("money",1);f.type("oplata16");assertEquals(0,f.player.getNumberOfEquipped("money"));
        f.click("ancient_cloak");assertTrue(f.player.isQuestCompleted(SLOT));f.reward("płaszcz pradawnych smoków",1);
        f.carry("money",300000);f.click("ancient_cloak");
        assertEquals(1,f.player.getNumberOfEquipped("płaszcz pradawnych smoków"));assertEquals(300000,f.player.getNumberOfEquipped("money"));
        f.capture("ancient-cloak-completed");
    }

    @Test public void dragonPrerequisiteDoesNotCountRepeatedNames(){
        f.player.setLevel(350);f.player.setQuest("find_dragons",";Gorath;Gorath;Gorath;Gorath;Gorath;Gorath;Gorath;Gorath;Gorath;Gorath;Gorath");
        f.greet("Terry");f.click("ancient_cloak");assertFalse(f.player.hasQuest(SLOT));
        f.player.setQuest("find_dragons",";Gorath;Velmord;Saphiron;Ardavyr;Kharion;Morthax;Dravok;Ignathar;Faldurion;Nalthar;Atherion");
        f.greet("Terry");f.click("ancient_cloak");assertEquals("green_cloaks",f.player.getQuest(SLOT));
    }
}
