/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.settings;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sound.facade.SoundGroup;
import games.stendhal.client.sound.facade.Time;
import games.stendhal.common.NotificationType;
import games.stendhal.common.math.Numeric;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Page for the sound settings.
 */
class SoundSettings {
	/** Name of the master property for playing/mute. */
	private static final String SOUND_PROPERTY = "sound.play";
	/** Prefix for the volume properties. */
	private static final String VOLUME_PROPERTY = "sound.volume.";
	/** Name of the sound device property. */
	private static final String DEVICE_PROPERTY = "sound.device";
	/** Default value of the sound device property. */
	private static final String DEFAULT_DEVICE = "auto - recommended";
	
	/** Container for the setting components */
	private final JComponent page;
	
	/** Toggle for mute */
	private final JCheckBox muteToggle;
	/**
	 * Container for the volume sliders. Exist to help turning them all,
	 * and their labels easily on or off.
	 */
	private List<JComponent> sliderComponents = new ArrayList<JComponent>(14);
	/** Volume adjuster for master channel */
	private final JSlider masterVolume;
	/** Volume adjuster for GUI channel */
	private final JSlider guiVolume;
	/** Volume adjuster for effects channel */
	private final JSlider effectsVolume;
	/** Volume adjuster for creatures channel */
	private final JSlider creaturesVolume;
	/** Volume adjuster for ambient channel */
	private final JSlider ambientVolume;
	/** Volume adjuster for music channel */
	private final JSlider musicVolume;

