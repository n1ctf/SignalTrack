package geocode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import com.google.gson.Gson;

public final class GeoCoder {

    private final Gson gson = new Gson();

    private volatile long lastRequest;
    
    private GeoCoder() {}
    
    public GeocodeResponse getLocation(String[] addressElements) throws IOException {
    	final StringBuilder sb = new StringBuilder();
    	final Object obj = new Object();
        
        for (String string : addressElements) {
            if (sb.length() > 0) {
                sb.append('+');
            }
            sb.append(URLEncoder.encode(string.replace(' ', '+'), "UTF-8"));
        }
        
        final String url = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=" + sb.toString();
        
        // Google limits this web service to 2500/day and 10 requests/s
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URI(url).toURL().openStream()))) {
            synchronized (obj) {
            	final long elapsed = System.currentTimeMillis() - lastRequest;
                try {
                    while (elapsed < 100) {
                        obj.wait(100 - elapsed);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return gson.fromJson(br, GeocodeResponse.class);
        } catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
            lastRequest = System.currentTimeMillis();
        }
        return null;
    }
}
