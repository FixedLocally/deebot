package me.lkp111138.deebot;


import com.pengrad.telegrambot.TelegramBot;
import me.lkp111138.deebot.game.Game;
import okhttp3.OkHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

public class Main {
    private static Connection conn;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // read config
        try {
            File file = new File("deebot.cfg");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] segments = line.split("=", 2);
                System.setProperty(segments[0].trim(), segments[1].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // abort
            System.exit(1);
        }
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s&useSSL=false", System.getProperty("db.host"), System.getProperty("db.name"), System.getProperty("db.user"), System.getProperty("db.pwd")));
        OkHttpClient retrying_client = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
        TelegramBot bot = new TelegramBot.Builder(System.getProperty("bot.token")).okHttpClient(retrying_client).build();
        new DeeBot(bot);
        Game.init(bot);
        System.out.println("Initialization complete\n");
    }

    public static Connection getConnection() throws SQLException {
        try {
            PreparedStatement stmt = conn.prepareStatement("select ? as num");
            stmt.setInt(1, 42);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("num") == 42) {
                return conn;
            }
            return getConnection();
        } catch (SQLException e) {
            try {
                conn = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s&useSSL=false", System.getProperty("db.host"), System.getProperty("db.name"), System.getProperty("db.user"), System.getProperty("db.pwd")));
                return conn;
            } catch (SQLException e1) {
                System.err.println("Couldn't reconnect!");
                e.printStackTrace();
                throw e;
            }
        }
    }
}
