package database;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import components.Inet4AddressPanel;
import tcp.TcpPortPanel;

public class DatabaseConfigComponent extends JDialog {

    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_CHANGE = "PROPERTY_CHANGE";

    private JTabbedPane tabbedPane;
    private JPanel configPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JLabel cryptoKeyLabel;
    private JTextField cryptoKey;
    private JPanel databaseConnectionPanel;
    private JPasswordField password;
    private JPanel credentialsPanel;
    private JLabel passwordLabel;
    private JPanel securityPanel;
    private JLabel classNameLabel;
    private JComboBox<String> className;
    private JLabel manualConnectionStringLabel;
    private JFormattedTextField connectionURL;
    private JCheckBox useAESEncryptionCheckBox;
    private JFormattedTextField userName;
    private JLabel userNameLabel;
    private final transient DatabaseConfig config;
    private Inet4AddressPanel ipv4;
    private TcpPortPanel port;

    public DatabaseConfigComponent(DatabaseConfig config) {
        this.config = config;

        initComponents();
        configureComponents();
        createGUI();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        configPanel = new JPanel();
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        applyButton = new JButton("Apply");
        securityPanel = new JPanel();
        userName = new JFormattedTextField(config.getUserName());
        password = new JPasswordField(config.getPassword());
        userNameLabel = new JLabel();
        passwordLabel = new JLabel();
        cryptoKey = new JTextField(config.getCryptoKey());
        cryptoKeyLabel = new JLabel();
        useAESEncryptionCheckBox = new JCheckBox();
        databaseConnectionPanel = new JPanel();

        final DefaultComboBoxModel<String> sqlDatabaseURIModel = new DefaultComboBoxModel<>(config.getDatasourceClassNameList());

        className = new JComboBox<>(sqlDatabaseURIModel);
        classNameLabel = new JLabel();
        connectionURL = new JFormattedTextField();
        manualConnectionStringLabel = new JLabel();

        credentialsPanel = new JPanel();

        port = new TcpPortPanel(config.getPort());
        ipv4 = new Inet4AddressPanel((Inet4Address) config.getInetAddress());
    }

    private void configureComponents() {
        setTitle("Database Settings");

        okButton.setMultiClickThreshhold(50L);
        okButton.addActionListener(_ -> {
            applyButton.doClick();
            dispose();
        });

        cancelButton.setMultiClickThreshhold(50L);
        cancelButton.addActionListener(_ -> dispose());

        applyButton.setMultiClickThreshhold(50L);
        applyButton.addActionListener(_ -> config.saveSettings());

        tabbedPane.addTab(" Database Configuration ", null, configPanel, null);

        credentialsPanel.setBorder(BorderFactory.createTitledBorder("Credentials"));

        databaseConnectionPanel.setBorder(BorderFactory.createTitledBorder("Database Connection"));

        classNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        classNameLabel.setText("Datasource Class");

        manualConnectionStringLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        manualConnectionStringLabel.setText("Connection String");

        className.setSelectedIndex(config.getDriver());
        className.addItemListener(this::classNameComboBoxActionPerformed);

        securityPanel.setBorder(BorderFactory.createTitledBorder("Security"));

        connectionURL.setText(config.getDatasourceURL());

        userName.setFont(new Font("Calabri", Font.PLAIN, 11));
        userName.setMinimumSize(new Dimension(150, 20));
        userName.addActionListener(this::userNameChangedEvent);

        userName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                userName.setFont(new Font("Calabri", Font.BOLD, 11));
            }

