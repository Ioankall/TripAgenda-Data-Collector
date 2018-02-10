package components;

import org.bson.Document;

import java.util.List;


public abstract class Place {

    protected String id = "";
    protected String name = "";
    protected String address = "";
    protected String postalCode = "";
    protected String city = "";
    protected String country = "";
    protected String phoneNumber = "";
    protected String url = "";
    protected String generalCategory = "";
    protected List<String> categories;

    //Setters - Getters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getGeneralCategory() { return generalCategory; }
    public void setGeneralCategory(String generalCategory) { this.generalCategory = generalCategory; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public abstract Document createDocument();
}
