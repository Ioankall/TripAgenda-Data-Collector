package foursquareIntegration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import components.PlacesPair;
import facebookIntegration.FacebookPlace;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;


public class FoursquareDataCollector {

    private final TreeMap<String, FacebookPlace> facebookVenues;

    private TreeMap<String, FoursquarePlace> venues;
    public TreeMap<String, FoursquarePlace> getAllFoursquareVenues() { return venues; }

    public FoursquareDataCollector (TreeMap<String, FacebookPlace> venuesExtractedFromFacebook) {
        facebookVenues = venuesExtractedFromFacebook;
    }

    public int scrap() throws InterruptedException{

        HashMap<String, String> setOfVenues1 = new HashMap<>();
        HashMap<String, String> setOfVenues2 = new HashMap<>();
        HashMap<String, String> setOfVenues3 = new HashMap<>();
        HashMap<String, String> setOfVenues4 = new HashMap<>();

        int numOfWord=0;
        for (String k: facebookVenues.keySet()) {
            String name = "";
            if (facebookVenues.get(k).getLatitude() != 0 && facebookVenues.get(k).getLongitude() != 0) {
                switch (numOfWord%4) {
                    case 0:
                        name = facebookVenues.get(k).getName().replace(" ", "%20");
                        setOfVenues1.put(name, String.valueOf(facebookVenues.get(k).getLatitude())+","+String.valueOf(facebookVenues.get(k).getLongitude()));
                        break;
                    case 1:
                        name = facebookVenues.get(k).getName().replace(" ", "%20");
                        setOfVenues2.put(name, String.valueOf(facebookVenues.get(k).getLatitude())+","+String.valueOf(facebookVenues.get(k).getLongitude()));
                        break;
                    case 2:
                        name = facebookVenues.get(k).getName().replace(" ", "%20");
                        setOfVenues3.put(name, String.valueOf(facebookVenues.get(k).getLatitude())+","+String.valueOf(facebookVenues.get(k).getLongitude()));
                        break;
                    default:
                        name = facebookVenues.get(k).getName().replace(" ", "%20");
                        setOfVenues4.put(name, String.valueOf(facebookVenues.get(k).getLatitude())+","+String.valueOf(facebookVenues.get(k).getLongitude()));
                        break;
                }
                numOfWord++;
            }
        }

        FoursquareScrapper [] foursquareScrappers = {
                new FoursquareScrapper(setOfVenues1),
                new FoursquareScrapper(setOfVenues2),
                new FoursquareScrapper(setOfVenues3),
                new FoursquareScrapper(setOfVenues4)
        };

        for (int i=0; i<foursquareScrappers.length; i++) {
            foursquareScrappers[i].start();
        }

        for (int i=0; i<foursquareScrappers.length; i++) {
            foursquareScrappers[i].join();
        }

        venues = foursquareScrappers[0].getVenues();
        return venues.size();
    }

    public void requestForAdditionalInformation(List<PlacesPair> completeMatches) throws JSONException {

        for ( PlacesPair pair : completeMatches ) {
            String foursquareId = pair.getFoursquarePlaceId();

            String query = "http://localhost/fsdetails.php?id=" + foursquareId;

            URL url;
            String response = "";
            int isNull = 0;
            do{
                try {
                    url = new URL(query);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                        for (String line; (line = reader.readLine()) != null;) {
                            response = response + line;
                        }
                    }catch (IOException ex) {
                        //Logger.getLogger(FoursquareScrapperThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (MalformedURLException ex) {
                    //Logger.getLogger(FoursquareScrapperThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(!response.startsWith("{")){
                    System.out.println(response);
                }
                if(response.startsWith("null")){
                    isNull++;
                }
            }while(response.isEmpty() || (response.startsWith("null") && isNull<3));


            if(!response.startsWith("{") || (response.startsWith("null") && isNull>2)){
                isNull = 0;
                return ;
            }

            JSONTokener tokener = new JSONTokener(response);
            JSONObject root = new JSONObject(tokener);
            JSONObject venue = root.getJSONObject("response").getJSONObject("venue");

            try {
                venues.get(foursquareId).setNumOfVisits(Integer.parseInt(venue.getJSONObject("stats").get("visitsCount").toString()));
            } catch (Exception ex) {
                System.out.println("Stats not found for venue " + foursquareId);
            }

            try {
                venues.get(foursquareId).setNumOfLikes(Integer.parseInt(venue.getJSONObject("likes").get("count").toString()));
            } catch (Exception ex) {
                System.out.println("Likes not found for venue " + foursquareId);
            }


            try{
                venues.get(foursquareId).setRating(Double.parseDouble(venue.get("rating").toString()));
                venues.get(foursquareId).setRatingSignals(Integer.parseInt(venue.get("ratingSignals").toString()));
            }catch(Exception ex){
                System.out.println("Ratings not found for venue " + foursquareId);
            }

            List<String> photos = new ArrayList<>();
            JSONArray groupsOfPhotos = venue.getJSONObject("photos").getJSONArray("groups");
            for (int i = 0; i < groupsOfPhotos.length(); i++){
                JSONArray items = groupsOfPhotos.getJSONObject(i).getJSONArray("items");
                for (int j = 0; j < items.length(); j++){
                    String pre = items.getJSONObject(j).get("prefix").toString();
                    String prefix = pre.replaceAll("\\/","");

                    String suf = items.getJSONObject(j).get("suffix").toString();
                    String suffix = suf.replaceAll("\\/","");

                    String width = items.getJSONObject(j).get("width").toString();
                    String height = items.getJSONObject(j).get("height").toString();

                    photos.add(pre+width+"x"+height+suf);
                }
            }
            venues.get(foursquareId).setPhotos(photos);

            List<String> tips = new ArrayList<>();
            JSONArray groupsOfTips = venue.getJSONObject("tips").getJSONArray("groups");
            for (int i = 0; i < groupsOfTips.length(); i++){
                JSONArray items = groupsOfTips.getJSONObject(i).getJSONArray("items");
                for (int j = 0; j < items.length(); j++){
                    String text = items.getJSONObject(j).get("text").toString();
                    tips.add(text);
                }
            }
            venues.get(foursquareId).setTips(tips);

            try{
                String tag = venue.get("tags").toString();
                tag = tag.replace("[", "");
                tag = tag.replace("]", "");
                char c = 34;
                tag = tag.replace(String.valueOf(c), "");
                String[] tags = tag.split(",");
                venues.get(foursquareId).setTags(tags);
            }catch(Exception ex){
                System.out.println("tags not found for venue " + foursquareId);
            }
        }
    }
}
