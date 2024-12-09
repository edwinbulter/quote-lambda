package ebulter.quote.lambda.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ebulter.quote.lambda.mapper.QuoteMapper;
import ebulter.quote.lambda.model.Quote;
import ebulter.quote.lambda.wsmodel.WsZenQuote;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ZenClient {
    private static final Logger logger = LoggerFactory.getLogger(ZenClient.class);
    private static final String URL = "https://zenquotes.io/api/random";
    private static final Gson gson = new Gson();
    private static final Type zenQuoteType = new TypeToken<List<WsZenQuote>>() {}.getType();

    public static Quote getQuote() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(URL);

        WsZenQuote zenQuote = null;
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.info("received ZenQuote: " + responseBody);
            List<WsZenQuote> quotes = gson.fromJson(responseBody, zenQuoteType);
            if (!quotes.isEmpty()) {
                zenQuote = quotes.get(0);
            }
        }
        return mapToQuote(zenQuote);
    }

    private static Quote mapToQuote(WsZenQuote wsZenQuote) {
        if (wsZenQuote != null) {
            return QuoteMapper.mapToQuote(wsZenQuote);
        }
        return null;
    }
}
