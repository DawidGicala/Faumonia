/* $Id: StendhalClient.java,v 1.258 2012/08/02 21:19:14 kiheru Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client;

import games.stendhal.client.entity.User;
import games.stendhal.client.gui.login.CharacterDialog;
import games.stendhal.client.listener.FeatureChangeListener;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.update.ClientGameConfiguration;
import games.stendhal.client.update.HttpClient;
import games.stendhal.common.Direction;
import games.stendhal.common.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import marauroa.client.BannedAddressException;
import marauroa.client.ClientFramework;
import marauroa.client.TimeoutException;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.Perception;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;

import org.apache.log4j.Logger;

/**
 * This class is the glue to Marauroa, it extends ClientFramework and allows us
 * to easily connect to an marauroa server and operate it easily.
 */
public class StendhalClient extends ClientFramework {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(StendhalClient.class);

	final Map<RPObject.ID, RPObject> world_objects;

	private final PerceptionHandler handler;

	private final RPObjectChangeDispatcher rpobjDispatcher;

	private final StaticGameLayers staticLayers;

	private final GameObjects gameObjects;

	protected static StendhalClient client;

	private final Cache cache;

	private final ArrayList<Direction> directions;

	private static final String LOG4J_PROPERTIES = "data/conf/log4j.properties";

	private String userName = "";

	private String character = null;

	private final UserContext userContext;

	/** Listeners to be called when zone changes. */
	private final List<ZoneChangeListener> zoneChangeListeners = new ArrayList<ZoneChangeListener>();
	
	/**
	 * The amount of content yet to be transfered.
	 */
	private int contentToLoad;

	/**
	 * Whether the client is in a batch update.
	 */
	private boolean inBatchUpdate = false;
	private final ReentrantLock drawingSemaphore = new ReentrantLock();

	private final StendhalPerceptionListener stendhalPerceptionListener;
	/** The zone currently under loading. */
	private Zone currentZone;
	
	private JFrame splashScreen;

	public static StendhalClient get() {
		return client;
	}

	public static void resetClient() {
		client = null;
	}

	public StendhalClient(final UserContext userContext, final PerceptionDispatcher perceptionDispatcher) {
		super(LOG4J_PROPERTIES);
		client = this;
		ClientSingletonRepository.setClientFramework(this);

		world_objects = new HashMap<RPObject.ID, RPObject>();
		staticLayers = new StaticGameLayers();
		gameObjects = GameObjects.createInstance(staticLayers);
		this.userContext = userContext;

		rpobjDispatcher = new RPObjectChangeDispatcher(gameObjects, userContext);
		final PerceptionToObject po = new PerceptionToObject();
		po.setObjectFactory(new ObjectFactory());
		perceptionDispatcher.register(po);
		stendhalPerceptionListener = new StendhalPerceptionListener(perceptionDispatcher, rpobjDispatcher, userContext, world_objects);
		handler = new PerceptionHandler(stendhalPerceptionListener);

		cache = new Cache();
		cache.init();

		directions = new ArrayList<Direction>(2);
	}

	@Override
	protected String getGameName() {
		return stendhal.GAME_NAME.toLowerCase(Locale.ENGLISH);
	}

	@Override
	protected String getVersionNumber() {
		return stendhal.VERSION;
	}

	public StaticGameLayers getStaticGameLayers() {
		return staticLayers;
	}

	public GameObjects getGameObjects() {
		return gameObjects;
	}

	/**
	 * Handle sync events before they are dispatched.
	 * 
	 * @param zoneid
	 *            The zone entered.
	 */
	protected void onBeforeSync(final String zoneid) {
		/*
		 * Simulate object disassembly
		 */
		for (final RPObject object : world_objects.values()) {
			if (object != userContext.getPlayer()) {
				rpobjDispatcher.dispatchRemoved(object);
			}
		}

		if (userContext.getPlayer() != null) {
			rpobjDispatcher.dispatchRemoved(userContext.getPlayer());
		}

		gameObjects.clear();

		// If player exists, notify zone leaving.
		if (!User.isNull()) {
			WorldObjects.fireZoneLeft(User.get().getID().getZoneID());
		}

		// Notify zone entering.
		WorldObjects.fireZoneEntered(zoneid);
	}

