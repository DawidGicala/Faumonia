package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.Constants;
import games.stendhal.common.NpcDialogueSnapshot.Reward;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.*;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import java.util.*;
import marauroa.common.game.RPObject;

/** Observes one synchronous native dialogue action, including its random and floor rewards. */
final class JourneyRewardObserver {
    final Map<String,Integer> bag,ground;
    final double xp,atk,def,karma;
    final boolean dwarf,citizen,fisher,chaos;
    final java.util.Set<String> badges=new java.util.HashSet<String>();
    JourneyRewardObserver(Player p){
        bag=carried(p);ground=floor(p);xp=p.getXP();atk=p.getAtkXP();def=p.getInt("def_xp");karma=p.getKarma();
        dwarf=p.isQuestCompleted("jailed_dwarf");citizen=p.isQuestCompleted("imperial_princess");fisher=p.isQuestCompleted("fishermans_license2");
        chaos=p.isQuestCompleted("chaos_tunnels");
        for(String city:Arrays.asList("ados","fado","kirdneh","nalwor","semos"))if(p.isQuestCompleted(city+"_badge"))badges.add(city);
    }
    static Map<String,Integer> carried(Player p){
        Map<String,Integer> result=new LinkedHashMap<String,Integer>();
        for(String slot:Constants.CARRYING_SLOTS)for(RPObject o:p.getSlot(slot))if(o instanceof Item){Item i=(Item)o;result.put(i.getName(),p.getNumberOfEquipped(i.getName()));}
        return result;
    }
    static Map<String,Integer> floor(Player p){
        Map<String,Integer> result=new LinkedHashMap<String,Integer>();
        if(p.getZone()!=null)for(Item i:p.getZone().getItemsOnGround())result.put(i.getName(),get(result,i.getName())+(i instanceof StackableItem?((StackableItem)i).getQuantity():1));return result;
    }
    static int get(Map<String,Integer> m,String n){return m.containsKey(n)?m.get(n):0;}
    static void add(List<Reward> rewards,String name,String sprite,double amount,boolean onGround){
        // Earlier pilot actions may already have reported a reward at a shared NPC.
        for(Reward r:rewards)if(name.equals(r.getName())&&onGround==r.isOnGround())amount-=r.getAmount();
        amount=Math.round(amount*1000000d)/1000000d;if(amount>0&&rewards.size()<32)rewards.add(new Reward(name,sprite,amount,onGround));
    }
    void finish(SpeakerNPC npc,Player p,List<Reward> rewards){
        add(rewards,"XP postaci","gui/dialogue/xp",p.getXP()-xp,false);
        add(rewards,"XP ataku","gui/dialogue/attack_xp",p.getAtkXP()-atk,false);
        add(rewards,"XP obrony","gui/dialogue/defense_xp",p.getInt("def_xp")-def,false);
        add(rewards,"Karma","gui/dialogue/karma",p.getKarma()-karma,false);
        Map<String,Integer> after=carried(p),afterGround=floor(p);
        for(String name:after.keySet())recordItem(rewards,name,get(after,name)-get(bag,name),false);
        // Santiago places exam specimens on his table; they are not player rewards.
        if(!"Santiago".equals(npc.getName()))for(String name:afterGround.keySet())recordItem(rewards,name,get(afterGround,name)-get(ground,name),true);
        if(!dwarf&&p.isQuestCompleted("jailed_dwarf"))add(rewards,"Handel z Hunelem","gui/dialogue/unlock",1,false);
        if(!citizen&&p.isQuestCompleted("imperial_princess"))add(rewards,"Obywatelstwo Kalavan","gui/dialogue/unlock",1,false);
        if(!fisher&&p.isQuestCompleted("fishermans_license2"))add(rewards,"Karta wędkarska","gui/dialogue/unlock",1,false);
        for(String city:Arrays.asList("ados","fado","kirdneh","nalwor","semos"))if(!badges.contains(city)&&p.isQuestCompleted(city+"_badge"))add(rewards,"Zasłużony mieszkaniec "+Character.toUpperCase(city.charAt(0))+city.substring(1),"gui/dialogue/unlock",1,false);
        if(!chaos&&p.isQuestCompleted("chaos_tunnels"))add(rewards,"Handel z Thorgalem","gui/dialogue/unlock",1,false);
    }
    static void recordItem(List<Reward> rewards,String name,int amount,boolean ground){
        if(amount<=0)return;Item item=SingletonRepository.getEntityManager().getItem(name);if(item!=null)add(rewards,name,"items/"+item.getItemClass()+"/"+item.getItemSubclass(),amount,ground);
    }
}
