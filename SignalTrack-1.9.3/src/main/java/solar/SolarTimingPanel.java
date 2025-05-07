package solar;

import java.awt.Font;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import time.AstronomicalTime;

public class SolarTimingPanel {
	private int dayOfYear;
	
	private double stationLatitudeDegrees;
	private double stationLongitudeDegrees;
	
	private JLabel sunriseLabel;
	private JLabel sunriseEndLabel;
	private JLabel eveningGoldenHourLabel;
	private JLabel morningGoldenHourLabel;
	private JLabel solarNoonLabel;
	private JLabel sunsetStartLabel;
	private JLabel sunsetLabel;
	private JLabel duskLabel;
	private JLabel nauticalDuskLabel;
	private JLabel nightLabel;
	private JLabel nadirLabel;
	private JLabel nightEndLabel;
	private JLabel nauticalDawnLabel;
	private JLabel dawnLabel;
	
	private JLabel sunrise;
	private JLabel sunriseEnd;
	private JLabel eveningGoldenHour;
	private JLabel morningGoldenHour;
	private JLabel solarNoon;
	private JLabel sunsetStart;
	private JLabel sunset;
	private JLabel dusk;
	private JLabel nauticalDusk;
	private JLabel night;
	private JLabel nadir;
	private JLabel nightEnd;
	private JLabel nauticalDawn;
	private JLabel dawn;
	
	public SolarTimingPanel(double stationLatitudeDegrees, double stationLongitudeDegrees) {
		this(ZonedDateTime.now(ZoneId.systemDefault()), stationLatitudeDegrees, stationLongitudeDegrees);
	}
	
	public SolarTimingPanel(ZonedDateTime zdt, double stationLatitudeDegrees, double stationLongitudeDegrees) {
		this.stationLatitudeDegrees = stationLatitudeDegrees;
		this.stationLongitudeDegrees = stationLongitudeDegrees;
		initializeComponents();
		updateComponents(zdt);
	}
	
	public void setZonedDateTime(ZonedDateTime zdt) {
		updateComponents(zdt);
	}

	private void initializeComponents() {
		sunriseLabel = new JLabel("Sunrise");
		sunriseEndLabel = new JLabel("Sunrise End");
		morningGoldenHourLabel = new JLabel("Morning Golden Hour");
		solarNoonLabel = new JLabel("Solar Noon");
		eveningGoldenHourLabel = new JLabel("Evening Golden Hour");
		sunsetStartLabel = new JLabel("Sunset Start");
		sunsetLabel = new JLabel("Sunset");
		duskLabel = new JLabel("Dusk");
		nauticalDuskLabel = new JLabel("Nautical Dusk");
		nightLabel = new JLabel("Night");	
		nadirLabel = new JLabel("Nadir");
		nightEndLabel = new JLabel("Night End");
		nauticalDawnLabel = new JLabel("Nautical Dawn");
		dawnLabel = new JLabel("Dawn");
		
		sunrise = new JLabel();
		sunriseEnd = new JLabel();
		eveningGoldenHour = new JLabel();
		morningGoldenHour = new JLabel();
		solarNoon = new JLabel();
		sunsetStart = new JLabel();
		sunset = new JLabel();
		dusk = new JLabel();
		nauticalDusk = new JLabel();
		night = new JLabel();
		nadir = new JLabel();
		nightEnd = new JLabel();
		nauticalDawn = new JLabel();
		dawn = new JLabel();
		
		sunriseLabel.setToolTipText("Sunrise (top edge of the sun appears on the horizon)");
		sunriseEndLabel.setToolTipText("Sunrise Ends (bottom edge of the sun touches the horizon)");
		morningGoldenHourLabel.setToolTipText("Morning Golden Hour (soft light, best time for photography) Ends");
		solarNoonLabel.setToolTipText("Solar Noon (sun is in the highest position)");
		eveningGoldenHourLabel.setToolTipText("Evening Golden Hour Starts");
		sunsetStartLabel.setToolTipText("Sunset Starts (bottom edge of the sun touches the horizon)");
		sunsetLabel.setToolTipText("Sunset (sun disappears below the horizon, evening civil twilight starts)");
		duskLabel.setToolTipText("Dusk (evening nautical twilight starts)");
		nauticalDuskLabel.setToolTipText("Nautical Dusk (evening astronomical twilight starts)");
		nightLabel.setToolTipText("Night Starts (dark enough for astronomical observations)");
		nadirLabel.setToolTipText("Nadir (darkest moment of the night, sun is in the lowest position)");
		nightEndLabel.setToolTipText("Night Ends (morning astronomical twilight starts)");
		nauticalDawnLabel.setToolTipText("Nautical Dawn (morning nautical twilight starts)");
		dawnLabel.setToolTipText("Dawn (morning nautical twilight ends, morning civil twilight starts)");
		
		sunrise.setToolTipText("Sunrise (top edge of the sun appears on the horizon)");
		sunriseEnd.setToolTipText("Sunrise Ends (bottom edge of the sun touches the horizon)");
		morningGoldenHour.setToolTipText("Morning Golden Hour (soft light, best time for photography) Ends");
		solarNoon.setToolTipText("Solar Noon (sun is in the highest position)");
		eveningGoldenHour.setToolTipText("Evening Golden Hour Starts");
		sunsetStart.setToolTipText("Sunset Starts (bottom edge of the sun touches the horizon)");
		sunset.setToolTipText("Sunset (sun disappears below the horizon, evening civil twilight starts)");
		dusk.setToolTipText("Dusk (evening nautical twilight starts)");
		nauticalDusk.setToolTipText("Nautical Dusk (evening astronomical twilight starts)");
		night.setToolTipText("Night Starts (dark enough for astronomical observations)");
		nadir.setToolTipText("Nadir (darkest moment of the night, sun is in the lowest position)");
		nightEnd.setToolTipText("Night Ends (morning astronomical twilight starts)");
		nauticalDawn.setToolTipText("Nautical Dawn (morning nautical twilight starts)");
		dawn.setToolTipText("Dawn (morning nautical twilight ends, morning civil twilight starts)");
	}
	
