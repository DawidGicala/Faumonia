/* $Id: ItemsXMLLoader.java,v 1.12 2010/12/05 14:01:40 martinfuchs Exp $ */
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
package games.stendhal.server.core.config;

import games.stendhal.server.core.rule.defaultruleset.DefaultItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class ItemsXMLLoader extends DefaultHandler {

	/** the logger instance. */
	private static final Logger LOGGER = Logger.getLogger(ItemsXMLLoader.class);

	private Class< ? > implementation;
	
	private String name;

	private String clazz;

	private String subclass;

	private String description;

	private String text;

	private double weight;

	private int value;

	/** slots where this item can be equipped. */
	private List<String> slots;

	/** Attributes of the item. */
	private Map<String, String> attributes;

	private List<DefaultItem> list;

	private boolean attributesTag;
	
	private String damageType;
	
	private Map<String, Double> susceptibilities = new HashMap<String, Double>();

	
	public List<DefaultItem> load(final URI uri) throws SAXException {
		list = new LinkedList<DefaultItem>();
		// Use the default (non-validating) parser
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			final SAXParser saxParser = factory.newSAXParser();

			final InputStream is = ItemsXMLLoader.class.getResourceAsStream(uri.getPath());

			if (is == null) {
				throw new FileNotFoundException("cannot find resource '" + uri
						+ "' in classpath");
			}
			try {
			saxParser.parse(is, this);
			} finally {
				is.close();
			}
		} catch (final ParserConfigurationException t) {
			LOGGER.error(t);
		} catch (final IOException e) {
			LOGGER.error(e);
			throw new SAXException(e);
		}

		return list;
	}

	@Override
	public void startDocument() {
		// do nothing
	}

	@Override
	public void endDocument() {
		// do nothing
	}

	@Override
	public void startElement(final String namespaceURI, final String lName, final String qName,
			final Attributes attrs) {
		text = "";
		if (qName.equals("item")) {
			name = attrs.getValue("name");
			attributes = new LinkedHashMap<String, String>();
			slots = new LinkedList<String>();
			description = "";
			implementation = null;
		} else if (qName.equals("type")) {
			clazz = attrs.getValue("class");
			subclass = attrs.getValue("subclass");
		} else if (qName.equals("implementation")) {

			final String className = attrs.getValue("class-name");

			try {
				implementation = Class.forName(className);
			} catch (final ClassNotFoundException ex) {
				LOGGER.error("Unable to load class: " + className);
			}
		} else if (qName.equals("weight")) {
			weight = Double.parseDouble(attrs.getValue("value"));
		} else if (qName.equals("value")) {
			value = Integer.parseInt(attrs.getValue("value"));
		} else if (qName.equals("slot")) {
			slots.add(attrs.getValue("name"));
		} else if (qName.equals("attributes")) {
			attributesTag = true;
		} else if (attributesTag) {
			attributes.put(qName, attrs.getValue("value"));
		} else if (qName.equals("damage")) {
			damageType = attrs.getValue("type");
		} else if (qName.equals("susceptibility")) {
			susceptibilities.put(attrs.getValue("type"), Double.valueOf(attrs.getValue("value")));
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String sName, final String qName) {
		if (qName.equals("item")) {
			final DefaultItem item = new DefaultItem(clazz, subclass, name, -1);
			item.setWeight(weight);
			item.setEquipableSlots(slots);
			item.setAttributes(attributes);
			item.setDescription(description);
			item.setValue(value);
			if (damageType != null) {
				item.setDamageType(damageType);
				// An optional element - reset it to avoid leaking to next items
				damageType = null;
			}
			item.setSusceptibilities(susceptibilities);
			susceptibilities.clear();

			if (implementation == null) {
				LOGGER.error("Item without defined implementation: " + name);
				return;
			}

			item.setImplementation(implementation);

			list.add(item);
		} else if (qName.equals("attributes")) {
			attributesTag = false;
		} else if (qName.equals("description")) {
			if (text != null) {
				description = text.trim();
			}
		}
	}

	@Override
	public void characters(final char[] buf, final int offset, final int len) {
		text = text + (new String(buf, offset, len)).trim();
	}
}