	@Override
	protected void onPerception(final MessageS2CPerception message) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("message: " + message);
			}

			if (message.getPerceptionType() == Perception.SYNC) {
				onBeforeSync(message.getRPZoneID().getID());
			}

			handler.apply(message, world_objects);
		} catch (final Exception e) {
			logger.error("error processing message " + message, e);
		}

		if (inBatchUpdate && (contentToLoad == 0)) {
			validateAndUpdateZone(currentZone);
			inBatchUpdate = false;
			/*
			 * Rapid zone change can cause two content transfers in a row. 
			 * Similarly a zone update that happens when the player changes
			 * zones. Only the latter will ever get a perception, so we need to
			 * release any locks we are holding, or the game screen will be
			 * permanently frozen.
			 */
			while (drawingSemaphore.getHoldCount() > 0) {
				drawingSemaphore.unlock();
			}
		}
	}

	@Override
	protected List<TransferContent> onTransferREQ(final List<TransferContent> items) {
		// A batch update has begun
		inBatchUpdate = true;
		logger.debug("Batch update started");

		String oldZone = (currentZone != null) ? currentZone.getName() : null;

		// Set the new area name
		for (TransferContent item : items) {
			final String name = item.name;
			final int i = name.indexOf(".0_floor");
			if (i > -1) {
			currentZone = new Zone(name.substring(0, i));
				break;
			}
		}

		// Is it just a reload for new coloring?
		if (currentZone != null) {
			boolean isZoneChange = !currentZone.getName().equals(oldZone);
			currentZone.setUpdate(!isZoneChange);
			if (isZoneChange) {
				logger.debug("Preparing for zone change");
				// Only true zone changes need to lock drawing
				drawingSemaphore.lock();
				staticLayers.clear();
				for (ZoneChangeListener listener : zoneChangeListeners) {
					listener.onZoneChange();
				}
			}
		}

		contentToLoad = 0;

		for (final TransferContent item : items) {
			if ((item.name != null) && item.name.endsWith(".data_map")) {
				// Tell the zone to be invalid until the data layer has been
				// added
				currentZone.requireDataLayer();
			}
			final InputStream is = cache.getItem(item);

			if (is != null) {
				item.ack = false;

				try {
					contentHandling(item.name, is);
					is.close();
				} catch (final Exception e) {
					e.printStackTrace();
					logger.error(e, e);

					// request retransmission
					item.ack = true;
				}
			} else {
				logger.debug("Content " + item.name + " is NOT on cache. We have to transfer");
				item.ack = true;
			}

			if (item.ack) {
				contentToLoad++;
			}
		}

		return items;
	}

	/**
	 * Add a listener to be called when the player changes zone.
	 * 
	 * @param listener
	 */
	public void addZoneChangeListener(ZoneChangeListener listener) {
		zoneChangeListeners.add(listener);
	}

	/**
	 * Determine if we are in the middle of transfering new content.
	 * 
	 * @return <code>true</code> if more content is to be transfered.
	 */
	public boolean isInTransfer() {
		return (contentToLoad != 0);
	}

	/**
	 * Load layer data
	 * 
	 * @param name name of the layer
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void contentHandling(final String name, final InputStream in) throws IOException, ClassNotFoundException {
		final int i = name.indexOf('.');

		if (name.endsWith(".jar")) {
			in.close();
			DataLoader.addJarFile(cache.getFilename(name));
		} else {
			if (i > -1) {
				final String layer = name.substring(i + 1);
				currentZone.addLayer(layer, in);
			}
		}
	}

	@Override
	protected void onTransfer(final List<TransferContent> items) {
		for (final TransferContent item : items) {
			try {
				if (item.cacheable) {
					cache.store(item, item.data);
				}
				/*
				 * For laggy connections: in two fast consecutive zone changes
				 * it can happen that the data for the first zone arrives only
				 * after the data for the second has been already offered. Ie.
				 * the zone has changed but the client receives data for the
				 * previous zone. Discard that and keep waiting for the real
				 * data.
				 */
				if (item.name.startsWith(currentZone.getName() + ".")) {
				contentHandling(item.name, new ByteArrayInputStream(item.data));
				} else {
					// Still waiting for the real data
					contentToLoad++;
				}
			} catch (final Exception e) {
				logger.error("onTransfer", e);
			}
		}

		contentToLoad -= items.size();

		/*
		 * Sanity check
		 */
		if (contentToLoad < 0) {
			logger.warn("More data transfer than expected");
			contentToLoad = 0;
		}
	}

	@Override
	protected void onAvailableCharacters(final String[] characters) {
		// see onAvailableCharacterDetails
	}

	@Override
	protected void onAvailableCharacterDetails(final Map<String, RPObject> characters) {

		// if there are no characters, create one with the specified name automatically
		if (characters.size() == 0) {
			if (character == null) {
				character = getAccountUsername();
			}
			logger.warn("The requested character is not available, trying to create character " + character);
			final RPObject template = new RPObject();
			try {
				final CharacterResult result = createCharacter(character, template);
				if (result.getResult().failed()) {
					logger.error(result.getResult().getText());
					JOptionPane.showMessageDialog(splashScreen, result.getResult().getText());
				}
			} catch (final Exception e) {
				logger.error(e, e);
			}
			return;
		}

		// autologin if a valid character was specified.
		if ((character != null) && (characters.keySet().contains(character))) {
			try {
				chooseCharacter(character);
				stendhal.setDoLogin();
				if (splashScreen != null) {
					splashScreen.dispose();
				}
			} catch (final Exception e) {
				logger.error("StendhalClient::onAvailableCharacters", e);
			}
			return;
		}

		// show character dialog
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CharacterDialog(characters, splashScreen);
			}
		});
	}

	@Override
	protected void onServerInfo(final String[] info) {
		// ignore server response
	}

	@Override
	protected void onPreviousLogins(final List<String> previousLogins) {
		// TODO: display this to the player
	}

	/**
	 * Add an active player movement direction.
	 * 
	 * @param dir
	 *            The direction.
	 * @param face
	 *            If to face direction only.
	 */
	public void addDirection(final Direction dir, final boolean face) {
		RPAction action;
		Direction odir;
		int idx;

		/*
		 * Cancel existing opposite directions
		 */
		odir = dir.oppositeDirection();

		if (directions.remove(odir)) {
			/*
			 * Send direction release
			 */
			action = new RPAction();
			action.put("type", "move");
			action.put("dir", -odir.get());

			send(action);
		}

		/*
		 * Handle existing
		 */
		idx = directions.indexOf(dir);
		if (idx != -1) {
			/*
			 * Already highest priority? Don't send to server.
			 */

			if (idx == (directions.size() - 1)) {
				logger.debug("Ignoring same direction: " + dir);
				return;
			}

			/*
			 * Move to end
			 */
			directions.remove(idx);
		}

		directions.add(dir);

	if (face) {
		action = new FaceRPAction(dir);
	} else {
		action = new MoveRPAction(dir);
	}
		
	send(action);
	}


