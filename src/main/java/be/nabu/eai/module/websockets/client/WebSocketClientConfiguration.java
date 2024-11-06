/*
* Copyright (C) 2019 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.eai.module.websockets.client;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.api.InterfaceFilter;
import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.libs.services.api.DefinedService;

@XmlRootElement(name = "websocketClient")
public class WebSocketClientConfiguration {
	private DefinedService messageService, connectService, disconnectService;
	private Integer ioPoolSize, processPoolSize;
	
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
	public Integer getIoPoolSize() {
		return ioPoolSize;
	}
	public void setIoPoolSize(Integer ioPoolSize) {
		this.ioPoolSize = ioPoolSize;
	}
	public Integer getProcessPoolSize() {
		return processPoolSize;
	}
	public void setProcessPoolSize(Integer processPoolSize) {
		this.processPoolSize = processPoolSize;
	}
}
