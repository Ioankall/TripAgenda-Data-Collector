package components;

public class PlacesPair {

    private String facebookPlaceId;
    private String foursquarePlaceId;
    private String googlePlaceId;

    //Setters - Getters
    public String getFacebookPlaceId() { return facebookPlaceId; }
    public void setFacebookPlaceId(String facebookPlaceId) { this.facebookPlaceId = facebookPlaceId; }
    public String getFoursquarePlaceId() { return foursquarePlaceId; }
    public void setFoursquarePlaceId(String foursquarePlaceId) { this.foursquarePlaceId = foursquarePlaceId; }
    public String getGooglePlaceId() { return googlePlaceId; }
    public void setGooglePlaceId(String googlePlaceId) { this.googlePlaceId = googlePlaceId; }

    public PlacesPair(String fbId, String fsId, String gId){
        facebookPlaceId = fbId;
        foursquarePlaceId = fsId;
        googlePlaceId = gId;
    }
}
