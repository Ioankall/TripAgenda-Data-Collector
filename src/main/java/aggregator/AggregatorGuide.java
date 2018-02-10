package aggregator;

import org.bson.Document;
import components.PlacesPair;
import components.Venue;
import facebookIntegration.FacebookPlace;
import foursquareIntegration.FoursquarePlace;
import googleIntegration.GoogleDataCollector;
import googleIntegration.GooglePlace;
import utilities.StringComparator;


import java.util.concurrent.TimeUnit;

import java.util.*;


public class AggregatorGuide {

    private static TreeMap<String, FacebookPlace> facebookPlaces;
    private static TreeMap<String, FoursquarePlace> foursquarePlaces;
    private static TreeMap<String, GooglePlace> googlePlaces;
    private static TreeMap<String,String> facebookAndFoursquarePairs;
    private static List<PlacesPair> completeMatches;
    private static List<Venue> aggregatedVenues;

    private boolean overideGoogle = false;
    public void setOverideGoogle(boolean o) { this.overideGoogle = o;}

    //Getter
    public List<Venue> getAggregatedVenues() { return aggregatedVenues; }
    public List<PlacesPair> getCompleteMatches() { return completeMatches; }
    public int getFbFsNumOfPairs() { return facebookAndFoursquarePairs.size(); }

    public AggregatorGuide(TreeMap<String, FacebookPlace> facebookPlaces, TreeMap<String, FoursquarePlace> foursquarePlaces) {
        this.facebookPlaces = facebookPlaces;
        this.foursquarePlaces = foursquarePlaces;
        aggregatedVenues = new ArrayList<>();
    }

    public void updateFoursquarePlaces(TreeMap<String, FoursquarePlace> foursquarePlaces) {
        this.foursquarePlaces = foursquarePlaces;
    }

    public void aggregateFaceookAndFoursquare() throws InterruptedException{
        TreeMap<String, HashSet<FacebookPlace>> facebookPlacesCategorized = new TreeMap<>();
        TreeMap<String, HashSet<FoursquarePlace>> foursquarePlacesCategorized = new TreeMap<>();

        for(String k: facebookPlaces.keySet()){
            String generalCategory = facebookPlaces.get(k).getGeneralCategory();
            String [] categories = generalCategory.split(", ");
            for(String c: categories){
                if(facebookPlacesCategorized.containsKey(c)){
                    HashSet<FacebookPlace> set = facebookPlacesCategorized.get(c);
                    set.add(facebookPlaces.get(k));
                    facebookPlacesCategorized.put(c, set);
                }else{
                    HashSet<FacebookPlace> set = new HashSet<>();
                    set.add(facebookPlaces.get(k));
                    facebookPlacesCategorized.put(c, set);
                }
            }
        }

        for(String k: foursquarePlaces.keySet()){
            String generalCategory = foursquarePlaces.get(k).getGeneralCategory();
            String [] categories = generalCategory.split(", ");
            for(String c: categories){
                if(foursquarePlacesCategorized.containsKey(c)){
                    HashSet<FoursquarePlace> set = foursquarePlacesCategorized.get(c);
                    set.add(foursquarePlaces.get(k));
                    foursquarePlacesCategorized.put(c, set);
                }else{
                    HashSet<FoursquarePlace> set = new HashSet<>();
                    set.add(foursquarePlaces.get(k));
                    foursquarePlacesCategorized.put(c, set);
                }
            }
        }

        HashMap<String, String> toCount = new HashMap<>();
        for(String k: facebookPlacesCategorized.keySet()){
            for(FacebookPlace p : facebookPlacesCategorized.get(k)){
                toCount.put(p.getId(), p.getName());
            }
        }

        Aggregator[] aggregatorThreads = {
                new Aggregator(facebookPlacesCategorized.get("Arts & Entertainment"), foursquarePlacesCategorized.get("Arts & Entertainment")),
                new Aggregator(facebookPlacesCategorized.get("Museums"), foursquarePlacesCategorized.get("Museums")),
                new Aggregator(facebookPlacesCategorized.get("Nightlife"), foursquarePlacesCategorized.get("Nightlife")),
                new Aggregator(facebookPlacesCategorized.get("Food & drink"), foursquarePlacesCategorized.get("Food & drink")),
                new Aggregator(facebookPlacesCategorized.get("Public places"), foursquarePlacesCategorized.get("Public places")),
                new Aggregator(facebookPlacesCategorized.get("Shopping"), foursquarePlacesCategorized.get("Shopping")),
                new Aggregator(facebookPlacesCategorized.get("Transportation & Accomondation"), foursquarePlacesCategorized.get("Transportation & Accomondation")),
                new Aggregator(facebookPlacesCategorized.get("Religion and Organizations"), foursquarePlacesCategorized.get("Religion and Organizations"))
        };
        for (int i = 0; i< aggregatorThreads.length; i++) {
            aggregatorThreads[i].start();
        }
        for (int i = 0; i< aggregatorThreads.length; i++) {
            aggregatorThreads[i].join();
        }

        facebookAndFoursquarePairs = aggregatorThreads[0].getMatches();
    }

