package facebookIntegration;

import facebook4j.Category;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.GeoLocation;
import facebook4j.Page;
import facebook4j.Place;
import facebook4j.Reading;
import facebook4j.ResponseList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FacebookScrapper extends Thread{

    //Facebook API's codes.
    private final String appID = "971710959517311";
    private final String appSecret = "1c4c8ba97a5b892cedb548054d82345f";

    private static TreeMap<String,FacebookPlace> venuesList;
    public TreeMap<String,FacebookPlace> getVenues() { return venuesList; }

    private List<String> queries;
    private Facebook facebook;
    private GeoLocation center;
    private int radius;


    public FacebookScrapper (String[] q, double latitude, double longitude, int distance) {

        venuesList = new TreeMap<>();
        facebook = new FacebookFactory().getInstance();
        facebook.setOAuthAppId(appID, appSecret);

        try {
            facebook.setOAuthAccessToken(facebook.getOAuthAppAccessToken());
        } catch (FacebookException e) {
            e.printStackTrace();
        }

        queries = new ArrayList<>();
        queries.addAll(Arrays.asList(q));
        center = new GeoLocation(latitude, longitude);
        radius = distance;
    }

    @Override
    public void run () {
        for(String q: queries){
            ResponseList<Place> results = null;
            try {
                results = facebook.searchPlaces(q, center, radius, new Reading().limit(500));
            } catch (FacebookException ex) {
                results = null;
                Logger.getLogger(FacebookScrapper.class.getName()).log(Level.SEVERE, null, ex);
            }

            if(results != null){
                for(Place p: results){

                    if(venuesList.containsKey(p.getId())) continue;

                    List<Category> categories = p.getCategories();
                    List<String> categoriesString = new ArrayList<>();
                    for(Category c: categories){
                        String temp = c.getName();
                        temp = temp.replace("&amp;", "&");
                        temp = temp.replace("&#039;", "'");
                        categoriesString.add(temp);
                    }

                    String generalCategory = findGeneralCategory(categoriesString);

                    double longitude, latitude;
                    try{
                        longitude = p.getLocation().getLongitude();
                        latitude = p.getLocation().getLatitude();
                    }catch(Exception ex){
                        longitude = 0;
                        latitude = 0;
                    }

                    Page page;
                    try {
                        page = facebook.getPage(p.getId(), new Reading().fields("likes", "checkins", "talking_about_count", "phone", "website", "were_here_count", "picture"));

                        int checkins = page.getCheckins();
                        int likes = page.getLikes();

                        if( checkins == 0 || likes <= 5) continue;

                        String address;
                        if(p.getLocation().getStreet() == null){
                            address = "";
                        } else {
                            address = p.getLocation().getStreet().toString();
                        }

                        String zip;
                        if(p.getLocation().getZip() == null){
                            zip = "";
                        } else {
                            zip = p.getLocation().getZip().toString();
                        }

                        String city;
                        if(p.getLocation().getCity() == null){
                            city = "";
                        } else {
                            city = p.getLocation().getCity().toString();
                        }

                        String country;
                        if(p.getLocation().getCountry() == null){
                            country = "";
                        } else {
                            country = p.getLocation().getCountry().toString();
                        }

                        String phone;
                        if(page.getPhone() == null){
                            phone = "";
                        } else {
                            phone = page.getPhone().toString();
                        }

                        String website;
                        if(page.getWebsite() == null){
                            website = "";
                        } else {
                            website = page.getWebsite().toString();
                        }

                        venuesList.put(p.getId(), new FacebookPlace(
                                p.getId().toString(),
                                p.getName().toString(),
                                address,
                                zip,
                                city,
                                country,
                                phone,
                                website,
                                generalCategory,
                                checkins,
                                page.getTalkingAboutCount().intValue(),
                                likes,
                                page.getWereHereCount().intValue(),
                                longitude,
                                latitude,
                                categoriesString));
                    } catch (FacebookException ex) {
                       continue;
                    }

                }
            }
        }
    }

    private String findGeneralCategory (List<String> categories) {
        String[] category1  = {"Arts & Entertainment", "Just For Fun", "Adult Entertainment", "Amusement", "Arcade", "Art Gallery", "Art Museum",
                "Art Restoration", "Art School", "Artistic Services", "Arts & Crafts Supply Store", "Arts & Marketing", "Auditorium",
                "Bands & Musicians", "Bingo Hall", "Bowling Alley", "Casino", "Circus", "Comedy Club", "Dance Instruction", "Entertainment Service",
                "Event Venue", "Go Karting", "Golf Course", "Graphic Design", "Internet Cafe", "Karaoke", "Laser Tag", "Malaysian Restaurant",
                "Martial Arts", "Movie & Television Studio", "Movie Theater", "Music Lessons & Instruction", "Music Production", "Music Store",
                "Orchestra", "Paintball", "Painter", "Party Center", "Performance Venue", "Performing Arts Education", "Pool & Billiards",
                "Race Track", "Snowmobiles", "Social Club", "Sports Bar", "Symphony", "Theater", "Theatrical Equipment", "Concert Venue",
                "Entertainer", "Video Games", "Web Design", "Web Development", "Wedding Planning", "Zoo & Aquarium", "Sports Event", "Sports Venue",
                "Event", "Club", "Attractions/Things to Do", "Public Places"};

        String[] category2 = {"Archaeological Services", "Historical Place", "History Museum", "Kingdom Hall", "Masonry", "Modern Art Museum",
                "Monument", "Statue & Fountain", "Museum", "Museum/Art Gallery"};

        String[] category3 = {"Performance Venue", "Clubhouse", "Dance Club", "DJ", "Hookah Lounge", "Jazz Club", "Late Night Restaurant", "Lounge", "Night Club", "Nightlife",
                "Bar", "Wine Bar", "Pub", "Event", "Club"};

        String[] category4 = {"Food/Beverages", "Restaurant/Cafe", "Place to Eat/Drink", "Concert Venue", "Wine Bar", "Food & Restaurant", "Restaurant", "Food & Grocery", "Afghani Restaurant", "African Restaurant",
                "American Restaurant", "Argentine Restaurant", "Asian Fusion Restaurant", "Asian Restaurant", "Bakery", "Bar & Grill",
                "Barbecue Restaurant", "Bartending Service", "Basque Restaurant", "Beer Garden", "Belgian Restaurant", "Brazilian Restaurant",
                "Breakfast & Brunch Restaurant", "Brewery", "British Restaurant", "Buffet Restaurant", "Burger Restaurant", "Burmese Restaurant",
                "Butcher", "Cafe", "Cafeteria", "Cajun & Creole Restaurant", "Cambodian Restaurant", "Canadian Restaurant", "Candy Store",
                "Cantonese Restaurant", "Caribbean Restaurant", "Caterer", "Chicken Restaurant", "Chicken Wings", "Chinese Restaurant", "Coffee Shop",
                "Continental Restaurant", "Creperie", "Cuban Restaurant", "Cupcake Shop", "Cyber Cafe", "Deli", "Dessert Place", "Dim Sum Restaurant",
                "Diner", "Dive Bar", "Donuts & Bagels", "Drive In Restaurant", "Ethiopian Restaurant", "Ethnic Grocery Store",
                "Family Style Restaurant", "Fast Food Restaurant", "Filipino Restaurant", "Fine Dining Restaurant", "Fish & Chips Shop", "Fishing",
                "Fondue Restaurant", "Food & Beverage Service & Distribution", "Food & Beverage Service & Distribution", "Food Consultant",
                "Food Stand", "Food Truck", "French Restaurant", "Frozen Yogurt Shop", "Fruit & Vegetable Store", "Gastropub", "Gay Bar",
                "German Restaurant", "Gluten-Free Restaurant", "Greek Restaurant", "Grocery Store", "Halal Restaurant", "Hawaiian Restaurant",
                "Health Food Store", "Himalayan Restaurant", "Hot Dog Joint", "Hot Dog Stand", "Health Food Restaurant", "Hungarian Restaurant", "Hunting and Fishing",
                "Ice Cream Parlor", "Ice Machines", "Indian Restaurant", "Indonesian Restaurant", "International Restaurant", "Irish Restaurant",
                "Italian Restaurant", "Japanese Restaurant", "Korean Restaurant", "Kosher Restaurant", "Latin American Restaurant",
                "Lebanese Restaurant", "Live & Raw Food Restaurant", "Maid & Butler", "Meat Shop", "Mediterranean Restaurant", "Mexican Restaurant",
                "Middle Eastern Restaurant", "Modern European Restaurant", "Mongolian Restaurant", "Moroccan Restaurant", "Nepalese Restaurant",
                "New American Restaurant", "Pakistani Restaurant", "Persian Restaurant", "Peruvian Restaurant", "Pho Restaurant", "Pizza Place",
                "Polish Restaurant", "Polynesian Restaurant", "Portuguese Restaurant", "Pub", "Race Cars", "Ramen Restaurant", "Refrigeration", "Restaurant Supply",
                "Restaurant Wholesale", "Russian Restaurant", "Salad Bar", "Sandwich Shop", "Scandinavian Restaurant", "Seafood Restaurant",
                "Singaporean Restaurant", "Smog Check Station", "Sorority & Fraternity", "Soul Food Restaurant", "Soup Restaurant", "Southern Restaurant",
                "Southwestern Restaurant", "Spanish Restaurant", "Specialty Grocery Store", "Steakhouse", "Sushi Restaurant", "Taiwanese Restaurant",
                "Take Out Restaurant", "Tanning Salon", "Tanning Salon Supplier", "Tapas Bar & Restaurant", "Tea Room", "Tex-Mex Restaurant", "Thai Restaurant", "Turkish Restaurant",
                "Vegetarian & Vegan Restaurant", "Crêperie", "Vending Machine Service", "Vietnamese Restaurant", "Winery & Vineyard", "Smoothie & Juice Bar"};

        String[] category5 = {"Public Places & Attractions", "Sports & Recreation", "Active Life", "Amusement Park Ride", "Athletic Education",
                "ATVs & Golf Carts", "Beach", "Beach Resort", "Bridge", "County", "City", "City Hall", "Classes", "Consulate & Embassy", "Ice Skating", "Landscaping",
                "Marina", "Marine Service Station", "Miniature Golf", "National Park", "Notary Public", "Ocean", "Outdoor Recreation", "Outdoor Services",
                "Outdoors", "Park", "Parking", "Petting Zoo", "Picnic Ground", "Playground", "Port", "Public Square", "Public Utility", "Racquetball Court", "Rafting",
                "Recreation Center", "Recreation Center", "Region", "Rock Climbing", "Rodeo", "Roller Coaster", "Roofer", "RV Park", "Scuba Diving", "Ski Resort",
                "Skin Care", "Sports Center", "Sports Club", "Sports Instruction", "Sports Promoter", "Sports Venue & Stadium", "State",
                "State Park", "Surfing Spot", "Surveyor", "Swimming Pool", "Swimming Pool Maintenance", "Swimwear", "Tennis", "Theme Park", "City", "Country", "Island",
                "Lake", "Landmark", "Mountain", "Neighborhood", "River", "Urban Farm", "Water Park", "Zoo", "Ski & Snowboard School", "Sky Diving", "Mountain Biking",
                "Boat Rental", "Boat Service", "Sports Venue", "Event", "Attractions/Things to Do", "Public Places"};

        String[] category6 = {"Business Services", "Home Improvement", "Professional Services", "Real Estate", "Shopping & Retail", "Accessories Store",
                "Agricultural Service", "Antique Store", "Antiques & Vintage", "Appliances", "Archery", "Armored Cars", "Audiovisual Equipment",
                "Auto Body Shop", "Auto Glass", "Automobile Leasing", "Automotive Consultant", "Automotive Customizing",
                "Automotive Manufacturing", "Automotive Parts & Accessories", "Automotive Repair", "Automotive Restoration", "Automotive Storage",
                "Automotive Trailer Services", "Automotive Wholesaler", "Aviation Fuel", "Big Box Retailer", "Bike Rental & Bike Share", "Bike Shop",
                "Blinds & Curtains", "Book & Magazine Distribution", "Borough", "Bridal Shop", "Business Center", "Business Consultant", "Business Supply Service",
                "Cabinets & Countertops", "Cable & Satellite Service", "Camera Store", "Car Dealership", "Car Parts & Accessories", "Car Rental",
                "Car Wash & Detailing", "Cargo & Freight", "Carnival Supplies", "Carpenter", "Carpet Cleaner", "Carpet & Flooring Store", "Children's Clothing Store",
                "Cleaning Service", "Clothing Store", "Clothing Supply & Distribution", "Collectibles Store", "Collection Agency", "Comic Book Store",
                "Commercial Automotive", "Commercial Bank", "Commercial & Industrial", "Computer Services", "Computer Store", "Computer Training",
                "Computers & Electronics", "Convenience Store", "Convention Center", "Copying & Printing", "Costume Shop", "Cultural Gifts Store", "Currency Exchange",
                "Department Store", "Discount Store", "Drugstore", "Dry Cleaner", "DVD & Video Store", "Educational Supplies", "Electrician",
                "Electronic Equipment Service", "Electronics Store", "Equipment Service & Supply", "Escrow Services", "Eyewear", "Farmers Market",
                "Fashion Designer", "Fireplaces", "Fireworks Retailer", "Flea Market", "Florist", "Formal Wear", "Franchising Service", "Furniture Repair",
                "Furniture Store", "Garage Door Services", "Garden Center", "Gardener", "Gas & Chemical Service", "Gas Station", "Gift Shop", "Glass Products",
                "Glass Service", "Gun Store", "Hardware Store", "Hardware & Tools Service", "Heating", "Ventilating & Air Conditioning", "Home Security",
                "Home Theater Store", "Home Window Service", "Jewelry Store", "Jewelry Supplier", "Laboratory Equipment", "Limo Service", "Liquor Store",
                "Lottery Retailer", "Market", "Market Research Consultant", "Mattress Wholesale", "Men's Clothing Store", "Merchandising Service", "Mobile Phone Shop",
                "Motorcycle Repair", "Motorcycles", "Musical Instrument Store", "Oil Lube & Filter Service", "Outdoor Equipment Store", "Outlet Store",
                "Packaging Supplies & Equipment", "Party Supplies", "Pawn Shop", "Pawn Shop", "Pet Breeder", "Pet Cemetery", "Pet Groomer", "Pet Sitter", "Pet Store",
                "Plastics", "Printing Service", "Promotional Item Services", "Recreational Vehicle Dealer", "Refrigeration Sales & Service", "Religious Book Store",
                "Rent to Own Store", "Rental Company", "Rental Shop", "Repair Service", "RV Dealership", "RV Repair", "Scooter Rental", "Seasonal Store",
                "Service Station Supply", "Shoe Store", "Shopping District", "Shopping Mall", "Shopping Service", "Signs & Banner Service", "Sporting Goods Store",
                "Sportswear", "Telemarketing Service", "Textiles", "Thrift or Consignment Store", "Ticket Sales", "Tire Dealer", "Tobacco Store", "Tools Service",
                "Appliances", "Book Store", "Office Supplies", "Toy Store", "Trophies & Engraving", "Truck Rental", "Truck Towing", "Vintage Store",
                "Wholesale & Supply Store", "Wig Store", "Window Service & Repair", "Women's Clothing Store", "Local Business", "Company", "Small Business",
                "Commercial & Industrial Equipment", "Automobiles and Parts", "Outdoor Gear/Sporting Goods", "Retail and Consumer Merchandise", "Pharmacy",
                "Automobiles and Parts", "Commercial Real Estate", "Automobiles and Parts", "Computers/Technology"};

        String[] category7 = {"Transit Stop", "Bus Line","Lodging", "Residence", "Spa", "Beauty & Personal Care", "Tours & Sightseeing", "Travel & Transportation, Airline", "Airline Industry Services", "Airport Lounge",
                "Airport Shuttle", "Airport Terminal", "Apartment & Condo Building", "Aromatherapy", "Barber Shop", "Beauty Salon", "Bed and Breakfast", "Bus Station", "Cabin", "Camp",
                "Campground", "Campus Building", "Charter Buses", "Cosmetics & Beauty Supply", "Cruise", "Cruise Excursions", "Day Spa", "Dorm", "Driving Range", "Eco Tours", "Educational Camp",
                "Emergency Roadside Service", "Exchange Program", "Exotic Car Rental", "Farm", "Ferry & Boat", "Fitness Center", "Forestry & Logging", "Gym", "Hair & Beauty Supply", "Hair Removal",
                "Hair Replacement", "Hair Salon", "Hairpieces & Extensions", "Halfway House", "Health Spa", "Highway", "Home", "Hostel", "Hotel Supply Service", "Housing & Homeless Shelter", "Inn",
                "Laser Hair Removal", "Lodge", "Massage", "Mattresses & Bedding", "Medical Spa", "Meeting Room", "Mobile Homes", "Motel", "Nail Salon", "Physical Fitness", "Private Plane Charter",
                "Private Transportation", "Public Transportation", "Railroad", "Resort", "Retirement & Assisted Living Facility", "Spa", "Street", "Subway & Light Rail Station", "Tattoo & Piercing",
                "Taxi", "Airport", "Hotel", "Tour Company", "Tour Guide", "Tourist Attraction", "Tourist Information", "Train Station", "Transportation Service", "Travel Agency",
                "Vacation Home Rental", "Yoga & Pilates", "Women's Health", "Spa Beauty & Personal Care","Beauty & Personal Care", "Tours/Sightseeing","Health/Beauty", "Event", "Travel & Transportation",
                "Aesthetics", "Transport/Freight", "Airline", "Doctor", "Spas/Beauty/Personal Care"};

        String[] category8 = {"Religious Center", "Community & Government", "African Methodist Episcopal Church", "Anglican Church", "Animal Shelter", "Apostolic Church",
                "Bank", "Bank Equipment & Service",  "Baptist Church", "Boating", "Buddhist Temple", "Catholic Church", "Cemetery",
                "Charismatic Church", "Charity Organization", "Christian Church", "Christian Science Church", "Church", "Church of Christ", "Church of God",
                "Church of Jesus Christ of Latter-day Saints", "College & University", "Congregational Church", "Episcopal Church", "Evangelical Church",
                "Full Gospel Church", "Holiness Church", "Independent Church", "Interdenominational Church", "Mennonite Church", "Methodist Church", "Mosque",  "Nazarene Church",
                "Nondenominational Church", "Police Station", "Political Organization", "Post Office", "Presbyterian Church","Sikh Temple", "Synagogue", "Library",
                "Wildlife Sanctuary", "Non-Profit Organization", "Advertising Agency","Public Relations", "Environmental Conservation", "High School","Consulting/Business Services",
                "School","TV Network","Organization","Technical Institute", "Media/News/Publishing", "Government Organization", "Elementary School", "Organization", "Education",
                "Hospital", "Company", "Event", "Broadcasting & Media Production", "Engineering/Construction", "Manufacturing", "Modeling Agency", "Clinic",
                "Publisher", "Small Business", "Home Decor", "Automotive", "Community Organization", "Janitorial Service", "Junior High School", "Corporate Office", "Non-Governmental Organization (NGO)",
                "Culinary School", "Other", "Advertising Service", "Aerospace/Defense", "Event Planning", "Date Spot","Movie Studio", "Internet/Software","Religious Organization",
                "Employment Agency Recruiter", "Financial Services", "Health/Medical/Pharmaceuticals", "Architect", "Local Education", "Doctor", "Employment Agency", "Recruiter",
                "Civic Structure", "Image Consultant", "Writing Service", "Public Services", "Eastern Orthodox Church", "Pet Service", "Geologic Service", "Seventh Day Adventist Church",
                "Management Service", "Startup", "Photographer", "Law Practice", "Prison & Correctional Facility", "Engineering Service", "Event Planner", "Law Practice",
                "Photographic Services & Equipment", "Personal Coaching", "Community Center", "Supply & Distribution Services"};


        String[][] categoriesList = {category1, category2, category3, category4, category5, category6, category7, category8};

        String generalCategory = "";

        for(String c: categories){
            int i=0;
            for(String[] list : categoriesList){
                i++;
                for(String cat: list){
                    if(cat.equals(c.toString().replace(",", ""))) {
                        if("".equals(generalCategory)){
                            generalCategory += "category" + Integer.toString(i);
                        }else{
                            generalCategory += ", ";
                            generalCategory += "category" + Integer.toString(i);
                        }
                        break;
                    }
                }
            }
        }

        generalCategory = generalCategory.replace("category1", "Arts & Entertainment");
        generalCategory = generalCategory.replace("category2", "Museums");
        generalCategory = generalCategory.replace("category3", "Nightlife");
        generalCategory = generalCategory.replace("category4", "Food & drink");
        generalCategory = generalCategory.replace("category5", "Public places");
        generalCategory = generalCategory.replace("category6", "Shopping");
        generalCategory = generalCategory.replace("category7", "Transportation & Accomondation");
        generalCategory = generalCategory.replace("category8", "Religion and Organizations");

        return generalCategory;
    }
}
