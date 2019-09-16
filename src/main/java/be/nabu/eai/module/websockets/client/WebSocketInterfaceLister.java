package be.nabu.eai.module.websockets.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.api.InterfaceLister;
import be.nabu.eai.developer.util.InterfaceDescriptionImpl;

public class WebSocketInterfaceLister implements InterfaceLister {

	private static Collection<InterfaceDescription> descriptions = null;
	
	@Override
	public Collection<InterfaceDescription> getInterfaces() {
		if (descriptions == null) {
			synchronized(WebSocketInterfaceLister.class) {
				if (descriptions == null) {
					List<InterfaceDescription> descriptions = new ArrayList<InterfaceDescription>();
					descriptions.add(new InterfaceDescriptionImpl("Web Sockets", "Client Message Listener", "be.nabu.eai.module.websockets.client.api.WebSocketClientListener.message"));
					descriptions.add(new InterfaceDescriptionImpl("Web Sockets", "Client Connect Listener", "be.nabu.eai.module.websockets.client.api.WebSocketClientListener.connected"));
					descriptions.add(new InterfaceDescriptionImpl("Web Sockets", "Client Disconnect Listener", "be.nabu.eai.module.websockets.client.api.WebSocketClientListener.disconnected"));
					WebSocketInterfaceLister.descriptions = descriptions;
				}
			}
		}
		return descriptions;
	}

}
