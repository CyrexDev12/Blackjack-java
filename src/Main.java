import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Deck deck = new Deck();
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameUI ui = new GameUI();
            ui.setVisible(true);
        });
        deck.shuffle();

        // Instantiate regular player and dealer
        Player player = new Player("You");
        Player dealer = new Player("Dealer", true); // Dealer has isDealer = true

        boolean continuePlaying = true;

        while (continuePlaying && player.getBalance() > 0) {
            // Clear hands and shuffle the deck at the start of each round
            player.clearHand();
            dealer.clearHand();
            deck = new Deck(); // Create a new deck for each round
            deck.shuffle();

            // Betting
            System.out.println("\nYour current balance: $" + player.getBalance());
            System.out.print("Enter your bet: ");
            int bet;
            try {
                bet = Integer.parseInt(scanner.nextLine());
                if (bet > player.getBalance() || bet <= 0) {
                    System.out.println("Invalid bet amount. Try again.");
                    continue;
                }
            } catch (NumberFormatException e) { // If the player doesnt enter an int
                System.out.println("Invalid input. Enter a number.");
                continue;
            }

            player.adjustBalance(-bet); // Deduct the bet from the player's balance

            // Initial dealing: two cards each
            player.receiveCard(deck.dealCard());
            player.receiveCard(deck.dealCard());
            dealer.receiveCard(deck.dealCard());
            dealer.receiveCard(deck.dealCard());

            // Display initial hands
            System.out.println("\nDealer's visible card: " + dealer.getHand().get(0));
            System.out.println("Your hand: " + player.getHand() + " (Score: " + player.calculateScore() + ")");

            // Player's turn
            boolean playerTurn = true;
            while (playerTurn) {
                System.out.print("Do you want to (h)it or (s)tand? ");
                String action = scanner.nextLine().trim().toLowerCase();

                if (action.equals("h")) {
                    Card newCard = deck.dealCard();
                    player.receiveCard(newCard);
                    System.out.println("You drew: " + newCard);
                    System.out.println("Your hand: " + player.getHand() + " (Score: " + player.calculateScore() + ")");

                    if (player.calculateScore() > 21) {
                        System.out.println("You busted! Dealer wins.");
                        playerTurn = false;
                        break;
                    }
                } else if (action.equals("s")) {
                    playerTurn = false;
                } else {
                    System.out.println("Invalid input. Please enter 'h' to hit or 's' to stand.");
                }
            }

            // If player busts, skip dealer's turn
            if (player.calculateScore() > 21) {
                // Player already busted; proceed to next round
            } else {
                // Dealer's turn: must hit until score >= 17
                System.out.println("\nDealer's turn:");
                System.out.println("Dealer's hand: " + dealer.getHand() + " (Score: " + dealer.calculateScore() + ")");

                while (dealer.calculateScore() < 17) {
                    System.out.println("Dealer chooses to hit.");
                    Card newCard = deck.dealCard();
                    dealer.receiveCard(newCard);
                    System.out.println("Dealer drew: " + newCard);
                    System.out.println("Dealer's hand: " + dealer.getHand() + " (Score: " + dealer.calculateScore() + ")");
                }

                // Check if dealer busts
                if (dealer.calculateScore() > 21) {
                    System.out.println("Dealer busted! You win.");
                    player.adjustBalance(bet * 2); // Payout is double the bet
                } else {
                    // Compare scores to determine the winner
                    int playerScore = player.calculateScore();
                    int dealerScore = dealer.calculateScore();

                    System.out.println("\nFinal Scores:");
                    System.out.println("Your score: " + playerScore);
                    System.out.println("Dealer's score: " + dealerScore);

                    if (playerScore > dealerScore) {
                        System.out.println("You win!");
                        player.adjustBalance(bet * 2); // Payout
                    } else if (dealerScore > playerScore) {
                        System.out.println("Dealer wins!");
                        // Bet already deducted
                    } else {
                        System.out.println("It's a tie! Returning your bet.");
                        player.adjustBalance(bet); // Return the bet
                    }
                }
            }

            // Display updated balance
            System.out.println("Your new balance: $" + player.getBalance());

            // Check if player wants to continue
            if (player.getBalance() > 0) {
                System.out.print("\nDo you want to play another round? (y/n): ");
                String continueInput = scanner.nextLine().trim().toLowerCase();
                if (!continueInput.equals("y")) {
                    continuePlaying = false;
                }
            } else {
                System.out.println("You have run out of balance! Game over.");
                continuePlaying = false;
            }
        }

        System.out.println("\nThank you for playing! Your final balance: $" + player.getBalance());
        scanner.close();
    }
}
