package games.stendhal.server.script;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.npc.action.DisableFeatureAction;
import games.stendhal.server.entity.player.Player;

import java.util.List;
/**
 * Script to disable a feature like keyring for a player
 *  
 * @author madmetzger
 */
public class DisableFeature extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		if(args != null && args.size() != 2) {
			admin.sendPrivateText("Użycie DisableFeature: [wojownik] [właściwość]");
		}
		final String feature = args.get(1);
		final String name = args.get(0);
		final Player player = SingletonRepository.getRuleProcessor().getPlayer(name);
		new DisableFeatureAction(feature).fire(player, null, null);
	}

}
