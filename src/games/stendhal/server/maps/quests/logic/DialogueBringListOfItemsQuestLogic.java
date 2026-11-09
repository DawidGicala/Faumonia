package games.stendhal.server.maps.quests.logic;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.npc.action.*;
import games.stendhal.server.entity.npc.condition.*;
/** Keeps the three refreshed collectors open; inventory and rewards remain native. */
public final class DialogueBringListOfItemsQuestLogic extends BringListOfItemsQuestLogic {
    public DialogueBringListOfItemsQuestLogic(BringListOfItemsQuest q){super(q);}
    protected void acceptQuest(){concreteQuest.getNPC().add(ConversationStates.QUEST_OFFERED,ConversationPhrases.YES_MESSAGES,new QuestNotStartedCondition(concreteQuest.getSlotName()),ConversationStates.ATTENDING,concreteQuest.respondToQuestAcception(),new SetQuestAndModifyKarmaAction(concreteQuest.getSlotName(),"",concreteQuest.getKarmaDiffForQuestResponse()));}
    protected void rejectQuest(){concreteQuest.getNPC().add(ConversationStates.QUEST_OFFERED,ConversationPhrases.NO_MESSAGES,new QuestNotStartedCondition(concreteQuest.getSlotName()),ConversationStates.ATTENDING,concreteQuest.respondToQuestRefusal(),new DecreaseKarmaAction(concreteQuest.getKarmaDiffForQuestResponse()));}
    protected void playerDoesNotWantToGiveItems(){concreteQuest.getNPC().add(new ConversationStates[]{ConversationStates.ATTENDING,ConversationStates.QUESTION_1},ConversationPhrases.NO_MESSAGES,new QuestActiveCondition(concreteQuest.getSlotName()),ConversationStates.ATTENDING,null,(p,s,n)->n.say(concreteQuest.respondToPlayerSayingHeHasNoItems(getListOfStillMissingItems(p,false))));}
}
