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

package be.nabu.eai.module.websockets.client.api;

import java.net.URI;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.validation.constraints.NotNull;

public interface WebSocketClientListener {
	@WebResult(name = "response")
	public Object message(@WebParam(name = "webSocketId") @NotNull String webSocketId, @WebParam(name = "uri") @NotNull URI uri);
	public void connected(@WebParam(name = "webSocketId") @NotNull String webSocketId, @WebParam(name = "uri") @NotNull URI uri);
	public void disconnected(@WebParam(name = "webSocketId") @NotNull String webSocketId, @WebParam(name = "uri") @NotNull URI uri);
}
