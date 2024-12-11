package ebulter.quote.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ebulter.quote.lambda.model.Quote;
import ebulter.quote.lambda.repository.QuoteRepository;
import ebulter.quote.lambda.service.QuoteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuoteHandlerTest {
    static final Gson gson = new Gson();
    static final Type quoteType = new TypeToken<Quote>() {}.getType();

    @Mock
    QuoteRepository quoteRepositoryMock;

    public static List<Quote> getQuoteTestData(int numberOfQuotes) {
        List<Quote> quotes = new ArrayList<>();
        for (int i=1; i<=numberOfQuotes; i++) {
            quotes.add(new Quote(i, "Quote"+i, "Author"+1, 0));
        }
        return quotes;
    }

    public static Quote parseJsonForQuote(String json) {
        return gson.fromJson(json, quoteType);
    }

    @Test
    public void handleRequest_GetQuoteGet_ShouldReturnAQuote() {
        // Arrange
        List<Quote> quotes = getQuoteTestData(1);
        when(quoteRepositoryMock.getAllQuotes()).thenReturn(quotes);

        QuoteHandler handler = new QuoteHandler(new QuoteService(quoteRepositoryMock));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/quote");
        Context context = Mockito.mock(Context.class);

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        // Assert
        Quote quote = parseJsonForQuote(response.getBody());
        Assertions.assertEquals(1, quote.getId());
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void handleRequest_GetQuotePost_ShouldReturnTheQuoteWithTheNotExcludedId() {

        // Arrange
        List<Quote> quotes = getQuoteTestData(6);
        when(quoteRepositoryMock.getAllQuotes()).thenReturn(quotes);

        QuoteHandler handler = new QuoteHandler(new QuoteService(quoteRepositoryMock));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
            .withHttpMethod("POST")
            .withBody("[1,2,3,4,5]")
            .withPath("/quote");
        Context context = Mockito.mock(Context.class);

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        // Assert
        Quote quote = parseJsonForQuote(response.getBody());
        Assertions.assertEquals(6, quote.getId());
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void handleRequest_LikeQuote_ShouldReturnTheLikedQuoteWithIncreasedLikesField() {
        // Arrange
        Quote quote = new Quote(1, "Quote 1", "Author 1", 0);
        when(quoteRepositoryMock.findById(1)).thenReturn(quote);

        QuoteHandler handler = new QuoteHandler(new QuoteService(quoteRepositoryMock));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
            .withHttpMethod("POST")
            .withPath("/like")
            .withPathParameters(new HashMap<>() {{
                put("id", "1");
            }});
        Context context = Mockito.mock(Context.class);

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        // Assert
        Quote resultQuote = parseJsonForQuote(response.getBody());
        Assertions.assertEquals(1, resultQuote.getLikes());
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void handleRequest_GetLikedQuotes_Success() {
        // Arrange
        QuoteHandler handler = new QuoteHandler(new QuoteService(quoteRepositoryMock));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
            .withHttpMethod("GET")
            .withPath("/liked");
        Context context = Mockito.mock(Context.class);

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        // Assert
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void handleRequest_InvalidRequest() {
        // Arrange
        QuoteHandler handler = new QuoteHandler(new QuoteService(quoteRepositoryMock));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
            .withHttpMethod("GET")
            .withPath("/invalid");
        Context context = Mockito.mock(Context.class);

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        // Assert
        Assertions.assertEquals(400, response.getStatusCode());
    }
}