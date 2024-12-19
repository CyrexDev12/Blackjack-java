import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Deck.java - Represents a deck of cards
public class Deck {
    private List<Card> cards; // List to store Card objects

    // Constructor to initialize the deck
    public Deck() {
        cards = new ArrayList<>();
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};

        // Create all 52 cards
        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    // Shuffle the deck
    public void shuffle() {
        Collections.shuffle(cards);
    }

    // Deal a card from the deck
    public Card dealCard() {
        if (!cards.isEmpty()) {
            return cards.remove(cards.size() - 1); // Remove and return the top card
        } else {
            return null; // If the deck is empty, return null
        }
    }

    // Print all cards in the deck (for debugging purposes)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card.toString()).append("\n");
        }
        return sb.toString();
    }
}
