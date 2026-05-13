package modules.marketplace.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Mirrors Symfony's ExchangeRateService.
 * Converts TND amounts to EUR, USD (and back).
 * Falls back to hardcoded rates if the API is unreachable.
 */
public class ExchangeRateService {

    // Fallback rates: 1 TND = x CURRENCY
    private static final Map<String, Double> FALLBACK_RATES = new HashMap<>();
    static {
        FALLBACK_RATES.put("TND", 1.0);
        FALLBACK_RATES.put("EUR", 0.30);   // 1 TND ≈ 0.30 EUR
        FALLBACK_RATES.put("USD", 0.32);   // 1 TND ≈ 0.32 USD
        FALLBACK_RATES.put("GBP", 0.25);
    }

    private Map<String, Double> cachedRates = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes

    /** Convert a TND amount to the target currency. */
    public double convert(double amountTND, String targetCurrency) {
        if ("TND".equalsIgnoreCase(targetCurrency)) return amountTND;
        double rate = getRate(targetCurrency);
        return amountTND * rate;
    }

    /** Convert back from a foreign currency to TND. */
    public double convertToTND(double amount, String fromCurrency) {
        if ("TND".equalsIgnoreCase(fromCurrency)) return amount;
        double rate = getRate(fromCurrency);
        if (rate == 0) return amount;
        return amount / rate;
    }

    /** Get the symbol for a currency code. */
    public static String getSymbol(String currency) {
        return switch (currency.toUpperCase()) {
            case "EUR" -> "€";
            case "USD" -> "$";
            case "GBP" -> "£";
            default    -> "TND";
        };
    }

    /** Format a TND amount in the given display currency. */
    public String format(double amountTND, String currency) {
        double converted = convert(amountTND, currency);
        return String.format("%.2f %s", converted, currency.toUpperCase());
    }

    private double getRate(String currency) {
        // Try to refresh rates every 10 minutes
        if (cachedRates == null || System.currentTimeMillis() - cacheTimestamp > CACHE_TTL_MS) {
            tryFetchRates();
        }
        if (cachedRates != null && cachedRates.containsKey(currency.toUpperCase())) {
            return cachedRates.get(currency.toUpperCase());
        }
        return FALLBACK_RATES.getOrDefault(currency.toUpperCase(), 1.0);
    }

    /** Tries to fetch live rates from a free API; silently falls back on failure. */
    private void tryFetchRates() {
        try {
            // Free API: exchangerate.host (no key needed for basic usage)
            URL url = new URL("https://api.exchangerate.host/latest?base=TND&symbols=EUR,USD,GBP,TND");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                Map<String, Double> rates = parseRatesJson(sb.toString());
                if (!rates.isEmpty()) {
                    cachedRates = rates;
                    cacheTimestamp = System.currentTimeMillis();
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            // Silently use fallback rates
            System.out.println("[ExchangeRateService] Using fallback rates: " + e.getMessage());
        }
    }

    /** Minimal JSON parser to extract "rates": { "EUR": 0.30, ... } */
    private Map<String, Double> parseRatesJson(String json) {
        Map<String, Double> rates = new HashMap<>();
        try {
            int ratesIdx = json.indexOf("\"rates\"");
            if (ratesIdx < 0) return rates;
            int braceStart = json.indexOf("{", ratesIdx);
            int braceEnd   = json.indexOf("}", braceStart);
            String ratesBlock = json.substring(braceStart + 1, braceEnd);
            for (String entry : ratesBlock.split(",")) {
                String[] kv = entry.split(":");
                if (kv.length == 2) {
                    String key = kv[0].replaceAll("[\"\\s]", "");
                    double val = Double.parseDouble(kv[1].trim());
                    rates.put(key.toUpperCase(), val);
                }
            }
        } catch (Exception e) {
            // ignore parse errors
        }
        return rates;
    }
}
