package com.financial.risk.stock_risk_mcp_server;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class StockRiskTools {

    @Tool(description = "Health check tool to verify Java MCP Risk Engine connectivity")
    public String pingEngine(String clientName) {
        return "Java Spring Boot Stock Risk Engine is ONLINE! Connected client: " + clientName;
    }

    @Tool(description = "Calculates basic annualized volatility for a given ticker symbol")
    public RiskResponse calculateBasicVolatility(String ticker, double priceChangeStdDev) {
        // Annualized Volatility = Daily Standard Deviation * sqrt(252 trading days)
        double annualizedVol = priceChangeStdDev * Math.sqrt(252);
        return new RiskResponse(ticker.toUpperCase(), annualizedVol, "SUCCESS");
    }

    public record RiskResponse(String ticker, double annualizedVolatility, String status) {}
}