    public void getAndAggregateGooglePlaces(){
        completeMatches = new ArrayList<>();

        if (!overideGoogle) {
            GoogleDataCollector google = new GoogleDataCollector();

            int count = 0;

            for(String fbId: facebookAndFoursquarePairs.keySet()){
                String fsId = facebookAndFoursquarePairs.get(fbId);

                String fbName = facebookPlaces.get(fbId).getName();
                String fsName = foursquarePlaces.get(fsId).getName();
                String location = facebookPlaces.get(fbId).getLatitude() + "," + facebookPlaces.get(fbId).getLongitude();

                if (count == 7) {
                    count = 0;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String googlePlaceId = google.searchForPlaceInGoogleDatabase(fsName, fbName, location);
                count++;

                if (googlePlaceId != null){
                    PlacesPair newPair = new PlacesPair(fbId, fsId, googlePlaceId);
                    completeMatches.add(newPair);
                }
            }

            googlePlaces = google.getGooglePlaces();
        } else {
            for(String fbId: facebookAndFoursquarePairs.keySet()) {
                String fsId = facebookAndFoursquarePairs.get(fbId);
                PlacesPair newPair = new PlacesPair(fbId, fsId, "");
                completeMatches.add(newPair);
            }
        }

    }

    public void completeDataAggregation(){
        for (PlacesPair pair : completeMatches) {
            Venue newVenue = new Venue();

            //Basic info
            newVenue.setName(foursquarePlaces.get(pair.getFoursquarePlaceId()).getName());
            newVenue.setLatitude(facebookPlaces.get(pair.getFacebookPlaceId()).getLatitude());
            newVenue.setLongitude(facebookPlaces.get(pair.getFacebookPlaceId()).getLongitude());
            newVenue.setAddress(foursquarePlaces.get(pair.getFoursquarePlaceId()).getAddress());
            newVenue.setCity(foursquarePlaces.get(pair.getFoursquarePlaceId()).getCity());
            newVenue.setCountry(foursquarePlaces.get(pair.getFoursquarePlaceId()).getCountry());
            newVenue.setGeneralCategory(foursquarePlaces.get(pair.getFoursquarePlaceId()).getGeneralCategory());
            newVenue.setPostalCode(facebookPlaces.get(pair.getFacebookPlaceId()).getPostalCode());
            newVenue.setFoursquareId(foursquarePlaces.get(pair.getFoursquarePlaceId()).getId());
            newVenue.setFacebookId(facebookPlaces.get(pair.getFacebookPlaceId()).getId());
            newVenue.setFoursquareRating(foursquarePlaces.get(pair.getFoursquarePlaceId()).getRating());
            newVenue.setFacebookLikes(facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfLikes());
            newVenue.setFacebookCheckins(facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfCheckins());
            newVenue.setFacebookTalkingAboutCount(facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfTalkingAboutCount());
            newVenue.setFacebookWereHereCount(facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfWereHereCount());
            newVenue.setFoursquareCheckins(foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfCheckins());
            newVenue.setFoursquareLikes(foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfLikes());
            newVenue.setFoursquareTips(foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfTips());
            newVenue.setFoursquareUsers(foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfUsers());
            newVenue.setFoursquareVisits(foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfVisits());
            newVenue.setPhotos(foursquarePlaces.get(pair.getFoursquarePlaceId()).getPhotos());


            //Popularity
            int popularityScore = facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfLikes()
                    + facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfCheckins()
                    + facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfWereHereCount()
                    + facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfTalkingAboutCount()
                    + foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfCheckins()
                    + foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfLikes()
                    + foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfTips()
                    + foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfVisits()
                    + foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfUsers();

            //Coming back ratio
            double comingBack = ((1.0 * facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfCheckins()/(1.0 * facebookPlaces.get(pair.getFacebookPlaceId()).getNumOfWereHereCount()))
                    + ((1.0 * foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfCheckins())/(1.0 * foursquarePlaces.get(pair.getFoursquarePlaceId()).getNumOfUsers())))/2;

            newVenue.setComingBackRatio(comingBack);
            newVenue.setPopularityScore(popularityScore);

            //Comments
            ArrayList<String> commentsList = new ArrayList<>();
            for(String str: foursquarePlaces.get(pair.getFoursquarePlaceId()).getTips()){
                commentsList.add(str);
            }
            newVenue.setComments(commentsList);

            //Rating
            double rating;
            double foursquareRating = foursquarePlaces.get(pair.getFoursquarePlaceId()).getRating();
            double googleRating;
            if (overideGoogle) {
                googleRating = 0;
            } else {
                googleRating = googlePlaces.get(pair.getGooglePlaceId()).getRating();
            }

            if(foursquareRating > 0 && googleRating > 0){
                rating = (foursquareRating + 2 * googleRating)/2;
            }else if(foursquareRating <= 0 && googleRating > 0){
                rating = 2*googleRating;
            }else if(googleRating <=0 && foursquareRating > 0){
                rating = foursquareRating;
            }else{
                rating = 0;
            }
            newVenue.setRating(rating);

            //Choose phone
            if(!overideGoogle && StringComparator.compareStrings(googlePlaces.get(pair.getGooglePlaceId()).getPhoneNumber(), foursquarePlaces.get(pair.getFoursquarePlaceId()).getPhoneNumber())
                    > StringComparator.compareStrings(facebookPlaces.get(pair.getFacebookPlaceId()).getPhoneNumber(), foursquarePlaces.get(pair.getFoursquarePlaceId()).getPhoneNumber())){
                newVenue.setPhoneNumber(googlePlaces.get(pair.getGooglePlaceId()).getPhoneNumber());
            }else{
                newVenue.setPhoneNumber(foursquarePlaces.get(pair.getFoursquarePlaceId()).getPhoneNumber());
            }

            //Choose website
            if(!overideGoogle && StringComparator.compareStrings(googlePlaces.get(pair.getGooglePlaceId()).getUrl(), foursquarePlaces.get(pair.getFoursquarePlaceId()).getUrl())
                    > StringComparator.compareStrings(facebookPlaces.get(pair.getFacebookPlaceId()).getUrl(), foursquarePlaces.get(pair.getFoursquarePlaceId()).getUrl())){
                newVenue.setPhoneNumber(googlePlaces.get(pair.getGooglePlaceId()).getUrl());
            }else{
                newVenue.setPhoneNumber(foursquarePlaces.get(pair.getFoursquarePlaceId()).getUrl());
            }

            //Categories
            String categories = "";
            Set<String> categoriesSet = new HashSet<>();
            for(String cat: facebookPlaces.get(pair.getFacebookPlaceId()).getCategories()){
                if(!categoriesSet.contains(cat)) categoriesSet.add(cat);
            }
            for(String cat: foursquarePlaces.get(pair.getFoursquarePlaceId()).getCategories()){
                if(!categoriesSet.contains(cat)) categoriesSet.add(cat);
            }
            for(String str: categoriesSet){
                if(categories.equals("")){
                    categories += str;
                }else{
                    categories += ", ";
                    categories += str;
                }
            }
            newVenue.setCategories(categories);

            //tags
            String tags = "";
            try{
                for(String str: foursquarePlaces.get(pair.getFoursquarePlaceId()).getTags()){
                    if(tags.equals("")){
                        tags += str;
                    }else{
                        tags += ", ";
                        tags += str;
                    }
                }
            }catch(Exception ex){
                System.out.println("NPE caught in tags");
            }
            newVenue.setTags(tags);

            aggregatedVenues.add(newVenue);
        }
    }

}
