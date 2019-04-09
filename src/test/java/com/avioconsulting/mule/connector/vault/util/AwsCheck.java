package com.avioconsulting.mule.connector.vault.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AwsCheck {

    final static private String AMI_URI = "http://169.254.169.254/latest/meta-data/ami-id";

    private static String getAmiId() {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(AMI_URI);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(500);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e) {

        }
        return result.toString();
    }

    public static boolean isExecutingOnAws() {
        return !getAmiId().isEmpty();
    }
}
