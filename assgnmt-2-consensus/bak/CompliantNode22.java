import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * {@code CompliantNode} refers to a node that follows the rules (not
 * malicious).
 * <p/>
 * Score: 68/100
 * <p/>
 * Tests for this assignment involve your submitted miner competing with a
 * number of different types of malicious miners
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.1, p_malicious = 0.3, p_txDistribution = 0.01, numRounds = 10
 * On average 13 out of 72 of nodes reach consensus
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.1, p_malicious = 0.3, p_txDistribution = 0.05, numRounds = 10
 * On average 70 out of 72 of nodes reach consensus
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.1, p_malicious = 0.45, p_txDistribution = 0.01, numRounds = 10
 * On average 9 out of 58 of nodes reach consensus
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.1, p_malicious = 0.45, p_txDistribution = 0.05, numRounds = 10
 * On average 46 out of 58 of nodes reach consensus
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.2, p_malicious = 0.3, p_txDistribution = 0.01, numRounds = 10
 * On average 59 out of 76 of nodes reach consensus
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.2, p_malicious = 0.3, p_txDistribution = 0.05, numRounds = 10
 * On average 71 out of 76 of nodes reach consensus
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.2, p_malicious = 0.45, p_txDistribution = 0.01, numRounds = 10
 * On average 33 out of 54 of nodes reach consensus
 *
 * Running test with parameters: numNodes = 100, p_graph = 0.2, p_malicious = 0.45, p_txDistribution = 0.05, numRounds = 10
 * On average 48 out of 54 of nodes reach consensus
 *
 * @since 05/28/18
 */
public class CompliantNode22 implements Node {
    private double pGraph;

    private double pMalicious;

    private double pTxDistribution;

    private int numRounds;

    private boolean[] followees;

    private boolean[] blackListed;

    private Set<Transaction> pendingTransactions;


    /**
     * @param p_graph          the pairwise connectivity probability of the
     *                         random graph: e.g. {.1, .2, .3}
     * @param p_malicious      the probability that a node will be set to be
     *                         malicious: e.g {.15, .30, .45}
     * @param p_txDistribution the probability that each of the initial valid
     *                         transactions will be communicated: e.g. {.01,
     *                         .05, .10}
     * @param numRounds        the number of rounds in the simulation e.g. {10,
     *                         20}
     */
    public CompliantNode(double p_graph, double p_malicious,
                         double p_txDistribution, int numRounds) {
        this.pGraph = p_graph;
        this.pMalicious = p_malicious;
        this.pTxDistribution = p_txDistribution;
        this.numRounds = numRounds;

        pendingTransactions = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        blackListed = new boolean[followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        if (!pendingTransactions.isEmpty()) {
            this.pendingTransactions.addAll(pendingTransactions);
        }
    }

    public Set<Transaction> sendToFollowers() {
        Set<Transaction> sendTransactions = new HashSet<>(pendingTransactions);
        pendingTransactions.clear();

        return sendTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        Set<Integer> senders =
                candidates.stream().map(c -> c.sender).collect(toSet());
        for (int i = 0; i < followees.length; i++) {
            if (followees[i] && !senders.contains(i)) {
                blackListed[i] = true;
            }
        }

        Map<Integer, Set<Transaction>> followeeToTransactionMap = new HashMap<>();
        Map<Transaction, Set<Integer>> txToFolloweeMap = new HashMap<>();
        for (Candidate c : candidates) {
            if (!blackListed[c.sender]) {
                if (!followeeToTransactionMap.containsKey(c.sender)) {
                    followeeToTransactionMap.put(c.sender, new HashSet<>());
                }

                if (!txToFolloweeMap.containsKey(c.tx)) {
                    txToFolloweeMap.put(c.tx, new HashSet<>());
                }

                txToFolloweeMap.get(c.tx).add(c.sender);
                if (followeeToTransactionMap.get(c.sender).contains(c.tx)) {
                    blackListed[c.sender] = true;
                } else {
                    followeeToTransactionMap.get(c.sender).add(c.tx);
                    pendingTransactions.add(c.tx);
                }
            }
        }

        txToFolloweeMap.forEach((k, v) -> {
            if (v.size() == 1) {
                Integer[] sdrs = v.toArray(new Integer[0]);
                Set<Transaction> txs = followeeToTransactionMap.get(sdrs[0]);
                boolean shouldBeBlackListed = true;
                for (Transaction tx : txs) {
                    if (txToFolloweeMap.get(tx).size() > 1) {
                        shouldBeBlackListed = false;
                        break;
                    }
                }

                if (shouldBeBlackListed) {
                    blackListed[sdrs[0]] = true;
                }
            }
        });
    }
}