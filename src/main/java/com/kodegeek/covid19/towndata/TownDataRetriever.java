package com.kodegeek.covid19.towndata;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Download and show COVID-19 for CT
 *
 * @author Jose Vicente Nunez
 */
@Version(author = "Jose Vicente Nunez Zuleta")
@Environment("production")
public class TownDataRetriever {

    static {
        ObjectInputFilter.Config.setSerialFilter(filterInfo -> {
            Class<?> clazz = filterInfo.serialClass();
            if (clazz == null) {
                return ObjectInputFilter.Status.ALLOWED;
            }
            if (String.class.isAssignableFrom(clazz)) {
                return ObjectInputFilter.Status.ALLOWED;
            }
            if (Covid19DataPerTown.class.isAssignableFrom(clazz)) {
                return ObjectInputFilter.Status.ALLOWED;
            }
            if (Covid19DataPerTownCache.class.isAssignableFrom(clazz)) {
                return ObjectInputFilter.Status.ALLOWED;
            }
            return ObjectInputFilter.Status.REJECTED;
        });
    }

    /**
     * Links to several Data sources for COVID-19 available from the CT Data portal.
     * https://data.ct.gov/stories/s/COVID-19-data/wa3g-tfvc/
     */
    @Version(author="Jose Vicente Nunez Zuleta")
    enum CTCovid19DataSource {

        TestsCasesAndDeathsByTown(
                "https://data.ct.gov/api/views/28fr-iqnx/rows.csv?accessType=DOWNLOAD",
                "https://data.ct.gov/resource/28fr-iqnx.json",
                "COVID-19 Tests, Cases, and Deaths (By Town)");

        public String csvDownloadUrl;
        public String sodaJsonUrl;
        public String description;

        CTCovid19DataSource(String csvDownloadUrl, String sodaJsonUrl, String description) {
            this.csvDownloadUrl = csvDownloadUrl;
            this.sodaJsonUrl = sodaJsonUrl;
            this.description = description;
        }

    }

    private static final Logger LOG = Logger.getLogger(TownDataRetriever.class.getName());

    /**
     * Builder to make it easier to get COVID-19 data instances per town
     */
    @Version(number=2, author="Jose Vicente Nunez Zuleta")
    public static class Covid19DataPerTownBuilder {

        private LocalDate lastUpdateDate;
        private int townNumber;
        private String town;
        private long totalCases;
        private long confirmedCases;
        private long probableCases;
        private double caseRate;
        private long totalDeaths;
        private long confirmedDeaths;
        private long probableDeaths;
        private long peopleTested;
        private double rateTestedPer100k;
        private long numberOfTests;
        private long numberOfPositives;
        private long numberOfNegatives;
        private long numberOfIndeterminates;

