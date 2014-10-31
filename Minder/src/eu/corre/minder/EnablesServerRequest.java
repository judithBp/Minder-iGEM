package eu.corre.minder;

import java.util.ArrayList;

public interface EnablesServerRequest {

	public void handleRequestResult(ArrayList<Object[]> result,
			String requestContext);
}
