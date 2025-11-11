import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.*;   // <-- music support
import java.io.InputStream;
import java.io.File; // <--- NEW IMPORT: Needed for absolute file paths

public class Main extends JPanel implements ActionListener, KeyListener {

    // Window size
    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;

    // Player
    private Player player;

    // Game objects
    private ArrayList<Enemy> enemies;
    private ArrayList<Particle> particles;

    // Controls
    private boolean up, down, left, right;

    // Random
    private Random rand = new Random();

    // Game loop timer (fixed)
    private javax.swing.Timer timer;
    private int timerDelay = 16;   // ~60 FPS

    // Game state
    private int score = 0;
    private boolean gameOver = false;
    private boolean started = false;

    // Music
    private Clip musicClip;

    // ---------------------------
    // Constructor
    // ---------------------------
    public Main () {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        player = new Player(WIDTH / 2, HEIGHT - 80);
        enemies = new ArrayList<>();
        particles = new ArrayList<>();

        timer = new javax.swing.Timer(timerDelay, this);
        timer.start();

        // Start background music
        // This path must exactly match the location of your bgm.wav file.
        playMusic("C:\\Users\\lenovo\\IdeaProjects\\Dodge the Blocks\\res\\bgm.wav");
    }

    // ---------------------------
    // Play looping background music (MODIFIED FOR ABSOLUTE PATH)
    // ---------------------------
    private void playMusic(String filename) {
        try {
            // 1. Create a File object from the absolute path
            File audioFile = new File(filename);

            if (!audioFile.exists()) {
                System.out.println("ERROR: Could not find music file at system path: " + filename);
                return;
            }

            // 2. Load the audio directly from the File object
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Music should be playing!");

        } catch (Exception e) {
            System.out.println("An error occurred during music playback. Check WAV format!");
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    // ---------------------------
    // Main game loop
    // ---------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            repaint();
            return;
        }

        if (started) {
            updatePlayer();
            spawnEnemies();
            updateEnemies();
            updateParticles();
            score++;
        }

        repaint();
    }

    // ---------------------------
    // Player movement
    // ---------------------------
    private void updatePlayer() {
        int speed = 6;

        if (left) player.x -= speed;
        if (right) player.x += speed;
        if (up) player.y -= speed;
        if (down) player.y += speed;

        if (player.x < 0) player.x = 0;
        if (player.x > WIDTH - player.size) player.x = WIDTH - player.size;
        if (player.y < 0) player.y = 0;
        if (player.y > HEIGHT - player.size) player.y = HEIGHT - player.size;
    }

    // ---------------------------
    // Enemy spawning
    // ---------------------------
    private void spawnEnemies() {
        if (rand.nextInt(15) == 0) {
            enemies.add(new Enemy(rand.nextInt(WIDTH - 40), -40));
        }
    }

    // ---------------------------
    // Enemy updates + collision
    // ---------------------------
    private void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            en.y += en.speed;

            if (en.y > HEIGHT + 50) {
                enemies.remove(i);
                continue;
            }

            if (player.collides(en)) {
                explode(player.x, player.y);
                gameOver = true;
            }
        }
    }

    // ---------------------------
    // Particles
    // ---------------------------
    private void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (!p.alive) particles.remove(i);
        }
    }

    private void explode(int x, int y) {
        for (int i = 0; i < 30; i++)
            particles.add(new Particle(x, y, rand));
    }

    // ---------------------------
    // Drawing
    // ---------------------------
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        g2.drawString("Score: " + score, 20, 30);

        player.draw(g2);

        for (Enemy en : enemies) en.draw(g2);
        for (Particle p : particles) p.draw(g2);

        if (!started) drawStart(g2);
        if (gameOver) drawGameOver(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(40, 40, 40));
        for (int x = 0; x < WIDTH; x += 50)
            g2.drawLine(x, 0, x, HEIGHT);
        for (int y = 0; y < HEIGHT; y += 50)
            g2.drawLine(0, y, WIDTH, y);
    }

    private void drawStart(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.setColor(Color.WHITE);
        g2.drawString("Arcade Dodger", WIDTH / 2 - 150, HEIGHT / 2 - 30);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Press SPACE to start",
                WIDTH / 2 - 110,
                HEIGHT / 2 + 20);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.setColor(Color.RED);
        g2.drawString("Game Over", WIDTH / 2 - 120, HEIGHT / 2 - 30);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Final Score: " + score,
                WIDTH / 2 - 70,
                HEIGHT / 2 + 10);
        g2.drawString("Press R to restart",
                WIDTH / 2 - 90,
                HEIGHT / 2 + 40);
    }

    // ---------------------------
    // Key handling
    // ---------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_SPACE && !started) started = true;
        if (k == KeyEvent.VK_R && gameOver) restart();

        if (k == KeyEvent.VK_LEFT) left = true;
        if (k == KeyEvent.VK_RIGHT) right = true;
        if (k == KeyEvent.VK_UP) up = true;
        if (k == KeyEvent.VK_DOWN) down = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_LEFT) left = false;
        if (k == KeyEvent.VK_RIGHT) right = false;
        if (k == KeyEvent.VK_UP) up = false;
        if (k == KeyEvent.VK_DOWN) down = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // ---------------------------
    // Restart the game
    // ---------------------------
    private void restart() {
        score = 0;
        gameOver = false;

        enemies.clear();
        particles.clear();

        player.x = WIDTH / 2;
        player.y = HEIGHT - 80;

        started = false;
    }

    // ---------------------------
    // Object classes
    // ---------------------------
    class Player {
        int x, y;
        int size = 40;

        Player(int x, int y) {
            this.x = x;
            this.y = y;
        }

        boolean collides(Enemy e) {
            return new Rectangle(x, y, size, size)
                    .intersects(new Rectangle(e.x, e.y, e.size, e.size));
        }

        void draw(Graphics2D g) {
            g.setColor(Color.CYAN);
            g.fillOval(x, y, size, size);
        }
    }

    class Enemy {
        int x, y;
        int size = 40;
        int speed = rand.nextInt(4) + 3;

        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void draw(Graphics2D g) {
            g.setColor(Color.RED);
            g.fillRect(x, y, size, size);
        }
    }

    class Particle {
        double x, y;
        double dx, dy;
        int life = 40;
        boolean alive = true;

        Particle(int x, int y, Random r) {
            this.x = x;
            this.y = y;
            dx = r.nextDouble() * 6 - 3;
            dy = r.nextDouble() * 6 - 3;
        }

        void update() {
            x += dx;
            y += dy;
            life--;
            if (life <= 0) alive = false;
        }

        void draw(Graphics2D g) {
            if (!alive) return;
            g.setColor(Color.ORANGE);
            g.fillOval((int) x, (int) y, 8, 8);
        }
    }

    // ---------------------------
    // Main entry
    // ---------------------------
    public static void main(String[] args) {
        JFrame frame = new JFrame("Arcade Dodger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        Main obj = new Main();

        frame.add(obj);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}