	private void updateComponents(ZonedDateTime zdt) {
		if (dayOfYear != zdt.getDayOfYear()) {
			dayOfYear = zdt.getDayOfYear();
					
			sunrise.setText(AstronomicalTime.getSunrise(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			sunriseEnd.setText(AstronomicalTime.getSunriseEnd(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			eveningGoldenHour.setText(AstronomicalTime.getGoldenHour(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			morningGoldenHour.setText(AstronomicalTime.getGoldenHourEnd(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			solarNoon.setText(AstronomicalTime.getSolarNoon(zdt, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			sunsetStart.setText(AstronomicalTime.getSunriseStart(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			sunset.setText(AstronomicalTime.getSunset(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			dusk.setText(AstronomicalTime.getDusk(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			nauticalDusk.setText(AstronomicalTime.getNauticalDusk(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			night.setText(AstronomicalTime.getNight(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			nadir.setText(AstronomicalTime.getNadir(zdt, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			nightEnd.setText(AstronomicalTime.getNightEnd(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			nauticalDawn.setText(AstronomicalTime.getNauticalDawn(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
			dawn.setText(AstronomicalTime.getDawn(zdt, stationLatitudeDegrees, stationLongitudeDegrees).format(DateTimeFormatter.ofPattern("HH:mm a")));
		}
	}
	
	public JPanel getSolarTimingPanelSingleColumn() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Solar Timing",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 14)));
		
		final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sunriseLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sunrise, 58, 58, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sunriseEndLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sunriseEnd, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(eveningGoldenHourLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eveningGoldenHour, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(morningGoldenHourLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(morningGoldenHour, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(solarNoonLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(solarNoon, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sunsetStartLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sunsetStart, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nadirLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nadir, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nightEndLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nightEnd, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nauticalDawnLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nauticalDawn, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sunsetLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sunset, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(duskLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dusk, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nauticalDuskLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nauticalDusk, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nightLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(night, 64, 64, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dawnLabel, 128, 128, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dawn, 64, 64, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sunriseLabel)
                    .addComponent(sunrise))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sunriseEndLabel)
                    .addComponent(sunriseEnd))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(morningGoldenHourLabel)
                    .addComponent(morningGoldenHour))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(solarNoonLabel)
                    .addComponent(solarNoon))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(eveningGoldenHourLabel)
                    .addComponent(eveningGoldenHour))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sunsetStartLabel)
                    .addComponent(sunsetStart))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sunsetLabel)
                    .addComponent(sunset))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(duskLabel)
                    .addComponent(dusk))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nauticalDuskLabel)
                    .addComponent(nauticalDusk))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nightLabel)
                    .addComponent(night))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nadirLabel)
                    .addComponent(nadir))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nightEndLabel)
                    .addComponent(nightEnd))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nauticalDawnLabel)
                    .addComponent(nauticalDawn))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(dawnLabel)
                    .addComponent(dawn))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        return panel;
	}
}
