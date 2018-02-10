package components;

import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class Venue extends Place {

    private String facebookId;
    private int facebookLikes;
    private int facebookWereHereCount;
    private int facebookCheckins;
    private int facebookTalkingAboutCount;

    private String foursquareId;
    private String tags;
    private double foursquareRating;
    private int foursquareCheckins;
    private int foursquareLikes;
    private int foursquareTips;
    private int foursquareVisits;
    private int foursquareUsers;

    private String googleId;

    private double popularityScore;
    private double comingBackRatio;
    private double rating;
    private ArrayList<String> comments;
    private List<String> photos;
    private String categories;

    private double latitude;
    private double longitude;



    //Setters - Getters
    public void setCategories(String categories) {
        this.categories = categories;
    }
    public void setLatitude(double lat) { latitude = lat;}
    public void setLongitude(double longi) { longitude = longi;}

    public String getFacebookId() {
        return facebookId;
    }
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public int getFacebookLikes() {
        return facebookLikes;
    }
    public void setFacebookLikes(int facebookLikes) {
        this.facebookLikes = facebookLikes;
    }

    public int getFacebookWereHereCount() {
        return facebookWereHereCount;
    }
    public void setFacebookWereHereCount(int facebookWereHereCount) { this.facebookWereHereCount = facebookWereHereCount; }

    public int getFacebookCheckins() {
        return facebookCheckins;
    }
    public void setFacebookCheckins(int facebookCheckins) {
        this.facebookCheckins = facebookCheckins;
    }

    public int getFacebookTalkingAboutCount() {
        return facebookTalkingAboutCount;
    }
    public void setFacebookTalkingAboutCount(int facebookTalkingAboutCount) { this.facebookTalkingAboutCount = facebookTalkingAboutCount; }

    public String getFoursquareId() {
        return foursquareId;
    }
    public void setFoursquareId(String foursquareId) {
        this.foursquareId = foursquareId;
    }

    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }

    public double getFoursquareRating() {
        return foursquareRating;
    }
    public void setFoursquareRating(double foursquareRating) {
        this.foursquareRating = foursquareRating;
    }

    public int getFoursquareCheckins() {
        return foursquareCheckins;
    }
    public void setFoursquareCheckins(int foursquareCheckins) {
        this.foursquareCheckins = foursquareCheckins;
    }

    public int getFoursquareLikes() {
        return foursquareLikes;
    }
    public void setFoursquareLikes(int foursquareLikes) {
        this.foursquareLikes = foursquareLikes;
    }

    public int getFoursquareTips() {
        return foursquareTips;
    }
    public void setFoursquareTips(int foursquareTips) {
        this.foursquareTips = foursquareTips;
    }

    public int getFoursquareVisits() {
        return foursquareVisits;
    }
    public void setFoursquareVisits(int foursquareVisits) {
        this.foursquareVisits = foursquareVisits;
    }

    public int getFoursquareUsers() {
        return foursquareUsers;
    }
    public void setFoursquareUsers(int foursquareUsers) {
        this.foursquareUsers = foursquareUsers;
    }

    public String getGoogleId() {
        return googleId;
    }
    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public double getPopularityScore() {
        return popularityScore;
    }
    public void setPopularityScore(double popularityScore) {
        this.popularityScore = popularityScore;
    }

    public double getComingBackRatio() {
        return comingBackRatio;
    }
    public void setComingBackRatio(double comingBackRatio) {
        this.comingBackRatio = comingBackRatio;
    }

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }

    public ArrayList<String> getComments() {
        return comments;
    }
    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public List<String> getPhotos() {
        return photos;
    }
    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public Venue() {}

    @Override
    public Document createDocument() {
        Document venue = new Document();

        venue.append("name", name);
        venue.append("location", new Document("longitude",longitude).append("latitude", latitude));
        venue.append("address", address);
        venue.append("city", city);
        venue.append("postalCode", postalCode);
        venue.append("country", country);
        venue.append("generalCategory", generalCategory);
        venue.append("phoneNumber", phoneNumber);
        venue.append("url", url);
        venue.append("categories", categories);
        venue.append("tags", tags);
        venue.append("facebookID", facebookId);
        venue.append("foursquareID", foursquareId);
        venue.append("foursquareRating", foursquareRating);
        venue.append("rating", rating);
        venue.append("facebookLikes", facebookLikes);
        venue.append("facebookWereHereCount", facebookWereHereCount);
        venue.append("facebookCheckins", facebookCheckins);
        venue.append("facebookTalkingAboutCount", facebookTalkingAboutCount);
        venue.append("foursquareCheckins", foursquareCheckins);
        venue.append("foursquareLikes", foursquareLikes);
        venue.append("foursquareTips", foursquareTips);
        venue.append("foursquareVisits", foursquareVisits);
        venue.append("foursquareUsers", foursquareUsers);
        venue.append("comingBackRatio", comingBackRatio);
        venue.append("popularityScore", popularityScore);

        //JSONArray commentsArray = new JSONArray(comments);
        venue.append("comments", comments);

        //JSONArray photosArray = new JSONArray(photos);
        venue.append("photos", photos);

        return venue;
    }
}
