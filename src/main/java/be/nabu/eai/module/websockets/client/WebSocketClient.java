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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.RepositoryThreadFactory;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.authentication.api.principals.BasicPrincipal;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.events.impl.EventDispatcherImpl;
import be.nabu.libs.http.api.HTTPResponse;
import be.nabu.libs.http.client.nio.NIOHTTPClientImpl;
import be.nabu.libs.http.core.CustomCookieStore;
import be.nabu.libs.http.server.nio.MemoryMessageDataProvider;
import be.nabu.libs.http.server.websockets.WebAuthorizationType;
import be.nabu.libs.http.server.websockets.WebSocketUtils;
import be.nabu.libs.http.server.websockets.api.OpCode;
import be.nabu.libs.http.server.websockets.api.WebSocketMessage;
import be.nabu.libs.http.server.websockets.api.WebSocketRequest;
import be.nabu.libs.http.server.websockets.impl.WebSocketRequestParserFactory;
import be.nabu.libs.nio.api.StandardizedMessagePipeline;
import be.nabu.libs.nio.api.events.ConnectionEvent;
import be.nabu.libs.nio.api.events.ConnectionEvent.ConnectionState;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.json.JSONBinding;
import be.nabu.utils.io.IOUtils;

public class WebSocketClient extends JAXBArtifact<WebSocketClientConfiguration> implements StoppableArtifact {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private NIOHTTPClientImpl client;
	private URI uri;
	private Token token;
	private boolean connected;
	
