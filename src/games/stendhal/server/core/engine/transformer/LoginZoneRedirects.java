/*
 * Faumonia - redirects selected saved login zones to configured destinations.
 *
 * PlayerTransformer.java only contains a one-time hook calling:
 * LoginZoneRedirects.apply(object, player)
 *
 * Add or edit redirect groups only in this file.
 */
package games.stendhal.server.core.engine.transformer;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import marauroa.common.game.RPObject;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.player.Player;

public final class LoginZoneRedirects {

	private static final Logger logger = Logger.getLogger(LoginZoneRedirects.class);

	private static final List<Redirect> REDIRECTS = Arrays.asList(

		// HELL
		new Redirect(
			Arrays.asList(
				"hell",
				"hell_secret"
			),
			"-1_nalwor_caves_e",
			46,
			66
		),

		// PUSTYNIA - wszystkie mapy
		new Redirect(
			Arrays.asList(
				"0_desert_pyramid_nw",
				"0_desert_pyramid_ne",
				"0_desert_pyramid_sw",
				"0_desert_pyramid_se",
				"int_desert_blacksmith",
				"int_desert_house_0",
				"int_desert_house_-1",
				"-1_desert_fire_pyramid",
				"-1_desert_pyramid",
				"-1_desert_corridor",
				"-1_desert_chess_pyramid",
				"-2_desert_black_river_n",
				"-2_desert_black_river_s"
			),
			"0_semos_city",
			95,
			40
		),

		// KIKAREUKIN 1-3
		new Redirect(
			Arrays.asList(
				"1_kikareukin_cave",
				"2_kikareukin_cave",
				"3_kikareukin_cave"
			),
			"0_semos_mountain_n_w4",
			61,
			40
		),

		// KIKAREUKIN 4-6
		new Redirect(
			Arrays.asList(
				"4_kikareukin_cave",
				"5_kikareukin_cave",
				"6_kikareukin_islands"
			),
			"3_kikareukin_cave",
			58,
			56
		),

		// DRAGON CAVE
		new Redirect(
			Arrays.asList(
				"-1_dragon_cave",
				"-1_dragon_cave_e",
				"-2_dragon_cave",
				"-3_dragon_cave",
				"-4_dragon_cave",
				"-5_dragon_cave"
			),
			"-1_drake_cave",
			8,
			2
		),

		// GLEBOKA JASKINIA - DZIKA WYSPA
		new Redirect(
			Arrays.asList(
				"-1_wild_island_deep_cave",
				"-2_wild_island_deep_cave",
				"-3_wild_island_deep_cave"
			),
			"0_wild_island_nw",
			96,
			56
		),

		// DRUGA JASKINIA - DZIKA WYSPA
		new Redirect(
			Arrays.asList(
				"-1_wild_island_cave"
			),
			"0_wild_island_nw",
			88,
			114
		),

		// WIELICZKA
		new Redirect(
			Arrays.asList(
				"-1_ados_ice_rock_w",
				"-1_ados_ice_rock_c",
				"-1_ados_ice_rock_e",
				"-1_ados_ice_rock_e2",
				"-1_ados_ice_rock_e3",
				"-1_ados_ice_rock_sw",
				"-1_ados_ice_rock_s",
				"-1_ados_ice_rock_se",
				"-1_ados_ice_rock_se2",
				"-1_ados_ice_rock_se3"
			),
			"0_ados_rock_w",
			126,
			61
		),

		// MALLEUS
		new Redirect(
			Arrays.asList(
				"malleus_plain"
			),
			"0_semos_city",
			95,
			40
		),

		// DRAGON LAND - glowny obszar, domy, warsztat, zamek i jaskinia
		new Redirect(
			Arrays.asList(
				"0_dragon_land_n",
				"0_dragon_land_s",
				"int_dragon_house_1",
				"int_dragon_house_2",
				"int_dragon_house_3",
				"int_dragon_house_4",
				"int_dragon_house_5",
				"int_dragon_house_6",
				"int_dragon_workshop",
				"int_dragon_castle",
				"int_dragon_castle_room_1",
				"int_dragon_castle_room_2",
				"int_dragon_castle_room_3",
				"int_dragon_castle_room_4",
				"int_dragon_castle_room_5",
				"int_dragon_castle_room_6",
				"int_dragon_castle_dragon_npc",
				"int_dragon_castle_dragon",
				"-1_dragon_land_cave"
			),
			"0_dragon_land_s",
			67,
			61
		),

		// DRAGON LAND - pulapki wyrzucaja calkiem do SEMOS
		new Redirect(
			Arrays.asList(
				"-2_water_trap",
				"-2_poison_pit",
				"-2_spikes_pit"
			),
			"0_semos_city",
			95,
			40
		),

		// MARBLE CAVE
		new Redirect(
			Arrays.asList(
				"-1_orril_marble_cave",
				"-2_orril_marble_cave",
				"-3_orril_marble_cave",
				"-4_orril_marble_cave"
			),
			"0_orril_mountain_n2_w2",
			116,
			7
		),

		// ORRIL TOWER
		new Redirect(
			Arrays.asList(
				"1_orril_tower",
				"2_orril_tower",
				"3_orril_tower",
				"4_orril_tower"
			),
			"0_orril_mountain_n_w2",
			39,
			17
		),

		// ORRIL GOLD MINE
		new Redirect(
			Arrays.asList(
				"-1_orril_gold_mine_e",
				"-1_orril_gold_mine_w"
			),
			"0_orril_mountain_w2",
			120,
			19
		)
	);

	private LoginZoneRedirects() {
		// utility class
	}

	/**
	 * Checks whether the player's saved zone belongs to one of the redirect
	 * groups and, if so, places the player in the configured destination.
	 *
	 * @return true if a redirect was found and the player was placed
	 */
	public static boolean apply(final RPObject object, final Player player) {
		if ((object == null) || (player == null) || !object.has("zoneid")) {
			return false;
		}

		final String sourceZone = object.get("zoneid");
		final Redirect redirect = findRedirect(sourceZone);

		if (redirect == null) {
			return false;
		}

		final StendhalRPZone destination =
			SingletonRepository.getRPWorld().getZone(redirect.destinationZone);

		if (destination == null) {
			logger.error("Login redirect destination zone does not exist: "
				+ redirect.destinationZone + " (source: " + sourceZone + ")");
			return false;
		}

		if (!StendhalRPAction.placeat(
				destination, player, redirect.x, redirect.y)) {
			logger.error("Cannot redirect player " + player.getName()
				+ " from " + sourceZone
				+ " to " + redirect.destinationZone
				+ " at " + redirect.x + "," + redirect.y);
			return false;
		}

		logger.info("Redirected player " + player.getName()
			+ " on login from " + sourceZone
			+ " to " + redirect.destinationZone
			+ " at " + redirect.x + "," + redirect.y);

		return true;
	}

	private static Redirect findRedirect(final String sourceZone) {
		for (final Redirect redirect : REDIRECTS) {
			if (redirect.sourceZones.contains(sourceZone)) {
				return redirect;
			}
		}
		return null;
	}

	private static final class Redirect {
		private final List<String> sourceZones;
		private final String destinationZone;
		private final int x;
		private final int y;

		private Redirect(final List<String> sourceZones,
				final String destinationZone,
				final int x,
				final int y) {
			this.sourceZones = sourceZones;
			this.destinationZone = destinationZone;
			this.x = x;
			this.y = y;
		}
	}
}
