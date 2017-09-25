import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.sun.activation.registries.LogSupport.log;

public class ScraperDriver {
    static Connection crunchifyConn = null;
    static PreparedStatement crunchifyPrepareStat = null;
    static DBConfig dbConfig;
    static String url;

    public static void main(String[] args){
        Validate.isTrue(args.length >= 1, "usage: supply url to fetch");
        Validate.isTrue(args.length == 2, "usage: supply how many time to fetch");
        url = args[0];
        print("Fetching %s...", url);

        int limit = Integer.parseInt(args[1]);

        dbConfig = DBConfig.Scrape;
        makeJDBCConnection();

        for(int i=0;i<limit;i++) {
            getandAddQuote();
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static void makeJDBCConnection() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            log("Congrats - Seems your MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            log("Sorry, couldn't found JDBC driver. Make sure you have added JDBC Maven Dependency Correctly");
            e.printStackTrace();
            return;
        }

        try {
            // DriverManager: The basic service for managing a set of JDBC drivers.
            crunchifyConn = DriverManager.getConnection("jdbc:mysql://"+dbConfig.host+":"+dbConfig.port+"/"+dbConfig.db,
                    dbConfig.user, dbConfig.password);
            if (crunchifyConn != null) {
                log("Connection Successful! Enjoy. Now it's time to push data");
            } else {
                log("Failed to make connection!");
            }
        } catch (SQLException e) {
            log("MySQL Connection Failed!");
            e.printStackTrace();
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
            log(quote + " added successfully");
        } catch (

                SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }

        Element content = doc.getElementsByClass("starter-template").first();
        String quote = content.getElementsByTag("h1").text();
        print(quote);
        addDataToDB(quote);
    }
}
