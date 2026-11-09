package games.stendhal.server.entity.npc.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.actions.NpcDialogueAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.maps.quests.ElvishArmor;
import java.util.*;
import marauroa.common.game.RPAction;
import org.junit.*;

/** Follow-up audit: shared services, old saves, stale requests and cancellation. */
public class AdditionalQuestDialogueReviewTest {
    AdditionalQuestDialogueTest f;
    @BeforeClass public static void init() { AdditionalQuestDialogueTest.init(); }
    @AfterClass public static void finish() { AdditionalQuestDialogueTest.finish(); }
    @Before public void create() { f=new AdditionalQuestDialogueTest(); f.createWorld(); }
    @After public void restore() { f.restoreNPCs(); }

    private RPAction action(String key,String value) {
        NpcDialogueSnapshot s=f.latest(); RPAction a=new RPAction();
        a.put("session",s.getSession()); a.put("revision",Long.toString(s.getRevision()));
        a.put(key,value); return a;
    }
    private void send(RPAction a) { AdditionalQuestDialogueTest.rules.turn+=34; new NpcDialogueAction().onAction(f.player,a); }
    private void chat(String npc,String text) { AdditionalQuestDialogueTest.rules.turn+=34; f.npcs.get(npc).listenTo(f.player,text); }

    @Test public void refusalAndReturnDoNotStrandAnyOfTheNewOffers() {
        for(String name:Arrays.asList("Plink","Eonna","Sally","Jef","Lupos")) {
            f.greet(name); if(!f.has("accept")) f.click("quest");
            f.click("decline"); f.greet(name); if(!f.has("accept")) f.click("quest");
            f.click("accept"); assertTrue(name,f.latest().isActive()); assertFalse(name,f.has("accept"));
        }
        assertEquals("start",f.player.getQuest("plinks_toy"));
        assertEquals("start",f.player.getQuest("clean_storage",0));
        assertEquals("start",f.player.getQuest("campfire"));
        assertEquals("start",f.player.getQuest("find_jefs_mom",0));
        assertEquals("",f.player.getQuest("elvish_armor"));
    }

    @Test public void jefOldIndexedRejectionCanResumeWithoutLosingCounter() {
        f.player.setQuest("find_jefs_mom","rejected;"+System.currentTimeMillis()+";7");
        f.greet("Jef"); f.click("quest"); f.click("accept");
        assertEquals("start",f.player.getQuest("find_jefs_mom",0)); assertEquals("7",f.player.getQuest("find_jefs_mom",2));
        assertFalse(f.has("quest")); assertTrue(f.has("hints"));
    }

    @Test public void ilisaTadHerbsAndOrbLessonCanBothBeCompleted() {
        f.player.setQuest("introduce_players","ilisa"); f.carry("flasza",1); f.greet("Ilisa");
        assertEquals("corpse&herbs",f.player.getQuest("introduce_players"));
        f.click("arandula"); assertTrue(f.latest().getSpeech().toLowerCase(java.util.Locale.ROOT).contains("arandul"));
        f.type("tematy"); f.click("quest"); f.click("instructions"); f.click("confirm");
        int xp=f.player.getXP(); f.carry("arandula",1); f.greet("Ilisa");
        assertEquals("eliksir",f.player.getQuest("introduce_players"));
        assertEquals(0,f.player.getNumberOfEquipped("arandula")); assertEquals(xp+200,f.player.getXP());
        assertEquals("done",f.player.getQuest("learn_scrying"));
        f.greet("Ilisa"); assertEquals(xp+200,f.player.getXP());
        assertNotNull(getClass().getClassLoader().getResource("data/sprites/examine/arandula.png"));
    }

