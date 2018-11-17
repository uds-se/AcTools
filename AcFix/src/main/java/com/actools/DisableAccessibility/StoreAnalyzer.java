package fr.inria.DisableAccessibility;

import com.akdeniz.googleplaycrawler.GooglePlay;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import com.akdeniz.googleplaycrawler.Utils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileInputStream;
import java.util.Properties;

public class StoreAnalyzer {

    private static GooglePlayAPI service;

    private static HttpClient getProxiedHttpClient(String host, Integer port) throws Exception {
        HttpClient client = new DefaultHttpClient(GooglePlayAPI.getConnectionManager());
        client.getConnectionManager().getSchemeRegistry().register(Utils.getMockedScheme());
        HttpHost proxy = new HttpHost(host, port);
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        return client;
    }

    public static void setup() throws Exception {

        Properties properties = new Properties();
        properties.load(new FileInputStream("./src/main/resources/login.conf"));

        String email = properties.getProperty("email");
        String password = properties.getProperty("password");
        String id = properties.getProperty("androidid");
        String securitytoken = properties.getProperty("securitytoken");

        service = new GooglePlayAPI(email, password, id);

        service.setClient(new DefaultHttpClient((GooglePlayAPI.getConnectionManager())));
        service.login(securitytoken);
    }

    public static void fetchPermissions() throws Exception {
        GooglePlay.DetailsResponse details = service.details("com.instagram.android");
        GooglePlay.AppDetails appDetails = details.getDocV2().getDetails().getAppDetails();
        appDetails.getPermissionList();
        int test = 12;
    }
}
