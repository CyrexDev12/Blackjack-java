import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.ArrayList;

public class GameUI extends JFrame {
    private Deck deck;
    private Player player;
    private Player dealer;

    private JLabel balanceLabel;
    private JLabel messageLabel;
    private JLabel dealerScoreLabel;
    private JLabel playerScoreLabel;

    private JButton hitButton;
    private JButton standButton;
    private JButton newGameButton;
    private JTextField betField;

    private JPanel dealerCardsPanel;
    private JPanel playerCardsPanel;

    private boolean roundActive = false;
    private int currentBet = 0;

    // Base card dimensions
    private static final int CARD_WIDTH_BASE = 80;
    private static final int CARD_HEIGHT_BASE = 115;

    // Current card dimensions (updated on resize)
    private int cardWidth = CARD_WIDTH_BASE;
    private int cardHeight = CARD_HEIGHT_BASE;

    private JPanel mainPanel;
    private JPanel playerPanel;
    private JPanel dealerPanel;

    // For animations
    private JLayeredPane layeredPane;
    private JLabel animatingCardLabel;
    private Point animStart;
    private Point animEnd;
    private Timer animationTimer;
    private int animDuration = 300; // ms
    private long animStartTime;
    private Card animatingNewCard;
    private boolean animatingCard = false;

    public GameUI() {
        setTitle("Blackjack Game");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center

        player = new Player("You");
        dealer = new Player("Dealer", true);
        deck = new Deck();
        deck.shuffle();

        createComponents();
        layoutComponents();
        addResizeListener();
        setupLayeredPane();

        // Initially no round: hide Hit & Stand, show only New Game
        hitButton.setVisible(false);
        standButton.setVisible(false);
        messageLabel.setText("Enter your bet and click 'New Game' to start.");
        newGameButton.setVisible(true);

        updateScaling();
    }

    private void createComponents() {
        balanceLabel = new JLabel("Balance: $" + player.getBalance());
        messageLabel = new JLabel("Enter your bet and click 'New Game' to start.", SwingConstants.RIGHT);
        dealerScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        playerScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);

