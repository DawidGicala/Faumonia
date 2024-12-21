/*
 * @(#) src/games/stendhal/common/ConfigurableFactory.java
 *
 * $Id: ConfigurableFactory.java,v 1.1 2007/12/14 17:20:57 nhnb Exp $
 */

package games.stendhal.server.core.config.factory;

/**
 * A general object factory that accepts confguration attributes.
 */
public interface ConfigurableFactory {

	/**
	 * Create an object.
	 * 
	 * @param ctx
	 *            Configuration context.
	 * 
	 * @return A new object, or <code>null</code> if allowed by the factory
	 *         type.
	 * 
	 * @throws IllegalArgumentException
	 *             If there is a problem with the attributes. The exception
	 *             message should be a value suitable for meaningful user
	 *             interpretation.
	 */
	Object create(ConfigurableFactoryContext ctx);
}