            @Override
            public void focusLost(FocusEvent e) {
                userName.setFont(new Font("Calabri", Font.PLAIN, 11));
            }
        });

        userName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NO OP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    userName.setFont(new Font("Calabri", Font.PLAIN, 11));
                    userName.transferFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NO OP
            }
        });

        password.setFont(new Font("Calabri", Font.PLAIN, 11));
        password.setMinimumSize(new Dimension(150, 20));
        password.addActionListener(this::passwordChangedEvent);

        password.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                password.setFont(new Font("Calabri", Font.BOLD, 11));
            }

            @Override
            public void focusLost(FocusEvent e) {
                password.setFont(new Font("Calabri", Font.PLAIN, 11));
            }
        });

        password.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NO OP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    password.setFont(new Font("Calabri", Font.PLAIN, 11));
                    password.transferFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NO OP
            }
        });

        userNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userNameLabel.setText("Username");

        passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordLabel.setText("Password");

        cryptoKeyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cryptoKeyLabel.setText("Encryption Key");

        cryptoKey.addActionListener(this::cryptoKeyActionPerformed);

        useAESEncryptionCheckBox.setText("Use AES Encryption");
        useAESEncryptionCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
        useAESEncryptionCheckBox.setSelected(config.isUseAESEncryption());

        ipv4.addPropertyChangeListener(evt -> {
            if ("ipv4Changed".equals(evt.getPropertyName())) {
                config.setInetAddress((InetAddress) evt.getNewValue());
            }
        });

        port.addPropertyChangeListener(evt ->  {
            if ("portChanged".equals(evt.getPropertyName())) {
                config.setPort((int) evt.getNewValue());
            }
        });
    }

    private void userNameChangedEvent(ActionEvent evt) {
    	final JFormattedTextField jtf = (JFormattedTextField) evt.getSource();
        config.setUserName(jtf.getText());
    }

    private void passwordChangedEvent(ActionEvent evt) {
    	final JPasswordField jpf = (JPasswordField) evt.getSource();
        config.setPassword(jpf.getPassword());
    }

    private void cryptoKeyActionPerformed(ActionEvent evt) {
    	final JFormattedTextField jtf = (JFormattedTextField) evt.getSource();
        config.setCryptoKey(jtf.getText());
    }

    private void classNameComboBoxActionPerformed(ItemEvent evt) {
    	final JComboBox<?> jcb = (JComboBox<?>) evt.getSource();
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            config.setDriver(jcb.getSelectedIndex());
            connectionURL.setText(config.getDatasourceURL());
        }
    }

    private void createGUI() {
        final GroupLayout layout = new GroupLayout(getContentPane());

        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(tabbedPane)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton, 80, 80, 80)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(applyButton, 80, 80, 80)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, 80, 80, 80))
                                )
                                .addContainerGap()
                        )
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(applyButton)
                                        .addComponent(okButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        )
        );

        final GroupLayout securityPanelLayout = new GroupLayout(securityPanel);

        securityPanel.setLayout(securityPanelLayout);

        securityPanelLayout.setHorizontalGroup(
                securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(securityPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(cryptoKeyLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(securityPanelLayout.createSequentialGroup()
                                                .addComponent(useAESEncryptionCheckBox)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(cryptoKey))
                                .addContainerGap()));

        securityPanelLayout.setVerticalGroup(
                securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(securityPanelLayout.createSequentialGroup()
                                .addGroup(securityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(cryptoKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cryptoKeyLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(useAESEncryptionCheckBox)
                                .addContainerGap(12, Short.MAX_VALUE)));

        final GroupLayout databaseConnectionPanelLayout = new GroupLayout(databaseConnectionPanel);

        databaseConnectionPanel.setLayout(databaseConnectionPanelLayout);

        databaseConnectionPanelLayout.setHorizontalGroup(
                databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(databaseConnectionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(classNameLabel)
                                        .addComponent(manualConnectionStringLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(connectionURL, GroupLayout.PREFERRED_SIZE, 500, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(databaseConnectionPanelLayout.createSequentialGroup()
                                                .addComponent(className, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(ipv4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(port, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        databaseConnectionPanelLayout.setVerticalGroup(
                databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(databaseConnectionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(ipv4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(className, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(classNameLabel)
                                        .addComponent(port, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(databaseConnectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(connectionURL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(manualConnectionStringLabel))
                                .addContainerGap(24, Short.MAX_VALUE))
        );

        final GroupLayout credentialsPanelLayout = new GroupLayout(credentialsPanel);

        credentialsPanel.setLayout(credentialsPanelLayout);

        credentialsPanelLayout.setHorizontalGroup(
                credentialsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(credentialsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(credentialsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(userNameLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(passwordLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(credentialsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(password, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(userName, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        credentialsPanelLayout.setVerticalGroup(
                credentialsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(credentialsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(credentialsPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(userNameLabel)
                                        .addComponent(userName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(credentialsPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(passwordLabel)
                                        .addComponent(password, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout configPanelLayout = new GroupLayout(configPanel);

        configPanel.setLayout(configPanelLayout);

        configPanelLayout.setHorizontalGroup(
                configPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(configPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(configPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(databaseConnectionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(securityPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(configPanelLayout.createSequentialGroup()
                                                .addComponent(credentialsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));

        configPanelLayout.setVerticalGroup(
                configPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(configPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(securityPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(databaseConnectionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(credentialsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension screenSize = tk.getScreenSize();

        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));

        setVisible(true);
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new RunnableImpl(args));
    }

    private static class RunnableImpl implements Runnable {

        public RunnableImpl(String[] args) {}

        @Override
        public void run() {
            new DatabaseConfigComponent(new DatabaseConfig(true));
        }
    }
}
