import com.complexible.common.rdf.query.resultio.TextTableQueryResultWriter;
import com.complexible.stardog.api.*;
import com.complexible.stardog.jena.SDJenaFactory;
import com.stardog.stark.query.SelectQueryResult;
import com.stardog.stark.query.io.QueryResultWriters;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



public class ConnectionManager {
    private final static String to = "InMusic";
    private final static String username = "admin";
    private final static String password = "admin";
    private final static String url = "http://localhost:5820";
    private final static long blockCapacityTime = 900;
    private final static TimeUnit blockCapacityTimeUnit = TimeUnit.SECONDS;
    private final static long expirationTime = 300;
    private final static TimeUnit expirationTimeUnit = TimeUnit.SECONDS;
    private static Connection connection = null;
    private static ConnectionPool connectionPool;
    private static ConnectionManager INSTANCE = new ConnectionManager();

    private ConnectionManager() {
        instantiateConnection(url, to, username, password);
    }

    public static ConnectionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConnectionManager();
        }
        return INSTANCE;
    }

    /**
     * Obtains the Stardog connection from the connection pool
     *
     * @return Stardog Connection
     */
    private static Connection getConnection() {
        return connectionPool.obtain();
    }

    public static void instantiateConnection(String server, String db, String username, String password) {
        final boolean reasoningType = true;

        ConnectionConfiguration connectionConfig = ConnectionConfiguration
                .to(db)
                .server(server)
                .reasoning(reasoningType)
                .credentials(username, password);

        // creates the Stardog connection pool
        connectionPool = createConnectionPool(connectionConfig);
        connection = getConnection();
    }

    /**
     * Now we want to create the configuration for our pool.
     *
     * @param connectionConfig the configuration for the connection pool
     * @return the newly created pool which we will use to get our Connections
     */
    public static ConnectionPool createConnectionPool(ConnectionConfiguration connectionConfig) {
        final int maxPool = 200;
        final int minPool = 10;
        ConnectionPoolConfig poolConfig= ConnectionPoolConfig
                .using(connectionConfig)
                .minPool(minPool)
                .maxPool(maxPool)
                .expiration(expirationTime, expirationTimeUnit)
                .blockAtCapacity(blockCapacityTime, blockCapacityTimeUnit);
        return poolConfig.create();
    }

    public BidiMap<String, String> getSongs() {
        SelectQuery query = connection.select("prefix me: <"+MusicOntology.NS+">" +
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
                "PREFIX mo: <http://purl.org/ontology/mo/>"+
                "select DISTINCT ?song ?songTitle ?artist where { ?song rdf:type mo:Track." +
                "?song dc:title  ?songTitle." +
                "?song foaf:maker ?maker." +
                "?maker foaf:name ?artist.}");
        List<String> songsURI =  query.execute().stream()
                .map(x -> Objects.requireNonNull(x.get("song")).toString())
                .collect(Collectors.toList());
        List<String> titles = getValuesFromQuery(query, "songTitle");
        List<String> artists = getValuesFromQuery(query, "artist");

        BidiMap<String, String> output = new DualHashBidiMap<>();
        for (int i = 0; i < titles.size(); i++) {
            output.put(songsURI.get(i),artists.get(i) + " - " + titles.get(i));
        }
        return output;
    }
    public HashMap<String, String> getLocations() {
        SelectQuery query = connection.select("prefix me: <"+MusicOntology.NS+">" +
                "select DISTINCT ?location ?locationName WHERE { ?location rdf:type me:Location." +
                "?location dc:title ?locationName.}");
        List<String> locationsName = getValuesFromQuery(query, "locationName");
        List<String> locationsURI = query.execute().stream().map(x -> String.valueOf(x.get("location")))
                .map(x -> x.substring(x.lastIndexOf("/")+1))
                .collect(Collectors.toList());
        HashMap<String, String> output = new HashMap<>();
        for (int i = 0; i < locationsName.size(); i++) {
            output.put(locationsURI.get(i), locationsName.get(i));
        }
        return output;

    }
    public List<String> getImages(){
        SelectQuery query = connection.select("prefix me: <"+MusicOntology.NS+">" +
                "select DISTINCT ?image  WHERE { ?image rdf:type me:Image." +
                "?image me:represents ?something}" );
        return query.execute().stream().map(x -> String.valueOf(x.get("image")))
                .map(x -> x.substring(x.lastIndexOf("/")+1))
                .collect(Collectors.toList());
    }
    public BidiMap<String, String> getActivityType() {
        SelectQuery query = connection.select("prefix me: <"+MusicOntology.NS+">" +
                "select DISTINCT ?activityName ?activityType WHERE { " +
                "?activity rdf:type me:Activity." +
                "?activity me:hasType ?activityType." +
                "?activityType dc:title ?activityName.}");
        List<String> activitiesName = getValuesFromQuery(query, "activityName");
        List<String> activitiesURI = query.execute().stream()
                .map(x -> String.valueOf(x.get("activityType")))
                .map(x -> x.substring(x.lastIndexOf("/")+1))
                .collect(Collectors.toList());
        DualHashBidiMap<String, String> output = new DualHashBidiMap<>();
        for (int i = 0; i < activitiesName.size(); i++) {
            output.put(activitiesURI.get(i), activitiesName.get(i));
        }
        return output;
    }

    private static List<String> getValuesFromQuery(SelectQuery query, final String name) {
        return query.execute().stream()
                .map(x -> x.get(name))
                .filter(Objects::nonNull)
                .map(x -> getResult(x.toString()))
                .collect(Collectors.toList());
    }

    private static String getResult(final String toDecode) {
        char code = '"';
        return  toDecode.substring(toDecode.indexOf(code) + 1, toDecode.substring(toDecode.indexOf(code) + 1).indexOf(code) + 1);
    }

    public void addQuery(final String name, final String surname, final String song,
                         final String whileDoingName, final String whileDoingDesc, final String whileDoingActivityTypeString,
                         final String wantToDoName, final String wantToDoDesc, final String wantToDoActivityTypeString,
                         final String memoryName, final String memoryDesc,
                         final String locationName, final String locationDesc, final String oldLocation,
                         final String imgPre, final String imgPost) {
        RandomString uriGenerator = new RandomString(20);
        Resource locationResource;
        Resource listeningResource;
        Resource activityResource;
        Resource memoryResource;
        Resource songResource;
        Resource imgPreResource;
        Resource imgPostResource;
        String locationURI;
        String listeningURI = MusicOntology.NS + uriGenerator.nextString();
        String activityDoingURI = MusicOntology.NS + uriGenerator.nextString();
        String memoryURI = MusicOntology.NS + uriGenerator.nextString();
        String wantToDoURI = MusicOntology.NS + uriGenerator.nextString();


        Model aModel = SDJenaFactory.createModel(connection);
        aModel.begin();
        imgPreResource = aModel.createResource(MusicOntology.NS+imgPre);
        imgPostResource = aModel.createResource(MusicOntology.NS+imgPost);
        locationResource = aModel.createResource(MusicOntology.LOCATION_TYPE);
        listeningResource = aModel.createResource(MusicOntology.MUSIC_LISTENING_TYPE);
        activityResource = aModel.createResource(MusicOntology.ACTIVITY_TYPE);
        memoryResource =  aModel.createResource(MusicOntology.EPISODIC_MEMORY_TYPE);
        songResource = aModel.createResource(song);
        Resource location;

        if (!oldLocation.equals("")) {
            locationURI = oldLocation.substring(0,oldLocation.indexOf("-"));
            location = aModel.createResource(MusicOntology.NS+locationURI);
        }else{
            locationURI = generateRandomURI();
            location = aModel.createResource(MusicOntology.NS+locationURI)
                    .addProperty(RDF.type, locationResource);
            if(!locationDesc.isEmpty()) {
                location.addLiteral(aModel.createProperty(String.valueOf(DC_11.description)), locationDesc);
            }if(!locationName.isEmpty()){
                location.addLiteral(aModel.createProperty(String.valueOf(DC_11.title)), locationName);
            }
        }

        Resource activityDoing = aModel.createResource(activityDoingURI)
                .addProperty(RDF.type,activityResource)
                .addProperty(aModel.createProperty(MusicOntology.HAS_TYPE), aModel.createResource(MusicOntology.NS+whileDoingActivityTypeString));
        if(!whileDoingDesc.isEmpty()) {
            activityDoing.addLiteral(aModel.createProperty(String.valueOf(DC_11.description)), whileDoingDesc);
        }if(!whileDoingName.isEmpty()){
            activityDoing.addLiteral((aModel.createProperty(String.valueOf(DC_11.title))), whileDoingName);
        }
        Resource wantToDo = aModel.createResource(wantToDoURI)
                .addProperty(RDF.type,activityResource)
                .addProperty(aModel.createProperty(MusicOntology.HAS_TYPE), aModel.createResource(MusicOntology.NS+wantToDoActivityTypeString));
        if(!wantToDoName.isEmpty()) {
            wantToDo.addLiteral(aModel.createProperty(String.valueOf(DC_11.description)), wantToDoDesc);
        }if(!wantToDoDesc.isEmpty()){
            wantToDo.addLiteral(aModel.createProperty(String.valueOf(DC_11.title)), wantToDoName);
        }
        Resource memory = aModel.createResource(memoryURI)
                .addProperty(RDF.type,memoryResource);
        if(!memoryName.isEmpty()) {
            memory.addLiteral(aModel.createProperty(String.valueOf(DC_11.description)), memoryDesc);
        }if(!memoryDesc.isEmpty()){
            memory.addLiteral(aModel.createProperty(String.valueOf(DC_11.title)), memoryName);
        }
        aModel.createResource(listeningURI)
                .addProperty( RDF.type,listeningResource)
                .addProperty( FOAF.name,name)
                .addProperty(FOAF.surname,surname)
                .addProperty(aModel.createProperty(MusicOntology.LISTEN_TO),songResource)
                .addProperty(aModel.createProperty(MusicOntology.WHILE_DOING),activityDoing)
                .addProperty(aModel.createProperty(MusicOntology.WANT_DO),wantToDo)
                .addProperty(aModel.createProperty(MusicOntology.HAS_LOCATION),location)
                .addProperty(aModel.createProperty(MusicOntology.REVIVES_MEMORY),memory)
                .addProperty(aModel.createProperty(MusicOntology.PRE_LISTENING_IMAGE),imgPreResource)
                .addProperty(aModel.createProperty(MusicOntology.POST_LISTENING_IMAGE),imgPostResource);
        aModel.commit();
        aModel.close();
    }
    private String generateRandomURI(){
        byte[] random_uris = new byte[20]; // length is bounded by 7
        new Random().nextBytes(random_uris);
        return new String(random_uris, StandardCharsets.UTF_8);
    }
}
