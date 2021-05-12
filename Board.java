

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.Timer;

class MyPoint extends Point {

    public MyPoint(int x, int y) {
        super(x, y);
    }

    public int deltaX = 0;
    public int deltaY = 0;
}

public class Board extends JPanel {

    private final int BOARD_WIDTH = 300;
    private final int BOARD_HEIGHT = 300;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;

    private final int FPS = 75;
    private final int PERIOD = 1000 / FPS;

    private final MyPoint p[] = new MyPoint[ALL_DOTS];

    private boolean inGame = true;

    private Timer timer;

    private Snake snake;
    private Apple apple;

    private Animation anim;

    public Board() {

        initBoard();
    }

    private void initBoard() {

        addKeyListener(new TAdapter());
        setBackground(Color.black);
        setFocusable(true);

        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        initGame();
    }

    private void initGame() {

        snake = new Snake();
        apple = new Apple();
        anim = new Animation();

        for (int y = 0; y < ALL_DOTS; y++) {

            p[y] = new MyPoint(0, 0);
        }

        for (int z = 0; z < snake.size(); z++) {

            p[z] = new MyPoint(50 - z * DOT_SIZE, 50);
        }

        locateApple();

        timer = new Timer(PERIOD, new GameCycle());
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        if (inGame) {

            drawApple(g);
            drawSnake(g);

            Toolkit.getDefaultToolkit().sync();

        } else {

            gameOver(g);
        }
    }

    private void drawSnake(Graphics g) {

        for (int z = 0; z < snake.size(); z++) {

            if (z == 0) {

                g.drawImage(snake.getHead(), p[z].x, p[z].y, this);
            } else {

                g.drawImage(snake.getBody(), p[z].x, p[z].y, this);
            }
        }
    }

    private void drawApple(Graphics g) {

        g.drawImage(apple.getApple(), apple.getX(), apple.getY(), this);
    }

    private void gameOver(Graphics g) {

        var g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        var msg = "Game Over";
        var smallFont = new Font("Helvetica", Font.BOLD, 14);
        var fontMetrics = getFontMetrics(smallFont);

        g2d.setColor(Color.white);
        g2d.setFont(smallFont);
        g2d.drawString(msg, (BOARD_WIDTH -
                fontMetrics.stringWidth(msg)) / 2, BOARD_HEIGHT / 2);
    }

    private void checkApple() {

        if (p[0].x == apple.getX() && p[0].y == apple.getY()) {

            Point last = p[snake.size() - 1];

            if (snake.getDirection() == Direction.RIGHT
                    || snake.getDirection() == Direction.LEFT) {

                p[snake.size()].x = last.x;
                p[snake.size()].y = last.y - DOT_SIZE;

            } else {

                p[snake.size()].x = last.x - DOT_SIZE;;
                p[snake.size()].y = last.y;
            }

            snake.grow();
            locateApple();
        }
    }

    private void move() {

        if (anim.finished()) {

            anim.reset();

            if (snake.getDirection() == Direction.RIGHT) {

                p[0].deltaX = 1;
                p[0].deltaY = 0;
            }

            if (snake.getDirection() == Direction.LEFT) {

                p[0].deltaX = -1;
                p[0].deltaY = 0;
            }

            if (snake.getDirection() == Direction.UP) {

                p[0].deltaX = 0;
                p[0].deltaY = -1;
            }

            if (snake.getDirection() == Direction.DOWN) {

                p[0].deltaX = 0;
                p[0].deltaY = 1;
            }

            for (int z = 0; z < snake.size(); z++) {

                if (p[z + 1].x == p[z].x) {

                    p[z + 1].deltaX = 0;
                } else if (p[z + 1].x > p[z].x) {

                    p[z + 1].deltaX = -1;
                } else {

                    p[z + 1].deltaX = 1;
                }

                if (p[z + 1].y == p[z].y) {

                    p[z + 1].deltaY = 0;
                } else if (p[z + 1].y > p[z].y) {

                    p[z + 1].deltaY = -1;
                } else {

                    p[z + 1].deltaY = 1;
                }
            }

        } else {

            anim.inc();

            for (int z = 0; z < snake.size(); z++) {

                p[z].translate(p[z].deltaX, p[z].deltaY);
            }
        }
    }

    private void checkCollision() {

        for (int z = snake.size(); z > 0; z--) {

            if (z > 4 && p[0].x == p[z].x && p[0].y == p[z].y) {

                inGame = false;
            }
        }

        if (p[0].y >= BOARD_HEIGHT) {

            inGame = false;
        }

        if (p[0].y < 0) {

            inGame = false;
        }

        if (p[0].x >= BOARD_WIDTH) {

            inGame = false;
        }

        if (p[0].x < 0) {

            inGame = false;
        }

        if (!inGame) {

            timer.stop();
        }
    }

    private void locateApple() {

        var random = new Random();

        int r = random.nextInt(RAND_POS);
        apple.setX(r * DOT_SIZE);

        r = random.nextInt(RAND_POS);
        apple.setY(r * DOT_SIZE);
    }

    private void doGameCycle() {

        if (inGame) {

            checkApple();
            checkCollision();
            move();
        }

        repaint();
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT &&

                    snake.getDirection() != Direction.RIGHT) {

                snake.setDirection(Direction.LEFT);
            }

            if (key == KeyEvent.VK_RIGHT &&
                    snake.getDirection() != Direction.LEFT) {

                snake.setDirection(Direction.RIGHT);
            }

            if (key == KeyEvent.VK_UP &&
                    snake.getDirection() != Direction.DOWN) {

                snake.setDirection(Direction.UP);
            }

            if (key == KeyEvent.VK_DOWN &&
                    snake.getDirection() != Direction.UP) {

                snake.setDirection(Direction.DOWN);
            }
        }
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            doGameCycle();
        }
    }
}
