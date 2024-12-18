/* $Id: CountObjects.java,v 1.9 2008/07/12 14:43:47 astridemma Exp $ */
package games.stendhal.server.script;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.Iterator;
import java.util.List;

import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;

/**
 * Counts the number of creatures on the world.
 * 
 * @author hendrik
 */
public class CountObjects extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		int count = 0;
		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		for (final IRPZone irpZone : world) {
			final StendhalRPZone zone = (StendhalRPZone) irpZone;
			final Iterator<RPObject> itr2 = zone.iterator();
			while (itr2.hasNext()) {
				itr2.next();
				count++;
			}
		}
		admin.sendPrivateText("Liczba objektów " + count);
	}
}
