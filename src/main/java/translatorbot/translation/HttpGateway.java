package translatorbot.translation;

import java.io.IOException;

public interface HttpGateway {

    String get(String url) throws IOException;
}
