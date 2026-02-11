package com.healthmetrics.tracker.config;

import com.healthmetrics.tracker.entity.DataValue;
import com.healthmetrics.tracker.entity.Facility;
import com.healthmetrics.tracker.entity.HealthIndicator;
import com.healthmetrics.tracker.repository.DataValueRepository;
import com.healthmetrics.tracker.repository.FacilityRepository;
import com.healthmetrics.tracker.repository.HealthIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Database seeder that initializes the application with sample data.
 * This class implements ApplicationRunner, which means it runs automatically
 * when the Spring Boot application starts
 */
@Component
@RequiredArgsConstructor
@Transactional
public class DataSeeder implements ApplicationRunner {

    /// Logger for tracking seeding operations
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    /// Injected repositories for database operations
    private final FacilityRepository facilityRepository;
    private final HealthIndicatorRepository indicatorRepository;
    private final DataValueRepository dataValueRepository;

    /// Random number generator for creating realistic test data
    private final Random random = new Random();

    /**
     * Main execution method called by Spring Boot on application startup.
     *
     * @param args Application arguments (not used here)
     */
    @Override
    public void run(ApplicationArguments args) {
        /// Only seed if database is empty
        if (facilityRepository.count() == 0) {
            logger.info("Database is empty. Starting data seeding process...");

            /// Seed in order: Facilities -> Indicators -> Data Values
            /// This order matters because DataValues depend on both Facilities and Indicators
            List<Facility> facilities = seedFacilities();
            List<HealthIndicator> indicators = seedHealthIndicators();
            seedDataValues(facilities, indicators);

            logger.info("Data seeding completed successfully!");
        } else {
            logger.info("Database already contains data. Skipping seeding process.");
        }
    }

    /**
     * Seeds the database with sample health facilities across Greece.
     * Creates a diverse set of hospitals, clinics, and health centers
     * in different regions.
     *
     * @return List of created facilities for use in data value seeding
     */
    private List<Facility> seedFacilities() {
        logger.info("Seeding facilities...");

        List<Facility> facilities = new ArrayList<>();

        // Athens Region - Major hospitals and health centers
        facilities.add(createFacility(
                "FAC001", "Athens General Hospital", "Hospital",
                "Attica", "Athens", 37.9838, 23.7275, true
        ));

        facilities.add(createFacility(
                "FAC002", "Piraeus Health Center", "Health Center",
                "Attica", "Piraeus", 37.9420, 23.6467, true
        ));

        facilities.add(createFacility(
                "FAC003", "Glyfada Medical Clinic", "Clinic",
                "Attica", "Glyfada", 37.8653, 23.7539, true
        ));

        // Thessaloniki Region - Northern Greece facilities
        facilities.add(createFacility(
                "FAC004", "Thessaloniki General Hospital", "Hospital",
                "Central Macedonia", "Thessaloniki", 40.6401, 22.9444, true
        ));

        facilities.add(createFacility(
                "FAC005", "Kalamaria Health Center", "Health Center",
                "Central Macedonia", "Kalamaria", 40.5825, 22.9475, true
        ));

        // Patras Region - Western Greece
        facilities.add(createFacility(
                "FAC006", "Patras University Hospital", "Hospital",
                "Western Greece", "Patras", 38.2466, 21.7346, true
        ));

        facilities.add(createFacility(
                "FAC007", "Agrinio Health Center", "Health Center",
                "Western Greece", "Agrinio", 38.6214, 21.4079, true
        ));

        // Heraklion Region - Crete
        facilities.add(createFacility(
                "FAC008", "Heraklion University Hospital", "Hospital",
                "Crete", "Heraklion", 35.3387, 25.1442, true
        ));

        facilities.add(createFacility(
                "FAC009", "Chania General Hospital", "Hospital",
                "Crete", "Chania", 35.5138, 24.0180, true
        ));

        facilities.add(createFacility(
                "FAC010", "Rethymno Health Center", "Health Center",
                "Crete", "Rethymno", 35.3662, 24.4824, true
        ));

        // Larissa Region - Central Greece
        facilities.add(createFacility(
                "FAC011", "Larissa General Hospital", "Hospital",
                "Thessaly", "Larissa", 39.6390, 22.4191, true
        ));

        facilities.add(createFacility(
                "FAC012", "Volos Medical Clinic", "Clinic",
                "Thessaly", "Volos", 39.3617, 22.9444, true
        ));

        // Ioannina Region - Northwestern Greece
        facilities.add(createFacility(
                "FAC013", "Ioannina University Hospital", "Hospital",
                "Epirus", "Ioannina", 39.6650, 20.8537, true
        ));

        // Rhodes Region - Dodecanese Islands
        facilities.add(createFacility(
                "FAC014", "Rhodes General Hospital", "Hospital",
                "South Aegean", "Rhodes", 36.4341, 28.2176, true
        ));

        facilities.add(createFacility(
                "FAC015", "Kos Health Center", "Health Center",
                "South Aegean", "Kos", 36.8933, 27.2906, true
        ));

        // Inactive facility for testing purposes
        facilities.add(createFacility(
                "FAC099", "Closed Facility - Historical Data Only", "Clinic",
                "Attica", "Athens", 37.9838, 23.7275, false
        ));

        // Save all facilities to database
        facilities = facilityRepository.saveAll(facilities);
        logger.info("Successfully seeded {} facilities", facilities.size());

        return facilities;
    }