        Font labelFont = new Font("SansSerif", Font.BOLD, 16);
        Font messageFont = new Font("SansSerif", Font.PLAIN, 14);
        balanceLabel.setFont(labelFont);
        messageLabel.setFont(messageFont);
        dealerScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        playerScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        hitButton = new JButton("HIT");
        standButton = new JButton("STAND");
        newGameButton = new JButton("New Game");

        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);
        hitButton.setFont(buttonFont);
        standButton.setFont(buttonFont);
        newGameButton.setFont(buttonFont);

        hitButton.setBackground(new Color(220, 20, 60));
        hitButton.setForeground(Color.BLACK);
        hitButton.setOpaque(true);
        hitButton.setContentAreaFilled(true);
        hitButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        hitButton.setPreferredSize(new Dimension(100, 45));

        standButton.setBackground(new Color(34, 139, 34));
        standButton.setForeground(Color.BLACK);
        standButton.setOpaque(true);
        standButton.setContentAreaFilled(true);
        standButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        standButton.setPreferredSize(new Dimension(100, 45));

        newGameButton.setOpaque(true);
        newGameButton.setContentAreaFilled(true);

        hitButton.addActionListener(e -> playerHit());
        standButton.addActionListener(e -> playerStand());
        newGameButton.addActionListener(e -> startNewRound());

        hitButton.setToolTipText("Draw another card.");
        standButton.setToolTipText("End your turn.");
        newGameButton.setToolTipText("Start a new round.");

        betField = new JTextField(5);

        dealerCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        dealerCardsPanel.setOpaque(false);
        playerCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        playerCardsPanel.setOpaque(false);
    }

    private void layoutComponents() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(0, 100, 0)); // billiards green
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 20)); // revert to better brown

        // Dealer panel top
        dealerPanel = new JPanel(new BorderLayout());
        dealerPanel.setOpaque(false);
        dealerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Dealer", TitledBorder.CENTER, TitledBorder.TOP));
        ((TitledBorder)dealerPanel.getBorder()).setTitleColor(Color.WHITE);
        dealerPanel.add(dealerCardsPanel, BorderLayout.CENTER);

        JPanel dealerScorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        dealerScorePanel.setOpaque(false);
        dealerScoreLabel.setForeground(Color.WHITE);
        dealerScorePanel.add(dealerScoreLabel);
        dealerPanel.add(dealerScorePanel, BorderLayout.SOUTH);

        mainPanel.add(dealerPanel, BorderLayout.NORTH);

        // Player panel center
        playerPanel = new JPanel(new BorderLayout());
        playerPanel.setOpaque(false);
        playerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Player", TitledBorder.CENTER, TitledBorder.TOP));
        ((TitledBorder)playerPanel.getBorder()).setTitleColor(Color.WHITE);
        playerPanel.add(playerCardsPanel, BorderLayout.CENTER);

        JPanel playerScorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        playerScorePanel.setOpaque(false);
        playerScoreLabel.setForeground(Color.WHITE);
        playerScorePanel.add(playerScoreLabel);
        playerPanel.add(playerScorePanel, BorderLayout.SOUTH);

        mainPanel.add(playerPanel, BorderLayout.CENTER);

        // Bottom panel: Balance + Bet on left, Buttons center, Message right
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // Left side: Balance + Bet field
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        leftPanel.setOpaque(false);
        balanceLabel.setForeground(Color.WHITE);
        leftPanel.add(balanceLabel);

        // Add bet label & field
        JLabel betLabel = new JLabel("Bet: ");
        betLabel.setForeground(Color.WHITE);
        leftPanel.add(betLabel);
        leftPanel.add(betField);
        bottomPanel.add(leftPanel, BorderLayout.WEST);

        // Center: Buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);
        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(newGameButton);
        bottomPanel.add(controlPanel, BorderLayout.CENTER);

        // Right: Message
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        messagePanel.setOpaque(false);
        messageLabel.setForeground(Color.WHITE);
        messagePanel.add(messageLabel);
        bottomPanel.add(messagePanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupLayeredPane() {
        layeredPane = getLayeredPane();
        animatingCardLabel = new JLabel();
        animatingCardLabel.setVisible(false);
        layeredPane.add(animatingCardLabel, JLayeredPane.DRAG_LAYER);
    }

    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateScaling();
                updateUIState();
            }
        });
    }

    private void updateScaling() {
        // Scale based on playerPanel height or mainPanel dimensions
        int panelHeight = playerPanel.getHeight();
        if (panelHeight == 0) panelHeight = getHeight();

        // Approximate scaling: cardHeight about 1/5 of playerPanel height
        cardHeight = Math.max(50, Math.min(panelHeight / 5, CARD_HEIGHT_BASE));
        cardWidth = (cardHeight * CARD_WIDTH_BASE) / CARD_HEIGHT_BASE; // keep aspect ratio
    }

    private void dealInitialCards() {
        player.clearHand();
        dealer.clearHand();
        deck = new Deck();
        deck.shuffle();

        player.receiveCard(deck.dealCard());
        player.receiveCard(deck.dealCard());
        dealer.receiveCard(deck.dealCard());
        dealer.receiveCard(deck.dealCard());
    }

    private void updateUIState() {
        dealerCardsPanel.removeAll();
        playerCardsPanel.removeAll();

        int dealerScore = dealer.calculateScore();
        int playerScore = player.calculateScore();

        dealerScoreLabel.setText("Score: " + dealerScore);
        playerScoreLabel.setText("Score: " + playerScore);

        if (!roundActive) {
            // Show dealer's full hand when round not active
            for (Card card : dealer.getHand()) {
                dealerCardsPanel.add(new JLabel(getScaledCardImage(card)));
            }
        } else {
            // Hide dealer's second card when round active
            if (!dealer.getHand().isEmpty()) {
                dealerCardsPanel.add(new JLabel(getScaledCardImage(cardAt(dealer, 0))));
            }
            if (dealer.getHand().size() > 1) {
                dealerCardsPanel.add(new JLabel(getScaledCardImage(null))); // back card
            }
        }

        for (Card card : player.getHand()) {
            playerCardsPanel.add(new JLabel(getScaledCardImage(card)));
        }

        balanceLabel.setText("Balance: $" + player.getBalance());

        dealerCardsPanel.revalidate();
        dealerCardsPanel.repaint();
        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
    }

    private Card cardAt(Player p, int index) {
        return p.getHand().size() > index ? p.getHand().get(index) : null;
    }

    private void startNewRound() {
        if (!validateBet()) return;
        if (player.getBalance() < currentBet) {
            JOptionPane.showMessageDialog(this, "Insufficient balance for that bet.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        player.adjustBalance(-currentBet);
        newRound();
    }

    private boolean validateBet() {
        String betText = betField.getText().trim();
        if (betText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a bet before starting a new game.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int bet = Integer.parseInt(betText);
            if (bet <= 0) {
                JOptionPane.showMessageDialog(this, "Bet must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            currentBet = bet;
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid bet amount. Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void playerHit() {
        if (!roundActive || animatingCard) return;

        animatingCard = true;
        animatingNewCard = deck.dealCard();

        // Animate card from top-left corner of window (deck position) to player's panel
        // For simplicity, start from top-left corner:
        animStart = new Point(20, 20); 
        // End at player panel last card position
        animEnd = getPlayerCardEndPosition();

        showCardAnimation(animatingNewCard, animStart, animEnd, () -> {
            // After animation completes, actually add card to player's hand
            player.receiveCard(animatingNewCard);
            animatingCard = false;
            animatingNewCard = null;
            updateUIState();

            if (player.calculateScore() > 21) {
                messageLabel.setText("You busted! Dealer wins. Click 'New Game'.");
                hitButton.setVisible(false);
                standButton.setVisible(false);
                roundActive = false;
                showDealerFinal();
                newGameButton.setVisible(true);
            }
        });
    }

    private Point getPlayerCardEndPosition() {
        // Roughly position at the player's panel center + offset
        SwingUtilities.layoutCompoundLabel(
            playerCardsPanel, playerCardsPanel.getFontMetrics(playerCardsPanel.getFont()),
            "", null, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
            new Rectangle(playerCardsPanel.getSize()), new Rectangle(), new Rectangle(), 0
        );

        // We'll place the card at the right end of playerCardsPanel
        int cardCount = player.getHand().size() + (animatingCard ? 1 : 0);
        int x = playerCardsPanel.getX() + playerCardsPanel.getWidth()/2 - (cardCount * (cardWidth + 5))/2 + (cardCount-1)*(cardWidth+5);
        int y = playerCardsPanel.getY() + playerCardsPanel.getParent().getY() + mainPanel.getInsets().top + 40;
        SwingUtilities.convertPointToScreen(new Point(x, y), this);
        Point panelOnScreen = playerCardsPanel.getLocationOnScreen();
        return new Point(panelOnScreen.x + playerCardsPanel.getWidth()/2, panelOnScreen.y + playerCardsPanel.getHeight()/2);
    }

    private void showCardAnimation(Card card, Point start, Point end, Runnable onComplete) {
        ImageIcon cardIcon = getScaledCardImage(null); // back card image during animation
        animatingCardLabel.setIcon(cardIcon);
        animatingCardLabel.setSize(cardIcon.getIconWidth(), cardIcon.getIconHeight());
        layeredPane.setLayer(animatingCardLabel, JLayeredPane.DRAG_LAYER);
        animatingCardLabel.setVisible(true);

        animatingCardLabel.setLocation(start);
        animStartTime = System.currentTimeMillis();

        animationTimer = new Timer(16, e -> {
            float progress = (System.currentTimeMillis() - animStartTime) / (float)animDuration;
            if (progress >= 1.0f) {
                progress = 1.0f;
                animationTimer.stop();
            }

            int nx = (int)(start.x + (end.x - start.x)*progress);
            int ny = (int)(start.y + (end.y - start.y)*progress);
            animatingCardLabel.setLocation(nx, ny);

            if (progress == 1.0f) {
                // End of animation
                animatingCardLabel.setVisible(false);
                if (onComplete != null) onComplete.run();
            }
        });
        animationTimer.start();
    }

    private void playerStand() {
        if (animatingCard) return;
        dealerTurn();
        determineWinner();
    }

    private void dealerTurn() {
        while (dealer.calculateScore() < 17) {
            dealer.receiveCard(deck.dealCard());
        }
    }

    private void determineWinner() {
        int playerScore = player.calculateScore();
        int dealerScore = dealer.calculateScore();

        if (dealerScore > 21 || playerScore > dealerScore) {
            messageLabel.setText("You win! Click 'New Game' to play again.");
            player.adjustBalance(currentBet * 2); // Win double the bet
        } else if (dealerScore > playerScore) {
            messageLabel.setText("Dealer wins! Click 'New Game' to try again.");
            // Bet already deducted at start
        } else {
            messageLabel.setText("It's a tie! Click 'New Game' to play again.");
            player.adjustBalance(currentBet); // return the bet
        }

        roundActive = false;
        hitButton.setVisible(false);
        standButton.setVisible(false);
        showDealerFinal();
        newGameButton.setVisible(true);
    }

    private void showDealerFinal() {
        dealerCardsPanel.removeAll();
        for (Card card : dealer.getHand()) {
            dealerCardsPanel.add(new JLabel(getScaledCardImage(card)));
        }

        playerCardsPanel.removeAll();
        for (Card card : player.getHand()) {
            playerCardsPanel.add(new JLabel(getScaledCardImage(card)));
        }

        balanceLabel.setText("Balance: $" + player.getBalance());

        dealerScoreLabel.setText("Score: " + dealer.calculateScore());
        playerScoreLabel.setText("Score: " + player.calculateScore());

        dealerCardsPanel.revalidate();
        dealerCardsPanel.repaint();
        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
    }

    private void newRound() {
        dealInitialCards();
        roundActive = true;
        messageLabel.setText("Hit or Stand?");
        hitButton.setVisible(true);
        standButton.setVisible(true);
        newGameButton.setVisible(false);
        updateUIState();
    }

    private String getRankAbbreviation(String rank) {
        switch (rank) {
            case "Ace":
                return "a";
            case "King":
                return "k";
            case "Queen":
                return "q";
            case "Jack":
                return "j";
            default:
                return rank.toLowerCase(); // numeric ranks
        }
    }

    private ImageIcon getScaledCardImage(Card card) {
        if (card == null) {
            return scaleImage(getCardBackImage().getImage(), cardWidth, cardHeight);
        }

        String rankAbbrev = getRankAbbreviation(card.getRank());
        String suit = card.getSuit().toLowerCase();
        String fileName = "/img/" + suit + "_" + rankAbbrev + ".png";

        java.net.URL imgURL = getClass().getResource(fileName);
        if (imgURL == null) {
            return scaleImage(getCardBackImage().getImage(), cardWidth, cardHeight);
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return scaleImage(icon.getImage(), cardWidth, cardHeight);
    }

    private ImageIcon getCardBackImage() {
        java.net.URL imgURL = getClass().getResource("/img/back.png");
        if (imgURL == null) {
            return new ImageIcon();
        }
        return new ImageIcon(imgURL);
    }

    private ImageIcon scaleImage(Image img, int width, int height) {
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameUI ui = new GameUI();
            ui.setVisible(true);
        });
    }
}
