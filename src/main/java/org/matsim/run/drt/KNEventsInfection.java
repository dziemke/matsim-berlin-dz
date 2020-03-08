package org.matsim.run.drt;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;

class KNEventsInfection{

        public static void main( String[] args ){
                boolean hasCommandLineArgs = true ;
                if ( args==null ){
                        hasCommandLineArgs = false;
                } else if ( args.length==0 ) {
                        hasCommandLineArgs=false;
                } else if ( args[0]==null ) {
                        hasCommandLineArgs=false;
                } else if ( args[0].equals( "" ) ) {
                        hasCommandLineArgs=false;
                }
                if ( hasCommandLineArgs ) {
                        throw new RuntimeException( "cannot deal with command line args for time being." );
                }

                Config config = ConfigUtils.createConfig( new EpisimConfigGroup() );
                EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule( config, EpisimConfigGroup.class );
                episimConfig.setCase( EpisimConfigGroup.Case.berlin1pct );

                
        	String filename;
                switch( episimConfig.getCase() ) {
                        case berlin1pct:
                                filename = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct/output-berlin-v5.4-1pct/berlin-v5.4-1pct.output_events_wo_linkEnterLeave.xml.gz";
                                break;
                        case berlin10pct:
                                filename = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/output-berlin-v5.4-10pct/berlin-v5.4-10pct.output_events_reduced.xml.gz";
                                break;
                        default:
                                throw new IllegalStateException( "Unexpected value: " + episimConfig.getCase() );
                }

                EventsManager events = EventsUtils.createEventsManager();
                
                events.addHandler( new InfectionEventHandler() );

                for ( int iteration=0 ; iteration<=1000 ; iteration++ ){
                        events.resetHandlers( iteration );
                        EventsUtils.readEvents( events, filename );
                }

        }

}