    /**
     * Helper method to create a Facility entity with all required fields.
     * This reduces code duplication and ensures consistency.
     *
     * @param code Unique facility code
     * @param name Facility name
     * @param type Type of facility (Hospital, Clinic, Health Center)
     * @param region Administrative region
     * @param district District within region
     * @param latitude GPS latitude coordinate
     * @param longitude GPS longitude coordinate
     * @param active Whether facility is currently operational
     * @return Configured Facility entity (not yet saved to database)
     */
    private Facility createFacility(String code, String name, String type,
                                    String region, String district,
                                    Double latitude, Double longitude,
                                    Boolean active) {
        Facility facility = new Facility();
        facility.setCode(code);
        facility.setName(name);
        facility.setType(type);
        facility.setRegion(region);
        facility.setDistrict(district);
        facility.setLatitude(latitude);
        facility.setLongitude(longitude);
        facility.setActive(active);
        // createdAt and updatedAt are set automatically by JPA auditing
        return facility;
    }

    /**
     * Seeds the database with health indicators covering various health domains.
     * Indicators are categorized and have different data types (numbers, percentages, booleans).
     *
     * @return List of created indicators for use in data value seeding
     */
    private List<HealthIndicator> seedHealthIndicators() {
        logger.info("Seeding health indicators...");

        List<HealthIndicator> indicators = new ArrayList<>();

        // Disease Surveillance Indicators
        indicators.add(createIndicator(
                "IND001", "Malaria Cases", "Disease Surveillance",
                "Total confirmed malaria cases reported", "NUMBER", "cases", true
        ));

        indicators.add(createIndicator(
                "IND002", "Tuberculosis Cases", "Disease Surveillance",
                "New tuberculosis cases diagnosed", "NUMBER", "cases", true
        ));

        indicators.add(createIndicator(
                "IND003", "COVID-19 Cases", "Disease Surveillance",
                "Confirmed COVID-19 positive cases", "NUMBER", "cases", true
        ));

        // Child Health Indicators
        indicators.add(createIndicator(
                "IND004", "Child Vaccination Coverage", "Child Health",
                "Percentage of children fully vaccinated", "PERCENTAGE", "%", true
        ));

        indicators.add(createIndicator(
                "IND005", "Child Malnutrition Cases", "Child Health",
                "Number of children diagnosed with malnutrition", "NUMBER", "cases", true
        ));

        indicators.add(createIndicator(
                "IND006", "Under-5 Mortality Rate", "Child Health",
                "Deaths per 1000 live births for children under 5", "NUMBER", "per 1000", true
        ));

        // Maternal Health Indicators
        indicators.add(createIndicator(
                "IND007", "Antenatal Care Visits", "Maternal Health",
                "Number of antenatal care visits by pregnant women", "NUMBER", "visits", true
        ));

        indicators.add(createIndicator(
                "IND008", "Facility-Based Deliveries", "Maternal Health",
                "Number of births occurring in health facilities", "NUMBER", "births", true
        ));

        indicators.add(createIndicator(
                "IND009", "Maternal Mortality Ratio", "Maternal Health",
                "Maternal deaths per 100,000 live births", "NUMBER", "per 100,000", true
        ));

        // Service Delivery Indicators
        indicators.add(createIndicator(
                "IND010", "Outpatient Visits", "Service Delivery",
                "Total number of outpatient consultations", "NUMBER", "visits", true
        ));

        indicators.add(createIndicator(
                "IND011", "Inpatient Admissions", "Service Delivery",
                "Total number of hospital admissions", "NUMBER", "admissions", true
        ));

        indicators.add(createIndicator(
                "IND012", "Emergency Room Visits", "Service Delivery",
                "Total number of emergency room visits", "NUMBER", "visits", true
        ));

        // Quality Indicators
        indicators.add(createIndicator(
                "IND013", "Patient Satisfaction Rate", "Quality of Care",
                "Percentage of patients satisfied with services", "PERCENTAGE", "%", true
        ));

        indicators.add(createIndicator(
                "IND014", "Average Waiting Time", "Quality of Care",
                "Average waiting time for consultation in minutes", "NUMBER", "minutes", true
        ));

        // Resource Indicators
        indicators.add(createIndicator(
                "IND015", "Bed Occupancy Rate", "Resources",
                "Percentage of hospital beds occupied", "PERCENTAGE", "%", true
        ));

        // Inactive indicator for testing
        indicators.add(createIndicator(
                "IND099", "Deprecated Indicator", "Other",
                "This indicator is no longer collected", "NUMBER", "units", false
        ));

        // Save all indicators to database
        indicators = indicatorRepository.saveAll(indicators);
        logger.info("Successfully seeded {} health indicators", indicators.size());

        return indicators;
    }

