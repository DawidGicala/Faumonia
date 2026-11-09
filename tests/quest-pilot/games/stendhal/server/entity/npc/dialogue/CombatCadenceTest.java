package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.impl.attack.AttackStrategy;
import games.stendhal.server.entity.creature.impl.attack.AttackStrategyFactory;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Executes real attack strategies; counts attempts independently of random hit rolls. */
public class CombatCadenceTest {
    private JourneyQuestFixture f;
    private static class CountingCreature extends Creature {
        final List<Integer> attempts = new ArrayList<Integer>();
        int rate = 5;
        boolean visible = true;
        CountingCreature() { setName("cadence test"); initHP(100); setPosition(20,20); }
        @Override public int getAttackRate() { return rate; }
        @Override public boolean attack() { attempts.add(JourneyQuestFixture.rules.turn); return false; }
        @Override public void tryToPoison() { }
        @Override public boolean hasLineOfSight(games.stendhal.server.entity.Entity target) { return visible; }
    }
    @BeforeClass public static void init() { JourneyQuestFixture.init(); }
    @AfterClass public static void finish() { JourneyQuestFixture.finish(); }
    @Before public void setup() { f=new JourneyQuestFixture(); f.createWorld(); }
    @After public void cleanup() { f.restoreNPCs(); }
    private CountingCreature creature() {
        CountingCreature c;
        do { c=new CountingCreature(); } while(c.getAttackTurn()==0);
        f.zone.add(c); c.setTarget(f.player); return c;
    }
    private void tick(AttackStrategy s,CountingCreature c,int turn,boolean near) {
        JourneyQuestFixture.rules.turn=turn; f.player.setPosition(near?21:40,20);
        if(s.canAttackNow(c))s.attack(c);
    }
    @Test public void steppingOutBetweenPlayerStrikesDoesNotEraseMeleeReadiness() {
        CountingCreature c=creature(); AttackStrategy s=AttackStrategyFactory.get(Collections.<String,String>emptyMap());
        for(int t=100;t<200;t++)tick(s,c,t,t%5==0);
        assertTrue("player has 20 strike opportunities; creature must be able to retaliate: "+c.attempts,c.attempts.size()>=18);
    }
    @Test public void stationaryMeleeKeepsExistingCadenceAndFirstPhase() {
        CountingCreature c=creature(); AttackStrategy s=AttackStrategyFactory.get(Collections.<String,String>emptyMap());
        for(int t=100;t<160;t++)tick(s,c,t,true);
        assertEquals(12,c.attempts.size());
        assertEquals(0,(c.attempts.get(0)+c.getAttackTurn())%5);
        for(int i=1;i<c.attempts.size();i++)assertEquals(5,(int)(c.attempts.get(i)-c.attempts.get(i-1)));
    }
    @Test public void farAwayTargetIsNeverHit() {
        CountingCreature c=creature(); AttackStrategy s=AttackStrategyFactory.get(Collections.<String,String>emptyMap());
        for(int t=100;t<200;t++)tick(s,c,t,false);
        assertTrue(c.attempts.isEmpty());
    }
    @Test public void rangedAttackAlsoRetainsMissedOpportunity() {
        CountingCreature c=creature(); AttackStrategy s=AttackStrategyFactory.get(Collections.singletonMap("archer","7"));
        for(int t=100;t<200;t++)tick(s,c,t,t%5==0);
        assertTrue(c.attempts.size()>=18);
    }
    @Test public void blockedLineOfSightNeverShoots() {
        CountingCreature c=creature(); c.visible=false; AttackStrategy s=AttackStrategyFactory.get(Collections.singletonMap("archer","7"));
        for(int t=100;t<200;t++)tick(s,c,t,true);
        assertTrue(c.attempts.isEmpty());
    }
    @Test public void missedDamageRollStillConsumesAttackInterval() {
        CountingCreature c=creature(); AttackStrategy s=AttackStrategyFactory.get(Collections.<String,String>emptyMap());
        for(int t=100;t<150;t++){tick(s,c,t,true);tick(s,c,t,true);}
        assertEquals(10,c.attempts.size());
    }
}
