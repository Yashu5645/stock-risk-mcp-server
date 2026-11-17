package com.financial.risk.stock_risk_mcp_server;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class StockRiskTools {

    private final OkHttpClient okHttpClient;

    public StockRiskTools(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Tool(description = "Health check tool to verify Java MCP Risk Engine connectivity")
    public String pingEngine(String clientName) {
        return "Java Spring Boot Stock Risk Engine is ONLINE! Connected client: " + clientName;
    }

        @Tool(description = "Calculates basic annualized volatility for a given ticker symbol")
    public RiskResponse calculateBasicVolatility(String ticker) {
        try {
            // Fetch stock data using our private method
            String stockDataJson = fetchStockData(ticker);
            if (stockDataJson == null || stockDataJson.isBlank()) {
                throw new IllegalStateException("Stock data response is empty");
            }
            
            // Parse JSON to extract closing prices
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(stockDataJson);

            // Navigate to your prices array in the JSON response
            com.fasterxml.jackson.databind.JsonNode pricesNode = rootNode.path("prices"); 

            org.apache.commons.math3.stat.descriptive.DescriptiveStatistics stats = new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics();

            if (pricesNode.isArray()) {
                double previousPrice = -1;
                for (com.fasterxml.jackson.databind.JsonNode node : pricesNode) {
                    double closePrice = node.path("close").asDouble();
                    if (previousPrice != -1) {
                        double dailyChange = (closePrice - previousPrice) / previousPrice;
                        stats.addValue(dailyChange);
                    }
                    previousPrice = closePrice;
                }
            }

            // Get the standard deviation of the price changes
            double priceChangeStdDev = stats.getStandardDeviation();
            
            // Annualized Volatility = Daily Standard Deviation * sqrt(252 trading days)
            double annualizedVol = priceChangeStdDev * Math.sqrt(252);
            
            return new RiskResponse(ticker.toUpperCase(), annualizedVol, "SUCCESS");
        } catch (Exception e) {
            return new RiskResponse(ticker.toUpperCase(), 0.0, "ERROR: " + e.getMessage());
        }
    }


    private String fetchStockData(String ticker) throws Exception {
    Request request = new Request.Builder()
        .url("https://api.example.com/stock/" + ticker)
        .build();
    try (Response response = okHttpClient.newCall(request).execute()) {
        return response.body().string();
    }
}
    public record RiskResponse(String ticker, double annualizedVolatility, String status) {}
}
