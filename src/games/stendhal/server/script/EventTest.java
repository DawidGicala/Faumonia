/* $Id: EventTest.java,v 1.5 2008/07/12 14:43:48 astridemma Exp $ */
package games.stendhal.server.script;

import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.List;

import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.Definition.DefinitionClass;

/**
 * Tries to add an RPEvent.
 * 
 * @author hendrik
 */
public class EventTest extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() < 1) {
			admin.sendPrivateText("Użyj: /script EventTest.class {jakiś tekst}");
			return;
		}

		final RPClass rpclass = new RPClass("testevent");
		rpclass.add(DefinitionClass.RPEVENT, "testevent", Definition.STANDARD);

		final RPEvent event = new RPEvent("testevent");
		event.put("arg", args.get(0));
		admin.addEvent(event);
	}
}
