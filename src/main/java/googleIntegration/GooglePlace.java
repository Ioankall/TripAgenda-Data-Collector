package googleIntegration;

import org.bson.Document;
import components.Place;
import java.util.ArrayList;

/**
 * Created by Gika on 21/1/2017.
 */
public class GooglePlace extends Place {

    private String googleUrl;
    private String vicinity;
    private double rating;
    private int numOfReviews;
    private ArrayList<String> reviews;
    private String hours;

    //Setters - Getters
    public String getGoogleUrl() { return googleUrl; }
    public void setGoogleUrl(String googleUrl) { this.googleUrl = googleUrl; }
    public String getVicinity() { return vicinity; }
    public void setVicinity(String vicinity) { this.vicinity = vicinity; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getNumOfReviews() { return numOfReviews; }
    public void setNumOfReviews(int numOfReviews) { this.numOfReviews = numOfReviews; }
    public ArrayList<String> getReviews() { return reviews; }
    public void setReviews(ArrayList<String> reviews) { this.reviews = reviews; }
    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }

    @Override
    public Document createDocument() {
        Document reviews = new Document();
        reviews.append("rating", rating);
        reviews.append("numOfReviews", numOfReviews);
        int count = 0;
        for(String r: this.reviews){
            count++;
            reviews.append("review_" + String.valueOf(count), r);
        }

        Document doc = new Document();
        if (id != null) doc.append("id", id);
        if (name != null) doc.append("name", name);
        if (phoneNumber != null) doc.append("phoneNumber", phoneNumber);
        if (address != null) doc.append("address", address);
        if (url != null) doc.append("url", url);
        if (googleUrl != null) doc.append("googleUrl", googleUrl);
        if (vicinity != null) doc.append("vicinity", vicinity);
        if (hours != null) doc.append("hours", hours);
        doc.append("Reviews",reviews);

        return doc;
    }
}
