package games.stendhal.server.entity.npc.behaviour.impl;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import java.util.List;
import java.util.Map;

/** Preserve money already paid on an old, unfinished order when its price increases. */
public final class RepricedProducerBehaviour extends ProducerBehaviour {
    private final int oldPrice,newPrice;
    public RepricedProducerBehaviour(String slot,List<String> verbs,String product,Map<String,Integer> resources,int time,int oldPrice) {
        super(slot,verbs,product,resources,time);
        this.oldPrice=oldPrice;this.newPrice=resources.get("money");
    }
    private String receiptKey(){return "price_v1_"+getQuestSlot();}
    public int remainingPayment(Player p){
        String order=p.getQuest(getQuestSlot());
        if(order==null||p.isQuestCompleted(getQuestSlot())||order.equals(p.getQuest(receiptKey())))return 0;
        return Math.multiplyExact(getNumberOfProductItems(p),Math.max(0,newPrice-oldPrice));
    }
    @Override public boolean transactAgreedDeal(ItemParserResult res,EventRaiser npc,Player p){
        boolean accepted=super.transactAgreedDeal(res,npc,p);
        if(accepted)p.setQuest(receiptKey(),p.getQuest(getQuestSlot()));
        return accepted;
    }
    @Override public void giveProduct(EventRaiser npc,Player p){
        if(isOrderReady(p)&&remainingPayment(p)>0){
            npc.say("Twoje zamówienie jest gotowe. Obecna cena to "+newPrice+" monet za parę; wcześniej wpłacone "+oldPrice+" monet za każdą parę pozostaje zaliczone. Do dopłaty: "+remainingPayment(p)+" monet. Potwierdź słowem #dopłata, a wydam zamówienie, gdy będziesz mieć miejsce.");return;
        }
        super.giveProduct(npc,p);
    }
    public void installPayment(final SpeakerNPC npc){
        npc.add(ConversationStates.ATTENDING,"dopłata",new ChatCondition(){
            public boolean fire(Player p,Sentence s,Entity e){return remainingPayment(p)>0&&isOrderReady(p);}
        },ConversationStates.ATTENDING,null,new ChatAction(){
            public void fire(Player p,Sentence s,EventRaiser raiser){
                int amount=remainingPayment(p);
                if(!p.drop("money",amount)){raiser.say("Do dopłaty pozostaje "+amount+" monet. Wpłacone wcześniej pieniądze i materiały są zapisane; poczekam, aż wrócisz z brakującą kwotą.");return;}
                p.setQuest(receiptKey(),p.getQuest(getQuestSlot()));
                giveProduct(raiser,p);
                p.notifyWorldAboutChanges();
            }
        });
    }
}
