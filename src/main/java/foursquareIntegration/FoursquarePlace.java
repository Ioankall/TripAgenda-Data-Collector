package foursquareIntegration;

import org.bson.Document;
import components.Place;
import java.util.List;

/**
 * Created by Gika on 21/1/2017.
 */
public class FoursquarePlace extends Place {

    private int numOfCheckins;
    private int numOfUsers;
    private int numOfTips;
    private int numOfVisits;
    private int numOfLikes;
    private double rating;
    private int ratingSignals;
    private List<String> photos;
    private List<String> tips;
    private String[] tags;

    //Setters - Getters
    public int getNumOfCheckins() { return numOfCheckins; }
    public void setNumOfCheckins(int numOfCheckins) { this.numOfCheckins = numOfCheckins; }
    public int getNumOfUsers() { return numOfUsers; }
    public void setNumOfUsers(int numOfUsers) { this.numOfUsers = numOfUsers; }
    public int getNumOfTips() { return numOfTips; }
    public void setNumOfTips(int numOfTips) { this.numOfTips = numOfTips; }
    public int getNumOfVisits() { return numOfVisits; }
    public void setNumOfVisits(int numOfVisits) { this.numOfVisits = numOfVisits; }
    public int getNumOfLikes() { return numOfLikes; }
    public void setNumOfLikes(int numOfLikes) { this.numOfLikes = numOfLikes; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getRatingSignals() { return ratingSignals; }
    public void setRatingSignals(int ratingSignals) { this.ratingSignals = ratingSignals; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public List<String> getTips() { return tips; }
    public void setTips(List<String> tips) { this.tips = tips; }
    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    @Override
    public Document createDocument() {
        Document cat = new Document();
        cat.append("numOfCategories", categories.size());
        int i=0;
        for (String c: categories) {
            cat.append("category_"+Integer.toString(i) , c);
            i++;
        }
        Document doc = new Document();
        if (id != null) doc.append("id", id);
        if (name != null) doc.append("name", name);
        if (address != null) doc.append("address", address);
        if (postalCode != null) doc.append("postalCode", postalCode);
        if (city != null) doc.append("city", city);
        if (country != null) doc.append("country", country);
        if (phoneNumber != null) doc.append("phoneNumber", phoneNumber);
        if (url != null) doc.append("url", url);
        if (numOfCheckins != 0) doc.append("numOfCheckins", numOfCheckins);
        if (numOfUsers != 0) doc.append("numOfUsers", numOfUsers);
        if (numOfTips != 0) doc.append("numOfTips", numOfTips);
        if (generalCategory != null) doc.append("generalCategory", generalCategory);
        doc.append("categories", cat);
        return doc;
    }
}
