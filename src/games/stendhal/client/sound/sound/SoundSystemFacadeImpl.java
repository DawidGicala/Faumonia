/* $Id: SoundSystemFacadeImpl.java,v 1.9 2012/07/13 05:56:12 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.sound.sound;

import games.stendhal.client.WorldObjects.WorldListener;
import games.stendhal.client.entity.User;
import games.stendhal.client.sound.facade.SoundGroup;
import games.stendhal.client.sound.facade.SoundHandle;
import games.stendhal.client.sound.facade.SoundSystemFacade;
import games.stendhal.client.sound.facade.Time;
import games.stendhal.client.sound.manager.DeviceEvaluator.Device;
import games.stendhal.client.sound.manager.SoundManagerNG.Sound;
import games.stendhal.common.math.Algebra;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class is the interface between the game logic and the
 * sound system.
 * 
 * @author hendrik, silvio
 */
public class SoundSystemFacadeImpl implements SoundSystemFacade, WorldListener {
	private static Logger logger = Logger.getLogger(SoundSystemFacadeImpl.class);
	
	private ExtendedSoundManager manager = new ExtendedSoundManager();

	public void playerMoved() {
		try {
			float[] position = Algebra.vecf((float) User.get().getX(), (float) User.get().getY());
			manager.setHearerPosition(position);
			manager.update();
		} catch (RuntimeException e) {
			logger.error(e, e);
		}
	}

	public void zoneEntered(String zoneName) {
		// ignored
	}

	public void zoneLeft(String zoneName) {
		// ignored
	}

	public void exit() {
		try {
			manager.exit();
		} catch (RuntimeException e) {
			logger.error(e, e);
		}
	}

	public SoundGroup getGroup(String groupName) {
		return manager.getGroup(groupName);
	}

	public void update() {
		try {
			manager.update();
		} catch (RuntimeException e) {
			logger.error(e, e);
		}
	}

	public void stop(SoundHandle sound, Time fadingDuration) {
		if (sound != null) {
		try {
				if (sound instanceof Sound) {
			manager.stop((Sound) sound, fadingDuration);
				} else {
					logger.error("sound handle not instance of Sound but " + sound, new Throwable());
				}
		} catch (RuntimeException e) {
			logger.error(e, e);
			}
		}
	}

	public void mute(boolean turnOffSound, boolean useFading, Time delay) {
		manager.mute(turnOffSound, useFading, delay);
	}

	public float getVolume() {
		return manager.getVolume();
	}

	public Collection<String> getGroupNames() {
		return manager.getGroupNames();
	}

	public void changeVolume(float volume) {
		manager.changeVolume(volume);
	}

	public List<String> getDeviceNames() {
		List<String> res = new LinkedList<String>();
		for (Device device : manager.getDevices()) {
			res.add(device.getName());
		}
		return res;
	}
}
