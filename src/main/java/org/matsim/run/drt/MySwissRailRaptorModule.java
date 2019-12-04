/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package org.matsim.run.drt;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.MainModeIdentifier;

/**
 * @author mrieser / SBB
 */
public class MySwissRailRaptorModule extends com.google.inject.AbstractModule {

    // Only difference to the core version is that the actual routing module bindings are commented out,
    // and that the config is passed in constructor.
    // I had to put the config in the constructor because with private modules my automagic with pre-injecting
    // the config doesn't work :-(,
    // but it also means we can now create two different ones with different configs. :-)
    // michaz

    private final Config config;

    public Config getConfig() {
        return config;
    }

    public MySwissRailRaptorModule(Config config) {
        this.config = config;
    }

    @Override
    public void configure() {
        if (getConfig().transit().isUseTransit()) {
            bind(SwissRailRaptor.class).toProvider(SwissRailRaptorFactory.class);

//            for (String mode : getConfig().transit().getTransitModes()) {
//                addRoutingModuleBinding(mode).toProvider(SwissRailRaptorRoutingModuleProvider.class);
//            }
            
            SwissRailRaptorConfigGroup srrConfig = ConfigUtils.addOrGetModule(getConfig(), SwissRailRaptorConfigGroup.class);

            if (srrConfig.isUseRangeQuery()) {
                bind(RaptorRouteSelector.class).to(ConfigurableRaptorRouteSelector.class);
            } else {
                bind(RaptorRouteSelector.class).to(LeastCostRaptorRouteSelector.class); // just a simple default in case it ever gets used.
            }
            
            switch (srrConfig.getScoringParameters()) {
            case Default:
                bind(RaptorParametersForPerson.class).to(DefaultRaptorParametersForPerson.class);
                break;
            case Individual:
                bind(RaptorParametersForPerson.class).to(IndividualRaptorParametersForPerson.class);
                break;
            }

            if (srrConfig.isUseIntermodalAccessEgress()) {
                bind(MainModeIdentifier.class).to(IntermodalAwareRouterModeIdentifier.class);
                switch (srrConfig.getIntermodalAccessEgressModeSelection()) {
                case CalcLeastCostModePerStop:
                    bind(RaptorStopFinder.class).to(DefaultRaptorStopFinder.class);
                    break;
                case RandomSelectOneModePerRoutingRequestAndDirection:
                    bind(RaptorStopFinder.class).to(RandomAccessEgressModeRaptorStopFinder.class);
                    break;
                }
            } else {
	            bind(RaptorStopFinder.class).to(DefaultRaptorStopFinder.class);
            }
            
            bind(RaptorIntermodalAccessEgress.class).to(DefaultRaptorIntermodalAccessEgress.class);
        }

    }

}