    /**
     * Helper method to create a HealthIndicator entity with all required fields.
     *
     * @param code Unique indicator code
     * @param name Indicator name
     * @param category Health domain category
     * @param description Detailed description of what the indicator measures
     * @param dataType Type of data (NUMBER, PERCENTAGE, BOOLEAN)
     * @param unit Unit of measurement
     * @param active Whether indicator is currently in use
     * @return Configured HealthIndicator entity (not yet saved to database)
     */
    private HealthIndicator createIndicator(String code, String name, String category,
                                            String description, String dataType,
                                            String unit, Boolean active) {
        HealthIndicator indicator = new HealthIndicator();
        indicator.setCode(code);
        indicator.setName(name);
        indicator.setCategory(category);
        indicator.setDescription(description);
        indicator.setDataType(dataType);
        indicator.setUnit(unit);
        indicator.setActive(active);
        return indicator;
    }

    /**
     * Seeds the database with realistic data values for the past 6 months.
     * Creates monthly data entries for each active facility and indicator combination.
     * Values are randomly generated but within realistic ranges for each indicator type.
     *
     * This simulates historical data collection similar to what would exist
     * in a production health metrics system.
     *
     * @param facilities List of seeded facilities
     * @param indicators List of seeded health indicators
     */
    private void seedDataValues(List<Facility> facilities, List<HealthIndicator> indicators) {
        logger.info("Seeding data values...");

        List<DataValue> dataValues = new ArrayList<>();

        // Only create data for active facilities and indicators
        List<Facility> activeFacilities = facilities.stream()
                .filter(Facility::getActive)
                .toList();

        List<HealthIndicator> activeIndicators = indicators.stream()
                .filter(HealthIndicator::getActive)
                .toList();

        // Generate data for the past 6 months
        LocalDate today = LocalDate.now();
        int monthsOfData = 6;

        for (int monthOffset = 0; monthOffset < monthsOfData; monthOffset++) {
            // Calculate period dates (monthly periods)
            LocalDate periodStart = today.minusMonths(monthOffset).withDayOfMonth(1);
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

            // Create data for each facility and indicator combination
            for (Facility facility : activeFacilities) {
                for (HealthIndicator indicator : activeIndicators) {
                    // Generate realistic value based on indicator type and category
                    BigDecimal value = generateRealisticValue(indicator, facility.getType());

                    DataValue dataValue = new DataValue();
                    dataValue.setFacility(facility);
                    dataValue.setIndicator(indicator);
                    dataValue.setPeriodStart(periodStart);
                    dataValue.setPeriodEnd(periodEnd);
                    dataValue.setPeriodType("MONTHLY");
                    dataValue.setValue(value);
                    dataValue.setComment(generateComment(indicator, value));
                    dataValue.setCreatedBy("SYSTEM_SEEDER");

                    dataValues.add(dataValue);
                }
            }
        }

        // Save all data values in batches for better performance
        dataValueRepository.saveAll(dataValues);
        logger.info("Successfully seeded {} data values", dataValues.size());
    }

