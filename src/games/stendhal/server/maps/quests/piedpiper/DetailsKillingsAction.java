package games.stendhal.server.maps.quests.piedpiper;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

public class DetailsKillingsAction implements ChatAction, ITPPQuestConstants {
	public void fire(final Player player, final Sentence sentence, final EventRaiser mayor) {
		if (TPPQuestHelperFunctions.calculateReward(player)==0) {
			mayor.say("Nie zabiłeś żadnych szczurów podczas inwazji #szczurów. "+
					  "Aby odebrać #nagrodę musisz zabić conajmniej "+
					  "jednego szczura.");
			return;
		}
		final StringBuilder sb = new StringBuilder("Cóż od ostatniej nagrody zabiłeś ");
		long moneys = 0;
		int kills = 0;
		for(int i=0; i<RAT_TYPES.size(); i++) {
			try {
				kills=Integer.parseInt(player.getQuest(QUEST_SLOT,i+1));
			} catch (NumberFormatException nfe) {
				// Have no records about this creature in player's slot.
				// Treat it as he never killed this creature.
				kills=0;
			}
			// must add 'and' word before last creature in list
			if(i==(RAT_TYPES.size()-1)) {
				sb.append("i ");
			}

			sb.append(Grammar.quantityplnoun(kills, RAT_TYPES.get(i), ""));
			sb.append(", ");
			moneys = moneys + kills*RAT_REWARDS.get(i);
		}
		sb.append("cóż dam tobie ");
		sb.append(moneys);
		sb.append(" money jako #nagrodę za pracę.");
		mayor.say(sb.toString());
	}


}
