package ebulter.quote.lambda.mapper;

import ebulter.quote.lambda.model.Quote;
import ebulter.quote.lambda.wsmodel.WsQuote;
import ebulter.quote.lambda.wsmodel.WsZenQuote;

public class QuoteMapper {

    public static Quote mapToQuote(WsZenQuote wsZenQuote) {
        Quote quote = new Quote();
        quote.setQuoteText(wsZenQuote.getQ());
        quote.setAuthor(wsZenQuote.getA());
        return quote;
    }

    public static WsQuote mapToWsQuote(Quote quote) {
        return new WsQuote(quote.getId(), quote.getQuoteText(), quote.getAuthor());
    }

}