    /**
     * Generates realistic values based on indicator type and facility type.
     * Uses different ranges for hospitals (higher capacity) vs clinics/health centers.
     *
     * @param indicator The health indicator being measured
     * @param facilityType Type of facility (Hospital, Clinic, Health Center)
     * @return Realistic value for the indicator
     */
    private BigDecimal generateRealisticValue(HealthIndicator indicator, String facilityType) {
        // Determine capacity multiplier based on facility type
        // Hospitals handle more cases than clinics or health centers
        double multiplier = switch (facilityType) {
            case "Hospital" -> 3.0;
            case "Clinic" -> 1.5;
            case "Health Center" -> 1.0;
            default -> 1.0;
        };

        // Generate values based on data type
        return switch (indicator.getDataType()) {
            case "PERCENTAGE" -> {
                // Percentages between 60-95% for quality indicators
                double baseValue = 60 + random.nextDouble() * 35;
                yield BigDecimal.valueOf(Math.round(baseValue * 100) / 100.0);
            }
            case "NUMBER" -> {
                // Different ranges based on indicator category
                double baseValue = switch (indicator.getCategory()) {
                    case "Disease Surveillance" -> random.nextInt(50) * multiplier; // 0-150
                    case "Child Health" -> random.nextInt(30) * multiplier; // 0-90
                    case "Maternal Health" -> random.nextInt(100) * multiplier; // 0-300
                    case "Service Delivery" -> random.nextInt(1000) * multiplier; // 0-3000
                    case "Quality of Care" -> random.nextInt(60); // 0-60 (minutes)
                    case "Resources" -> random.nextInt(100); // 0-100
                    default -> random.nextInt(100) * multiplier;
                };
                yield BigDecimal.valueOf(Math.round(baseValue));
            }
            case "BOOLEAN" -> BigDecimal.valueOf(random.nextBoolean() ? 1 : 0);
            default -> BigDecimal.valueOf(random.nextInt(100));
        };
    }

    /**
     * Generates contextual comments for data values.
     * Adds metadata about notable trends or values.
     *
     * @param indicator The health indicator
     * @param value The recorded value
     * @return Descriptive comment about the data
     */
    private String generateComment(HealthIndicator indicator, BigDecimal value) {
        // Add comments for noteworthy values
        if (indicator.getDataType().equals("PERCENTAGE")) {
            double percentValue = value.doubleValue();
            if (percentValue > 90) {
                return "Excellent performance - above target";
            } else if (percentValue < 70) {
                return "Below target - needs attention";
            }
        }

        // For high case numbers in disease surveillance
        if (indicator.getCategory().equals("Disease Surveillance") && value.intValue() > 100) {
            return "High case load - monitoring closely";
        }

        return "Routine data collection";
    }
}