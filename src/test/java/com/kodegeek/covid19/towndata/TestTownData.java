package com.kodegeek.covid19.towndata;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class TestTownData {

    private static final String COVID_DATA_FILE;
    static {
        COVID_DATA_FILE = Paths
                .get("src","test","resources", "covid-data-snapshot.json")
                .toAbsolutePath()
                .toUri()
                .toString();
    }

    @BeforeClass
    public static void setup() {

    }

    @Test
    public void testGetCovidData() {
        try {
            final List<TownDataRetriever.Covid19DataPerTown> townList =
                    TownDataRetriever.Covid19DataPerTown.getCovidData(COVID_DATA_FILE);
            assertNotNull("Could not retrieve town data", townList);
            for(TownDataRetriever.Covid19DataPerTown townData: townList) {
                assertNotNull("Got null town data in town list", townData);
                assertNotNull(townData.getTown());
                assertNotNull(townData.getLastUpdateDate());
                assertTrue(String.format("Invalid %f",
                        townData.getCaseRate()), townData.getCaseRate() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getConfirmedDeaths()), townData.getConfirmedDeaths() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getConfirmedCases()), townData.getConfirmedCases() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getNumberOfIndeterminates()), townData.getNumberOfIndeterminates() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getTownNumber()), townData.getTownNumber() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getNumberOfTests()), townData.getNumberOfTests() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getPeopleTested()), townData.getPeopleTested() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getNumberOfPositives()), townData.getNumberOfPositives() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getNumberOfNegatives()), townData.getNumberOfNegatives() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getProbableCases()), townData.getProbableCases() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getProbableDeaths()), townData.getProbableDeaths() >= 0);
                assertTrue(String.format("Invalid %f",
                        townData.getRateTestedPer100k()), townData.getRateTestedPer100k() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getNumberOfIndeterminates()), townData.getNumberOfIndeterminates() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getTotalCases()), townData.getTotalCases() >= 0);
                assertTrue(String.format("Invalid %d",
                        townData.getConfirmedDeaths()), townData.getConfirmedDeaths() >= 0);
            }
        } catch (NumberFormatException|IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
