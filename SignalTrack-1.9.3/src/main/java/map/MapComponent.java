package map;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import map.AbstractMap.MapEvent;
import map.AbstractMap.SignalTrackMapNames;

public class MapComponent extends JDialog {

    private static final long serialVersionUID = 1L;

    private JButton applyButton;
    private JButton cancelButton;
    private JButton okButton;

    private JRadioButton openStreetMapRadioButton;
    private JRadioButton virtualEarthStreetMapRadioButton;
    private JRadioButton virtualEarthSatelliteMapRadioButton;
    private JRadioButton virtualEarthHybridMapRadioButton;
    private JRadioButton worldwindMapRadioButton;

    private JCheckBox displayShapesCheckBox;
    private JCheckBox showStatusBarCheckBox;

    private JTabbedPane tabbedPane;

    private SignalTrackMapNames initialMapName;

    private final transient AbstractMap abstractMap;

    public MapComponent(AbstractMap abstractMap) {
        this.abstractMap = abstractMap;

        initializeComponents();
        drawGUI();

    }

    private void initializeComponents() {
    	final Toolkit tk = Toolkit.getDefaultToolkit();
        tk.getScreenSize();

        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Map Settings");

        applyButton = new JButton("Apply");
        applyButton.setMultiClickThreshhold(50L);

        cancelButton = new JButton("Cancel");
        cancelButton.setMultiClickThreshhold(50L);

        okButton = new JButton("OK");
        okButton.setMultiClickThreshhold(50L);

        openStreetMapRadioButton = new JRadioButton("Use OpenStreetMap Road AbstractMap");
        virtualEarthStreetMapRadioButton = new JRadioButton("Use Virtual Earth Street AbstractMap");
        virtualEarthSatelliteMapRadioButton = new JRadioButton("Use Virtual Earth Satellite AbstractMap");
        virtualEarthHybridMapRadioButton = new JRadioButton("Use Virtual Earth Hybrid AbstractMap");
        worldwindMapRadioButton = new JRadioButton("Use NASA Worldwind AbstractMap");

        displayShapesCheckBox = new JCheckBox("Display Shapes");
        showStatusBarCheckBox = new JCheckBox("Show Status Bar");

        final ButtonGroup selectedMapTypeButtonGroup = new ButtonGroup();

        selectedMapTypeButtonGroup.add(openStreetMapRadioButton);
        selectedMapTypeButtonGroup.add(virtualEarthStreetMapRadioButton);
        selectedMapTypeButtonGroup.add(virtualEarthSatelliteMapRadioButton);
        selectedMapTypeButtonGroup.add(virtualEarthHybridMapRadioButton);
        selectedMapTypeButtonGroup.add(worldwindMapRadioButton);

        tabbedPane = new JTabbedPane();

        final RadioButtonHandler rbh = new RadioButtonHandler();

        openStreetMapRadioButton.addItemListener(rbh);
        virtualEarthStreetMapRadioButton.addItemListener(rbh);
        virtualEarthSatelliteMapRadioButton.addItemListener(rbh);
        virtualEarthHybridMapRadioButton.addItemListener(rbh);
        worldwindMapRadioButton.addItemListener(rbh);

        displayShapesCheckBox.setSelected(abstractMap.isDisplayShapes());
        showStatusBarCheckBox.setSelected(abstractMap.isShowStatusBar());

        final ButtonModel model;

        model = switch (abstractMap.getSignalTrackMapName()) {
            case OpenStreetMap ->
                openStreetMapRadioButton.getModel();
            case VirtualEarthMap ->
                virtualEarthStreetMapRadioButton.getModel();
            case VirtualEarthSatellite ->
                virtualEarthSatelliteMapRadioButton.getModel();
            case VirtualEarthHybrid ->
                virtualEarthHybridMapRadioButton.getModel();
            case WorldWindMap ->
                worldwindMapRadioButton.getModel();
        };

        initialMapName = abstractMap.getSignalTrackMapName();

        selectedMapTypeButtonGroup.setSelected(model, true);

        displayShapesCheckBox.addActionListener(this::displayShapesCheckBoxActionEvent);

        showStatusBarCheckBox.addActionListener(this::showStatusBarCheckBoxActionEvent);

        okButton.addActionListener(_ -> okButtonActionEvent());

        cancelButton.addActionListener(_ -> cancelButtonActionEvent());

        applyButton.addActionListener(_ -> applyButtonActionEvent());

    }

    private void displayShapesCheckBoxActionEvent(ActionEvent event) {
    	final JCheckBox jcb = (JCheckBox) event.getSource();
        abstractMap.setDisplayShapes(jcb.isSelected());
        abstractMap.getPropertyChangeSupport().firePropertyChange(AbstractMap.MapEvent.DISPLAY_SHAPES.name(), null, jcb.isSelected());
    }

