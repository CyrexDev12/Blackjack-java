import java.util.ArrayList;
import java.util.List;

// Player.java - Represents a player in the game
public class Player {
    private String name;
    private int balance; // Currency or chips the player has
    private List<Card> hand;
    private boolean isDealer; // Flag to indicate if this player is the dealer

    // Constructor with isDealer flag
    public Player(String name, boolean isDealer) {
        this.name = name;
        this.isDealer = isDealer;
        if (!isDealer) {
            this.balance = 1000; // Starting amount for regular players
        }
        this.hand = new ArrayList<>();
    }

    // Overloaded constructor for regular players (defaults isDealer to false)
    public Player(String name) {
        this(name, false);
    }

    // Method to add a card to the player's hand
    public void receiveCard(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }

    // Method to calculate the score of the player's hand
    public int calculateScore() {
        int score = 0;
        int aces = 0;

        for (Card card : hand) {
            String rank = card.getRank();
            switch (rank) {
                case "2": case "3": case "4": case "5": case "6":
                case "7": case "8": case "9": case "10":
                    score += Integer.parseInt(rank);
                    break;
                case "Jack": case "Queen": case "King":
                    score += 10;
                    break;
                case "Ace":
                    score += 11;
                    aces++;
                    break;
            }
        }

        // Adjust for Aces if score is over 21
        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }

        return score;
    }

    // Method to adjust the player's balance after a win or loss
    public void adjustBalance(int amount) {
        if (!isDealer) { // Only adjust balance for regular players
            balance += amount;
        }
    }

    // Method to return the current balance
    public int getBalance() {
        return isDealer ? 0 : balance; // Dealers have no balance
    }

    // Method to get the player's hand
    public List<Card> getHand() {
        return hand;
    }

    // Method to clear the player's hand (e.g., between rounds)
    public void clearHand() {
        hand.clear();
    }

    // Method to get the player's name
    public String getName() {
        return name;
    }

    // String representation of the player's hand and score
    @Override
    public String toString() {
        if (isDealer) {
            return name + "'s hand: " + hand + " (Score: " + calculateScore() + ")";
        } else {
            return name + "'s hand: " + hand + " (Score: " + calculateScore() + ", Balance: $" + balance + ")";
        }
    }
}
