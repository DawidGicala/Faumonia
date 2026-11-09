package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import java.util.*;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import org.junit.*;
import static org.junit.Assert.*;

/** Repeatable mixed menu/text/close sequences across real configured quest NPCs. */
public class DialogueSequenceAuditTest {
    private CollectionQuestFixture f;
    private final Set<String> spriteResources=new TreeSet<String>();
    @BeforeClass public static void init(){JourneyQuestFixture.init();}
    @AfterClass public static void finish(){JourneyQuestFixture.finish();}
    @Before public void setup(){f=new CollectionQuestFixture();f.createWorld();}
    @After public void teardown(){f.restoreNPCs();}
    @Test public void newPlayerCanExploreAndInterruptOfferedTopics(){walk(29001,1,false);}
    @Test public void veteranCanMixTextShoppingTopicsAndInterruptedConfirmations(){walk(29002,350,true);}

    private NpcDialogueSnapshot latestOrNull(){
        NpcDialogueSnapshot result=null;
        for(RPEvent e:f.player.events())if(NpcDialogueSnapshot.EVENT.equals(e.getName())){
            result=NpcDialogueSnapshot.fromWire(e.getList("data"));assertNotNull("Invalid published snapshot",result);
            assertEquals(result.toWire(),NpcDialogueSnapshot.fromWire(result.toWire()).toWire());
            if(!result.getSprite().isEmpty())spriteResources.add("data/sprites/"+result.getSprite()+".png");
            for(NpcDialogueSnapshot.Item item:result.getItems())spriteResources.add(resourcePath(item.getSprite()));
            for(NpcDialogueSnapshot.Reward reward:result.getRewards())spriteResources.add(resourcePath(reward.getSprite()));
        }
        return result;
    }
    private static String resourcePath(String sprite){return "data/"+(sprite.startsWith("gui/")?"":"sprites/")+sprite+".png";}
    private void walk(int seed,int level,boolean supplies){
        f.player.setLevel(level);
        if(supplies){f.carry("money",20000000);for(String item:Arrays.asList("polano","ser","arandula","żelazo","szynka","mięso","kanapka","jabłko","woda"))f.carry(item,500);}
        Random random=new Random(seed);int visited=0,actions=0,purchases=0;
        List<String> trace=new ArrayList<String>();
        List<String> names=new ArrayList<String>(f.npcs.keySet());Collections.sort(names);
        for(String name:names){
            SpeakerNPC npc=f.npcs.get(name);if(npc==null||!PilotDialogueReplies.supports(npc))continue;
            try{
                for(SpeakerNPC old:f.npcs.values())if(old!=null&&old.getAttending()==f.player)old.listenTo(f.player,"bye");
                f.player.clearEvents();f.player.setPosition(npc.getX()+1,npc.getY());JourneyQuestFixture.rules.turn+=34;
                npc.listenTo(f.player,"hi");NpcDialogueSnapshot current=latestOrNull();
                if(current==null||!current.isActive())continue;
                assertEquals(name,current.getNpcName());visited++;
                for(int step=0;step<32;step++){
                    trace.add(name+" state="+npc.getEngine().getCurrentState()+" replies="+current.getReplies().size());
                    while(trace.size()>12)trace.remove(0);
                    if(!current.isActive()){
                        f.player.clearEvents();npc.listenTo(f.player,"hi");current=latestOrNull();
                        if(current==null||!current.isActive())break;
                    }
                    RPAction a=new RPAction();a.put("session",current.getSession());a.put("revision",Long.toString(current.getRevision()));
                    int choice=random.nextInt(10);
                    if(choice==0){a.put("close","1");trace.add("X");}
                    else if(choice==1){a.put("text","nie wiem");trace.add("text: nie wiem");}
                    else if(choice==2){a.put("text","oferta");trace.add("text: oferta");}
                    else if(choice==3&&current.isShop()){
                        List<NpcDialogueSnapshot.Item> products=new ArrayList<NpcDialogueSnapshot.Item>();
                        for(NpcDialogueSnapshot.Item item:current.getItems())if(item.isForSale()&&item.getMaxAmount()>0)products.add(item);
                        if(products.isEmpty()){a.put("close","1");}
                        else{NpcDialogueSnapshot.Item item=products.get(random.nextInt(products.size()));
                            int count=1+random.nextInt(Math.min(3,item.getMaxAmount()));
                            a.put("buy",item.getId());a.put("amount",Integer.toString(count));purchases++;
                            trace.add("buy "+count+" "+item.getName());}
                    }
                    else if(!current.getReplies().isEmpty()){
                        NpcDialogueSnapshot.Reply r=current.getReplies().get(random.nextInt(current.getReplies().size()));
                        a.put("reply",r.getId());trace.add("reply: "+r.getId());
                    }else{a.put("text","bye");trace.add("text: bye");}
                    f.player.clearEvents();JourneyQuestFixture.rules.turn+=34;
                    new NpcDialogueAction().onAction(f.player,a);NpcDialogueSnapshot next=latestOrNull();
                    assertNotNull("Bound action needs a response",next);assertEquals(name,next.getNpcName());
                    assertTrue("Monotonic revisions",next.getRevision()>current.getRevision());
                    assertTrue("Money must not become negative",f.player.getNumberOfEquipped("money")>=0);
                    current=next;actions++;
                }
            }catch(Throwable failure){throw new AssertionError("Seed "+seed+" at "+name+" trace="+trace,failure);}
        }
        assertTrue("Broad NPC coverage",visited>=90);assertTrue("Mixed sequences executed",actions>=2500);
        assertTrue("Actual shop requests exercised",purchases>=5);
        List<String> missing=new ArrayList<String>();for(String path:spriteResources)if(getClass().getClassLoader().getResource(path)==null)missing.add(path);
        assertTrue("Dialogues reference missing sprites: "+missing,missing.isEmpty());
        try{java.nio.file.Files.write(java.nio.file.Paths.get(System.getProperty("quest.pilot.output","."),"sequence-assets-"+seed+".txt"),spriteResources,java.nio.charset.StandardCharsets.UTF_8);
            java.nio.file.Files.write(java.nio.file.Paths.get(System.getProperty("quest.pilot.output","."),"sequence-missing-assets-"+seed+".txt"),missing,java.nio.charset.StandardCharsets.UTF_8);
        }catch(java.io.IOException e){throw new AssertionError(e);}
        System.out.println("DIALOGUE_SEQUENCE_AUDIT seed="+seed+" NPCs="+visited+" actions="+actions+" purchases="+purchases+" assets="+spriteResources.size()+" missing="+missing.size());
    }
}
