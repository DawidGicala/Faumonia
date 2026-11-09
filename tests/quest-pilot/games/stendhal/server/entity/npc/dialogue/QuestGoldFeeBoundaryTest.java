package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.logic.QuestGoldFees;
import games.stendhal.server.maps.quests.*;
import org.junit.*;
import static org.junit.Assert.*;

public class QuestGoldFeeBoundaryTest {
    private CollectionQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();}
    @After public void cleanup(){f.restoreNPCs();}

    @Test public void adosCostsFiveThousandAndRefreshesOldUnpaidQuote() {
        QuestGoldFees.Fee fee=QuestGoldFees.forSlot("ados_badge");
        f.player.setLevel(597);
        assertEquals(5000,fee.price(f.player));
        assertTrue(QuestGoldFees.description(fee.slot,f.player).contains("5000 monet"));
        assertTrue(QuestGoldFees.catalogueDescription(fee.slot).contains("5000 monet"));
        f.player.setQuest(fee.slot,"start;zbroja żywiołów");
        String progress=f.player.getQuest(fee.slot);
        f.player.setQuest(fee.quoteKey(),"5000000");
        SpeakerNPC npc=NPCList.get().get(fee.npc);
        npc.setAttending(f.player);npc.setCurrentState(ConversationStates.ATTENDING);
        assertFalse(QuestGoldFees.requirePaid(f.player,fee.slot,new EventRaiser(npc)));
        f.carry("money",5000);
        npc.getEngine().step(f.player,fee.command());
        assertEquals("5000",f.player.getQuest(fee.quoteKey()));
        assertFalse(fee.paid(f.player));
        assertEquals(5000,f.player.getNumberOfEquipped("money"));
        npc.getEngine().step(f.player,fee.command());
        assertTrue(fee.paid(f.player));
        assertEquals("5000",f.player.getQuest(fee.paidKey()));
        assertEquals(0,f.player.getNumberOfEquipped("money"));
        assertEquals(progress,f.player.getQuest(fee.slot));
        f.player.setQuest(fee.paidKey(),"5000000");
        assertTrue(QuestGoldFees.requirePaid(f.player,fee.slot,new EventRaiser(npc)));
        assertEquals("5000000",f.player.getQuest(fee.paidKey()));
    }

    @Test public void crownUsesLongArithmeticCapAndRequotesAfterLevelChange(){
        QuestGoldFees.Fee fee=QuestGoldFees.forSlot("crown_for_the_wannabe_king");
        int[][] cases={{0,0},{250,2093802},{300,2512562},{400,3350083},{500,4187604},{597,5000000},{700,5000000}};
        for(int[] c:cases){f.player.setLevel(c[0]);assertEquals(c[1],fee.price(f.player));}
        f.player.setQuest(fee.slot,"reward");f.player.setLevel(300);f.greet("Kendra Mattori");f.click("give_message");
        f.carry("money",5000000);f.player.setLevel(400);f.type(fee.command());
        assertFalse(fee.paid(f.player));assertEquals(5000000,f.player.getNumberOfEquipped("money"));
        f.type(fee.command());assertTrue(fee.paid(f.player));assertEquals(1649917,f.player.getNumberOfEquipped("money"));
    }

    @Test public void barioKeepsLastCloakUntilExplicitPaymentAndNeverChargesOnGreeting(){
        f.player.setQuest("cloaks_for_bario","1");f.carry("lazurowy płaszcz elficki",1);f.carry("money",50000);
        f.greet("Bario");f.click("give_lazurowy płaszcz elficki");
        assertEquals("1",f.player.getQuest("cloaks_for_bario"));assertEquals(1,f.player.getNumberOfEquipped("lazurowy płaszcz elficki"));
        assertEquals(50000,f.player.getNumberOfEquipped("money"));assertTrue(f.latest().getRewards().isEmpty());
        assertTrue(f.payFeeIfRequested("cloaks_for_bario"));f.click("supplies");f.click("give_lazurowy płaszcz elficki");
        assertTrue(f.player.isQuestCompleted("cloaks_for_bario"));assertEquals(0,f.player.getNumberOfEquipped("lazurowy płaszcz elficki"));
        f.carry("money",50000);f.greet("Bario");f.type("oplata4");assertEquals(50000,f.player.getNumberOfEquipped("money"));
    }

    @Test public void crownDoesNotWithdrawAtIvanOrRepeatedlyAtKendra(){
        f.player.setQuest("crown_for_the_wannabe_king","reward");f.player.setLevel(597);f.carry("money",5000000);
        f.greet("Ivan Abe");f.type("oplata22");assertEquals(5000000,f.player.getNumberOfEquipped("money"));
        f.greet("Kendra Mattori");f.click("give_message");assertTrue(f.latest().getSpeech().contains("5000000"));
        assertTrue(f.payFeeIfRequested("crown_for_the_wannabe_king"));f.click("give_message");assertTrue(f.player.isQuestCompleted("crown_for_the_wannabe_king"));
        int attack=f.player.getAtkXP();f.type("oplata22");f.type("nagroda");assertEquals(attack,f.player.getAtkXP());
    }
}
