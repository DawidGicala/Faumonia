/* $Id: PlaySoundEntity.java,v 1.2 2010/09/01 16:03:28 nhnb Exp $ */
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
package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.common.constants.SoundLayer;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.events.SoundEvent;

/**
 * Plays a sound on "use".
 *
 * @author hendrik
 */
public class PlaySoundEntity extends UseableEntity {
	private String[] sounds;
	private int radius;
	private int volume;

	/**
	 * creates a new PlaySoundEntity
	 *
	 * @param sound   name of sound (or sounds) to play
	 * @param radius  hearing radius
	 * @param volume  volume
	 */
	public PlaySoundEntity(String sound, int radius, int volume) {
		this.sounds = sound.split(", *");
		this.radius = radius;
		this.volume = volume;
	}

	/**
	 * plays a sound
	 */
	public boolean onUsed(RPEntity user) {
		if (!nextTo(user)) {
			user.sendPrivateText("Stąd nie możesz dosięgnąć.");
			return false;
		}
		String sound = sounds[(int) (Math.random() * sounds.length)];
		SoundEvent event = new SoundEvent(sound, radius, volume, SoundLayer.AMBIENT_SOUND);
		this.addEvent(event);
		this.notifyWorldAboutChanges();
		return true;
	}

}
