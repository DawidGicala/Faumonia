package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.impl.attack.AttackStrategy;
import games.stendhal.server.entity.creature.impl.attack.AttackStrategyFactory;
import games.stendhal.server.maps.quests.logic.QuestGoldFees;
import java.math.BigInteger;
import java.util.*;
import marauroa.common.game.RPAction;
import org.junit.*;
import static org.junit.Assert.*;

/** Independent arithmetic and seeded malformed-input/cadence regression checks. */
public class ExtendedAudit28Test {
    private JourneyQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new JourneyQuestFixture();f.createWorld();}
    @After public void cleanup(){f.restoreNPCs();}

    private NpcDialogueSnapshot snapshot(boolean shop,int money,List<NpcDialogueSnapshot.Reward> rewards){
        return new NpcDialogueSnapshot("audit28",1,"Carmen","opis","npc/welcomernpc","temat","","Witaj!","","",true,true,
            Collections.<NpcDialogueSnapshot.Reply>emptyList(),Collections.<NpcDialogueSnapshot.Item>emptyList(),shop,money,rewards);
    }
    @Test public void balanceWithoutAnOpenShopIsRejectedByTheProtocolContract(){
        for(int money:new int[]{0,1,5000000,Integer.MAX_VALUE}){
            try { snapshot(false,money,Collections.<NpcDialogueSnapshot.Reward>emptyList());fail("Balance outside shop accepted"); }
            catch(IllegalArgumentException expected) { }
        }
    }
    @Test public void allReceiptAndBalanceCombinationsRoundTrip(){
        for(boolean shop:new boolean[]{false,true})for(int money:new int[]{-1,0,1234})for(boolean receipt:new boolean[]{false,true}){
            if(!shop&&money>=0)continue;
            List<NpcDialogueSnapshot.Reward> rewards=receipt?Arrays.asList(new NpcDialogueSnapshot.Reward("Karma","gui/dialogue/karma",0.5,false)):Collections.<NpcDialogueSnapshot.Reward>emptyList();
            NpcDialogueSnapshot original=snapshot(shop,money,rewards),restored=NpcDialogueSnapshot.fromWire(original.toWire());
            assertNotNull(restored);assertEquals(original.toWire(),restored.toWire());
        }
    }
    @Test public void seededCorruptWireDataNeverThrowsOrCreatesAnUnstableSnapshot(){
        Random random=new Random(28001);
        List<String> valid=snapshot(true,5000,Arrays.asList(new NpcDialogueSnapshot.Reward("XP","gui/dialogue/xp",100,false))).toWire();
        String[] values={null,"","-1","2147483648","NaN","Infinity","999999999999999999999","4","1","0","<html>","żółć"};
        for(int attempt=0;attempt<3000;attempt++){
            List<String> wire=new ArrayList<String>(valid);
            int changes=1+random.nextInt(4);
            for(int i=0;i<changes;i++){
                int mode=random.nextInt(3);
                if(mode==0&&!wire.isEmpty())wire.set(random.nextInt(wire.size()),values[random.nextInt(values.length)]);
                else if(mode==1&&!wire.isEmpty())wire.remove(random.nextInt(wire.size()));
                else wire.add(values[random.nextInt(values.length)]);
            }
            NpcDialogueSnapshot parsed=NpcDialogueSnapshot.fromWire(wire);
            if(parsed!=null)assertNotNull("Accepted wire must round-trip at mutation "+attempt,NpcDialogueSnapshot.fromWire(parsed.toWire()));
        }
    }
    @Test public void crownPriceMatchesIndependentBigIntegerCalculationForEveryLevel(){
        QuestGoldFees.Fee fee=QuestGoldFees.forSlot("crown_for_the_wannabe_king");
        int previous=-1;
        for(int level=0;level<=700;level++){
            f.player.setLevel(level);
            int expected=BigInteger.valueOf(5000000).multiply(BigInteger.valueOf(Math.min(level,597))).divide(BigInteger.valueOf(597)).intValueExact();
            assertEquals("level "+level,expected,fee.price(f.player));
            assertTrue(fee.price(f.player)>=previous);previous=fee.price(f.player);
        }
    }
    @Test public void priceArithmeticDoesNotOverflowAcrossLargeShopQuotes(){
        Random random=new Random(28002);
        for(int i=0;i<2000;i++){
            int price=random.nextInt(Integer.MAX_VALUE),quantity=1+random.nextInt(1000),percent=100+random.nextInt(901);
            NpcDialogueSnapshot.Item item=new NpcDialogueSnapshot.Item("item","test","",0,0,price,percent,1000,"");
            long expected=BigInteger.valueOf(price).multiply(BigInteger.valueOf(quantity)).multiply(BigInteger.valueOf(percent)).divide(BigInteger.valueOf(100)).longValueExact();
            assertEquals(expected,item.priceFor(quantity));
        }
    }
    @Test public void malformedPanelPacketsDoNotSpendMoneyChangeQuestsOrBreakTheNextValidReply(){
        f.carry("money",20000);f.greet("Hayunn");
        String[] bad={"","-1","0","2147483648","999999999999999999999","NaN","unknown"};
        for(int i=0;i<210;i++){
            NpcDialogueSnapshot s=f.latest();RPAction a=new RPAction();a.put("session",s.getSession());a.put("revision",Long.toString(s.getRevision()));
            switch(i%7){
                case 0:a.put("buy","fake");a.put("amount",bad[(i/7)%bad.length]);break;
                case 1:a.put("reply","secret");break;
                case 2:a.put("text","tak");a.put("reply","accept");break;
                case 3:a.put("close","1");a.put("buy","fake");break;
                case 4:a.put("revision",bad[(i/7)%bad.length]);a.put("reply","quest");break;
                case 5:a.put("text",String.join("",Collections.nCopies(501,"x")));break;
                default:a.put("session","foreign");a.put("reply","quest");break;
            }
            // A burst of invalid packets must not advance the world's idle timeout.
            new NpcDialogueAction().onAction(f.player,a);
            assertEquals(20000,f.player.getNumberOfEquipped("money"));assertFalse(f.player.hasQuest("task_hayunn"));assertTrue(f.latest().isActive());
        }
        f.click("quest");assertTrue(f.latest().isActive());
    }
    private static class Counter extends Creature{
        int rate;boolean visible=true;List<Integer> turns=new ArrayList<Integer>();
        Counter(int rate){this.rate=rate;setName("audit cadence");initHP(100);setPosition(20,20);}
        @Override public int getAttackRate(){return rate;}
        @Override public boolean attack(){turns.add(JourneyQuestFixture.rules.turn);return false;}
        @Override public void tryToPoison(){}
        @Override public boolean hasLineOfSight(games.stendhal.server.entity.Entity target){return visible;}
    }
    @Test public void everyMeleeRateRetainsReadinessWithoutBurstingAfterMovement(){
        Random random=new Random(28003);
        for(int rate=1;rate<=12;rate++){
            Counter c=new Counter(rate);f.zone.add(c);c.setTarget(f.player);
            AttackStrategy strategy=AttackStrategyFactory.get(Collections.<String,String>emptyMap());
            int last=-1;
            for(int turn=100;turn<1100;turn++){
                boolean near=random.nextInt(4)!=0;f.player.setPosition(near?21:40,20);JourneyQuestFixture.rules.turn=turn;
                int before=c.turns.size();strategy.attack(c);strategy.attack(c);
                assertTrue(c.turns.size()-before<=1);
                if(!near)assertEquals(before,c.turns.size());
                if(last>=0&&near&&turn-last>=rate)assertEquals(before+1,c.turns.size());
                if(c.turns.size()>before){if(last>=0)assertTrue(turn-last>=rate);last=turn;}
            }
            assertTrue(c.turns.size()>30);
        }
    }
    @Test public void rangedLineOfSightAndSeparateCreaturesHaveSeparateCooldowns(){
        Counter a=new Counter(5),b=new Counter(5);f.zone.add(a);f.zone.add(b);a.setTarget(f.player);b.setTarget(f.player);
        AttackStrategy strategy=AttackStrategyFactory.get(Collections.singletonMap("archer","7"));
        for(int turn=100;turn<300;turn++){
            JourneyQuestFixture.rules.turn=turn;f.player.setPosition(25,20);a.visible=turn%7!=0;b.visible=turn%9!=0;
            int as=a.turns.size(),bs=b.turns.size();strategy.attack(a);strategy.attack(b);
            if(!a.visible)assertEquals(as,a.turns.size());if(!b.visible)assertEquals(bs,b.turns.size());
        }
        assertTrue(a.turns.size()>30);assertTrue(b.turns.size()>30);
        for(Counter c:Arrays.asList(a,b))for(int i=1;i<c.turns.size();i++)assertTrue(c.turns.get(i)-c.turns.get(i-1)>=5);
    }
}
