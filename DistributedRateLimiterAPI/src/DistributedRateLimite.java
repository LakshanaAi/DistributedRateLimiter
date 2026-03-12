import java.util.*;

/**
 * UseCase3StripeRateLimiter
 * Simulates Stripe payment API throttling using a token bucket system.
 */

class StripeTokenBucket {

    int tokens;
    int maxTokens;
    long lastRefillTime;

    public StripeTokenBucket(int maxTokens) {
        this.maxTokens = maxTokens;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public void refillTokens() {

        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;

        // refill every hour
        if (elapsed >= 3600000) {
            tokens = maxTokens;
            lastRefillTime = now;
        }
    }

    public boolean allowPaymentRequest() {

        refillTokens();

        if (tokens > 0) {
            tokens--;
            return true;
        }

        return false;
    }

    public int getRemainingTokens() {
        return tokens;
    }
}

public class DistributedRateLimite {

    // clientId -> token bucket
    private HashMap<String, StripeTokenBucket> clients = new HashMap<>();

    private int requestLimit = 1000;

    public void processPaymentRequest(String clientId) {

        clients.putIfAbsent(clientId, new StripeTokenBucket(requestLimit));

        StripeTokenBucket bucket = clients.get(clientId);

        if (bucket.allowPaymentRequest()) {

            System.out.println("Payment API Request Allowed (" +
                    bucket.getRemainingTokens() +
                    " remaining)");

        } else {

            System.out.println("Payment API Rate Limit Exceeded. Try again later.");
        }
    }

    public static void main(String[] args) {

        DistributedRateLimite limiter = new DistributedRateLimite();

        String client = "client_789";

        // simulate payment API requests
        for (int i = 0; i < 5; i++) {
            limiter.processPaymentRequest(client);
        }
    }
}