        Covid19DataPerTownBuilder lastUpdateDate(LocalDate lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        Covid19DataPerTownBuilder townNumber(int townNumber) {
            this.townNumber = townNumber;
            return this;
        }

        Covid19DataPerTownBuilder town(String town) {
            this.town = town;
            return this;
        }

        Covid19DataPerTownBuilder totalCases(long totalCases) {
            this.totalCases = totalCases;
            return this;
        }

        Covid19DataPerTownBuilder confirmedCases(long confirmedCases) {
            this.confirmedCases = confirmedCases;
            return this;
        }

        Covid19DataPerTownBuilder probableCases(long probableCases) {
            this.probableCases = probableCases;
            return this;
        }

        Covid19DataPerTownBuilder caseRate(double caseRate) {
            this.caseRate = caseRate;
            return this;
        }

        Covid19DataPerTownBuilder totalDeaths(long totalDeaths) {
            this.totalDeaths = totalDeaths;
            return this;
        }

        Covid19DataPerTownBuilder confirmedDeaths(long confirmedDeaths) {
            this.confirmedDeaths = confirmedDeaths;
            return this;
        }

        Covid19DataPerTownBuilder probableDeaths(long probableDeaths) {
            this.probableDeaths = probableDeaths;
            return this;
        }

        Covid19DataPerTownBuilder peopleTested(long peopleTested) {
            this.peopleTested = peopleTested;
            return this;
        }

        Covid19DataPerTownBuilder rateTestedPer100k(double rateTestedPer100k) {
            this.rateTestedPer100k = rateTestedPer100k;
            return this;
        }

        Covid19DataPerTownBuilder numberOfTests(long numberOfTests) {
            this.numberOfTests = numberOfTests;
            return this;
        }

        Covid19DataPerTownBuilder numberOfPositives(long numberOfPositives) {
            this.numberOfPositives = numberOfPositives;
            return this;
        }

        Covid19DataPerTownBuilder numberOfNegatives(long numberOfNegatives) {
            this.numberOfNegatives = numberOfNegatives;
            return this;
        }

        Covid19DataPerTownBuilder numberOfIndeterminates(long numberOfIndeterminates) {
            this.numberOfIndeterminates = numberOfIndeterminates;
            return this;
        }

        public Covid19DataPerTown build() throws InvalidClassException {
            Covid19DataPerTown covidData = new Covid19DataPerTown(this);
            validateCovidData();
            return covidData;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Covid19DataPerTownBuilder.class.getSimpleName() + "[", "]")
                    .add("lastUpdateDate=" + lastUpdateDate)
                    .add("townNumber=" + townNumber)
                    .add("town='" + town + "'")
                    .add("totalCases=" + totalCases)
                    .add("confirmedCases=" + confirmedCases)
                    .add("probableCases=" + probableCases)
                    .add("caseRate=" + caseRate)
                    .add("totalDeaths=" + totalDeaths)
                    .add("confirmedDeaths=" + confirmedDeaths)
                    .add("probableDeaths=" + probableDeaths)
                    .add("peopleTested=" + peopleTested)
                    .add("rateTestedPer100k=" + rateTestedPer100k)
                    .add("numberOfTests=" + numberOfTests)
                    .add("numberOfPositives=" + numberOfPositives)
                    .add("numberOfNegatives=" + numberOfNegatives)
                    .add("numberOfIndeterminates=" + numberOfIndeterminates)
                    .toString();
        }

        private void validateCovidData() {
            if (
                    lastUpdateDate == null ||
                            townNumber < 0 ||
                            town == null ||
                            totalCases < 0 ||
                            confirmedCases < 0 ||
                            probableCases < 0 ||
                            caseRate < 0 ||
                            totalDeaths < 0 ||
                            confirmedDeaths < 0 ||
                            probableDeaths < 0 ||
                            peopleTested < 0 ||
                            rateTestedPer100k < 0 ||
                            numberOfTests < 0 ||
                            numberOfPositives < 0 ||
                            numberOfNegatives < 0 ||
                            numberOfIndeterminates < 0
            ) {
                throw new RuntimeException(this.toString());
            }
        }

        /**
         * Reuse the builder
         */
        public void clear() {
            this.townNumber = 0;
            this.town = null;
            this.lastUpdateDate = null;
            this.caseRate = 0.0;
            this.confirmedCases = 0;
            this.confirmedDeaths = 0;
            this.numberOfNegatives = 0;
            this.numberOfPositives = 0;
            this.numberOfTests = 0;
            this.numberOfIndeterminates = 0;
            this.peopleTested = 0;
            this.totalCases = 0;
            this.totalDeaths = 0;
            this.rateTestedPer100k = 0;
            this.probableCases = 0;
            this.probableDeaths = 0;
        }

    }

    /**
     * Model COVID-19 data per town:
     * Last update date
     * Town number
     * Town
     * Total cases
     * Confirmed cases
     * Probable cases
     * Case rate
     * Total deaths
     * Confirmed deaths
     * Probable deaths
     * People tested
     * Rate tested per 100k
     * Number of tests
     * Number of positives
     * Number of negatives
     * Number of indeterminates
     */
    @Version(number=2, author="Jose Vicente Nunez Zuleta")
    public static class Covid19DataPerTown implements Serializable {

        static final long serialVersionUID = 61L;
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("L/d/y");
        private final LocalDate lastUpdateDate;
        private final int townNumber;
        private final String town;
        private final long totalCases;
        private final long confirmedCases;
        private final long probableCases;
        private final double caseRate;
        private final long totalDeaths;
        private final long confirmedDeaths;
        private final long probableDeaths;
        private final long peopleTested;
        private final double rateTestedPer100k;
        private final long numberOfTests;
        private final long numberOfPositives;
        private final long numberOfNegatives;
        private final long numberOfIndeterminates;

        public LocalDate getLastUpdateDate() {
            return lastUpdateDate;
        }

        public int getTownNumber() {
            return townNumber;
        }

        public String getTown() {
            return town;
        }

