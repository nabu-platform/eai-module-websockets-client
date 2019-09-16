package be.nabu.eai.module.websockets.client;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class WebSocketClientManager extends JAXBArtifactManager<WebSocketClientConfiguration, WebSocketClient> {

	public WebSocketClientManager() {
		super(WebSocketClient.class);
	}

	@Override
	protected WebSocketClient newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new WebSocketClient(id, container, repository);
	}

}