    @Test public void ilisaPaidHealingPreservesLessonAndRequiresPaymentOnlyOnce() {
        f.player.setQuest("learn_scrying","done"); f.player.setHP(20); f.carry("money",100);
        f.greet("Ilisa"); f.click("heal"); assertEquals(ConversationStates.HEAL_OFFERED,f.npcs.get("Ilisa").getEngine().getCurrentState());
        f.click("cancel"); assertEquals(20,f.player.getHP()); assertEquals(100,f.player.getNumberOfEquipped("money"));
        f.type("ulecz"); RPAction confirm=action("reply","confirm"); send(confirm); send(confirm);
        assertEquals(f.player.getBaseHP(),f.player.getHP()); assertEquals(77,f.player.getNumberOfEquipped("money"));
        assertEquals("done",f.player.getQuest("learn_scrying"));
    }

    @Test public void newNpcShopsActuallyBuyAtQuotedPriceAndRejectReplay() {
        f.carry("money",100000);
        for(String name:Arrays.asList("Ilisa","Xoderos")) {
            f.greet(name); f.click("offer"); NpcDialogueSnapshot.Item item=f.latest().getItems().get(0);
            int money=f.player.getNumberOfEquipped("money"), owned=f.player.getNumberOfEquipped(item.getName());
            RPAction buy=action("buy",item.getId()); buy.put("amount","1"); send(buy);
            assertEquals(money,f.player.getNumberOfEquipped("money"));
            RPAction confirm=action("reply","confirm"); send(confirm); send(confirm); send(buy);
            assertEquals(name,money-item.priceFor(1),f.player.getNumberOfEquipped("money"));
            assertEquals(name,owned+1,f.player.getNumberOfEquipped(item.getName()));
        }
    }

    @Test public void newNpcShopRefusesPurchaseWhenMoneyDisappears() {
        f.carry("money",100000); f.greet("Xoderos"); f.click("offer");
        NpcDialogueSnapshot.Item item=f.latest().getItems().get(0);
        RPAction buy=action("buy",item.getId()); buy.put("amount","1"); send(buy);
        f.player.drop("money",100000); f.click("confirm");
        assertEquals(0,f.player.getNumberOfEquipped("money")); assertEquals(0,f.player.getNumberOfEquipped(item.getName()));
    }

    @Test public void staleDeliveryButtonsCannotConsumeAnIncompleteStack() {
        String[][] cases={{"Joshua","hungry_joshua","kanapka","5"},{"Sally","campfire","polano","10"}};
        for(String[] c:cases) {
            f.player.setQuest(c[1],"start"); int amount=Integer.parseInt(c[3]); f.carry(c[2],amount); f.greet(c[0]);
            if("Joshua".equals(c[0])) f.click("give_food");
            RPAction confirm=action("reply","confirm"); f.player.drop(c[2],1); int xp=f.player.getXP(); send(confirm);
            assertEquals("start",f.player.getQuest(c[1])); assertEquals(amount-1,f.player.getNumberOfEquipped(c[2]));
            assertEquals(xp,f.player.getXP()); assertFalse(f.has("confirm"));
            f.carry(c[2],1); f.type("nie");
            if(f.has("give_food")) f.click("give_food");
            if(f.has("give_wood")) f.click("give_wood");
            f.click("confirm"); assertFalse("start".equals(f.player.getQuest(c[1])));
        }
    }

    @Test public void jefRewardReplayCannotTakeAnotherFlowerOrPayTwice() {
        f.player.setQuest("find_jefs_mom","found_mom;123;2"); f.carry("bielikrasa",2); f.greet("Jef");
        RPAction give=action("reply","give_flower"); int xp=f.player.getXP(); send(give);
        int fish=f.player.getNumberOfEquipped("pokolec"); send(give);
        assertEquals(xp+5000,f.player.getXP()); assertEquals(1,f.player.getNumberOfEquipped("bielikrasa"));
        assertEquals(fish,f.player.getNumberOfEquipped("pokolec")); assertEquals("3",f.player.getQuest("find_jefs_mom",2));
    }