        public long getTotalCases() {
            return totalCases;
        }

        public long getProbableCases() {
            return probableCases;
        }

        public double getCaseRate() {
            return caseRate;
        }

        public long getConfirmedDeaths() {
            return confirmedDeaths;
        }

        public long getProbableDeaths() {
            return probableDeaths;
        }

        public long getPeopleTested() {
            return peopleTested;
        }

        public double getRateTestedPer100k() {
            return rateTestedPer100k;
        }

        public long getNumberOfTests() {
            return numberOfTests;
        }

        public long getNumberOfPositives() {
            return numberOfPositives;
        }

        public long getNumberOfNegatives() {
            return numberOfNegatives;
        }

        public long getNumberOfIndeterminates() {
            return numberOfIndeterminates;
        }

        public long getConfirmedCases() {
            return this.confirmedCases;
        }

        @SuppressWarnings("unused")
        public long getTotalDeaths() {
            return totalDeaths;
        }

        public enum Parts {
            LastUpdateDate,
            TownNumber,
            Town,
            TotalCases,
            ConfirmedCases,
            ProbableCases,
            CaseRate,
            TotalDeaths,
            ConfirmedDeaths,
            ProbableDeaths,
            PeopleTested,
            RateTestedPer100k,
            NumberOfTests,
            NumberOfPositives,
            NumberOfNegatives,
            NumberOfIndeterminates
        }

        public static Parts[] CSVPartsVals = Parts.values();

        public Covid19DataPerTown(Covid19DataPerTown copy) {
            this.townNumber = copy.townNumber;
            this.town = copy.town;
            this.lastUpdateDate = copy.lastUpdateDate;
            this.caseRate = copy.caseRate;
            this.confirmedCases = copy.confirmedCases;
            this.confirmedDeaths = copy.confirmedDeaths;
            this.numberOfNegatives = copy.numberOfNegatives;
            this.numberOfPositives = copy.numberOfPositives;
            this.numberOfTests = copy.numberOfTests;
            this.numberOfIndeterminates = copy.numberOfIndeterminates;
            this.peopleTested = copy.peopleTested;
            this.totalCases = copy.totalCases;
            this.totalDeaths = copy.totalDeaths;
            this.rateTestedPer100k = copy.rateTestedPer100k;
            this.probableCases = copy.probableCases;
            this.probableDeaths = copy.probableDeaths;
        }

        private Object readResolve() throws ObjectStreamException {
            return new Covid19DataPerTown(this);
        }

        private Covid19DataPerTown(Covid19DataPerTownBuilder builder) {
            this.townNumber = builder.townNumber;
            this.town = builder.town;
            this.lastUpdateDate = builder.lastUpdateDate;
            this.caseRate = builder.caseRate;
            this.confirmedCases = builder.confirmedCases;
            this.confirmedDeaths = builder.confirmedDeaths;
            this.numberOfNegatives = builder.numberOfNegatives;
            this.numberOfPositives = builder.numberOfPositives;
            this.numberOfTests = builder.numberOfTests;
            this.numberOfIndeterminates = builder.numberOfIndeterminates;
            this.peopleTested = builder.peopleTested;
            this.totalCases = builder.totalCases;
            this.totalDeaths = builder.totalDeaths;
            this.rateTestedPer100k = builder.rateTestedPer100k;
            this.probableCases = builder.probableCases;
            this.probableDeaths = builder.probableDeaths;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Covid19DataPerTown.class.getSimpleName() + "[", "]")
                    .add("lastUpdateDate=" + lastUpdateDate)
                    .add("townNumber=" + townNumber)
                    .add("town='" + town + "'")
                    .add("totalCases=" + totalCases)
                    .add("confirmedCases=" + confirmedCases)
                    .add("probableCases=" + probableCases)
                    .add("caseRate=" + caseRate)
                    .add("totalDeaths=" + totalDeaths)
                    .add("confirmedDeaths=" + confirmedDeaths)
                    .add("probableDeaths=" + probableDeaths)
                    .add("peopleTested=" + peopleTested)
                    .add("rateTestedPer100k=" + rateTestedPer100k)
                    .add("numberOfTests=" + numberOfTests)
                    .add("numberOfPositives=" + numberOfPositives)
                    .add("numberOfNegatives=" + numberOfNegatives)
                    .add("numberOfIndeterminates=" + numberOfIndeterminates)
                    .toString();
        }

