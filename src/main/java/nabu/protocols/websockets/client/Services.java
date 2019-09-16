package nabu.protocols.websockets.client;

import java.io.IOException;
import java.net.URI;

import javax.jws.WebParam;
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
	
}
