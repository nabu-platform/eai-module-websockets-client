package be.nabu.eai.module.websockets.client.api;

import java.net.URI;

import javax.jws.WebParam;
import javax.validation.constraints.NotNull;

public interface WebSocketClientListener {
	public void message(@WebParam(name = "webSocketId") @NotNull String webSocketId, @WebParam(name = "uri") @NotNull URI uri);
	public void connected(@WebParam(name = "webSocketId") @NotNull String webSocketId, @WebParam(name = "uri") @NotNull URI uri);
	public void disconnected(@WebParam(name = "webSocketId") @NotNull String webSocketId, @WebParam(name = "uri") @NotNull URI uri);
}
