import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ScraperDriver {
    static Connection crunchifyConn = null;
    static PreparedStatement crunchifyPrepareStat = null;
    static DBConfig dbConfig;
    static String url;

    static final Logger logger = Logger.getLogger(ScraperDriver.class);

    public static void main(String[] args){
        PropertyConfigurator.configure("log4j.properties");
        Validate.isTrue(args.length >= 1, "usage: supply url to fetch");
        Validate.isTrue(args.length == 2, "usage: supply how many time to fetch");
        url = args[0];
        print("Fetching %s...", url);

        int limit = Integer.parseInt(args[1]);

        dbConfig = DBConfig.Scrape;
        makeJDBCConnection();

        for(int i=0;((limit==-1)?true:(i<limit));i++) {
            getandAddQuote();
        }
    }

    private static void print(String msg, Object... args) {
        logger.info(String.format(msg, args));
    }

    private static void makeJDBCConnection() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Sorry, couldn't found JDBC driver. Make sure you have added JDBC Maven Dependency Correctly");
            logger.warn(e.getMessage());
            return;
        }

        try {
            // DriverManager: The basic service for managing a set of JDBC drivers.
            crunchifyConn = DriverManager.getConnection("jdbc:mysql://"+dbConfig.host+":"+dbConfig.port+"/"+dbConfig.db,
                    dbConfig.user, dbConfig.password);
            if (crunchifyConn == null) {
                logger.error("Failed to make connection!");
            }
        } catch (SQLException e) {
            logger.error("MySQL Connection Failed!");
            logger.error(e.getMessage());
            return;
        }

    }

    private static void addDataToDB(String quote) {

        try {
            String insertQueryStatement = "INSERT  INTO  quotes(quote)  VALUES  (?)";

            crunchifyPrepareStat = crunchifyConn.prepareStatement(insertQueryStatement);
            crunchifyPrepareStat.setString(1, quote);

            // execute insert SQL statement
            crunchifyPrepareStat.executeUpdate();
            logger.info(quote + " added successfully");
        } catch (SQLException e) {
            logger.warn(e.getMessage());
        }
    }

    private static void getandAddQuote(){
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.8,id;q=0.6")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Host", "setomulyadi.ml")
                    .header("Upgrade-Insecure-Requests","1")
                    .header("Accept-Encoding", "gzip,deflate,sdch").get();
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }

        Element content = doc.getElementsByClass("starter-template").first();
        if(content!=null){
            String quote = content.getElementsByTag("h1").text();
            print(quote);
            addDataToDB(quote);
        } else {
            logger.error("null content");
        }
    }
}
