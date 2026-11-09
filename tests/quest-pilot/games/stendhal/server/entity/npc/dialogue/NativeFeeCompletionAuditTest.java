package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.maps.quests.*;
import games.stendhal.server.maps.quests.logic.QuestGoldFees;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

/** Independent audit of real quest completion transitions, not calls to the fee helper. */
@RunWith(Parameterized.class)
public class NativeFeeCompletionAuditTest {
    @Parameterized.Parameters public static Collection<Object[]> cases(){return Arrays.asList(new Object[][]{
        {6,"Żywioł Ognia","forging;0","materiały ognia"},
        {7,"Żywioł Wody","forging;0","materiały wody"},
        {8,"Alrak","forging;0","praca alraka"},
        {10,"Josephine","done","druga kolekcja"},
        {12,"Vulcanus","forging;0","dostawa"},
        {14,"Baldemar","forging;0","dostawa"},
        {16,"Josephine","sewing;0","pradawny płaszcz"},
        {20,"Kolekcjoner Andrzej","forging;0","dostawa"},
        {31,"Grafindle","lorithien","przesyłka"}
    });}
    final int id;final String npc,state,command;CollectionQuestFixture f;QuestGoldFees.Fee fee;
    public NativeFeeCompletionAuditTest(int id,String npc,String state,String command){this.id=id;this.npc=npc;this.state=state;this.command=command;}
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){
        f=new CollectionQuestFixture();f.createWorld();
        if(id==16)new AncientDragonCloak().addToWorld();
        if(id==31)new TakeGoldforGrafindle().addToWorld();
        for(QuestGoldFees.Fee candidate:QuestGoldFees.all())if(candidate.id==id)fee=candidate;
        assertNotNull(fee);f.player.setLevel(597);f.player.setQuest(fee.slot,state);
        if(id==10)f.player.setQuest("cloaks_collector","done");
        if(id==31)f.carry("sztabka złota",26);
    }
    @After public void clean(){f.restoreNPCs();}
    private void request(){f.greet(npc);f.type(command);}
    @Test public void insufficientPaymentPreservesNativeStageAndExplicitPaymentUnlocksOnlyOneCompletion(){
        int price=fee.price(f.player),xp=f.player.getXP(),hp=f.player.getBaseHP();f.carry("money",price-1);
        request();assertTrue("Native completion must request fee "+id,fee.pending(f.player));
        assertEquals(state,f.player.getQuest(fee.slot));assertEquals(price-1,f.player.getNumberOfEquipped("money"));
        assertEquals(xp,f.player.getXP());assertEquals(hp,f.player.getBaseHP());assertTrue(f.latest().getRewards().isEmpty());
        if(id==31)assertEquals(26,f.player.getNumberOfEquipped("sztabka złota"));
        f.type(fee.command());assertFalse(fee.paid(f.player));assertEquals(price-1,f.player.getNumberOfEquipped("money"));
        f.carry("money",1);f.type(fee.command());assertEquals(0,f.player.getNumberOfEquipped("money"));assertTrue(fee.paid(f.player));
        assertEquals(state,f.player.getQuest(fee.slot));
        f.type(command);assertTrue("Native completion after explicit fee "+id,fee.completed(f.player));
        String done=f.player.getQuest(fee.slot);int totalXp=f.player.getXP(),totalHp=f.player.getBaseHP(),money=f.player.getNumberOfEquipped("money");
        if(id==31)assertEquals(1,f.player.getNumberOfEquipped("sztabka złota"));
        f.greet(npc);f.type(fee.command());f.type(command);
        assertEquals(done,f.player.getQuest(fee.slot));assertEquals(totalXp,f.player.getXP());assertEquals(totalHp,f.player.getBaseHP());assertEquals(money,f.player.getNumberOfEquipped("money"));
    }
    @Test public void interruptedFeeQuoteDoesNotSpendAndRemainsPayableOnNextConversation(){
        int price=fee.price(f.player);f.carry("money",price);request();
        assertEquals(price,f.player.getNumberOfEquipped("money"));assertTrue(fee.pending(f.player));
        f.type("bye");assertFalse(f.latest().isActive());
        f.greet(npc);assertTrue(f.latest().isActive());assertEquals(price,f.player.getNumberOfEquipped("money"));
        f.type(fee.command());assertTrue(fee.paid(f.player));assertEquals(0,f.player.getNumberOfEquipped("money"));
        f.type(command);assertTrue(fee.completed(f.player));
    }
}
