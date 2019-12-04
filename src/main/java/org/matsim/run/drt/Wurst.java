package org.matsim.run.drt;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.ControlerDefaultsModule;
import org.matsim.core.controler.NewControlerModule;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.corelisteners.ControlerDefaultCoreListenersModule;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioByInstanceModule;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Collections;

public class Wurst {

    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        config.transit().setUseTransit(true);
        config.transit().setTransitModes(Collections.singleton("pt"));
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
        Injector injector = org.matsim.core.controler.Injector.createInjector(
                config,
                new ScenarioByInstanceModule(ScenarioUtils.createScenario(config)),
                AbstractModule.override(Collections.singleton(new ControlerDefaultsModule()), new SwissRailRaptorModule()),
                new NewControlerModule(),
                new ControlerDefaultCoreListenersModule());
        injector.getInstance(TripRouter.class);
    }
}
