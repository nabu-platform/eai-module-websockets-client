package be.nabu.eai.module.websockets.client;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.api.InterfaceFilter;
import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.libs.services.api.DefinedService;

@XmlRootElement(name = "websocketClient")
public class WebSocketClientConfiguration {
	private DefinedService messageService, connectService, disconnectService;
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	@InterfaceFilter(implement = "be.nabu.eai.module.websockets.client.api.WebSocketClientListener.message")
	public DefinedService getMessageService() {
		return messageService;
	}
	public void setMessageService(DefinedService messageService) {
		this.messageService = messageService;
	}
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	@InterfaceFilter(implement = "be.nabu.eai.module.websockets.client.api.WebSocketClientListener.connected")
	public DefinedService getConnectService() {
		return connectService;
	}
	public void setConnectService(DefinedService connectService) {
		this.connectService = connectService;
	}
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	@InterfaceFilter(implement = "be.nabu.eai.module.websockets.client.api.WebSocketClientListener.disconnected")
	public DefinedService getDisconnectService() {
		return disconnectService;
	}
	public void setDisconnectService(DefinedService disconnectService) {
		this.disconnectService = disconnectService;
	}
}
