/*
 * @(#) src/games/stendhal/server/entity/area/CreatureProtectionAreaFactory.java
 *
 * $Id: PlaySoundEntityFactory.java,v 1.1 2010/09/01 09:42:07 nhnb Exp $
 */

package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.server.core.config.factory.ConfigurableFactoryContext;
import games.stendhal.server.entity.mapstuff.sound.LoopedAmbientSoundSource;
import games.stendhal.server.entity.mapstuff.sound.SoundSourceFactory;

/**
 * A factory for PeriodicAmbientSoundSource.
 */
public class PlaySoundEntityFactory extends SoundSourceFactory {

	/**
	 * Create an PlaySoundEntity.
	 * 
	 * @param ctx
	 *            Configuration context.
	 * 
	 * @return An PlaySoundEntity.
	 * 
	 * @throws IllegalArgumentException
	 *             If there is a problem with the attributes.
	 * @see LoopedAmbientSoundSource
	 */
	public PlaySoundEntity create(final ConfigurableFactoryContext ctx) {
		PlaySoundEntity source;

		source = new PlaySoundEntity(getSound(ctx), getRadius(ctx), getVolume(ctx));

		return source;
	}

}
