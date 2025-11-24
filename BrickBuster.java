import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class BrickBuster extends JPanel implements ActionListener {
    private final int BOARD_WIDTH = 800;
    private final int BOARD_HEIGHT = 600;
    private final int PADDLE_WIDTH = 100;
    private final int PADDLE_HEIGHT = 20;
    private final int BALL_SIZE = 20;
    private final int BRICK_WIDTH = 75;
    private final int BRICK_HEIGHT = 20;
    private final int BRICK_ROWS = 5;
    private final int BRICK_COLS = 10;
    private final int DELAY = 8;

    private Rectangle paddle;
    private Rectangle ball;
    private ArrayList<Brick> bricks;
    private int ballDx = 3;
    private int ballDy = -3;
    private int paddleSpeed = 0;
    private boolean running = false;
    private Timer timer;
    private int score = 0;
    private int lives = 3;
    private Font consoleFont;
    private Random random;
    private int currentPaddleWidth;

    class Brick {
        Rectangle rect;
        int health;
        int maxHealth;

        Brick(int x, int y, int health) {
            this.rect = new Rectangle(x, y, BRICK_WIDTH, BRICK_HEIGHT);
            this.health = health;
            this.maxHealth = health;
        }

        Color getColor() {
            switch (maxHealth) {
                case 1: return Color.YELLOW;
                case 2: return health == 2 ? Color.ORANGE : new Color(255, 165, 0, 150);
                case 3: return health == 3 ? Color.RED : 
                           health == 2 ? new Color(255, 100, 100) : new Color(255, 150, 150);
                default: return Color.WHITE;
            }
        }
    }

    public BrickBuster() {
        this.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        consoleFont = new Font("Courier New", Font.BOLD, 16);
        random = new Random();

        initGame();
        startGame();
    }

    public void initGame() {
        currentPaddleWidth = PADDLE_WIDTH;
        paddle = new Rectangle(
            BOARD_WIDTH / 2 - currentPaddleWidth / 2,
            BOARD_HEIGHT - 50,
            currentPaddleWidth,
            PADDLE_HEIGHT
        );

        ball = new Rectangle(
            BOARD_WIDTH / 2 - BALL_SIZE / 2,
            BOARD_HEIGHT / 2,
            BALL_SIZE,
            BALL_SIZE
        );

        bricks = new ArrayList<>();
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLS; col++) {
                int health = row + 1;
                bricks.add(new Brick(
                    col * (BRICK_WIDTH + 5) + 35,
                    row * (BRICK_HEIGHT + 5) + 50,
                    health
                ));
            }
        }
    }

    public void startGame() {
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            g.setColor(Color.WHITE);
            g.fillRect(paddle.x, paddle.y, paddle.width, paddle.height);

            g.setColor(Color.YELLOW);
            g.fillOval(ball.x, ball.y, ball.width, ball.height);

            for (Brick brick : bricks) {
                g.setColor(brick.getColor());
                g.fillRect(brick.rect.x, brick.rect.y, brick.rect.width, brick.rect.height);
                g.setColor(Color.WHITE);
                g.drawRect(brick.rect.x, brick.rect.y, brick.rect.width, brick.rect.height);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString(String.valueOf(brick.health), 
                    brick.rect.x + brick.rect.width/2 - 3, 
                    brick.rect.y + brick.rect.height/2 + 4);
            }

            g.setColor(Color.WHITE);
            g.setFont(consoleFont);
            g.drawString("SCORE: " + score, 10, 25);
            g.drawString("LIVES: " + lives, 10, 45);
            g.drawString("BRICKS: " + bricks.size(), BOARD_WIDTH - 120, 25);

            g.drawString("A/D or LEFT/RIGHT to move", 10, BOARD_HEIGHT - 40);
            g.drawString("ESC to restart", 10, BOARD_HEIGHT - 20);
        } else {
            gameOver(g);
        }
    }

    public void update() {
        if (running) {
            paddle.x += paddleSpeed;
            if (paddle.x < 0) paddle.x = 0;
            if (paddle.x > BOARD_WIDTH - paddle.width) {
                paddle.x = BOARD_WIDTH - paddle.width;
            }

            ball.x += ballDx;
            ball.y += ballDy;

            if (ball.x <= 0 || ball.x >= BOARD_WIDTH - ball.width) {
                ballDx = -ballDx;
            }
            if (ball.y <= 0) {
                ballDy = -ballDy;
            }

            if (ball.intersects(paddle)) {
                ballDy = -ballDy;
                int hitPos = (ball.x + ball.width / 2) - (paddle.x + paddle.width / 2);
                ballDx = hitPos / 10;
                if (ballDx == 0) ballDx = 1;
            }

            for (int i = bricks.size() - 1; i >= 0; i--) {
                Brick brick = bricks.get(i);
                if (ball.intersects(brick.rect)) {
                    brick.health--;
                    ballDy = -ballDy;
                    score += 10 * brick.maxHealth;
                    if (brick.health <= 0) {
                        bricks.remove(i);
                    }
                    break;
                }
            }

            if (ball.y > BOARD_HEIGHT) {
                lives--;
                currentPaddleWidth = Math.max(50, currentPaddleWidth - 20); // Reduce paddle width, min 50
                paddle.width = currentPaddleWidth;
                paddle.x = Math.min(paddle.x, BOARD_WIDTH - paddle.width); // Adjust position if needed
                if (lives <= 0) {
                    running = false;
                } else {
                    resetBall();
                }
            }

            if (bricks.isEmpty()) {
                running = false;
            }
        }
    }

    public void resetBall() {
        ball.x = BOARD_WIDTH / 2 - BALL_SIZE / 2;
        ball.y = BOARD_HEIGHT / 2;
        ballDx = 3;
        ballDy = -3;
    }

    public void gameOver(Graphics g) {
        String message;
        Color textColor;

        if (bricks.isEmpty()) {
            message = "YOU WIN!";
            textColor = Color.GREEN;
        } else {
            message = "GAME OVER";
            textColor = Color.RED;
        }

        g.setColor(textColor);
        g.setFont(new Font("Courier New", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString(message, 
            (BOARD_WIDTH - metrics1.stringWidth(message)) / 2, 
            BOARD_HEIGHT / 2 - 50);

        g.setColor(Color.WHITE);
        g.setFont(consoleFont);
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        String scoreText = "Final Score: " + score;
        g.drawString(scoreText, 
            (BOARD_WIDTH - metrics2.stringWidth(scoreText)) / 2, 
            BOARD_HEIGHT / 2);

        String restartText = "Press ESC to restart";
        g.drawString(restartText, 
            (BOARD_WIDTH - metrics2.stringWidth(restartText)) / 2, 
            BOARD_HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    paddleSpeed = -5;
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    paddleSpeed = 5;
                    break;
                case KeyEvent.VK_ESCAPE:
                    timer.stop();
                    score = 0;
                    lives = 3;
                    ballDx = 3;
                    ballDy = -3;
                    paddleSpeed = 0;
                    initGame();
                    startGame();
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    paddleSpeed = 0;
                    break;
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Brick Buster");
        BrickBuster game = new BrickBuster();

        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        frame.getContentPane().setBackground(Color.BLACK);

        System.out.println("=== BRICK BUSTER GAME ===");
        System.out.println("FEATURES:");
        System.out.println("• Brick Health System - Top rows need multiple hits");
        System.out.println("• Paddle shrinks when losing a life (minimum 50px)");
        System.out.println("Controls: A/D or Arrow Keys, ESC to restart");
        System.out.println("=====================");
    }
}