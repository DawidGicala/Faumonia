/* $Id: GC.java,v 1.6 2008/07/12 14:43:48 astridemma Exp $ */
package games.stendhal.server.script;

import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.List;

/**
 * Runs the garbage collector manually (for memory profiling).
 * 
 * @author hendrik
 */
public class GC extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		System.gc();
	}
}
