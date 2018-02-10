package googleIntegration;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;
import se.walkercrou.places.Review;
import utilities.StringComparator;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class GoogleDataCollector {

    private GooglePlaces client;
    private final String googleAPIKey = "AIzaSyB0lPZ7U-xbvpfCUbE1SccTPAv7rCMpFOA";
    private static TreeMap<String, GooglePlace> googlePlaces;
    public static TreeMap<String, GooglePlace> getGooglePlaces() { return googlePlaces; }

    public GoogleDataCollector () {
        googlePlaces = new TreeMap<>();
        client = new GooglePlaces(googleAPIKey);
    }

    public String searchForPlaceInGoogleDatabase(String foursquareName, String facebookName, String location){
        int radius = 1000;

        List<Place> places;
        try{
            places = client.getPlacesByQuery(foursquareName, Param.name("location").value(location), Param.name("radius").value(radius));
        }catch(Exception ex){
            System.out.println(ex);
            places = null;
        }

        if (places != null && !places.isEmpty()) {
            for(Place p: places){
                if(StringComparator.compareStrings(p.getName(), foursquareName) > 0.7 || StringComparator.compareStrings(p.getName(), facebookName) > 0.7 ) {
                    GooglePlace googlePlace = new GooglePlace();
                    try{
                        Place placeWithDetails = p.getDetails();

                        googlePlace.setId(placeWithDetails.getPlaceId());
                        googlePlace.setName(placeWithDetails.getName());
                        googlePlace.setPhoneNumber(placeWithDetails.getPlaceId());
                        googlePlace.setUrl(placeWithDetails.getWebsite());
                        googlePlace.setGoogleUrl(placeWithDetails.getGoogleUrl());
                        googlePlace.setAddress(placeWithDetails.getAddress());
                        googlePlace.setVicinity(placeWithDetails.getVicinity());

                        List<Review> tempListOfReviews = placeWithDetails.getReviews();
                        ArrayList<String> reviews = new ArrayList<>();

                        if (!tempListOfReviews.isEmpty()) {
                            int count = 0;
                            int sum = 0;
                            double rating;
                            for(Review r: tempListOfReviews) {
                                reviews.add(r.toString());
                                count++;
                                sum += r.getRating();
                            }
                            rating = sum/count;
                            googlePlace.setReviews(reviews);
                            googlePlace.setNumOfReviews(count);
                            googlePlace.setRating(rating);
                        }

                        googlePlaces.put(googlePlace.getId(), googlePlace);
                        return googlePlace.getId();

                    }catch(Exception ex){
                        System.out.println("Error in getting details from google! \n" + ex);
                        return null;
                    }

                }
            }
        }
        return null;
    }


}
