package games.stendhal.server.maps.pol.zakopane.gorasmoka;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.scroll.BalloonScroll;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Adds the listener for teleporting back to the islands if you login in the clouds
 */
public class AddBalloonListener implements ZoneConfigurator {
	
	private static final String BALLOON = "balonik";
	
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			public void onLoggedIn(final Player player) {
				BalloonScroll scroll = (BalloonScroll) SingletonRepository.getEntityManager().getItem(BALLOON);
				scroll.teleportBack(player);
			}

		});
	}



}
