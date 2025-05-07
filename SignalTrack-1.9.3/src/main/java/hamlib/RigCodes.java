package hamlib;

import java.awt.event.ItemEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import components.ThreeDimArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class RigCodes implements AutoCloseable {

    private static final boolean DEBUG = true;
    private static final String ENV_RIGCTLD_PATH = "ENV_RIGCTLD_PATH";
    private static final String DEFAULT_RIGCTLD_PATH = System.getenv("RIGCTLD_PATH");
    private static final String ENV_SELECTED_INDEX = "ENV_SELECTED_INDEX";
    private static final int DEFAULT_SELECTED_INDEX = 0;
    private static ThreeDimArrayList<Integer, String, String> radioCodeList = getRadioCodeList(DEBUG);
    private static final Logger LOG = Logger.getLogger(RigCodes.class.getName());

    private JComboBox<String> comboBox;
    private DefaultComboBoxModel<String> defaultComboBoxModel;
    
    public RigCodes() {
        this(Preferences.systemRoot().node(RigCodes.class.getName()).getInt(ENV_SELECTED_INDEX, DEFAULT_SELECTED_INDEX));
    }

    public RigCodes(int rigCode) {
    	configureLogger();
        String[] strArray = new String[radioCodeList.size()];
        for (int i = 0; i < radioCodeList.size(); i++) {
            strArray[i] = String.format("%s  %s", radioCodeList.getT2(i), radioCodeList.getT3(i));
        }
        defaultComboBoxModel = new DefaultComboBoxModel<>(strArray);
        comboBox = new JComboBox<>(defaultComboBoxModel);
        comboBox.setSelectedIndex(rigCode);
        addListeners(); 
    }
    
    private void configureLogger() {
		Handler fh = null;
		Handler ch = null;
		try {
			fh = new FileHandler("%t/RigCodes.log");
			ch = new ConsoleHandler();
			LOG.addHandler(fh);
			LOG.setLevel(Level.FINEST);
			LOG.addHandler(ch);
			LOG.setUseParentHandlers(false);
		} catch (SecurityException | IOException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

    public void setManufacturerModel(String manufacturer, String model) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String cb = comboBox.getItemAt(i);
            if (cb.toUpperCase(Locale.US).contains(manufacturer.toUpperCase(Locale.US)) && cb.toUpperCase(Locale.US).contains(model.toUpperCase(Locale.US))) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
        if (comboBox.getSelectedIndex() == -1 && comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    public static String runSysCommand(String[] array) {
        String result = "";
        try {
            Process process = new ProcessBuilder(array).redirectErrorStream(true).start();
            Scanner scanner;
            try (InputStream inputStream = process.getInputStream()) {
                scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                result = scanner.useDelimiter("\\Z").next();
                process.destroy();
            }
            scanner.close();
        } catch (IOException e) {
        	LOG.log(Level.WARNING, "IOException", e);
        }
        return result;
    }

    public static ThreeDimArrayList<Integer, String, String> getRadioCodeList(boolean debug) {
        ThreeDimArrayList<Integer, String, String> list = new ThreeDimArrayList<>();
        String s = runSysCommand(new String[]{getRigctldPath(), "-l"});
        for (var item : s.split(System.getProperty("line.separator"))) {
            if (item.length() >= 55) {
                try {
                    String rigCode = StringUtils.normalizeSpace(item.substring(0, 6));
                    String mfr = StringUtils.normalizeSpace(item.substring(8, 31));
                    String mod = StringUtils.normalizeSpace(item.substring(31, 55));
                    list.add(Integer.parseInt(rigCode), mfr, mod);
                    if (debug) {
                    	LOG.log(Level.INFO, "{0} {1} {2}", new Object[] {rigCode, mfr, mod});
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                	LOG.log(Level.INFO, "Invalid line: {0}", item);
                }
            }
        }
        list = sortByT2T3(list);
        return list;
    }

    private static ThreeDimArrayList<Integer, String, String> sortByT2T3(ThreeDimArrayList<Integer, String, String> unsorted) {
        ThreeDimArrayList<Integer, String, String> sorted = new ThreeDimArrayList<>();

        List<String> list = new ArrayList<>();

        for (int i = 0; i < unsorted.size(); i++) {
            String str = unsorted.getT2(i) + "," + unsorted.getT3(i) + "," + unsorted.getT1(i).toString();
            list.add(str);
        }

        Collections.sort(list);

        list.stream().map(str -> str.split(",")).forEachOrdered(s -> sorted.add(Integer.parseInt(s[2]), s[0], s[1]));

        return sorted;
    }

    public static int getRigCode(String manufacturer, String model) {
        int rigCode = 0;
        if (radioCodeList == null) {
            radioCodeList = getRadioCodeList(DEBUG);
        }
        for (int i = 0; i < radioCodeList.size(); i++) {
            if (radioCodeList.get(i).getT2().replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US).contains(manufacturer.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US))
                    && radioCodeList.get(i).getT3().replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US).contains(model.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US))
                    || manufacturer.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US).contains(radioCodeList.get(i).getT2().replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US))
                    && model.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US).contains(radioCodeList.get(i).getT3().replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.US))) {
                rigCode = radioCodeList.get(i).getT1();
            }
        }
        return rigCode;
    }

    public static Set<String> getManufacturerSet() {
        if (radioCodeList == null) {
            radioCodeList = getRadioCodeList(DEBUG);
        }
        return new TreeSet<>(radioCodeList.getT2List());
    }

    public static Set<String> getModelSetForManufacturer(String manufacturer) {
        if (radioCodeList == null) {
            radioCodeList = getRadioCodeList(DEBUG);
        }
        Set<String> modelSet = new TreeSet<>();
        for (int i = 0; i < radioCodeList.size(); i++) {
            if (radioCodeList.get(i).getT2().contains(manufacturer)) {
                modelSet.add(radioCodeList.get(i).getT3());
            }
        }
        return modelSet;
    }

    public static String findExeFileOnWindows(String filename) {
        File f = new File("\\");
        Pattern p = Pattern.compile("(?i).*?" + filename + "\\.exe.*");
        return recurseSearch(f, p);
    }

    public static String recurseSearch(File dir, Pattern search) {
        String result = null;
        File[] listFile = dir.listFiles();

        if (listFile != null) {
            for (File f : listFile) {
                if (f.isDirectory()) {
                    result = recurseSearch(f, search);
                    if (result != null) {
                        return result;
                    }
                } else {
                    if (search.matcher(f.getName()).matches()) {
                        return f.getPath();
                    }
                }
            }
        }
        return result;
    }

    public static String getRigctldPath() {
        String os = System.getProperty("os.name").toLowerCase(Locale.US);
        if (os.contains("win")) {
            String rigctldPath = Preferences.systemRoot().node(RigCodes.class.getName()).get(ENV_RIGCTLD_PATH, DEFAULT_RIGCTLD_PATH);
            File f = new File(rigctldPath);
            if (!f.exists() || f.isDirectory()) {
                rigctldPath = findExeFileOnWindows("rigctld");
                rigctldPath = "\"" + rigctldPath + "\"";
                Preferences.systemRoot().node(RigCodes.class.getName()).put(ENV_RIGCTLD_PATH, rigctldPath);
            }
            return rigctldPath;
        } else {
            return "rigctld";
        }
    }

    public JComboBox<String> getComboBox() {
        return comboBox;
    }

    public int getSelectedRigCode() {
        return radioCodeList.get(comboBox.getSelectedIndex()).getT1();
    }

    public String getSelectedManufacturer() {
        return radioCodeList.get(comboBox.getSelectedIndex()).getT2();
    }

    public String getSelectedModel() {
        return radioCodeList.get(comboBox.getSelectedIndex()).getT3();
    }

    private void addListeners() {
        comboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
            	Preferences.systemRoot().node(RigCodes.class.getName()).putInt(ENV_SELECTED_INDEX, comboBox.getSelectedIndex());
            }
        });
    }
    
    @Override
    public void close() {
    	for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
	        handler.close();
		}
    }

}
