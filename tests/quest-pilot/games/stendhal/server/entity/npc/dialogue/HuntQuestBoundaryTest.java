package games.stendhal.server.entity.npc.dialogue;
import static org.junit.Assert.*;
import games.stendhal.common.*;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.*;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import org.junit.*;

public class HuntQuestBoundaryTest {
    HuntQuestFixture f;
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new HuntQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    class ObservedSpawn extends CreatureRespawnPoint {
        final List<Observer> seen=new ArrayList<Observer>();
        ObservedSpawn(String name,int x,int y){super(f.zone,x,y,SingletonRepository.getEntityManager().getCreature(name),1);}
        @Override public void addObserver(Observer o){super.addObserver(o);seen.add(o);}
        void die(RPEntity killer){for(Observer o:seen)o.update(null,new CircumstancesOfDeath(killer,getPrototypeCreature(),f.zone));}
    }
    ObservedSpawn spawn(String name,int x,int y){ObservedSpawn s=new ObservedSpawn(name,x,y);f.zone.getRespawnPointList().add(s);return s;}
    @Test public void schoolSpidersRequireObservedSpawnsAndDirectPlayerKill(){
        List<ObservedSpawn> spawns=new ArrayList<ObservedSpawn>();for(String name:KillSpiders.getDialogueCreatures())spawns.add(spawn(name,40,60));
        new games.stendhal.server.maps.magic.school.SpidersCreatures().configureZone(f.zone,Collections.<String,String>emptyMap());
        for(ObservedSpawn s:spawns){assertEquals(1,s.seen.size());s.die(f.player);}assertFalse(f.player.hasQuest("kill_all_spiders"));
        f.start("Morgrin");for(ObservedSpawn s:spawns)s.die(SingletonRepository.getEntityManager().getCreature("szczur"));
        assertEquals("started",f.player.getQuest("kill_all_spiders"));
        for(ObservedSpawn s:spawns)s.die(f.player);assertEquals(QuestProgress.State.READY_TO_DELIVER,f.progress("KillSpiders").getState());
        f.greet("Morgrin");assertTrue(new KillSpiders().isCompleted(f.player));f.reward("mityczne jajo",1);f.reward("XP postaci",5000);f.capture("Morgrin-reward");
        int xp=f.player.getXP();f.type("zadanie");f.type("tak");assertEquals(xp,f.player.getXP());
    }
    @Test public void drowOnlyObservedAreasCountAndAmuletIsRequired(){
        List<ObservedSpawn> spawns=new ArrayList<ObservedSpawn>();for(String name:KillDarkElves.getDialogueCreatures())spawns.add(spawn(name,34,55));
        ObservedSpawn outside=spawn("elf ciemności kapitan",80,90);
        new games.stendhal.server.maps.nalwor.tunnel.DrowCreatures().configureZone(f.zone,Collections.<String,String>emptyMap());
        assertTrue(outside.seen.isEmpty());f.start("Maerion");
        for(ObservedSpawn s:spawns){assertEquals(1,s.seen.size());s.die(f.player);}
        f.greet("Maerion");assertFalse(new KillDarkElves().isCompleted(f.player));assertEquals(QuestProgress.State.ACTIVE,f.progress("KillDarkElves").getState());f.capture("Maerion-needs-amulet");
        f.carry("amulet",1);f.type("meldunek");assertEquals("done",f.player.getQuest("kill_dark_elves"));f.reward("pierścień szmaragdowy",1);f.reward("XP postaci",50000);assertFalse(f.player.isEquipped("amulet"));f.capture("Maerion-reward");
    }
    @Test public void legacyDrowRewardStillUsesLegacyAmount(){
        f.player.setQuest("kill_dark_elves","start");for(String n:Arrays.asList("elf ciemności łucznik","elf ciemności kapitan","coś"))f.player.setSoloKill(n);f.carry("amulet",1);
        f.greet("Maerion");assertEquals("done",f.player.getQuest("kill_dark_elves"));f.reward("XP postaci",20000);
    }
    @Test public void legacySchoolHuntRemainsRedeemable(){
        f.player.setQuest("kill_all_spiders","start");for(String n:KillSpiders.getDialogueCreatures())f.player.setSoloKill(n);
        f.greet("Morgrin");assertTrue(new KillSpiders().isCompleted(f.player));f.reward("mityczne jajo",1);
    }
    @Test public void thorgalPartialSetAndAtomicFullDeliveryUnlockTrade(){
        f.carry("hełm chaosu",1);f.greet("Thorgal");assertEquals("start",f.player.getQuest("chaos_tunnels"));assertTrue(f.latest().isActive());f.capture("Thorgal-partial");
        f.click("supplies");assertTrue(f.player.isEquipped("hełm chaosu"));f.click("offer");assertTrue(f.latest().getItems().isEmpty());
        for(String n:CollectChaosThorgal.getRequiredItems())if(!f.player.isEquipped(n))f.carry(n,1);
        f.type("meldunek");assertTrue(f.payFeeIfRequested("chaos_tunnels"));f.type("meldunek");assertEquals("done",f.player.getQuest("chaos_tunnels"));f.reward("zwój chaosu",4);f.reward("XP postaci",500000);f.reward("XP ataku",20000);f.reward("XP obrony",20000);f.reward("Handel z Thorgalem",1);
        for(String n:CollectChaosThorgal.getRequiredItems())assertFalse(n,f.player.isEquipped(n));f.capture("Thorgal-reward");
        f.click("offer");assertEquals(1,f.latest().getItems().size());assertTrue(f.latest().getItems().get(0).isForSale());f.capture("Thorgal-shop");
        int xp=f.player.getXP();f.greet("Thorgal");assertEquals(xp,f.player.getXP());
    }
    @Test public void realHuntLevelGates(){
        f.player.setLevel(199);f.greet("Ivar");f.type("zadanie");assertFalse(f.has("accept"));assertFalse(f.player.hasQuest("kill_chaos_monsters"));
        f.player.setLevel(249);f.greet("Luck");f.type("zadanie");assertFalse(f.has("accept"));assertFalse(f.player.hasQuest("kill_stone"));
    }
    @Test public void declineAndResumeDoesNotClaimAReward(){
        f.greet("Walt");f.click("quest");f.click("decline");assertEquals("rejected",f.player.getQuest("kill_ados_spiders"));assertTrue(f.latest().getRewards().isEmpty());
        f.type("wątki");f.click("quest");f.click("accept");assertEquals("start",f.player.getQuest("kill_ados_spiders",0));
    }
    @Test public void sumRecordOrderDoesNotMatterAndUnlistedCreatureDoesNotCount(){
        f.greet("Think");f.click("quest");String record=f.player.getQuest("kill_chaos",2);String[] tokens=record.split(",");List<String> groups=new ArrayList<String>();
        for(int i=0;i<tokens.length;i+=5)groups.add(String.join(",",Arrays.copyOfRange(tokens,i,i+5)));Collections.reverse(groups);f.player.setQuest("kill_chaos",2,String.join(",",groups));
        for(int i=0;i<10;i++)f.player.setSoloKill("szczur");assertEquals(0,f.progress("KillChaos").getObjectives().get(0).getCurrent());
        f.player.setSoloKill(tokens[0]);assertEquals(1,f.progress("KillChaos").getObjectives().get(0).getCurrent());
    }
    @Test public void nineteenOctavioTargetsSurviveWireRoundTrip(){
        f.start("Octavio");QuestProgress p=f.progress("KillHigh");assertEquals(19,p.getObjectives().size());assertNotNull(QuestProgress.fromWire(p.toWire()));assertEquals(19,f.latest().getItems().size());f.capture("Octavio-nineteen-targets");
    }
    @Test public void staleAcceptCannotResetCompletedHunt(){
        f.greet("Walt");f.click("quest");f.player.setQuest("kill_ados_spiders","done;"+System.currentTimeMillis());String saved=f.player.getQuest("kill_ados_spiders");
        f.click("accept");assertEquals(saved,f.player.getQuest("kill_ados_spiders"));assertFalse(f.has("accept"));
    }
    @Test public void everyRandomHuntVariantUsesItsOwnTargetsAndFreshBaseline() throws Exception {
        for(String name:Arrays.asList("KillEasyMonsters","KillMediumMonsters","KillForWod","KillForVerdan")){
            IQuest q=f.quests.get(name);NativeHuntDetails details=(NativeHuntDetails)q;
            java.lang.reflect.Field field=q.getClass().getDeclaredField("enemyForces");field.setAccessible(true);
            Map<?,?> groups=(Map<?,?>)field.get(q);
            for(Object group:groups.keySet()){
                f.player.setQuest(q.getSlotName(),"start;"+group);
                Map<String,marauroa.common.Pair<Integer,Integer>> targets=details.getDialogueHuntTargets(f.player);
                assertFalse(targets.isEmpty());
                List<String> availableTargets=new ArrayList<String>();
                for(String target:targets.keySet()){
                    if("samuraj".equals(target))continue; // Removed optional member; the group total is achievable with its other members.
                    assertNotNull(name+": "+target,SingletonRepository.getEntityManager().getCreature(target));
                    availableTargets.add(target);
                    f.player.setSoloKill(target);f.player.setSharedKill(target);
                }
                new games.stendhal.server.entity.npc.action.StartRecordingKillsAction(q.getSlotName(),details.getDialogueKillIndex(),targets).fire(f.player,null,null);
                assertEquals(0,HuntQuestProgress.read(q,f.player).getObjectives().get(0).getCurrent());
                assertFalse("Group must contain living targets",availableTargets.isEmpty());
                String target=availableTargets.get(0);int total=details.getDialogueHuntTotal(f.player);
                for(int i=0;i<total;i++)f.player.setSharedKill(target);
                assertEquals(QuestProgress.State.READY_TO_DELIVER,HuntQuestProgress.read(q,f.player).getState());
                f.greet(q.getNPCName());f.click("supplies");assertTrue(q.getNPCName()+" / "+group,q.isCompleted(f.player));
                f.capture(name+"-variant-"+group);
            }
        }
    }
    @Test public void configuredHuntCreaturesExist(){
        for(IQuest q:f.quests.values())if(q instanceof NativeHuntDetails){
            for(String name:((NativeHuntDetails)q).getDialogueHuntTargets(f.player).keySet()){
                assertNotNull(q.getName()+": "+name,SingletonRepository.getEntityManager().getCreature(name));
            }
        }
    }
    @Test public void arachneLoadsByNameAndMapTileAndCountsForBothUnchangedHunts(){
        Creature named=SingletonRepository.getEntityManager().getCreature("arachne");
        Creature mapped=SingletonRepository.getEntityManager().getCreature("../../tileset/logic/creature/hybrid.png",13);
        assertNotNull("Arachne by name",named);assertNotNull("Arachne from Nalwor map tile",mapped);assertEquals("arachne",mapped.getName());
        assertEquals("hybrid",named.get("class"));assertEquals("arachne",named.get("subclass"));assertEquals(6000,named.getBaseHP());
        for(String item:Arrays.asList("sztylet mroku","sztylet arachne","trucizna arachne","bardzo mocna trucizna","gruczoł przędzy"))assertNotNull(item,SingletonRepository.getEntityManager().getItem(item));
        f.start("Walt");f.start("Octavio");f.player.setSoloKill(named.getName());
        for(String quest:Arrays.asList("KillAdosSpiders","KillHigh")){
            QuestProgress progress=f.progress(quest);boolean found=false;
            for(QuestProgress.Objective goal:progress.getObjectives())if("kill:arachne".equals(goal.getId())){
                found=true;assertEquals(1,goal.getCurrent());assertEquals("KillAdosSpiders".equals(quest)?1:2,goal.getRequired());
            }
            assertTrue(quest,found);
        }
    }
}
