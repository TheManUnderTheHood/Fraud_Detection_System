package com.example.transaction.service;

import com.example.transaction.Repository.TransactionRepository;
import com.example.transaction.entity.Account;
import com.example.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Allows us to print logs to the console
public class AiAgentService {

    private final ChatClient.Builder chatClientBuilder;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    // @Async tells Spring: "Run this in a separate background thread immediately! Do not make the user wait."
    @Async
    public void analyzeTransactionContext(Transaction currentTx, Account sender) {
        log.info("🤖 AI Agent waking up to analyze transaction TX-{}...", currentTx.getId());

        // 1. Gather Context (the user's past outgoing behavior)
        List<Transaction> history = transactionRepository.findBySenderAccountIdOrReceiverAccountId(sender.getId(), sender.getId()).stream()
                .filter(tx -> tx.getSenderAccount().getId().equals(sender.getId()))
                .filter(tx -> !tx.getId().equals(currentTx.getId()))
                .sorted(Comparator.comparing(Transaction::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .toList();

        // Include the Receiver ID so the AI can detect if they are sending to the same person repeatedly!
        String historyString = history.stream()
                .map(tx -> String.format("Sent $%s to Acc#%d on %s", tx.getAmount(), tx.getReceiverAccount().getId(), tx.getTimestamp()))
                .collect(Collectors.joining(" | "));

        if (historyString.isEmpty()) {
            historyString = "NO HISTORY (New or Dormant Account).";
        }

        BigDecimal avgHistoryAmount = history.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!history.isEmpty()) {
            avgHistoryAmount = avgHistoryAmount.divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);
        }

        // 2. Build the Advanced AML Prompt
        String prompt = String.format("""
            You are a Senior Anti-Money Laundering (AML) and Fraud Investigator AI.
            Analyze the following transaction for financial crime typologies.
            
            [USER CONTEXT]
            - Current Available Balance: $%s
            - Historical Outgoing Average: $%s
            - Recent Transaction History: %s
            
            [NEW TRANSACTION EVENT]
            - Attempted Transfer Amount: $%s
            - Receiver Account ID: %d
            
            [AML TYPOLOGIES TO CHECK]
            1. STRUCTURING / SMURFING: Is the amount intentionally sitting just below major reporting thresholds (e.g., $9,999, $49,900, $95,000) coupled with unusual behavior?
            2. BUST-OUT / ACCOUNT TAKEOVER: Is the user suddenly draining nearly 100%% of their available balance in a single high-value transaction, especially if their history shows only small transfers?
            3. DORMANT REACTIVATION: Is there NO history, yet they are immediately attempting to move a massive sum of money (>$10,000)?
            4. BEHAVIORAL ANOMALY: Does this transaction deviate violently from their historical baseline (e.g., an 800%%+ increase compared to their average)?
            
            [STRICT OUTPUT POLICY]
            - If ANY of the above typologies are met, you MUST reply EXACTLY with: 'FLAGGED: [Name of Typology] - [One sentence explaining why]'.
            - If the transaction appears normal, logical, and safe, reply EXACTLY with: 'CLEAN'.
            - DO NOT output any conversational text, pleasantries, or markdown. Only output the exact string format.
            """,
                sender.getBalance(),
                avgHistoryAmount,
                historyString,
                currentTx.getAmount(),
                currentTx.getReceiverAccount().getId());

        // 3. Call OpenRouter (OpenAI-compatible) using Spring AI ChatClient
        try {
            ChatClient chatClient = chatClientBuilder.build();
            String aiRaw = chatClient.prompt(prompt).call().content();
            String aiResponse = aiRaw == null ? "" : aiRaw.trim();

            log.info("🤖 AI Agent Conclusion: {}", aiResponse);

            // 4. Take Action based on AI Decision
            // 4. Take Action based on AI Decision
            // 4. Take Action based on AI Decision (Bulletproofed)
            if (aiResponse.toUpperCase().contains("FLAGGED")) {
                // The AI caught something! Create a Fraud Alert asynchronously.
                fraudDetectionService.createFraudAlert(currentTx, "[AI AGENT] " + aiResponse);
                log.warn("🚨 AI Agent generated a new Fraud Alert for TX-{}", currentTx.getId());
            }

        } catch (Exception e) {
            log.error("❌ AI Agent failed to analyze transaction: {}", e.getMessage());
        }
    }
}
