public class Card {
    private String rank;
    private String suit;

    // Constructor 

    public Card(String rank, String suit) {
        this.rank = rank;
        this.suit = suit; 
    }

    public String getRank() {
        return rank;
    }

    public String getSuit() {
        return suit;
    }

    // String representation of the card

    public String toString() {
        return rank + " of " + suit;
    }
}
