package json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;

/**
 *
 * @author n1ctf
 */
public class JsonReader implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(JsonReader.class.getName());
    private final boolean debug;
    private BufferedReader rd;
    
    public JsonReader(boolean debug) {
        this.debug = debug;
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
    }

    private synchronized String readAll(final Reader rd) {
        final StringBuilder sb = new StringBuilder(8192);
        try {
        	int cp;
        	while (rd.ready()) {
        		cp = rd.read();
				sb.append((char) cp);
				if (cp == 13 || cp == 10 || cp == -1) {
					break;
				}
        	}
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return sb.toString();
    }

    public static List<String> jsonArrayToStringList(final JSONArray jsonArray) {
        final List<String> list = new ArrayList<>(1);
        for (Object json : jsonArray) {
            list.add(getCleanText(json.toString()));
        }
        return list;
    }

    public String readJsonFromUrl(final String uri) {
        String str = null;
        try (InputStream is = new URI(uri).toURL().openStream()) { 
        	rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            str = readAll(rd).replaceAll("\\s", "");
            if (debug) {
                LOG.log(Level.INFO, "******** JsonReader ********* \rRequest: {0}\rReply: {1}\r****** END JsonReader *******", new Object[]{uri, str});
            }
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE, null, e);
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, null, e);
        } catch (IllegalArgumentException e) {
            LOG.log(Level.SEVERE, null, e);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return str;
    }

    private static String getCleanText(final String text) {
        int i = text.indexOf(':');
        return text.substring(i + 2, text.length() - 2);
    }

    @Override
    public void close() {
    	try {
			if (rd != null) rd.close();
		} catch (IOException e) {
			LOG.log(Level.WARNING, e.getMessage());
		}
    }

}