/**
	 * Remove a player movement direction.
	 * 
	 * @param dir
	 *            The direction.
	 * @param face
	 *            If to face direction only.
	 */
	public void removeDirection(final Direction dir, final boolean face) {
		RPAction action;
		int size;

		/*
		 * Send direction release
		 */
		action = new RPAction();
		action.put("type", "move");
		action.put("dir", -dir.get());

		send(action);

		/*
		 * Client side direction tracking (for now)
		 */
		directions.remove(dir);

		// Existing one reusable???
		size = directions.size();
		if (size == 0) {
			action = new RPAction();
			action.put("type", "stop");
		} else {
			if (face) {
				action = new FaceRPAction(directions.get(size - 1));

			} else {
				action = new MoveRPAction(directions.get(size - 1));

			}
		}

		send(action);
	}

	/**
	 * Stop the player.
	 */
	public void stop() {
		directions.clear();

		final RPAction rpaction = new RPAction();

		rpaction.put("type", "stop");
		rpaction.put("attack", "");

		send(rpaction);
	}

	public void addFeatureChangeListener(final FeatureChangeListener l) {
		userContext.addFeatureChangeListener(l);
	}

	public void removeFeatureChangeListener(final FeatureChangeListener l) {
		userContext.removeFeatureChangeListener(l);
	}


	//
	//

	public void setAccountUsername(final String username) {
		userContext.setName(username);
		userName = username;
	}

	public String getCharacter() {
		return character;
	}

	public void setCharacter(String character) {
		this.character = character;
	}

	/**
	 * Set the splash screen window. Used for transient windows.
	 * 
	 * @param splash first screen window
	 */
	public void setSplashScreen(JFrame splash) {
		splashScreen = splash;
	}

	public String getAccountUsername() {
		return userName;
	}

	/**
	 * Check to see if the object is the connected user. This is an ugly hack
	 * needed because the perception protocol distinguishes between normal and
	 * private (my) object changes, but not full add/removes.
	 *
	 * @param object
	 *            An object.
	 * 
	 * @return <code>true</code> if it is the user object.
	 */
	public boolean isUser(final RPObject object) {
		if (object.getRPClass().subclassOf("player")) {
			return getCharacter().equalsIgnoreCase(object.get("name"));
		} else {
			return false;
		}
	}

	/**
	 * Return the Cache instance.
	 * 
	 * @return cache
	 */
	public Cache getCache() {
		return cache;
	}
	static class MoveRPAction extends RPAction {
		public MoveRPAction(final Direction dir) {
			put("type", "move");
			put("dir", dir.get());
		}
	}

	static class FaceRPAction extends RPAction {
		public FaceRPAction(final Direction dir) {
			put("type", "face");
			put("dir", dir.get());
		}
	}

	public RPObject getPlayer() {
		return userContext.getPlayer();
		
	}

	@Override
	public synchronized boolean chooseCharacter(String character)
			throws TimeoutException, InvalidVersionException,
			BannedAddressException {
		boolean res = super.chooseCharacter(character);
		if (res) {
			this.character = character;
		}
		return res;
	}

	public void releaseDrawingSemaphore() {
		drawingSemaphore.unlock();
	}

	public boolean tryAcquireDrawingSemaphore() {
		return drawingSemaphore.tryLock();
	}

	/**
	 * Interface for listeners that need to be informed when the user is
	 * changing zone.
	 */
	public static interface ZoneChangeListener {
		/**
		 * Called when the user is changing zone.
		 */
		void onZoneChange();
		/**
		 * Called when the zone is updated, such as when the coloring changes.
		 */
		void onZoneUpdate();
	}
	
	/**
	 * Validate a zone (prepare the tilesets), and set it as the current zone.
	 * Zones that are just updates (changed colors, for example), get validated
	 * in a background thread.
	 *
	 * @param zone zone to be validated
	 */
	private void validateAndUpdateZone(final Zone zone) {
		// Build the tileset data in a background thread for updates, so that
		// the client does not pause when zone colors change.
		if (zone.isUpdate()) {
			Thread worker = new Thread() {
				@Override
				public void run() {
					zone.validate();
					// Push "zone change" back to the game loop, so that zone
					// name checking works correctly, and that there aren't two
					// zone changes happening from two different threads.
					GameLoop.get().runOnce(new Runnable() {
						public void run() {
							if (!zone.getName().equals(staticLayers.getAreaName())) {
								/*
								 * The player has changed zones while the zone,
								 * update was running. Just throw the zone away
								 * as outdated.
								 */
								return;
							}
							staticLayers.setZone(zone);
							for (ZoneChangeListener listener : zoneChangeListeners) {
								listener.onZoneUpdate();
							}
						}
					});
				}
			};
			worker.start();
		} else {
			zone.validate();
			staticLayers.setZone(zone);
		}
	}

	/**
	 * Connect to the server, and if our version is too outdated, display a message.
	 *
	 * @throws IOException in case of an input/output error
	 */
	@Override
	public void connect(final String host, final int port) throws IOException {
		String gameName = ClientGameConfiguration.get("GAME_NAME").toLowerCase(Locale.ENGLISH);

		// include gamename, so that arianne.sf.net can ignore non stendhal games
		// include server name and port because we want to support different versions for
		// the main server and the test server
		String url = "http://FaumoniaOnline.sourceforge.net/versioncheck/"
				+ URLEncoder.encode(gameName, "UTF-8") + "/"
				+ URLEncoder.encode(host, "UTF-8") + "/"
				+ URLEncoder.encode(Integer.toString(port), "UTF-8") + "/"
				+ URLEncoder.encode(Version.getVersion(), "UTF-8");
		HttpClient httpClient = new HttpClient(url);
		String message = httpClient.fetchFirstLine();
		if ((message != null) && (message.trim().length() > 0)) {
			JOptionPane.showMessageDialog(splashScreen,
				new JLabel(message), "Sprawdzanie wersji",
				JOptionPane.WARNING_MESSAGE);
		}

		super.connect(host, port);
	}

	@Override
	public void send(RPAction action) {

		// work around a bug in the chat-action definition in 0.98 and below
		String type = action.get("type");
		String serverVersion = User.getServerRelease();
		if (((serverVersion == null) || (serverVersion.compareTo("0.40")) >= 0) && (RPClass.getRPClass(type) != null)) {
			action.setRPClass(type);
			action.remove("type");
		}
		super.send(action);
	}
}