    @Test public void luposLastSampleMissingAndReplayCannotUnlockEarlyOrTwice() {
        List<String> required=((ElvishArmor)f.quests.get("ElvishArmor")).getNeededItems();
        String value=""; for(int i=0;i<required.size()-1;i++) value+=";"+required.get(i);
        f.player.setQuest("elvish_armor",value); String last=required.get(required.size()-1); f.carry(last,1);
        f.greet("Lupos"); f.click("supplies"); String id="give_"+(required.size()-1);
        RPAction old=action("reply",id); f.player.drop(last,1); int xp=f.player.getXP(); send(old);
        assertEquals(value,f.player.getQuest("elvish_armor")); assertEquals(xp,f.player.getXP()); assertFalse(f.has(id));
        f.carry(last,2); f.type("rynsztunku"); RPAction give=action("reply",id); send(give); send(give);
        assertEquals("done",f.player.getQuest("elvish_armor")); assertEquals(xp+20000,f.player.getXP());
        assertEquals(1,f.player.getNumberOfEquipped(last));
    }

    @Test public void closeWindowReleasesAllNewNpcsEvenWithStaleRevision() {
        for(String name:Arrays.asList("Plink","Ilisa","Xoderos","Joshua","Jef","Amber","Eonna","Sally","Lupos")) {
            f.greet(name); RPAction close=action("close","1");
            if(f.has("quest")) f.click("quest"); else if(f.has("hints")) f.click("hints");
            send(close); assertFalse(name,f.latest().isActive()); assertFalse(name,f.npcs.get(name).isTalking());
        }
    }

    @Test public void oldSessionCannotCloseNewConversationOrAcceptItsOffer() {
        f.greet("Sally"); f.click("quest"); RPAction accept=action("reply","accept"), close=action("close","1");
        f.click("goodbye"); f.greet("Sally"); f.click("quest");
        // Old packets arrive together; do not advance the clock beyond the normal NPC timeout.
        new NpcDialogueAction().onAction(f.player,accept); new NpcDialogueAction().onAction(f.player,close);
        assertTrue(f.latest().isActive()); assertFalse(f.player.hasQuest("campfire"));
        f.click("accept"); assertEquals("start",f.player.getQuest("campfire"));
    }

    @Test public void plainChatCanFinishJefWithoutUsingPanelActions() {
        f.greet("Jef"); chat("Jef","zadanie"); chat("Jef","tak");
        assertEquals("start",f.player.getQuest("find_jefs_mom",0));
        f.greet("Amber"); chat("Amber","Jef"); assertEquals("found_mom",f.player.getQuest("find_jefs_mom",0));
        f.greet("Jef"); int xp=f.player.getXP(); chat("Jef","cała");
        assertEquals("done",f.player.getQuest("find_jefs_mom",0)); assertEquals(xp+5000,f.player.getXP());
        f.reward("XP postaci",5000); assertTrue(f.latest().isActive());
    }

    @Test public void luposBuybackCancelMissingItemAndReplayAreAtomic() {
        f.player.setQuest("elvish_armor","done");f.carry("zbroja elficka",2);f.greet("Lupos");
        f.type("sell 1 zbroja elficka");assertTrue(f.has("confirm"));f.click("cancel");
        assertEquals(2,f.player.getNumberOfEquipped("zbroja elficka"));assertEquals(0,f.player.getNumberOfEquipped("money"));
        f.type("sell 2 zbroja elficka");f.player.drop("zbroja elficka",1);f.click("confirm");
        assertEquals(1,f.player.getNumberOfEquipped("zbroja elficka"));assertEquals(0,f.player.getNumberOfEquipped("money"));
        f.type("sell 1 zbroja elficka");RPAction confirm=action("reply","confirm");send(confirm);send(confirm);
        int price=games.stendhal.server.core.engine.SingletonRepository.getShopList().get("buyelvish").get("zbroja elficka");
        assertEquals(price,f.player.getNumberOfEquipped("money"));assertEquals(0,f.player.getNumberOfEquipped("zbroja elficka"));
    }

    @Test public void luposBuybackRechecksAuraAtConfirmation() {
        f.player.setQuest("elvish_armor","done");f.carry("zbroja elficka",1);f.greet("Lupos");f.type("sell 1 zbroja elficka");
        f.player.put("last_player_kill_time","1234");f.click("confirm");
        assertEquals(1,f.player.getNumberOfEquipped("zbroja elficka"));assertEquals(0,f.player.getNumberOfEquipped("money"));
    }
}
