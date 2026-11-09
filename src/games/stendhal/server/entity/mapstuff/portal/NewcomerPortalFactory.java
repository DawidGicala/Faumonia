package games.stendhal.server.entity.mapstuff.portal;
import games.stendhal.server.core.config.factory.ConfigurableFactoryContext;
public final class NewcomerPortalFactory extends AccessCheckingPortalFactory {
    @Override protected AccessCheckingPortal createPortal(ConfigurableFactoryContext ctx){return new NewcomerPortal();}
}
