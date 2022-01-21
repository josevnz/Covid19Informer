package com.kodegeek.covid19.informer;

import com.kodegeek.covid19.towndata.Environment;
import com.kodegeek.covid19.towndata.TownDataRetriever;
import com.kodegeek.covid19.towndata.Version;

import java.util.List;

import org.apache.commons.cli.*;


@Environment("production")
@Version(author = "Jose Vicente Nunez Zuleta")
@Version(number = 2, author = "Jose Vicente Nunez Zuleta")
@Version(number = 3, author = "Jose Vicente Nunez Zuleta")
public class ConsoleInformer {

    public static final int DEFAULT_CONFIRMED_CASES_LIMIT = 10_000;

    /**
     * Entry point
     *
     * @param args Ignored for now
     * @throws Exception If data cannot be loaded for any reason
     */
    public static void main(final String... args) throws Exception {
        final HelpFormatter formatter = new HelpFormatter();
        final Options options = new Options();
        try {
            options.addOption("l", "limit", true, "Override the default confirmed cases limit");
            options.addOption("h", "help", false, "Help");
            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(options, args);
            var limit = DEFAULT_CONFIRMED_CASES_LIMIT;

            if (cmd.hasOption("help")) {
                formatter.printHelp(ConsoleInformer.class.getSimpleName(), options, true);
                System.exit(0);
            }

            if (cmd.hasOption("limit")) {
                limit = Integer.parseInt(cmd.getOptionValue("limit", String.valueOf(DEFAULT_CONFIRMED_CASES_LIMIT)));
                if (limit < 0) {
                    throw new IllegalArgumentException("Limit cannot be a negative number!");
                }
            }
            final var number_of_cases = limit;

            final List<TownDataRetriever.Covid19DataPerTown> covidData =
                    TownDataRetriever.Covid19DataPerTown.getCovidData();
            TownDataRetriever.Covid19DataPerTown.printCovidData(
                    covidData,
                    cd -> cd.getConfirmedCases() > number_of_cases
            );
        } catch (UnrecognizedOptionException exp) {
            System.err.println(exp.getMessage());
            formatter.printHelp(ConsoleInformer.class.getSimpleName(), options, true);
            System.exit(100);
        } catch (ParseException|IllegalArgumentException exp) {
            exp.printStackTrace();
            System.exit(100);
        }
    }

}
