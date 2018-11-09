package com.bfach2;

import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Establishes connection to database, handles query and the return of results as an Entry list
 */
public class EntryDatabaseObject {

  // Constants

  // constants for db properties
  private static final String PROPERTIES_FILE = "resources/databaseConnection.properties";
  private static final Properties PROPERTIES = new Properties();
  private static final String HOST = "host";
  private static final String PORT = "port";
  private static final String DB_TYPE = "dbType";
  private static final String DB_NAME = "db";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";

  private static final List<String> QUERY_FIELD_NAMES = Arrays.asList(
      "guides.First", "locations.location", "reservation.First", "reservation.Last",
      "reservation.StartDay");
  private static final List<String> QUERY_TABLE_NAMES = Arrays.asList("guides",
      "locations", "reservation");
  private static final List<String> QUERY_FILTERS = Arrays
      .asList("reservation.guide=guides.idguides", "reservation.location=locations.idlocations",
          "reservation.StartDay < ");

  private static final int GUIDE_INDEX = 0;
  private static final int LOCATION_INDEX = 1;
  private static final int FIRST_NAME_INDEX = 2;
  private static final int LAST_NAME_INDEX = 3;
  private static final int DAY_INDEX = 4;

  // load constants from properties file
  static {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream properties = classLoader.getResourceAsStream(PROPERTIES_FILE);

    try {
      PROPERTIES.load(properties);
    } catch (IOException ex) {
      System.err.println("Could not load properties file from resources.");
    }
  }

  // class attributes

  // used as URL to connect to DB
  private String dbURL;

  // info needed to authenticate with db
  private String username;
  private String password;

  // constructor
  public EntryDatabaseObject() {
    // build up the URL string from properties
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("jdbc:");
    strBuilder.append(PROPERTIES.getProperty(DB_TYPE) + "://");
    strBuilder.append(PROPERTIES.getProperty(HOST) + ":");
    strBuilder.append(PROPERTIES.getProperty(PORT) + "/");
    strBuilder.append(PROPERTIES.getProperty(DB_NAME));
    this.dbURL = strBuilder.toString();

    // grab authentication details from properties
    this.username = PROPERTIES.getProperty(USERNAME);
    this.password = PROPERTIES.getProperty(PASSWORD);

    // wire in the mysql driver
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException cnfe) {
      System.out.println("Error loading driver " + cnfe.getMessage());
    }

  }

  public List<Entry> getEntries(LocalDate start) throws SQLException {
    // hold all of the relevant entries in a list
    List<Entry> entries = new ArrayList<>();

    // try to connect to the database holding tables relevant to the entry
    Connection connection = DriverManager.getConnection(dbURL, username, password);

    // query the DB for entries in the date range
    String queryString = generateQueryString(start);
    Statement statement = connection
        .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    try {
      System.out.println("Submitting query: " + queryString);
      ResultSet results = statement.executeQuery(queryString);

      // each row is an entry; parse fields out and create entry object
      while (results.next()) {
        // gather results from query, generate Entry objects
        String guide = results.getString(QUERY_FIELD_NAMES.get(GUIDE_INDEX));
        String location = results.getString(QUERY_FIELD_NAMES.get(LOCATION_INDEX));
        String firstName = results.getString(QUERY_FIELD_NAMES.get(FIRST_NAME_INDEX));
        String lastName = results.getString(QUERY_FIELD_NAMES.get(LAST_NAME_INDEX));
        String startDate = results.getString(QUERY_FIELD_NAMES.get(DAY_INDEX));

        Entry resultEntry = new Entry.Builder().guide(guide).location(location)
            .startDate(toLocalDate(startDate)).firstName(firstName).lastName(lastName).build();

        // store entry object for result return
        entries.add(resultEntry);
      }
    } catch (SQLException sqlEx) {
      sqlEx.printStackTrace();
    }

    return entries;
  }

  private String generateQueryString(LocalDate startDate) {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("SELECT ");
    strBuilder.append(QUERY_FIELD_NAMES.stream().collect(Collectors.joining(",")));
    strBuilder.append(" FROM ");
    strBuilder.append(QUERY_TABLE_NAMES.stream().collect(Collectors.joining(",")));
    strBuilder.append(" WHERE ");
    strBuilder.append(QUERY_FILTERS.stream().collect(Collectors.joining(" AND ")));
    strBuilder.append("'" + startDate.toString() + "'");

    return strBuilder.toString();
  }

  private LocalDate toLocalDate(String date) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return LocalDate.parse(date, dateFormatter);
  }

  public static void main(String[] args) {
    EntryDatabaseObject entryDBO = new EntryDatabaseObject();
    try {
      List<Entry> entries = entryDBO.getEntries(LocalDate.now());
      for (Entry entry : entries) {
        System.out.println(entry.toString());
      }
    } catch (SQLException sqlEx) {
      System.out.println(sqlEx);
    }
  }

}
