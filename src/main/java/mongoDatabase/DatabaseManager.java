package mongoDatabase;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import components.City;
import components.Venue;
import facebookIntegration.FacebookPlace;
import foursquareIntegration.FoursquarePlace;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseManager {

    private static DatabaseManager databaseManager;

    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
        }
        return databaseManager;
    }

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    //Database names
    private final String databaseName = "TripAgenda_test";
    private final String citiesCollection = "cities";
    private final String facebookPlacesCollection = "facebookPlaces";
    private final String foursquarePlacesCollection = "foursquarePlaces";

    private DatabaseManager() {
        mongoClient = new MongoClient();
        mongoDatabase = mongoClient.getDatabase(databaseName);
    }

    public void databaseCheck() {
        System.out.println("Checking database status..");

        if (mongoDatabase.getCollection("cities").count() == 0) {
            System.out.println("There aren't any cities in database. Inserting Athens..");
            ListIndexesIterable<Document> citiesIndexes = mongoDatabase.getCollection("cities").listIndexes();
            int numOfIndexes = 0;
            for (Document index : citiesIndexes) {
                numOfIndexes++;
                System.out.println(index.toString());
            }
            if (numOfIndexes == 0) {
                System.out.println("Creating indexes..");
                mongoDatabase.getCollection("cities").createIndex(new BasicDBObject("name", 1).append("unique", true));
            }
            insertAthensToDatabase();
        }

        List<City> cities = getSupportedCitiesFromDatabase();
        for (City city : cities) {
            if (mongoDatabase.getCollection(city.getCityName()).count() == 0) {
                System.out.println("No venues in database for city: " + city.getCityName());
                ListIndexesIterable<Document> citiesIndexes = mongoDatabase.getCollection(city.getCityName()).listIndexes();
                int numOfIndexes = 0;
                for (Document index : citiesIndexes) {
                    numOfIndexes++;
                    System.out.println(index.toString());
                }
                if (numOfIndexes == 0) {
                    System.out.println("Creating indexes..");
                    mongoDatabase.getCollection(city.getCityName()).createIndex(new BasicDBObject("location", "2d").append("facebookID", 1).append("unique", true));
                }
            }
        }


        if (mongoDatabase.getCollection("users").count() == 0) {
            System.out.println("No users in database.");
            ListIndexesIterable<Document> citiesIndexes = mongoDatabase.getCollection("users").listIndexes();
            int numOfIndexes = 0;
            for (Document index : citiesIndexes) {
                numOfIndexes++;
                System.out.println(index.toString());
            }
            if (numOfIndexes == 0) {
                System.out.println("Creating indexes..");
                mongoDatabase.getCollection("users").createIndex(new BasicDBObject("username", 1).append("unique", true));
            }
        }
    }

    public List<City> getSupportedCitiesFromDatabase(){
        List<City> supportedCities = new ArrayList<>();

        MongoCollection<Document> collectionOfCities = mongoDatabase.getCollection(citiesCollection);
        FindIterable<Document> citiesIterable = collectionOfCities.find();

        try{
            for(Document doc: citiesIterable) {
                City city = new City(doc.get("city").toString(), Double.parseDouble(doc.get("latitude").toString()), Double.parseDouble(doc.get("longitude").toString()));
                supportedCities.add(city);
            }
        }catch(Exception ex){
            System.out.println(ex);
            return null;
        }

        return supportedCities;
    }

    public boolean insertCityToDatabase(City newCity){
        MongoCollection<Document> collectionOfCities = mongoDatabase.getCollection(citiesCollection);
        collectionOfCities.insertOne(newCity.getDocument());
        return true;
    }

    public boolean writeFacebookPlacesToDatabase(TreeMap<String,FacebookPlace> facebookPlaces) {
        for(String k: facebookPlaces.keySet()){
            Document result = mongoDatabase.getCollection(facebookPlacesCollection).find(eq("id", facebookPlaces.get(k).getId())).first();

            if (result == null) {
                mongoDatabase.getCollection(facebookPlacesCollection).insertOne(facebookPlaces.get(k).createDocument());
            } else {
                mongoDatabase.getCollection(facebookPlacesCollection).updateOne(new Document("_id", result.get("_id")), new Document("$set", result));
            }
        }
        return true;
    }
    public TreeMap<String,FacebookPlace> getFacebookPlacesFromDatabase(){
        TreeMap<String,FacebookPlace> facebookPlaces = new TreeMap<>();

        FindIterable<Document> results = mongoDatabase.getCollection(facebookPlacesCollection).find();

        for(Document d: results) {
            FacebookPlace p = new FacebookPlace();

            p.setId(d.get("id").toString());
            p.setName(d.get("name").toString());

            try {
                p.setAddress(d.get("address").toString());
            }catch(Exception e){}

            try{
                p.setPostalCode(d.get("postalCode").toString());
            }catch(Exception e){}

            try{
                p.setCity(d.get("city").toString());
            }catch(Exception e){}

            try{
                p.setCountry(d.get("country").toString());
            }catch(Exception e){}

            try{
                p.setPhoneNumber(d.get("phoneNumber").toString());
            }catch(Exception e){}

            try{
                p.setUrl(d.get("url").toString());
            }catch(Exception e){}

            try{
                p.setLatitude(Double.parseDouble(d.get("latitude").toString()));
            }catch(Exception e){}

            try{
                p.setLongitude(Double.parseDouble(d.get("longitude").toString()));
            }catch(Exception e){}

            try{
                p.setGeneralCategory(d.get("generalCategory").toString());
            }catch(Exception e){}

            try{
                p.setNumOfLikes(Integer.parseInt(d.get("numOfLikes").toString()));
            }catch(Exception e){}

            try{
                p.setNumOfCheckins(Integer.parseInt(d.get("numOfCheckins").toString()));
            }catch(Exception e){}

            try{
                List<String> list = new ArrayList<>();
                Document m = (Document) d.get("categories");
                for(int j=0; j<Integer.parseInt(m.get("numOfCategories").toString()); j++){
                    String key = "category_"+Integer.toString(j);
                    list.add(m.get(key).toString());
                }
                p.setCategories(list);
            }catch(Exception e){}


            facebookPlaces.put(p.getId(), p);
        }

        return facebookPlaces;
    }
    public boolean writeFoursquarePlacesToDatabase(TreeMap<String,FoursquarePlace> foursquarePlaces){
        for (String k : foursquarePlaces.keySet()) {
            Document result = mongoDatabase.getCollection(foursquarePlacesCollection).find(eq("id", foursquarePlaces.get(k).getId())).first();

            if(result == null){
                mongoDatabase.getCollection(foursquarePlacesCollection).insertOne(foursquarePlaces.get(k).createDocument());
            }else{
                mongoDatabase.getCollection(foursquarePlacesCollection).updateOne(new Document("_id", result.get("_id")), new Document("$set", result));
            }
        }
        return true;
    }
    public TreeMap<String,FoursquarePlace> getFoursquarePlacesFromDatabase(){
        TreeMap<String, FoursquarePlace> foursquarePlaces = new TreeMap<>();

        FindIterable<Document> results = mongoDatabase.getCollection(foursquarePlacesCollection).find();

        for(Document d: results) {
            FoursquarePlace p = new FoursquarePlace();

            p.setId(d.get("id").toString());
            p.setName(d.get("name").toString());

            try {
                p.setAddress(d.get("address").toString());
            }catch(Exception e){}

            try{
                p.setPostalCode(d.get("postalCode").toString());
            }catch(Exception e){}

            try{
                p.setCity(d.get("city").toString());
            }catch(Exception e){}

            try{
                p.setCountry(d.get("country").toString());
            }catch(Exception e){}

            try{
                p.setPhoneNumber(d.get("phoneNumber").toString());
            }catch(Exception e){}

            try{
                p.setUrl(d.get("url").toString());
            }catch(Exception e){}

            try{
                p.setGeneralCategory(d.get("generalCategory").toString());
            }catch(Exception e){}

            try{
                List<String> list = new ArrayList<>();
                Document m = (Document) d.get("categories");
                for(int j=0; j<Integer.parseInt(m.get("numOfCategories").toString()); j++){
                    String key = "category_"+Integer.toString(j);
                    list.add(m.get(key).toString());
                }
                p.setCategories(list);
            }catch(Exception e){}

            foursquarePlaces.put(p.getId(), p);
        }
        return foursquarePlaces;
    }

    public boolean writeAggregatedVenuesToDatabase(String cityName, List<Venue> venues) {
        for (Venue venue : venues) {
            Document doc = venue.createDocument();
            Document result = mongoDatabase.getCollection(cityName).find(eq("facebookID", venue.getFacebookId())).first();
            if (result == null || result.isEmpty()) {
                mongoDatabase.getCollection(cityName).insertOne(doc);
            } else {
                mongoDatabase.getCollection(cityName).updateOne(new Document("_id", result.get("_id")), doc);
            }
        }
        return true;
    }

    public void insertAthensToDatabase(){
        City athens = new City("Athens", 37.983917, 23.729360);
        insertCityToDatabase(athens);
    }

}

