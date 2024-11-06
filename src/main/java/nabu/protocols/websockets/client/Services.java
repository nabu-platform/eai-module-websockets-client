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

package nabu.protocols.websockets.client;

import java.io.IOException;
import java.net.URI;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.module.websockets.client.WebSocketClient;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.authentication.api.Token;

@WebService
public class Services {
	
	public void connect(@NotNull @WebParam(name = "webSocketClientId") String webSocketClientId, @NotNull @WebParam(name = "uri") URI uri, @WebParam(name = "token") Token token) {
		WebSocketClient client = (WebSocketClient) EAIResourceRepository.getInstance().resolve(webSocketClientId);
		client.connect(uri, token);
	}
	
	public void disconnect(@NotNull @WebParam(name = "webSocketClientId") String webSocketClientId) {
		WebSocketClient client = (WebSocketClient) EAIResourceRepository.getInstance().resolve(webSocketClientId);
		client.disconnect();
	}
	
	public void send(@NotNull @WebParam(name = "webSocketClientId") String webSocketClientId, @WebParam(name = "content") Object content) throws IOException {
		WebSocketClient client = (WebSocketClient) EAIResourceRepository.getInstance().resolve(webSocketClientId);
		client.send(content);
	}
	
	@WebResult(name = "connected")
	public boolean isConnected(@NotNull @WebParam(name = "webSocketClientId") String webSocketClientId) {
		WebSocketClient client = (WebSocketClient) EAIResourceRepository.getInstance().resolve(webSocketClientId);
		return client.isConnected();
	}
}