        /**
         * Get the COVID-19 town data for the state of CT
         * @param url To read the data from. Optional.
         * @return Fresh data
         * @throws IOException If you cannot read the data
         * @throws NumberFormatException Data is not clean
         */
        public static List<Covid19DataPerTown> getCovidData(String... url) throws IOException, NumberFormatException {

            final List<Covid19DataPerTown> data = new ArrayList<>();
            final ExecutorService executor = Executors.newFixedThreadPool(2);
            final URL covidURL;
            if (url.length > 0) {
                covidURL = new URL(url[0]);
            } else {
                covidURL = new URL(CTCovid19DataSource.TestsCasesAndDeathsByTown.csvDownloadUrl);
            }

            Callable<List<Covid19DataPerTown>> callable = () -> {

                Covid19DataPerTownCache.getInstance().loadFromCache(data);
                if (! data.isEmpty()) {
                    LOG.log(Level.INFO,
                            String.format("Found fresh data in local cache %s",
                                    Covid19DataPerTownCache.CACHE_LOCATION));
                    return data;
                }

                LOG.log(Level.INFO,
                        String.format("Getting data from %s",
                                CTCovid19DataSource.TestsCasesAndDeathsByTown.csvDownloadUrl));
                Covid19DataPerTownBuilder builder = new Covid19DataPerTownBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(covidURL.openStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String[] parts = line.split(",", CSVPartsVals.length);
                        if (parts.length != CSVPartsVals.length) {
                            LOG.log(Level.SEVERE,
                                    String.format("Skipping invalid line %s", line));
                            continue;
                        }
                        if ("Town number".equals(parts[Parts.TownNumber.ordinal()])) {
                            continue; // Skip the header
                        }
                        try {
                            sanitizeData(parts);
                            final Covid19DataPerTown covidData = builder
                                    .town(parts[Parts.Town.ordinal()])
                                    .townNumber(Integer.parseInt(parts[Parts.TownNumber.ordinal()]))
                                    .lastUpdateDate(LocalDate.parse(parts[Parts.LastUpdateDate.ordinal()], FORMATTER))
                                    .caseRate(Double.parseDouble(parts[Parts.TownNumber.ordinal()]))
                                    .rateTestedPer100k(Double.parseDouble(parts[Parts.RateTestedPer100k.ordinal()]))
                                    .confirmedCases(Long.parseLong(parts[Parts.ConfirmedCases.ordinal()]))
                                    .confirmedDeaths(Long.parseLong(parts[Parts.ConfirmedDeaths.ordinal()]))
                                    .numberOfIndeterminates(Long.parseLong(parts[Parts.NumberOfIndeterminates.ordinal()]))
                                    .numberOfNegatives(Long.parseLong(parts[Parts.ConfirmedDeaths.ordinal()]))
                                    .numberOfTests(Long.parseLong(parts[Parts.NumberOfNegatives.ordinal()]))
                                    .numberOfPositives(Long.parseLong(parts[Parts.NumberOfPositives.ordinal()]))
                                    .peopleTested(Long.parseLong(parts[Parts.PeopleTested.ordinal()]))
                                    .probableCases(Long.parseLong(parts[Parts.ProbableCases.ordinal()]))
                                    .probableDeaths(Long.parseLong(parts[Parts.ProbableDeaths.ordinal()]))
                                    .totalCases(Long.parseLong(parts[Parts.TotalCases.ordinal()]))
                                    .totalDeaths(Long.parseLong(parts[Parts.TotalDeaths.ordinal()])).build();
                            if (covidData != null) {
                                data.add(covidData);
                            }
                            builder.clear();
                        } catch (NumberFormatException nfe) {
                            throw new IOException(String.format("Could not parse this line: %s", line), nfe);
                        }
                    }
                    Covid19DataPerTownCache.getInstance().saveCache(data);
                    return data;
                } catch (IOException exp) {
                    throw new IOException("There was a problem processing the data", exp);
                }
            };
            final Future<List<Covid19DataPerTown>> future = executor.submit(callable);
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException("There was a problem processing the data", e);
            } finally {

                    executor.shutdown();

            }
        }

        /**
         * Make sure un-parsable data get reasonable defaults
         *
         * @param parts Tokenized array
         */
        private static void sanitizeData(String... parts) {
            for (int i = 0; i < parts.length; i++) {
                switch (CSVPartsVals[i]) {
                    case Town:
                        break;
                    case LastUpdateDate:
                        if ("".equals(parts[i])) {
                            parts[i] = "06/06/1966";
                        }
                        break;
                    default:
                        if ("".equals(parts[i])) {
                            parts[i] = "0";
                        }
                }
            }
        }

        /**
         * Print COVID-19 town data, based on a criteria
         * @param data Full data
         * @param predicate Filter criteria
         */
        public static void printCovidData(
                final List<Covid19DataPerTown> data, final Predicate<Covid19DataPerTown> predicate) {
            data.stream()
                    .filter(predicate)
                    .forEach(covid19DataPerTown -> LOG.log(Level.INFO, covid19DataPerTown.toString()));
        }

    }

    @Version(number=2, author="Jose Vicente Nunez Zuleta")
    static class Covid19DataPerTownCache implements Serializable {

        static final long serialVersionUID = 60L;

        final public static int TOO_OLD_DAYS = 1;
        final public static Path CACHE_LOCATION = Paths.get(
                System.getProperty("user.home", "/home/josevnz"),
                "Downloads",
                "covid19_ct.ser");
        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        final List<Covid19DataPerTown> data;
        LocalDateTime dateTime;

        private static final Covid19DataPerTownCache cdptc = new Covid19DataPerTownCache();

        private Covid19DataPerTownCache() {
            data = new ArrayList<>();
            dateTime = LocalDateTime.now();
        }

        public Covid19DataPerTownCache(Covid19DataPerTownCache copy) {
            data = new ArrayList<>(copy.data);
            dateTime = LocalDateTime.from(copy.dateTime);
        }

        private Object readResolve() throws ObjectStreamException {
            return new Covid19DataPerTownCache(this);
        }

        /**
         * Singleton
         * @return Same instance of the cache
         */
        public static Covid19DataPerTownCache getInstance() {
            return cdptc;
        }

        /**
         * Confirm if data in cache is too old to be trusted
         * @param days Number of days threshold
         * @return True if days condition is met
         */
        public boolean isTooOld(int days) {
            try {
                readWriteLock.readLock().lock();
                return Duration.between(LocalDateTime.now(), dateTime).toDays() >= days;
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * Save processed COVID-19 town data to disk
         * @param fullData Data to be saved in the serialized cache, for later use?
         */
        public void saveCache(List<Covid19DataPerTown> fullData) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_LOCATION.toFile()))) {
                if (Files.notExists(CACHE_LOCATION.getParent())) {
                    Files.createDirectories(CACHE_LOCATION.getParent());
                }
                try {
                    readWriteLock.writeLock().lock();
                    if (fullData.isEmpty()) {
                        return;
                    }
                    data.clear();
                    data.addAll(fullData);
                    dateTime = LocalDateTime.now();
                } finally {
                    readWriteLock.writeLock().unlock();
                }
                oos.writeObject(this);
                oos.flush();
                LOG.log(Level.INFO, String.format("Saved copy of data to %s", CACHE_LOCATION));
            } catch (FileNotFoundException e) {
                LOG.log(Level.SEVERE, String.format("Could not save to location, missing directory? %s", CACHE_LOCATION), e);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, String.format("Could not save to location %s", CACHE_LOCATION), e);
            }
        }

        /**
         * Load the COVID-19 town data from disk (faster than network download!)
         */
        public void loadFromCache(List<Covid19DataPerTown> data) {
            if (Files.exists(CACHE_LOCATION)) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CACHE_LOCATION.toFile()))) {
                    final Covid19DataPerTownCache cache = (Covid19DataPerTownCache) ois.readObject();
                    if (cache.isTooOld(TOO_OLD_DAYS)) {
                        LOG.log(Level.WARNING, String.format("Data from cache is too old %s", CACHE_LOCATION));
                    } else {
                        try {
                            readWriteLock.writeLock().lock();
                            data.clear();
                            data.addAll(cache.data);
                        } finally {
                            readWriteLock.writeLock().lock();
                        }
                    }
                } catch (InvalidClassException iCe) {
                    LOG.log(Level.SEVERE, String.format("Cannot re-use cache: %s", CACHE_LOCATION), iCe);
                } catch (IOException | ClassNotFoundException ioExp) {
                    LOG.log(Level.SEVERE, String.format("Could not load data: %s", CACHE_LOCATION), ioExp);
                }
            }
        }

    }




}
