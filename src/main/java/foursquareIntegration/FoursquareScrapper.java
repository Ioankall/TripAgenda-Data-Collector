package foursquareIntegration;

import java.util.HashMap;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class FoursquareScrapper extends Thread {

    private static TreeMap<String,FoursquarePlace> venueList;
    private HashMap<String, String> queries;
    static int queryCount = 0;


    public TreeMap<String,FoursquarePlace> getVenues(){ return venueList; }

    public FoursquareScrapper(HashMap<String, String> q){
        venueList = new TreeMap<>();
        queries = q;
    }

    @Override
    public void run(){
        boolean next = false;
        for(String k: queries.keySet()){
            //System.out.println(k);
            next = false;
            String ll = queries.get(k);
            JSONObject root = null;

            int numOfTry = 0;
            try {
                do{
                    numOfTry++;
                    // http://freedns.afraid.org/subdomain/
                    String query = "http://localhost/fsquery.php?ll=" + ll + "&radius=500&query=" + k + "&intent=checkin&limit=50";
                    URL url;
                    String resp = "";
                    try {
                        url = new URL(query);
                        queryCount++;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                            for (String line; (line = reader.readLine()) != null;) {
                                resp = resp + line;
                            }
                        }   catch (IOException ex) {
                            //Logger.getLogger(FoursquareScrapperThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (MalformedURLException ex) {
                        //Logger.getLogger(FoursquareScrapperThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if("".equals(resp)) continue;
                    int startPositionOfJson = resp.indexOf("{");
                    if(startPositionOfJson <= 0){
                        next = true;
                        break;
                    }
                    String substring = resp.substring(startPositionOfJson);
                    if(substring != null){
                        JSONTokener tokener = new JSONTokener(substring);
                        try {
                            root = new JSONObject(tokener);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }while(root != null && root.getJSONObject("meta").getInt("code") != 200 && numOfTry<3);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(next) continue;

            try{
                JSONArray arr = root.getJSONObject("response").getJSONArray("venues");

                for (int i = 0; i < arr.length(); i++)
                {
                    FoursquarePlace p = new FoursquarePlace();

                    String id = arr.getJSONObject(i).getString("id");
                    p.setId(id);

                    if(venueList.containsKey(id)) continue;

                    String name = arr.getJSONObject(i).getString("name");
                    p.setName(name);

                    try{
                        String phone = arr.getJSONObject(i).getJSONObject("contact").getString("phone");
                        p.setPhoneNumber(phone);
                    }catch(Exception ex){}

                    try{
                        String address = arr.getJSONObject(i).getJSONObject("location").getString("address");
                        p.setAddress(address);
                    }catch(Exception ex){}

                    try{
                        String postalCode = arr.getJSONObject(i).getJSONObject("location").getString("postalCode");
                        p.setPostalCode(postalCode);
                    }catch(Exception ex){}

                    try{
                        String city = arr.getJSONObject(i).getJSONObject("location").getString("city");
                        p.setCity(city);
                    }catch(Exception ex){}

                    try{
                        String country = arr.getJSONObject(i).getJSONObject("location").getString("country");
                        p.setCountry(country);
                    }catch(Exception ex){}

                    JSONArray categoriesArray;
                    categoriesArray = arr.getJSONObject(i).getJSONArray("categories");
                    List<String> categories = new ArrayList<>();
                    for (int c = 0; c < categoriesArray.length(); c++){
                        categories.add(categoriesArray.getJSONObject(c).getString("name"));
                    }
                    p.setCategories(categories);

                    p.setGeneralCategory(findGeneralCategory(categories));

                    int checkins = arr.getJSONObject(i).getJSONObject("stats").getInt("checkinsCount");
                    p.setNumOfCheckins(checkins);

                    int users = arr.getJSONObject(i).getJSONObject("stats").getInt("usersCount");
                    p.setNumOfUsers(users);

                    int tips = arr.getJSONObject(i).getJSONObject("stats").getInt("tipCount");
                    p.setNumOfTips(tips);

                    try{
                        String website = arr.getJSONObject(i).getString("url");
                        p.setUrl(website);
                    }catch(Exception ex){}

                    venueList.put(id, p);
                }
            }catch(Exception ex){}

        }
    }

    private String findGeneralCategory (List<String> categories) {
        String[] category1  = {"Aquarium", "Arcade", "Art", "Gallery", "Bowling", "Casino", "Circus", "Comedy", "Concert", "Hall", "Golf",
                "Entertainment", "Kart", "Laser", "Movie", "Theater", "Indie", "Multiplex", "Opera", "Performing Arts", "Venue", "Pool", "Hall",
                "Sculpture", "Racetrack", "Roller", "Stadium", "Baseball", "Basketball", "Cricket", "Ground", "Football", "Soccer", "Hockey",
                "Arena", "Tennis", "Track", "Theme Park", "Ride", "Water Park", "Park", "Zoo", "Art Gallery", "General Entertainment", "Music Venue",
                "Soccer Stadium", "Basketball Stadium", "Public Art", "Concert Hall", "Athletics & Sports", "Comedy Club", "Indie Movie Theater",
                "Street Art", "Movie Theater", "Tennis Stadium", "Internet Cafe", "College Arts Building", "Indie Theater", "Entertainment Service",
                "Arts & Entertainment", "Bowling Alley", "Opera House", "Laser Tag", "Rugby Stadium", "College Theater"};

        String[] category2 = {"Historic", "Memorial", "Museum", "Planetarium", "Monument / Landmark", "Art Museum", "Art Gallery", "Historic Site",
                "Memorial Site", "Concert Hall", "History Museum", "Science Museum", "Erotic Museum"};

        String[] category3 = {"Country Dance", "Club", "Jazz", "Piano", "Bar", "Rock", "Salsa", "Festival", "Music", "Nightlife Spot",
                "Beach Bar", "Beer Garden", "Champagne Bar", "Cocktail Bar", "Dive Bar", "Gay Bar", "Hookah Bar", "Hotel Bar", "Karaoke Bar",
                "Pub", "Sake Bar", "Sports Bar", "Whisky Bar", "Wine Bar", "Brewery", "Lounge", "Night Market", "Nightclub", "Other Nightlife",
                "Speakeasy", "Strip Club", "Irish Pub Winery", "Music Venue", "Piano Bar", "Performing Arts Venue", "Comedy Club", "Jazz Club",
                "Irish Pub", "Rock Club", "Opera House"};

        String[] category4 = {"Piano Bar", "Tea Room", "Kafenio", "Juice Bar", "Restaurant", "Afghan Restaurant", "African Restaurant",
                "Ethiopian Restaurant", "American Restaurant", "Asian Restaurant ", " Cambodian Restaurant", "Chinese Restaurant",
                "Anhui Restaurant", "Beijing Restaurant", "Cantonese Restaurant", "Chinese Aristocrat Restaurant", "Chinese Breakfast Place",
                "Dim Sum Restaurant", "Dongbei Restaurant", "Fujian Restaurant", "Guizhou Restaurant", "Hainan Restaurant", "Hakka Restaurant",
                "Henan Restaurant", "Hong Kong Restaurant", "Huaiyang Restaurant", "Hubei Restaurant,Hunan Restaurant", "Imperial Restaurant",
                "Jiangsu Restaurant", "Jiangxi Restaurant Macanese Restaurant", "Manchu Restaurant", "Peking Duck Restaurant",
                "Shaanxi Restaurant", "Shandong Restaurant", "Shanghai Restaurant,Shanxi Restaurant", "Szechuan Restaurant", "Taiwanese Restaurant",
                "Tianjin Restaurant", "Xinjiang Restaurant,Yunnan Restaurant", "Zhejiang Restaurant", "Filipino Restaurant", "Himalayan Restaurant",
                "Hotpot Restaurant", "Japanese Restaurant", "Donburi Restaurant", "Japanese Curry Restaurant", "Kaiseki Restaurant",
                "Kushikatsu Restaurant", "Monjayaki Restaurant", "Nabe Restaurant", "Okonomiyaki Restaurant", "Ramen Restaurant",
                "Shabu-Shabu Restaurant", "Soba Restaurant", "Sukiyaki Restaurant", "Sushi Restaurant", "Takoyaki Place", "Tempura Restaurant",
                "Tonkatsu Restaurant", "Udon Restaurant", "Unagi Restaurant", "Wagashi Place", "Yakitori Restaurant", "Yoshoku Restaurant",
                "Korean Restaurant", "Malaysian Restaurant", "Mongolian Restaurant", "Noodle House", "Acai House", "Baiano Restaurant",
                "Central Brazilian Restaurant", "Churrascaria", "Empada House", "Goiano Restaurant", "Mineiro Restaurant",
                "Northeastern Brazilian Restaurant", "Northern Brazilian Restaurant", "Pastelaria", "Southeastern Brazilian Restaurant",
                "Southern Brazilian Restaurant", "Tapiocaria", "Breakfast Spot", "Bubble Tea Shop", "Buffet", "Burger Joint", "Cafeteria",
                "Cafe", "Cajun / Creole Restaurant", "Caribbean Restaurant", "Caucasian Restaurant", "Coffee Shop", "Comfort Food Restaurant",
                "Creperie", "Czech Restaurant", "Deli / Bodega", "Dessert Shop", "Cupcake Shop", "Donut Shop", "Frozen Yogurt", "Ice Cream Shop",
                "Pie Shop", "Diner", "Distillery", "Dumpling Restaurant", "Eastern European Restaurant", "Belarusian Restaurant",
                "Romanian Restaurant", "Tatar Restaurant", "English Restaurant", "Falafel Restaurant", "Fast Food Restaurant", "Fish & Chips Shop",
                "Fondue Restaurant", "Food Court", "Food Truck", "French Restaurant", "Fried Chicken Joint", "Friterie", "Gastropub",
                "German Restaurant", "Gluten-free Restaurant", "Greek Restaurant", "Bougatsa Shop", "Cretan Restaurant", "Fish Taverna",
                "Grilled Meat Restaurant", "Magirio", "Meze Restaurant", "Modern Greek Restaurant", "Ouzeri", "Patsa Restaurant", "Souvlaki Shop",
                "Taverna", "Tsipouro Restaurant", "Halal Restaurant", "Hawaiian Restaurant", "Hot Dog Joint,Hungarian Restaurant", "Indian Restaurant",
                "Andhra Restaurant", "Awadhi Restaurant", "Bengali Restaurant,Chaat Place", "Chettinad Restaurant", "Dhaba", "Dosa Place",
                "Goan Restaurant", "Gujarati Restaurant", "Hyderabadi Restaurant", "Indian Chinese Restaurant", "Indian Sweet Shop", "Irani Cafe",
                "Jain Restaurant", "Karnataka Restaurant", "Kerala Restaurant", "Maharashtrian Restaurant", "Mughlai Restaurant",
                "Multicuisine Indian Restaurant", "North Indian Restaurant", "Northeast Indian Restaurant", "Parsi Restaurant", "Punjabi Restaurant",
                "Rajasthani Restaurant", "South Indian Restaurant", "Udupi Restaurant", "Indonesian Restaurant", "Acehnese Restaurant",
                "Balinese Restaurant", "Betawinese Restaurant", "Indonesian Meatball Place", "Javanese Restaurant", "Manadonese Restaurant",
                "Padangnese Restaurant", "Sundanese Restaurant", "Italian Restaurant", "Abruzzo Restaurant", "Agriturismo", "Aosta Restaurant",
                "Basilicata Restaurant", "Calabria Restaurant", "Campanian Restaurant", "Emilia Restaurant", "Friuli Restaurant", "Ligurian Restaurant",
                "Lombard Restaurant", "Malga", "Marche Restaurant", "Molise Restaurant", "Piadineria", "Piedmontese Restaurant", "Puglia Restaurant",
                "Rifugio di Montagna", "Romagna Restaurant", "Roman Restaurant,Sardinian Restaurant", "South Tyrolean Restaurant", "Trattoria",
                "Osteria", "Trentino Restaurant", "Tuscan Restaurant", "Umbrian Restaurant", "Veneto Restaurant", "Jewish Restaurant",
                "Kosher Restaurant", "Latin American Restaurant", "Arepa Restaurant", "Cuban Restaurant", "Empanada Restaurant", "Mac & Cheese Joint",
                "Mediterranean Restaurant", "Moroccan Restaurant", "Mexican Restaurant", "Burrito Place", "Taco Place", "Middle Eastern Restaurant",
                "Persian Restaurant", "Modern European Restaurant", "Molecular Gastronomy Restaurant", "Pakistani Restaurant", "Pizza Place",
                "Polish Restaurant", "Portuguese Restaurant", "Russian Restaurant", "Blini House", "Pelmeni House", "Salad Place", "Sandwich Place",
                "Scandinavian Restaurant", "Seafood Restaurant", "Snack Place", "Soup Place", "South American Restaurant", "Argentinian Restaurant",
                "Peruvian Restaurant", "Southern / Soul Food Restaurant", "Spanish Restaurant", "Paella Restaurant", "Tapas Restaurant",
                "Sri Lankan Restaurant", "Steakhouse", "Swiss Restaurant", "Turkish Restaurant", "Borek Place", "Cigkofte Place", "Doner Restaurant",
                "Gozleme Place", "Kebab Restaurant", "Kofte Place", "Kokorec Restaurant", "Manti Place", "Meyhane", "Pide Place",
                "Turkish Home Cooking Restaurant", "Ukrainian Restaurant", "Varenyky restaurant", "West-Ukrainian Restaurant,Vegetarian / Vegan Restaurant",
                "Wings Joint","Chocolate Shop","Food & Drink Shop", "Bistro", "Bakery", "Café", "Winery", "New American Restaurant", "Vegetarian / Vegan Restaurant",
                "Asian Restaurant", "Vietnamese Restaurant", "Performing Arts Venue", "Thai Restaurant", "Brazilian Restaurant", "Trattoria/Osteria",
                "BBQ Joint", "Pet Café", "Hot Dog Joint", "Israeli Restaurant", "College Cafeteria", "Food Service", "Australian Restaurant", "Food",
                "Belgian Restaurant", "Hungarian Restaurant", "Shanxi Restaurant", "Airport Food Court"};

        String[] category5 = {"Athletics & Sports Badminton Court", "Baseball Field", "Basketball Court", "Bowling Green", "Golf Course",
                "Hockey Field", "Paintball Field", "Rugby Pitch", "Skate Park", "Skating Rink", "Soccer Field", "Sports Club", "Squash Court",
                "Tennis Court", "Volleyball Court", "Bath House", "Bathing Area", "Beach", "Nudist Beach", "Surf Spot", "Botanical Garden", "Bridge",
                "Campground", "Castle", "Cemetery", "Dive Spot", "Dog Run", "Farm", "Field", "Fishing Spot", "Forest", "Garden", "Gun Range",
                "Harbor / Marina", "Hot Spring", "Island", "Lake", "Lighthouse", "Mountain,National Park", "Nature Preserve", "Other Great Outdoors",
                "Palace", "Park", "Pedestrian Plaza", "Playground", "Plaza", "Pool", "Rafting", "Recreation Center", "River", "Rock Climbing Spot",
                "Scenic Lookout", "Sculpture Garden", "Ski Area", "Apres Ski Bar", "Ski Chairlift", "Ski Chalet", "Ski Lodge", "Ski Trail", "Stables",
                "States & Municipalities", "City", "Country", "Neighborhood", "State", "Town", "Village", "Summer Camp", "Trail", "Tree",
                "Vineyard", "Volcano", "Well", "Soccer Stadium", "Basketball Stadium", "Public Art", "Athletics & Sports", "Tennis Stadium",
                "Theme Park Ride / Attraction", "Fountain", "Outdoor Sculpture", "College Track", "Go Kart Track", "Racecourse", "Track Stadium",
                "Baseball Stadium", "College Stadium", "Outdoors & Recreation", "Laser Tag", "Rugby Stadium"};

        String[] category6 = {"ATM", "Adult Boutique", "Antique Shop", "Arts & Crafts Store", "Astrologer","Auto Garage","Automotive Shop","Baby Store","Bank",
                "Betting Shop", "Big Box Store","Bike Shop","Board Shop","Bookstore","Bridal Shop","Business Service","Camera Store","Candy Store","Car Dealership","Car Wash",
                "Carpet Store","Check Cashing Service","Christmas Market", "Clothing Store", "Accessories Store","Boutique","Kids Store",
                "Lingerie Store", "Men's Store", "Shoe Store", "Women's Store", "Comic Shop","Construction & Landscaping", "Convenience Store","Cosmetics Shop",
                "Costume Shop", "Credit Union", "Daycare", "Department Store", "Design Studio", "Discount Store", "Dive Shop", "Drugstore / Pharmacy", "Dry Cleaner",
                "EV Charging Station", "Electronics Store", "Event Service", "Fabric Shop", "Financial or Legal Service", "Fireworks Store", "Fishing Store", "Flea Market",
                "Flower Shop", "Beer Store", "Butcher", "Cheese Shop", "Farmers Market", "Fish Market", "Gourmet Shop", "Grocery Store", "Health Food Store",
                "Liquor Store", "Organic Grocery", "Street Food Gathering", "Supermarket", "Wine Shop", "Frame Store", "Fruit & Vegetable Store", "Furniture / Home Store",
                "Lighting Store", "Gaming Cafe", "Garden Center", "Gas Station / Garage", "Shopping Mall", "Pop-Up Shop", "Optical Shop", "Cambodian Restaurant",
                "Miscellaneous Shop", "Perfume Shop", "Music Store", "Jewelry Store", "Smoke Shop", "Travel Agency", "Market", "Bagel Shop", "Mobile Phone Shop",
                "Shoe Repair", "Sporting Goods Shop", "Gift Shop", "Pet Store", "Tattoo Parlor", "Tailor Shop", "Motorcycle Shop", "Pawn Shop", "Paper / Office Supplies Store",
                "Souvenir Shop", "Hardware Store", "Thrift / Vintage Store", "Other Repair Shop", "Outlet Store", "Toy / Game Store", "Record Shop", "Luggage Store",
                "Used Bookstore", "Gas Station", "Leather Goods Store", "Video Game Store", "Internet Cafe", "Shipping Store", "Warehouse Store", "Herbs & Spices Store",
                "Stationery Store", "Locksmith", "Hobby Shop", "Gun Shop", "Video Store", "Shop & Service", "College Bookstore", "Print Shop", "Auto Workshop",
                "Knitting Store"};

        String[] category7 = {"Airport Tram", "Airport", "Food Court", "Airport Gate", "Airport Lounge", "Airport Terminal", "Airport Tram,Plane",
                "Bike Rental / Bike Share", "Boat or Ferry", "Border Crossing", "Bus Station", "Bus Line", "Bus Stop", "Cable Car", "Cruise",
                "General Travel", "Hotel", "Bed & Breakfast", "Boarding House", "Hostel", "Hotel Pool", "Motel", "Resort", "Roof Deck", "Intersection",
                "Light Rail Station", "Metro Station", "Moving Target", "Pier", "RV Park", "Rental Car Location", "Rest Area", "Road", "Street",
                "Taxi Stand", "Taxi", "Toll Booth", "Toll Plaza", "Tourist Information Center", "Train Station", "Platform", "Train", "Tram Station",
                "Transportation Service", "Travel Lounge", "Tunnel", "Health & Beauty Service", "Spa", "Medical Center", "Gym", "Gym / Fitness Center",
                "Salon / Barbershop", "Gymnastics Gym", "Plane", "Massage Studio", "Nail Salon", "Yoga Studio", "College Gym", "Gas Station", "Climbing Gym",
                "Gym Pool", "Boxing Gym", "Cycle Studio", "Tanning Salon", "Pool Hall", "Travel & Transport", "Heliport", "Airport Service"};

        String[] category8 = {"Animal Shelter", "Auditorium", "Building", "Club House", "Community Center", "Convention Center", "Meeting Room",
                "Cultural Center", "Distribution Center", "Event Space", "Factory", "Fair", "Funeral Home", "Government Building", "Capitol Building",
                "City Hall", "Courthouse", "Embassy / Consulate", "Fire Station", "Police Station", "Town Hall", "Library", "Medical Center,Acupuncturist",
                "Alternative Healer", "Chiropractor", "Dentist's Office", "Doctor's Office", "Emergency Room", "Eye Doctor", "Hospital", "Laboratory",
                "Mental Health Office", "Veterinarian", "Military Base", "Non-Profit", "Office", "Advertising Agency", "Campaign Office", "Conference Room",
                "Corporate Cafeteria", "Coworking Space", "Tech Startup", "Parking", "Post Office", "Prison", "Radio Station", "Recruiting Agency", "School",
                "Circus School", "Driving School", "Elementary School", "Flight School", "High School", "Language School", "Middle School", "Music School",
                "Nursery School", "Preschool", "Private School", "Religious School", "Swim School", "Social Club", "Spiritual Center", "Buddhist Temple",
                "Church", "Hindu Temple", "Monastery", "Mosque", "Prayer Room", "Shrine", "Synagogue", "Temple", "TV Station,Voting Booth", "Warehouse",
                "Residence Assisted Living", "Home (private)", "Housing Development,Residential Building (Apartment / Condo)", "Trailer Park", "Medical Center",
                "Other Event", "Conference", "College Auditorium", "Residential Building (Apartment / Condo)", "Home Service", "College Residence Hall",
                "Travel Agency", "College Academic Building", "University", "Photography Studio", "Student Center", "Parade", "Laundry Service", "Pet Service",
                "Recording Studio", "TV Station", "Lawyer", "College Library", "Laundromat", "College Administrative Building", "Housing Development", "Gas Station",
                "Sorority House", "Fraternity House", "Martial Arts Dojo", "Newsstand", "Assisted Living", "College Lab", "Real Estate Office", "Dance Studio",
                "Photography Lab", "College Classroom", "Voting Booth", "Community College", "Convention", "College Arts Building", "General College & University",
                "Trade School", "Medical School", "Locksmith", "College Engineering Building", "Law School", "College & University", "Professional & Other Places",
                "College Rec Center", "College Track", "College Science Building", "College History Building", "Canal", "Maternity Clinic", "Business Center",
                "Canal Lock", "Business Center", "College Quad", "Auto Workshop", "College Technology Building", "Recycling Facility", "Storage Facility",
                "College Communications Building", "IT Services", "Wedding Hall", "Adult Education Center"};

        String[][] categoriesList = {category1, category2, category3, category4, category5, category6, category7, category8};

        String generalCategory = "";

        for(String c: categories){
            int i=0;
            for(String[] list : categoriesList){
                i++;
                for(String cat: list){
                    if(c.equals(cat)) {
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
