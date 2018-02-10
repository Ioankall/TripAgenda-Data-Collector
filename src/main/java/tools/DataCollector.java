package tools;

import facebook4j.FacebookException;
import aggregator.AggregatorGuide;
import components.City;
import facebookIntegration.FacebookDataCollector;
import foursquareIntegration.FoursquareDataCollector;
import mongoDatabase.DatabaseManager;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataCollector {

    public static void main(String[] args) throws FacebookException, InterruptedException, JSONException {

        long startTime = System.nanoTime();

        try {
            DatabaseManager.getInstance().databaseCheck();
        } catch (Exception ex) {
            System.out.println("Database not running.");
            System.out.println(ex);
            return;
        }

        List<City> supportedCities = DatabaseManager.getInstance().getSupportedCitiesFromDatabase();



        if (args.length == 0) {
            for (City city : supportedCities) {
                updateCity(city);

                System.out.println("Pausing process for 60 minutes, before continuing to next city.");
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                System.out.println("Current time: " + sdf.format(cal.getTime()));
                TimeUnit.MINUTES.sleep(60);
            }
        } else if (args.length == 1) {
            boolean argumentFound = false;
            for (City city : supportedCities) {
                if (city.getCityName().compareTo(args[0]) == 0) {
                    updateCity(city);
                    argumentFound = true;
                    break;
                }
            }
            if (!argumentFound) {
                System.out.println("The requested city does not exist in database.");
            }
        }

        long endTime = System.nanoTime();

        System.out.println("Process completed in " + (endTime - startTime)/1000000000 + " sec");
    }

    private static void updateCity(City city) throws FacebookException, InterruptedException, JSONException {
        long startTime;
        long endTime;

        System.out.println("Data collection for city '" + city.getCityName() + "' has been started.");

        //Facebook scrapping
        startTime = System.nanoTime();
        System.out.println("Information retrieval from Facebook has been started.");
        FacebookDataCollector facebookDataCollector = new FacebookDataCollector();
        int numOfVenuesExtractedFromFacebook = facebookDataCollector.scrap(city.getLatitude(), city.getLongitude(), 50000);
        endTime = System.nanoTime();
        System.out.println("Information about " + numOfVenuesExtractedFromFacebook + " venues has been collected from Facebook.");
        System.out.println("Information retrieval from Facebook took "+ (endTime - startTime)/1000000000 + " seconds.");

        // Foursquare scrapping
        startTime = System.nanoTime();
        System.out.println("Information retrieval from Foursquare has been started.");
        FoursquareDataCollector foursquareDataCollector = new FoursquareDataCollector(facebookDataCollector.getAllFacebookVenues());
        int numOfVenuesExtractedFromFoursquare = foursquareDataCollector.scrap();
        endTime = System.nanoTime();
        System.out.println("Information about " + numOfVenuesExtractedFromFoursquare + " venues has been collected from Foursquare.");
        System.out.println("Information retrieval from Foursquare took " + (endTime - startTime)/1000000000 + " seconds.");

        // Aggregating information from Facebook and Foursquare.
        // Verifying information with Google information
        startTime = System.nanoTime();
        System.out.println("Information aggregation between Facebook's and Foursquare's data has started.");
        AggregatorGuide aggregator = new AggregatorGuide(facebookDataCollector.getAllFacebookVenues(), foursquareDataCollector.getAllFoursquareVenues());
        aggregator.setOverideGoogle(true);
        aggregator.aggregateFaceookAndFoursquare();
        System.out.println("The 1st stage of aggregation process has been completed.");
        System.out.println( aggregator.getFbFsNumOfPairs() + " venues have been matched between Fb and Fs");
        System.out.println("Data collection from Google has begun.");
        aggregator.getAndAggregateGooglePlaces();
        System.out.println("Data collection from Google has finished. The verification process has started.");

//        System.out.println("Pausing process for 60 minutes.");
//        Calendar cal = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//        System.out.println("Current time: " + sdf.format(cal.getTime()));
//        TimeUnit.MINUTES.sleep(60);

        System.out.println("The process has been continued. Finalizing data aggregation.");
        foursquareDataCollector.requestForAdditionalInformation(aggregator.getCompleteMatches());
        aggregator.updateFoursquarePlaces(foursquareDataCollector.getAllFoursquareVenues());
        aggregator.completeDataAggregation();
        System.out.println("Data aggregation has been completed successfully.");
        System.out.println(aggregator.getAggregatedVenues().size() + " venues has been extracted");
        endTime = System.nanoTime();
        System.out.println("Data aggregation and verification took " + (endTime - startTime)/1000000000 + " seconds.");

        System.out.println("Storing data in database...");
        DatabaseManager.getInstance().writeAggregatedVenuesToDatabase(city.getCityName(), aggregator.getAggregatedVenues());
        System.out.println("Data has been saved successfully.");
    }
}
