package org.matsim.run.drt;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorRoutingModuleProvider;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.ExplodedConfigModule;
import org.matsim.core.router.RoutingModule;

class TwoSwissRailRaptorsModule extends AbstractModule {
    private final Config config;

    public TwoSwissRailRaptorsModule(Config config) {
        super(config);
        this.config = config;
    }

    @Override
    public void install() {
        Config config1 = config; // TODO
        Config config2 = config; // TODO
        install(new PrivateModule() {
            @Override
            protected void configure() {
                install(new MySwissRailRaptorModule(config1));
                bind(RoutingModule.class).annotatedWith(Names.named("one")).toProvider(SwissRailRaptorRoutingModuleProvider.class);
                expose(RoutingModule.class).annotatedWith(Names.named("one"));
            }
        });
        install(new PrivateModule() {
            @Override
            protected void configure() {
                install(new MySwissRailRaptorModule(config2));
                // TODO: This only replaces the config _as the module sees it_, not the config _as parts of the
                // TODO: router get it injected_.
                // TODO: Overriding a binding that was bound "upstream" _does not work_,
                // TODO: not with private modules, not with child injectors, not at all.
                // TODO: THIS DOES NOT WORK, it will complain about a duplicate binding:
                // install(Modules.override(new MySwissRailRaptorModule(config1)).with(new ExplodedConfigModule(config1)));
                // TODO: (Which makes sense. It's just as if I would bind Config at this level again, override or not.
                // TODO: What we are looking for here is not so much overriding a binding in a module from the outside (that works)
                // TODO: but _shielding_ a module from a binding that was _made_ outside. (That does not work.)
                // TODO: This becomes clear if you forget the muddledness introduced by Controler.addOverridingModule:
                // TODO: The overriding modules are (in the end) wrapping the default modules, not the other way round.
                bind(RoutingModule.class).annotatedWith(Names.named("two")).toProvider(SwissRailRaptorRoutingModuleProvider.class);
                expose(RoutingModule.class).annotatedWith(Names.named("two"));
            }
        });
        addRoutingModuleBinding("pt-one").to(Key.get(RoutingModule.class, Names.named("one")));
        addRoutingModuleBinding("pt-two").to(Key.get(RoutingModule.class, Names.named("two")));
    }
}
