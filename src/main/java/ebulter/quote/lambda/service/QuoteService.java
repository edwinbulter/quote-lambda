package ebulter.quote.lambda.service;

import ebulter.quote.lambda.client.ZenClient;
import ebulter.quote.lambda.model.Quote;
import ebulter.quote.lambda.repository.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class QuoteService {
    private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);

    public static Quote getQuote(final Set<Integer> idsToExclude) {
        logger.info("start reading all quotes from DB");
        List<Quote> currentDatabaseQuotes = QuoteRepository.getAllQuotes();
        logger.info("finished reading all quotes from DB");
        if (currentDatabaseQuotes.size() < 10 || currentDatabaseQuotes.size() <= idsToExclude.size()) {
            try {
                Set<Quote> fetchedQuotes = ZenClient.getSomeUniqueQuotes();
                //Note: In the following statement, the quotes are compared (and subsequently removed) solely by quoteText.
                fetchedQuotes.removeAll(new HashSet<>(currentDatabaseQuotes));
                AtomicInteger idGenerator = new AtomicInteger(currentDatabaseQuotes.size() + 1);
                fetchedQuotes.forEach(quote -> quote.setId(idGenerator.getAndIncrement()));
                QuoteRepository.saveAll(fetchedQuotes);
                currentDatabaseQuotes = QuoteRepository.getAllQuotes();
            } catch (IOException e) {
                logger.error("Failed to read quotes from ZenQuotes: {}", e.getMessage());
            }
        }
        Random random = new Random();
        if (!idsToExclude.isEmpty()) {
            List<Quote> filteredDatabaseQuotes = currentDatabaseQuotes.stream().filter(quote -> !idsToExclude.contains(quote.getId())).toList();
            int randomIndex = random.nextInt(filteredDatabaseQuotes.size());
            return filteredDatabaseQuotes.get(randomIndex);
        } else {
            int randomIndex = random.nextInt(currentDatabaseQuotes.size());
            return currentDatabaseQuotes.get(randomIndex);
        }
    }

    public static Quote likeQuote(int idToLike) {
        Quote quote = QuoteRepository.findById(idToLike);
        if (quote != null) {
            quote.setLikes(quote.getLikes() + 1);
            QuoteRepository.updateLikes(quote);
            return quote;
        } else {
            return getErrorQuote("Quote to like not found");
        }
    }

    public static List<Quote> getLikedQuotes() {
        return QuoteRepository.getLikedQuotes();
    }

    public static Quote getErrorQuote(String errorMessage) {
        Quote errorQuote = new Quote();
        errorQuote.setQuoteText(errorMessage);
        return errorQuote;
    }
}