    private void showStatusBarCheckBoxActionEvent(ActionEvent event) {
    	final JCheckBox jcb = (JCheckBox) event.getSource();
        abstractMap.setShowStatusBar(jcb.isSelected());
        abstractMap.getPropertyChangeSupport().firePropertyChange(AbstractMap.MapEvent.SHOW_STATUS_BAR.name(), null, jcb.isSelected());
    }

    private class RadioButtonHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (tabbedPane.getTabCount() > 1) {
                tabbedPane.removeTabAt(1);
            }
            if (ie.getSource() == openStreetMapRadioButton) {
                abstractMap.getPropertyChangeSupport().firePropertyChange(MapEvent.MAP_PROVIDER_CHANGE.name(), initialMapName, SignalTrackMapNames.OpenStreetMap);
                initialMapName = SignalTrackMapNames.OpenStreetMap;
            } else if (ie.getSource() == virtualEarthStreetMapRadioButton) {
                abstractMap.getPropertyChangeSupport().firePropertyChange(MapEvent.MAP_PROVIDER_CHANGE.name(), initialMapName, SignalTrackMapNames.VirtualEarthMap);
                initialMapName = SignalTrackMapNames.VirtualEarthMap;
            } else if (ie.getSource() == virtualEarthSatelliteMapRadioButton) {
                abstractMap.getPropertyChangeSupport().firePropertyChange(MapEvent.MAP_PROVIDER_CHANGE.name(), initialMapName, SignalTrackMapNames.VirtualEarthSatellite);
                initialMapName = SignalTrackMapNames.VirtualEarthSatellite;
            } else if (ie.getSource() == virtualEarthHybridMapRadioButton) {
                abstractMap.getPropertyChangeSupport().firePropertyChange(MapEvent.MAP_PROVIDER_CHANGE.name(), initialMapName, SignalTrackMapNames.VirtualEarthHybrid);
                initialMapName = SignalTrackMapNames.VirtualEarthHybrid;
            } else if (ie.getSource() == worldwindMapRadioButton) {
                abstractMap.getPropertyChangeSupport().firePropertyChange(MapEvent.MAP_PROVIDER_CHANGE.name(), initialMapName, SignalTrackMapNames.WorldWindMap);
                initialMapName = SignalTrackMapNames.WorldWindMap;
                tabbedPane.addTab("Worldwind Configuration", null, abstractMap.getConfigPanel(), null);
            }
        }
    }

    private void cancelButtonActionEvent() {
        dispose();
    }

    private void okButtonActionEvent() {
        applyButton.doClick();
        dispose();
    }

    private void applyButtonActionEvent() {
        abstractMap.savePreferences();
        abstractMap.getPropertyChangeSupport().firePropertyChange(AbstractMap.MapEvent.PROPERTY_CHANGE.name(), null, true);
    }

    private JPanel getMapTypeButtonGroupPanel() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(openStreetMapRadioButton)
                                .addComponent(virtualEarthStreetMapRadioButton)
                                .addComponent(virtualEarthSatelliteMapRadioButton)
                                .addComponent(virtualEarthHybridMapRadioButton)
                                .addComponent(worldwindMapRadioButton))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openStreetMapRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(virtualEarthStreetMapRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(virtualEarthSatelliteMapRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(virtualEarthHybridMapRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(worldwindMapRadioButton)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        panel.setBorder(BorderFactory.createTitledBorder("Map Settings"));

        return panel;
    }

    private JPanel getMainPanel() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        final JPanel mapTypeButtonGroupPanel = getMapTypeButtonGroupPanel();
        final JPanel graphicsPanel = getGraphicsPanel();

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(mapTypeButtonGroupPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(graphicsPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mapTypeButtonGroupPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(graphicsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        return panel;

    }

    private JPanel getGraphicsPanel() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(showStatusBarCheckBox)
                                .addComponent(displayShapesCheckBox))
                        .addContainerGap(180, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(showStatusBarCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(displayShapesCheckBox)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        panel.setBorder(BorderFactory.createTitledBorder("Graphics"));

        return panel;
    }

    private JPanel getGUI() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(tabbedPane, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton, 90, 90, 90)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(applyButton, 90, 90, 90)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, 90, 90, 90)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tabbedPane, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(cancelButton)
                                        .addComponent(applyButton)
                                        .addComponent(okButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        return panel;
    }

    private void drawGUI() {
        tabbedPane.addTab("Map Configuration", null, getMainPanel(), null);

        add(getGUI());

        pack();

        final Toolkit tk = Toolkit.getDefaultToolkit();

        final Dimension screenSize = tk.getScreenSize();

        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));

        setVisible(true);
    }

}
