package ebulter.quote.lambda.repository;

import ebulter.quote.lambda.model.Quote;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteRepository {
    private static final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private static final String TABLE_NAME = "Quotes";

    public static List<Quote> getAllQuotes() {
        ScanResponse scanResponse = dynamoDb.scan(ScanRequest.builder().tableName(TABLE_NAME).build());
        return scanResponse.items().stream().map(item ->
                new Quote(Long.parseLong(item.get("id").n()), item.get("quoteText").s(), item.get("author").s())).toList();
    }

    public static void saveQuote(Quote quote) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().n(String.valueOf(quote.getId())).build());
        item.put("quoteText", AttributeValue.builder().s(quote.getQuoteText()).build());
        item.put("author", AttributeValue.builder().s(quote.getAuthor()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDb.putItem(putItemRequest);

    }

}