	public WebSocketClient(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "websocket-client.xml", WebSocketClientConfiguration.class);
	}
	
	@SuppressWarnings("unchecked")
	public void send(Object object) throws IOException {
		if (!connected) {
			throw new IllegalStateException("The websocket is not connected");
		}
		if (object != null) {
			if (!(object instanceof ComplexContent)) {
				object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
			}
			if (object == null) {
				throw new IllegalArgumentException("Can only send complex content over websockets");
			}
			List<StandardizedMessagePipeline<WebSocketRequest, WebSocketMessage>> pipelines = WebSocketUtils.getWebsocketPipelines(getClient().getNIOClient(), null);
			if (pipelines == null || pipelines.isEmpty()) {
				throw new IllegalStateException("Could not find valid websocket pipelines");
			}
			ComplexContent content = (ComplexContent) object;
			JSONBinding binding = new JSONBinding(content.getType(), Charset.forName("UTF-8"));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			binding.marshal(output, content);

			WebSocketMessage webSocketMessage = WebSocketUtils.newMessage(output.toByteArray());
			pipelines.get(0).getResponseQueue().add(webSocketMessage);
		}
	}
	
	private NIOHTTPClientImpl getClient() {
		if (client == null) {
			synchronized(this) {
				if (client == null) {
					try {
						Integer ioPoolSize = getConfig().getIoPoolSize();
						if (ioPoolSize == null || ioPoolSize < 3) {
							ioPoolSize = 3;
						}
						Integer processPoolSize = getConfig().getProcessPoolSize();
						if (processPoolSize == null || processPoolSize < 1) {
							processPoolSize = 1;
						}
						EventDispatcherImpl dispatcher = new EventDispatcherImpl();
						client = new NIOHTTPClientImpl(
							SSLContext.getDefault(), 
							ioPoolSize, 
							processPoolSize, 
							1,
							dispatcher, 
							new MemoryMessageDataProvider(), 
							new CookieManager(new CustomCookieStore(), CookiePolicy.ACCEPT_ORIGINAL_SERVER), 
							new RepositoryThreadFactory(getRepository()), 
							false
						);
						// unlimited lifetime
						client.getNIOClient().setMaxLifeTime(0l);
						WebSocketUtils.allowWebsockets(client, new MemoryMessageDataProvider());
						
						client.getDispatcher().subscribe(ConnectionEvent.class, new EventHandler<ConnectionEvent, Void>() {
							@Override
							public Void handle(ConnectionEvent event) {
								WebSocketRequestParserFactory parserFactory = WebSocketUtils.getParserFactory(event.getPipeline());
								if (parserFactory != null) {
									if (ConnectionState.CLOSED.equals(event.getState())) {
										// if the boolean is still set, we did not disconnect of our own choosing
										if (connected) {
											logger.warn("Websocket connection " + getId() + " closed, reconnecting...");
											try {
												connect(uri, token);
											}
											catch (Exception e) {
												connected = false;
												logger.error("Websocket connection " + getId() + " could be not set up again", e);
											}
										}
									}
									else if (ConnectionState.UPGRADED.equals(event.getState())) {
										connected = true;
										logger.info("Websocket connection " + getId() + " set up");
									}
								}
								return null;
							}
						});
						
						client.getDispatcher().subscribe(WebSocketRequest.class, new EventHandler<WebSocketRequest, WebSocketMessage>() {
							@SuppressWarnings("unchecked")
							@Override
							public WebSocketMessage handle(WebSocketRequest event) {
								if (getConfig().getMessageService() == null) {
									logger.error("Could not find message service for websocket: " + getId());
								}
								else {
									try {
										ComplexType input = getConfig().getMessageService().getServiceInterface().getInputDefinition();
										ComplexContent content = null;
										for (Element<?> child : input) {
											if (child.getType() instanceof ComplexType) {
												JSONBinding binding = new JSONBinding((ComplexType) child.getType(), Charset.forName("UTF-8"));
												try {
													ComplexContent childContent = binding.unmarshal(event.getData(), new Window[0]);
													content = input.newInstance();
													content.set(child.getName(), childContent);
													break;
												}
												catch (IOException e) {
													continue;
												}
												catch (ParseException e) {
													continue;
												}
											}
											else if (child.getType() instanceof SimpleType && ((SimpleType<?>) child.getType()).getInstanceClass().equals(byte[].class)) {
												content = input.newInstance();
												content.set(child.getName(), IOUtils.toBytes(IOUtils.wrap(event.getData())));
												break;
											}
											else if (child.getType() instanceof SimpleType && ((SimpleType<?>) child.getType()).getInstanceClass().equals(InputStream.class)) {
												content = input.newInstance();
												content.set(child.getName(), event.getData());
												break;
											}
										}
										if (content == null) {
											throw new RuntimeException("Could not unmarshal the incoming data");
										}
										
										content.set("webSocketId", getId());
										content.set("uri", uri);
										Future<ServiceResult> run = getRepository().getServiceRunner().run(
											getConfig().getMessageService(), 
											getRepository().newExecutionContext(token), 
											content 
										);
										ServiceResult serviceResult = run.get();
										if (serviceResult.getException() != null) {
											throw serviceResult.getException();
										}
										else if (serviceResult.getOutput() != null) {
											Object result = serviceResult.getOutput().get("response");
											if (result != null) {
												if (!(result instanceof ComplexContent)) {
													result = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(result);
												}
												if (result != null) {
													JSONBinding binding = new JSONBinding(((ComplexContent) result).getType(), Charset.forName("UTF-8"));
													binding.setIgnoreRootIfArrayWrapper(true);
													ByteArrayOutputStream output = new ByteArrayOutputStream();
													binding.marshal(output, (ComplexContent) result);
													byte [] bytes = output.toByteArray();
													return WebSocketUtils.newMessage(OpCode.TEXT, true, bytes.length, IOUtils.wrap(bytes, true));
												}
											}
										}
									}
									catch (Exception e) {
										logger.error("Could not process incoming websocket message for " + getId(), e);
									}
								}
								return null;
							}
						});
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return client;
	}
	
	public void disconnect() {
		connected = false;
		try {
			client.close();
		}
		catch (IOException e) {
			logger.warn("Could not close websocket client for " + getId(), e);
		}
		finally {
			client = null;
		}
	}

	public void connect(URI uri, Token principal) {
		if (connected) {
			disconnect();
		}
		this.uri = uri;
		this.token = principal;
		try {
			HTTPResponse upgrade = WebSocketUtils.upgrade(
				getClient(), 
				uri.getScheme().equals("https") ? SSLContext.getDefault() : null, 
				uri.getHost(), 
				uri.getPort() < 0 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort(), 
				uri.getPath(), 
				principal, 
				new MemoryMessageDataProvider(), 
				client.getDispatcher(), 
				new ArrayList<String>(),
				principal instanceof BasicPrincipal ? WebAuthorizationType.BASIC : null
			);
	
			if (upgrade.getCode() >= 100 && upgrade.getCode() < 300) {
				logger.info("Websocket connection " + getId() + " connected");
			}
			else {
				logger.warn("Websockets not available: " + upgrade.getCode());
			}
		}
		catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		}
	}
	
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void stop() throws IOException {
		if (connected) {
			disconnect();
		}
	}
}
