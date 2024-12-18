/* $Id: ListNPCs.java,v 1.9 2008/07/12 14:43:48 astridemma Exp $ */
package games.stendhal.server.script;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.List;

/**
 * Lists all npcs and there position.
 * 
 * @author hendrik
 */
public class ListNPCs extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		final StringBuilder res = new StringBuilder();
		final NPCList npcs = SingletonRepository.getNPCList();
		for (final String name : npcs.getNPCs()) {
			final SpeakerNPC npc = npcs.get(name);
			res.append("\r\n" + name + "\t jest w ");
			res.append(npc.getZone().getName() + " (");
			res.append(npc.getX() + ", " + npc.getY() + ")");
		}
		admin.sendPrivateText(res.toString());
	}

}
