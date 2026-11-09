package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.maps.quests.logic.QuestGoldFees;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class QuestGoldFeesTest {
    @Parameterized.Parameters public static Collection<Object[]> data(){
        List<Object[]> rows=new ArrayList<Object[]>();
        for(QuestGoldFees.Fee fee:QuestGoldFees.all())rows.add(new Object[]{fee});
        return rows;
    }
    private final QuestGoldFees.Fee fee;
    private CollectionQuestFixture f;
    private SpeakerNPC npc;
    public QuestGoldFeesTest(QuestGoldFees.Fee fee){this.fee=fee;}
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){
        f=new CollectionQuestFixture();f.createWorld();f.player.setLevel(597);
        String name=fee.id==16?"Josephine":fee.id==22?"Kendra Mattori":fee.npc;
        npc=NPCList.get().get(name);assertNotNull(name,npc);
        f.player.setQuest(fee.slot,"forging;0");
        npc.setAttending(f.player);npc.setCurrentState(ConversationStates.ATTENDING);
    }
    @After public void cleanup(){f.restoreNPCs();}
    private void pay(){npc.getEngine().step(f.player,fee.command());}

    @Test public void missingMoneyExactPaymentReplayAndSaveCopy(){
        String progress=f.player.getQuest(fee.slot);
        assertFalse(QuestGoldFees.requirePaid(f.player,fee.slot,new EventRaiser(npc)));
        int price=fee.price(f.player);f.carry("money",price-1);
        pay();assertEquals(price-1,f.player.getNumberOfEquipped("money"));assertFalse(fee.paid(f.player));
        assertEquals(progress,f.player.getQuest(fee.slot));
        f.carry("money",1);pay();assertEquals(0,f.player.getNumberOfEquipped("money"));assertTrue(fee.paid(f.player));
        assertEquals(progress,f.player.getQuest(fee.slot));
        f.carry("money",77);pay();assertEquals(77,f.player.getNumberOfEquipped("money"));
        Player restored=new Player(new RPObject(f.player));
        assertTrue(fee.paid(restored));assertTrue(QuestGoldFees.requirePaid(restored,fee.slot,new EventRaiser(npc)));
        assertEquals(progress,restored.getQuest(fee.slot));assertEquals(Integer.toString(price),restored.getQuest(fee.paidKey()));
    }

    @Test public void alreadyFinishedQuestHasNoRetroactiveFee(){
        f.player.setQuest(fee.slot,fee.id==10?"done;rewarded":"done;123");f.carry("money",1234);
        assertTrue(QuestGoldFees.requirePaid(f.player,fee.slot,new EventRaiser(npc)));
        assertFalse(f.player.hasQuest(fee.quoteKey()));pay();
        assertEquals(1234,f.player.getNumberOfEquipped("money"));assertFalse(fee.paid(f.player));
    }

    @Test public void typingPaymentBeforeQuoteCannotDeductAnything(){
        f.carry("money",6000000);pay();assertEquals(6000000,f.player.getNumberOfEquipped("money"));assertFalse(fee.paid(f.player));
    }
}
