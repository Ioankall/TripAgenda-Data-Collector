package facebookIntegration;

import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import facebook4j.FacebookException;
import java.util.TreeMap;
import org.bson.Document;
import utilities.StringComparator;

public class FacebookDataCollector {

    private TreeMap<String, FacebookPlace> venues;
    public TreeMap<String, FacebookPlace> getAllFacebookVenues(){ return venues; }

    //Keywords extracted from Facebook supported categories, separated in groups of more general categories
    private final String[] public_places = {"City", "Airport", "Terminal", "Beach", "Public", "Place", "Landmark", "Landscaping", "Lake", "Mountain", "Park", "Tours", "Sightseeing"};
    private final String[] history_and_art = {"Historical", "Monument", "Museum", "Church", "Art", "Library", "Gallery", "Archaeological"};
    private final String[] culture_and_sports = {"Concert", "Venue", "Sports", "Stadium", "Movie", "Attraction", "Zoo", "Aquarium"};
    private final String[] food_and_drink = {"Bar", "Night", "Club", "Restaurant", "Lounge", "Pub", "Tea", "Nightlife", "Cafe", "Diner", "Entertainment", "Food", "Burger", "Breakfast", "Desert", "Club", "Bakery", "Pizza", "Salad", "Juice"};
    private final String[] shopping = {"Shopping", "Mall", "Retail", "Clothing", "Outlet"};
    private final String[] accomondation_and_personal_care = {"Gym", "Spa", "Pool",  "Hotel", "Motel", "Lodging"};

    //Used to extract info from Facebook's API. Creates one thread per category.
    public int scrap (double latitude, double longitude, int radius) throws FacebookException, InterruptedException {
        FacebookScrapper [] facebookScrappers = {
            new FacebookScrapper(food_and_drink, latitude, longitude, radius),
            new FacebookScrapper(shopping, latitude, longitude, radius),
            new FacebookScrapper(accomondation_and_personal_care, latitude, longitude, radius),
            new FacebookScrapper(culture_and_sports, latitude, longitude, radius),
            new FacebookScrapper(history_and_art, latitude, longitude, radius),
            new FacebookScrapper(public_places, latitude, longitude, radius)
        };

        for (int i=0; i<facebookScrappers.length; i++) {
            facebookScrappers[i].start();
        }

        for (int i=0; i<facebookScrappers.length; i++) {
            facebookScrappers[i].join();
        }

        venues = facebookScrappers[0].getVenues();
        mergeDuplicates();

        return venues.size();
    }

    private void mergeDuplicates () {
        TreeMap<String, FacebookPlace> venuesNoDuplicates = new TreeMap<>();
        StringComparator cmp = new StringComparator();
        boolean duplicateFound;

        for (String k: venues.keySet()) {
            String name = venues.get(k)
                    .getName();
            duplicateFound = false;
            for (String d: venuesNoDuplicates.keySet()) {
                if (cmp.compareStrings(name, venuesNoDuplicates.get(d).getName())>0.8) {
                    venuesNoDuplicates.get(d).setNumOfCheckins(venuesNoDuplicates.get(d).getNumOfCheckins() + venues.get(k).getNumOfCheckins());
                    venuesNoDuplicates.get(d).setNumOfLikes(venuesNoDuplicates.get(d).getNumOfLikes() + venues.get(k).getNumOfLikes());
                    venuesNoDuplicates.get(d).setNumOfTalkingAboutCount(venuesNoDuplicates.get(d).getNumOfTalkingAboutCount() + venues.get(k).getNumOfTalkingAboutCount());
                    venuesNoDuplicates.get(d).setNumOfWereHereCount(venuesNoDuplicates.get(d).getNumOfWereHereCount() + venues.get(k).getNumOfWereHereCount());
                    duplicateFound = true;
                    break;
                }
            }
            if (!duplicateFound) {
                venuesNoDuplicates.put(k, venues.get(k));
            }
        }

        venues = venuesNoDuplicates;
    }
}
