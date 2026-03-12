import java.util.*;

/**
 * UseCase2GitHubRateLimiter
 * Simulates GitHub API rate limiting per client.
 */

class RateLimitBucket {

    int used;
    int limit;
    long resetTime;

    public RateLimitBucket(int limit) {
        this.limit = limit;
        this.used = 0;
        this.resetTime = System.currentTimeMillis() + 3600000; // 1 hour
    }

    public void resetIfNeeded() {
        if (System.currentTimeMillis() > resetTime) {
            used = 0;
            resetTime = System.currentTimeMillis() + 3600000;
        }
    }

    public boolean allowRequest() {

        resetIfNeeded();

        if (used < limit) {
            used++;
            return true;
        }

        return false;
    }

    public int getRemaining() {
        return limit - used;
    }

    public long getResetTime() {
        return resetTime;
    }
}

public class DistributedRateLimiter {

    // clientId -> rate bucket
    private HashMap<String, RateLimitBucket> clients = new HashMap<>();

    private int limitPerHour = 1000;

    public void checkRateLimit(String clientId) {

        clients.putIfAbsent(clientId, new RateLimitBucket(limitPerHour));

        RateLimitBucket bucket = clients.get(clientId);

        if (bucket.allowRequest()) {
            System.out.println("Allowed (" +
                    bucket.getRemaining() +
                    " requests remaining)");
        } else {

            long seconds = (bucket.getResetTime() - System.currentTimeMillis()) / 1000;

            System.out.println("Denied (0 requests remaining, retry after "
                    + seconds + " seconds)");
        }
    }

    public void getRateLimitStatus(String clientId) {

        RateLimitBucket bucket = clients.get(clientId);

        if (bucket == null) {
            System.out.println("Client not found");
            return;
        }

        System.out.println("Rate Limit Status:");
        System.out.println("{ used: " + bucket.used +
                ", limit: " + bucket.limit +
                ", reset: " + bucket.getResetTime() + " }");
    }

    public static void main(String[] args) {

        DistributedRateLimiter limiter = new DistributedRateLimiter();

        String client = "abc123";

        // simulate API requests
        for (int i = 0; i < 5; i++) {
            limiter.checkRateLimit(client);
        }

        limiter.getRateLimitStatus(client);
    }
}

