package games.stendhal.server.maps.quests;
import games.stendhal.server.entity.npc.*;
import static games.stendhal.server.entity.npc.ConversationStates.*;
/** Explicit exit from an expedition topic cancels its pending yes/no question. */
final class ExpeditionQuestConversation {
    static void exit(SpeakerNPC n){
        for(ConversationStates s:new ConversationStates[]{EXPEDITION_DISCUSSION,EXPEDITION_OFFERED}){
            n.add(s,"inne sprawy",null,ATTENDING,"Dobrze, porozmawiajmy o innych sprawach.",null);
            n.add(s,ConversationPhrases.GOODBYE_MESSAGES,null,IDLE,"Do zobaczenia. Przygotuj się dobrze i wróć, kiedy będziesz gotowy.",null);
        }
    }
}
