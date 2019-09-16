package be.nabu.eai.module.websockets.client;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class WebSocketClientGUIManager extends BaseJAXBGUIManager<WebSocketClientConfiguration, WebSocketClient> {

	public WebSocketClientGUIManager() {
		super("Web Socket Client", WebSocketClient.class, new WebSocketClientManager(), WebSocketClientConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected WebSocketClient newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new WebSocketClient(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	public String getCategory() {
		return "Web";
	}
}
