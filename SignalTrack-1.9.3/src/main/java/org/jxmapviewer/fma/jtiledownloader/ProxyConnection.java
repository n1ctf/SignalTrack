/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.jxmapviewer.fma.jtiledownloader;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyConnection {
	private static final Logger log = Logger.getLogger(ProxyConnection.class.getName());

	private ProxyConnection() {
		throw new IllegalStateException("Utility class");
	}
	
	public static void setProxyData(String host, int port) {
		System.getProperties().put("http.proxySet", "true");
		log.log(Level.INFO, "http.proxyHost = {0}", host);
		System.getProperties().put("http.proxyHost", host);
		log.log(Level.INFO, "http.proxyPort = {0}", port);
		System.getProperties().put("http.proxyPort", String.valueOf(port));
	}

	public static void setProxyData(String host, int port, String username, String password) {
		setProxyData(host, port);
		log.log(Level.FINE, "Set Proxy--- Host: {0}, Port: {1}, UserName: {2}, Password: {3}", new Object[] {host, port, username, password});
		Authenticator.setDefault(new ProxyAuth(username, password));
	}

	private static class ProxyAuth extends Authenticator {
		private final String _username;
		private final String _password;

		public ProxyAuth(String username, String passwort) {
			_username = username;
			_password = passwort;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return (new PasswordAuthentication(_username, _password.toCharArray()));
		}
	}

}
