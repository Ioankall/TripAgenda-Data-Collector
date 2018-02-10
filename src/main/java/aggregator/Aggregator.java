package aggregator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import org.bson.Document;
import facebookIntegration.FacebookPlace;
import foursquareIntegration.FoursquarePlace;
import utilities.StringComparator;


public class Aggregator extends Thread {

    private HashSet<FacebookPlace> facebookPlaces;
    private HashSet<FoursquarePlace> foursquarePlaces;
    private static TreeMap<String, String> matches;

    // Getters
    public static TreeMap<String, String> getMatches() { return matches; }

    public Aggregator(HashSet<FacebookPlace> fb, HashSet<FoursquarePlace> fs) {
        facebookPlaces = fb;
        foursquarePlaces = fs;
        matches = new TreeMap<>();
    }

    @Override
    public void run(){
        for(FacebookPlace fb: facebookPlaces){
            for(FoursquarePlace fs: foursquarePlaces){
                if(isPlacesIdentical(fb,fs)){
                    matches.put(fb.getId(), fs.getId());
                    break;
                }
            }
        }
    }

    private boolean isPlacesIdentical(FacebookPlace fb, FoursquarePlace fs) {
        if(StringComparator.compareStrings(fb.getName(), fs.getName())>0.8    ||
                StringComparator.compareStrings(fb.getAddress(), fs.getAddress())>0.95 && StringComparator.compareStrings(fb.getName(), fs.getName())>0.5     ||
                !fb.getUrl().isEmpty() && !fs.getUrl().isEmpty() && StringComparator.compareStrings(fb.getUrl(), fs.getUrl())>0.8        ||
                !fb.getPhoneNumber().isEmpty() && !fs.getUrl().isEmpty() && StringComparator.compareStrings(fb.getPhoneNumber(), fs.getPhoneNumber())>0.8){
            return true;
        }
        return false;
    }

    private List<Document> createDocumentsOfMatches(){
        List<Document> documents = new ArrayList<>();
        for(String fbKey: matches.keySet()){
            Document doc = new Document();
            doc.append("fbId", fbKey);
            doc.append("fsId", matches.get(fbKey));
            documents.add(doc);
        }
        return documents;
    }

}
