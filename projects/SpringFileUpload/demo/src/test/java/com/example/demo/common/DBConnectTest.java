package com.example.demo.common;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;

public class DBConnectTest {
	
    private static final String DRIVER = "org.h2.Driver";
    private static final String URL = "jdbc:h2:~/test";
    private static final String USER = "sa";
    private static final String PW = "sa";

    @Test
    public void testConnect() throws Exception {

        Class.forName(DRIVER);

        try (Connection con = DriverManager.getConnection(URL, USER, PW)) {
            System.out.println(con);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