	/**
	 * Create sound settings page.
	 */
	SoundSettings() {
		int pad = SBoxLayout.COMMON_PADDING;
		page = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		page.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));

		// click mode
		muteToggle = new JCheckBox("Odtwórz dźwięki");
		boolean soundOn = Boolean.parseBoolean(WtWindowManager.getInstance().getProperty(SOUND_PROPERTY, "true"));
		muteToggle.setSelected(soundOn);
		muteToggle.addItemListener(new MuteListener());
		page.add(muteToggle);

		// Device selector
		JComponent hbox = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		JComponent selectorLabel = new JLabel("Urządzenie dźwięku:");
		hbox.add(selectorLabel);
		JComponent selector = createDeviceSelector();
		hbox.add(selector);
		selector.setToolTipText("<html>Wyjściowe urządzenie dźwięku. <b>auto</b> powinno"
				+ " działać u większości osób,<br>ale wypróbuj inne jeżeli nie możesz otrzymać"
				+ " zadowalającego dźwięku</html>");
		sliderComponents.add(selectorLabel);
		sliderComponents.add(selector);
		page.add(hbox);

		// Sliders for the sound channels
		JComponent row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		page.add(row, SBoxLayout.constraint(SLayout.EXPAND_X));
		JLabel label = new JLabel("Główny");
		row.add(label);
		SBoxLayout.addSpring(row);
		masterVolume = createMasterVolumeSlider();
		masterVolume.setToolTipText("Głośność wszystki kanałów dźwięku");
		row.add(masterVolume);
		sliderComponents.add(label);
		sliderComponents.add(masterVolume);

		row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		page.add(row, SBoxLayout.constraint(SLayout.EXPAND_X));
		label = new JLabel("GUI");
		row.add(label);
		SBoxLayout.addSpring(row);
		guiVolume = createVolumeSlider("gui");
		guiVolume.setToolTipText("Głośność interaktywnych operacji jak zamykanie okien");
		row.add(guiVolume);
		sliderComponents.add(label);
		sliderComponents.add(guiVolume);

		row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		page.add(row, SBoxLayout.constraint(SLayout.EXPAND_X));
		label = new JLabel("Efekty");
		row.add(label);
		SBoxLayout.addSpring(row);
		effectsVolume = createVolumeSlider("sfx");
		effectsVolume.setToolTipText("Głośność walki i inne efekty");
		row.add(effectsVolume);
		sliderComponents.add(label);
		sliderComponents.add(effectsVolume);

		row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		page.add(row, SBoxLayout.constraint(SLayout.EXPAND_X));
		label =new JLabel("Potwory");
		row.add(label);
		SBoxLayout.addSpring(row);
		creaturesVolume = createVolumeSlider("creature");
		creaturesVolume.setToolTipText("Głośność potworów");
		row.add(creaturesVolume);
		sliderComponents.add(label);
		sliderComponents.add(creaturesVolume);

		row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		page.add(row, SBoxLayout.constraint(SLayout.EXPAND_X));
		label = new JLabel("Otoczenie");
		row.add(label);
		SBoxLayout.addSpring(row);
		ambientVolume = createVolumeSlider("ambient");
		row.add(ambientVolume);
		sliderComponents.add(label);
		sliderComponents.add(ambientVolume);

		row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		page.add(row, SBoxLayout.constraint(SLayout.EXPAND_X));
		label = new JLabel("Muzyka");
		row.add(label);
		SBoxLayout.addSpring(row);
		musicVolume = createVolumeSlider("music");
		musicVolume.setToolTipText("Głośność dźwięku");
		row.add(musicVolume);
		sliderComponents.add(label);
		sliderComponents.add(musicVolume);
		
		// Disable the sliders if the sound is off
		for (JComponent comp : sliderComponents) {
			comp.setEnabled(soundOn);
		}
	}
	
	/**
	 * Get the component containing the sound settings.
	 * 
	 * @return sound settings page
	 */
	JComponent getComponent() {
		return page;
	}
	
	/**
	 * Create a selector for sound devices.
	 * 
	 * @return combo box with device options
	 */
	private JComponent createDeviceSelector() {
		final JComboBox selector = new JComboBox();
		
		// Fill with available device names
		selector.addItem(DEFAULT_DEVICE);
		for (String name : ClientSingletonRepository.getSound().getDeviceNames()) {
			selector.addItem(name);
		}
		
		final WtWindowManager wm = WtWindowManager.getInstance();
		String current = wm.getProperty(DEVICE_PROPERTY, DEFAULT_DEVICE);
		selector.setSelectedItem(current);
		 
		selector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected = selector.getSelectedItem();
				wm.setProperty(DEVICE_PROPERTY, (selected != null) ? selected.toString() : DEFAULT_DEVICE);
				wm.save();
				// Warn the user about the delayed effect
				String msg = "Zmiana urządzenia dźwięku zadziała dopiero przy kolejnym włączeniu gry.";
				ClientSingletonRepository.getUserInterface().addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});
		
		return selector;
	}
	
	/**
	 * Create a volume slider for the master channel.
	 */
	private JSlider createMasterVolumeSlider() {
		JSlider slider = new JSlider(0, 100);
		float volume = ClientSingletonRepository.getSound().getVolume();
		slider.setValue(Numeric.floatToInt(volume, 100f));
		slider.addChangeListener(new MasterVolumeListener());
		
		return slider;
	}
	
	/**
	 * Create a volume slider, and initialize its value from the volume of a
	 * channel.
	 * 
	 * @param channel
	 * @return volume slider for the channel
	 */
	private JSlider createVolumeSlider(String channel) {
		JSlider slider = new JSlider(0, 100);
		SoundGroup group = ClientSingletonRepository.getSound().getGroup(channel);
		slider.setValue(Numeric.floatToInt(group.getVolume(), 100f));
		slider.addChangeListener(new ChannelChangeListener(channel, group));
		
		return slider;
	}
	
	/**
	 * Listener for toggling the sound on or off. Disables and enables the
	 * volume sliders as needed.
	 */
	private class MuteListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			boolean soundOn = (e.getStateChange() == ItemEvent.SELECTED);
			WtWindowManager.getInstance().setProperty(SOUND_PROPERTY, Boolean.toString(soundOn));
			ClientSingletonRepository.getSound().mute(!soundOn, true, new Time(2, Time.Unit.SEC));
			for (JComponent comp : sliderComponents) {
				comp.setEnabled(soundOn);
			}
		}
	}
	
	/**
	 * Listener for adjusting the master volume slider.
	 */
	private static class MasterVolumeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			int value = source.getValue();
			ClientSingletonRepository.getSound().changeVolume(Numeric.intToFloat(value, 100.0f));
			WtWindowManager.getInstance().setProperty(VOLUME_PROPERTY + "master", Integer.toString(value));
		}
	}
	
	/**
	 * Listener for adjusting the sound channel sliders. Adjusts the volume of
	 * the sound group corresponding to the slider appropriately.
	 */
	private static class ChannelChangeListener implements ChangeListener {
		private final SoundGroup group;
		private final String groupName; 
		
		/**
		 * Create a ChannelChangeListener for a sound group.
		 * 
		 * @param groupName name of the sound group
		 * @param group
		 */
		public ChannelChangeListener(String groupName, SoundGroup group) {
			this.group = group;
			this.groupName = groupName;
		}
		
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			int value = source.getValue();
			group.changeVolume(Numeric.intToFloat(value, 100f));
			WtWindowManager.getInstance().setProperty(VOLUME_PROPERTY + groupName, Integer.toString(value));
		}
	}
}