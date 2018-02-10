package facebookIntegration;

import java.util.List;
import org.bson.Document;
import components.Place;


public class FacebookPlace extends Place{

    private int numOfCheckins;
    private int numOfTalkingAboutCount;
    private int numOfLikes;
    private int numOfWereHereCount;
    private double longitude;
    private double latitude;

    //Setters - Getters
    public int getNumOfCheckins() { return numOfCheckins; }
    public void setNumOfCheckins(int numOfCheckins) { this.numOfCheckins = numOfCheckins; }
    public int getNumOfTalkingAboutCount() { return numOfTalkingAboutCount; }
    public void setNumOfTalkingAboutCount(int numOfTalkingAboutCount) { this.numOfTalkingAboutCount = numOfTalkingAboutCount; }
    public int getNumOfLikes() { return numOfLikes; }
    public void setNumOfLikes(int numOfLikes) { this.numOfLikes = numOfLikes; }
    public int getNumOfWereHereCount() { return numOfWereHereCount; }
    public void setNumOfWereHereCount(int numOfWereHereCount) { this.numOfWereHereCount = numOfWereHereCount; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public FacebookPlace(){}

    public FacebookPlace(String id, String name, String address, String postalCode, String city, String country, String phoneNumber, String url, String generalCategory,
                              int numOfCheckins, int numOfTalkingAboutCount, int numOfLikes, int numOfWereHereCount, double longitude, double latitude, List<String> categories){
        this.id = id;
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.url = url;
        this.generalCategory = generalCategory;
        this.numOfCheckins = numOfCheckins;
        this.numOfTalkingAboutCount = numOfTalkingAboutCount;
        this.numOfLikes = numOfLikes;
        this.numOfWereHereCount = numOfWereHereCount;
        this.longitude = longitude;
        this.latitude = latitude;
        this.categories = categories;
    }

    @Override
    public Document createDocument(){

        Document cat = new Document();
        cat.append("numOfCategories", categories.size());
        int i=0;
        for(String c: categories){
            cat.append("category_"+Integer.toString(i) , c);
            i++;
        }

        Document doc = new Document();
        if(id != null) doc.append("id", id);
        if(name != null) doc.append("name", name);
        if(address != null) doc.append("address", address);
        if(postalCode != null) doc.append("postalCode", postalCode);
        if(city != null) doc.append("city", city);
        if(country != null) doc.append("country", country);
        if(phoneNumber != null) doc.append("phoneNumber", phoneNumber);
        if(url != null) doc.append("url", url);
        if(numOfCheckins != 0) doc.append("numOfCheckins", numOfCheckins);
        if(numOfLikes != 0) doc.append("numOfLikes", numOfLikes);
        if(numOfWereHereCount != 0) doc.append("numOfWereHereCount", numOfWereHereCount);
        if(numOfTalkingAboutCount != 0) doc.append("numOfTalkingAbout", numOfTalkingAboutCount);
        if(generalCategory != null) doc.append("generalCategory", generalCategory);
        doc.append("categories", cat);
        doc.append("longitude", longitude);
        doc.append("latitude", latitude);
        return doc;
    }
}

