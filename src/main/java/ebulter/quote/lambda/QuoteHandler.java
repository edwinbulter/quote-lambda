package ebulter.quote.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ebulter.quote.lambda.client.ZenClient;
import ebulter.quote.lambda.model.Quote;
import ebulter.quote.lambda.repository.QuoteRepository;
import ebulter.quote.lambda.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {
    private static final Logger logger = LoggerFactory.getLogger(QuoteHandler.class);

    private static final Gson gson = new Gson();
    private static final Type quoteType = new TypeToken<Quote>() {}.getType();

    public GatewayResponse handleRequest(Map<String, Object> event, Context context) {
        //todo: use the event to derive the action
        //if path endsWith "/quote" AND httpMethod="GET" then return a random quote
        //if path endsWith "/quote" AND httpMethod="POST" then use body with ids to return a random quote
        //if path contains "/quote/like" AND httpMethod="PATCH" then ...

        String path = (String) event.get("path");
        String httpMethod = (String) event.get("httpMethod");

        logger.info("path={}, httpMethod={}", path, httpMethod);

        Quote quote = null;
        if (path.endsWith("/quote")) {
            if ("GET".equals(httpMethod)) {
                quote = getQuote(Collections.emptyList());
            } else if (httpMethod.equals("POST")) {
                String jsonBody = (String) event.get("body");
                List<Integer> idList = new Gson().fromJson(jsonBody, new TypeToken<List<Integer>>() {}.getType());
                quote = getQuote(idList==null ? Collections.emptyList() : idList);
            }
        } else {
            quote = getErrorQuote("Invalid request");
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");  // enable CORS

        String responseBody = gson.toJson(quote, quoteType);
        return new GatewayResponse(responseBody, headers, 200);
    }

    private Quote getQuote(List<Integer> idsToExclude) {
        try {
            logger.info("start fetching quote");
            Quote quote = ZenClient.getQuote();
            logger.info("Finished fetching quote");
            if (quote != null) {
                logger.info("start reading all quotes from DB");
                List<Quote> allQuotes = QuoteRepository.getAllQuotes();
                logger.info("finished reading all quotes from DB");
                quote.setId((long)allQuotes.size() + 1);
                logger.info("start saving quote with id {}", quote.getId());
                QuoteRepository.saveQuote(quote);
                logger.info("finished saving quote with id {}", quote.getId());
                return quote;
            }
        } catch (IOException e) {
            return getErrorQuote(e.getMessage());
        }
        return getErrorQuote("Failed to fetch quote");
    }


    private static Quote getErrorQuote(String errorMessage) {
        Quote errorQuote = new Quote();
        errorQuote.setQuoteText(errorMessage);
        return errorQuote;
    }

}
