package components;

import org.bson.Document;

public class City {

    private String cityName;
    private double longitude;
    private double latitude;

    //Getters
    public String getCityName() { return cityName; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }

    public City(String name, double lat, double lon){
        cityName = name;
        latitude = lat;
        longitude = lon;
    }

    public Document getDocument(){
        Document doc = new Document();
        doc.append("city", cityName);
        doc.append("latitude", String.valueOf(latitude));
        doc.append("longitude", String.valueOf(longitude));
        return doc;
    }
}
