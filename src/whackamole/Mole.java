package whackamole;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.swing.*;

/**
 * Whack-a-Mole (Java Swing)
 *
 * Event classes used:
 *   MouseEvent      - MouseListener + MouseMotionListener (hammer, whack, hover)
 *   KeyEvent        - KeyListener (SPACE, P, R, ESC, keys 1-9)
 *   ActionEvent     - menu items AND the three javax.swing.Timer objects
 *   ItemEvent       - difficulty radio items and option check boxes
 *   WindowEvent     - confirm on close, auto-pause when the window loses focus
 *   FocusEvent      - keyboard-active indicator on the game board
 *   ComponentEvent  - window resize
 *
 */
public class Mole extends JFrame {

    
    // CONSTANTS
  
    private static final int ROWS = 3, COLS = 3, PAD = 14;
    private static final int GAME_SECONDS = 30;
    private static final int ANIM_MS = 30;                       // animation frame
    private static final int RISE_MS = 160, FALL_MS = 180, HIT_MS = 220;
    private static final int NORMAL = 0, GOLDEN = 1, BOMB = 2;   // mole types
    private static final int IDLE = 0, RUNNING = 1, PAUSED = 2, OVER = 3;   // game states

    private static final String[] LEVEL_NAMES = {"Easy", "Medium", "Hard"};
    private static final int[] SPAWN_MS = {1100, 850, 620};      // delay between moles
    private static final int[] LIFE_MS = {1400, 1050, 800};      // how long a mole stays up

    private static final String[] EVENT_TYPES = {
        "MouseEvent", "KeyEvent", "ActionEvent", "Timer tick",
        "ItemEvent", "WindowEvent", "FocusEvent", "ComponentEvent"
    };

    private static final Color BG = new Color(43, 32, 24);
    private static final Color PANEL = new Color(70, 52, 40);
    private static final Color CREAM = new Color(255, 236, 200);
    private static final Color GOLD = new Color(255, 200, 40);

  
    // GAME STATE
  
    private int state = IDLE;
    private int score = 0, best = 0, combo = 0, timeLeft = GAME_SECONDS;
    private int difficulty = 1;                  // 0 easy, 1 medium, 2 hard
    private long gameClock = 0;                  // only advances while the game runs
    private boolean newBest = false;
    private int hoverIdx = -1;

    private final Slot[] slots = new Slot[ROWS * COLS];
    private final List<Pop> pops = new ArrayList<Pop>();
    private final Random rnd = new Random();

   
    // UI COMPONENTS
    
    private JLabel lblScore, lblTime, lblBest, lblCombo, lblStatus, lblLevel;
    private GameBoard board;
    private JMenuItem menuStart, menuPause, menuRestart;
    private JCheckBoxMenuItem chkSound, chkHighlight;

    private final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
    private final Map<String, JLabel> eventLabels = new LinkedHashMap<String, JLabel>();

    
    // TIMERS (Person 3)
  
    private javax.swing.Timer countdownTimer;   // 1 second: game clock
    private javax.swing.Timer spawnTimer;       // moles pop up
    private javax.swing.Timer animTimer;        // animation frames

    
    // SMALL DATA CLASSES
    
    private static class Slot {                 
        int type;
        long spawnAt;
        int life;
        boolean hit;
        long hitAt;
        double hitProgress;
    }

    private static class Pop {                 
        String text;
        int x, y, age;
        Color color;
    }

    
    // CONSTRUCTOR

    public Mole() {
        setTitle("Whack-a-Mole");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);   // WindowEvent asks first

        loadBest();
        buildUI();
        buildMenus();
        setupTimers();
        setupMouseEvents();
        setupKeyEvents();
        setupFocusEvents();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);

        setupWindowEvents();
        resetGame();
    }

   
    // UI
   
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // ----- header: title + score / time / best / combo -----
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));

        JLabel title = new JLabel("WHACK-A-MOLE", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(CREAM);
        header.add(title, BorderLayout.NORTH);

        JPanel hud = new JPanel(new GridLayout(1, 4, 8, 0));
        hud.setOpaque(false);
        lblScore = hudLabel();
        lblTime = hudLabel();
        lblBest = hudLabel();
        lblCombo = hudLabel();
        hud.add(lblScore);
        hud.add(lblTime);
        hud.add(lblBest);
        hud.add(lblCombo);
        header.add(hud, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // ----- game board -----
        board = new GameBoard();
        JPanel boardWrap = new JPanel(new BorderLayout());
        boardWrap.setBackground(BG);
        boardWrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
        boardWrap.add(board, BorderLayout.CENTER);
        root.add(boardWrap, BorderLayout.CENTER);

        // ----- event monitor (shows every event class being used) -----
        root.add(buildMonitor(), BorderLayout.EAST);

        // ----- status bar -----
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(new Color(30, 22, 16));
        status.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        lblStatus = new JLabel("Last event: -");
        lblStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(190, 225, 170));
        lblLevel = new JLabel();
        lblLevel.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblLevel.setForeground(CREAM);
        status.add(lblStatus, BorderLayout.CENTER);
        status.add(lblLevel, BorderLayout.EAST);
        root.add(status, BorderLayout.SOUTH);
    }

    private JLabel hudLabel() {
        JLabel l = new JLabel("", JLabel.CENTER);
        l.setOpaque(true);
        l.setBackground(PANEL);
        l.setForeground(CREAM);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        return l;
    }

    private JPanel buildMonitor() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel t = new JLabel("EVENT MONITOR");
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        t.setForeground(GOLD);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(10));

        for (String key : EVENT_TYPES) {
            counts.put(key, 0);
            JLabel l = new JLabel();
            l.setFont(new Font("Monospaced", Font.BOLD, 12));
            l.setForeground(CREAM);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            eventLabels.put(key, l);
            refreshMonitorLabel(key);
            p.add(l);
            p.add(Box.createVerticalStrut(6));
        }

        p.add(Box.createVerticalStrut(14));
        JLabel tips = new JLabel("<html><font color='#D9C7A5'>"
                + "<b>Controls</b><br>"
                + "SPACE &nbsp;start / pause<br>"
                + "P &nbsp;pause &nbsp; R &nbsp;restart<br>"
                + "1-9 &nbsp;whack a hole<br>"
                + "ESC &nbsp;exit</font></html>");
        tips.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tips.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(tips);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private void refreshMonitorLabel(String key) {
        eventLabels.get(key).setText(String.format("%-14s %4d", key, counts.get(key)));
    }

    /** Counts an event for the monitor and (optionally) shows it in the status bar. */
    private void logEvent(String type, String detail) {
        Integer n = counts.get(type);
        counts.put(type, n == null ? 1 : n + 1);
        refreshMonitorLabel(type);
        if (detail != null) {
            lblStatus.setText("Last event: " + type + " - " + detail);
        }
    }

 
    // MENUS + ITEM EVENTS (Person 3)
    

    // MENU EVENT: every menu item sends its ActionEvent here
    private final ActionListener menuListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            logEvent("ActionEvent", "menu -> " + cmd);

            if ("start".equals(cmd)) {
                startGame();
            } else if ("pause".equals(cmd)) {
                pauseGame();
            } else if ("restart".equals(cmd)) {
                restartGame();
            } else if ("exit".equals(cmd)) {
                confirmExit();
            } else if ("instructions".equals(cmd)) {
                showInstructions();
            } else if ("about".equals(cmd)) {
                showAbout();
            }
        }
    };

    private void buildMenus() {
        JMenuBar bar = new JMenuBar();

        // ----- Game -----
        JMenu game = new JMenu("Game");
        game.setMnemonic(KeyEvent.VK_G);
        menuStart = menuItem("Start", "start");
        menuPause = menuItem("Pause", "pause");
        menuRestart = menuItem("Restart", "restart");
        game.add(menuStart);
        game.add(menuPause);
        game.add(menuRestart);
        game.addSeparator();
        game.add(menuItem("Exit", "exit"));

        // ----- Difficulty (ItemEvent) -----
        JMenu level = new JMenu("Difficulty");
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < LEVEL_NAMES.length; i++) {
            level.add(levelItem(i, group));
        }

        // ----- Options (ItemEvent) -----
        JMenu options = new JMenu("Options");
        chkSound = new JCheckBoxMenuItem("Sound SFX (Beep)", true);
        chkHighlight = new JCheckBoxMenuItem("Highlight hole under cursor", true);
        ItemListener optionListener = new ItemListener() {
            // ITEM EVENT: a check box was ticked or unticked
            @Override
            public void itemStateChanged(ItemEvent e) {
                JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
                logEvent("ItemEvent", src.getText() + " = "
                        + (e.getStateChange() == ItemEvent.SELECTED ? "ON" : "OFF"));
                board.repaint();
            }
        };
        chkSound.addItemListener(optionListener);
        chkHighlight.addItemListener(optionListener);
        options.add(chkSound);
        options.add(chkHighlight);

        // ----- Help -----
        JMenu help = new JMenu("Help");
        help.add(menuItem("Instructions", "instructions"));
        help.add(menuItem("About", "about"));

        bar.add(game);
        bar.add(level);
        bar.add(options);
        bar.add(help);
        setJMenuBar(bar);
    }

    private JMenuItem menuItem(String text, String command) {
        JMenuItem item = new JMenuItem(text);
        item.setActionCommand(command);
        item.addActionListener(menuListener);
        return item;
    }

    private JRadioButtonMenuItem levelItem(final int lvl, ButtonGroup group) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(LEVEL_NAMES[lvl], lvl == difficulty);
        group.add(item);
        item.addItemListener(new ItemListener() {
            // ITEM EVENT: a difficulty was selected
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    logEvent("ItemEvent", "difficulty -> " + LEVEL_NAMES[lvl]);
                    setDifficulty(lvl);
                }
            }
        });
        return item;
    }

    private void setDifficulty(int lvl) {
        difficulty = lvl;
        spawnTimer.setDelay(SPAWN_MS[lvl]);
        updateLevelLabel();
    }

    private void updateLevelLabel() {
        lblLevel.setText("Difficulty: " + LEVEL_NAMES[difficulty]);
    }

    private void updateMenus() {
        menuStart.setText(state == PAUSED ? "Resume" : "Start");
        menuStart.setEnabled(state != RUNNING);
        menuPause.setEnabled(state == RUNNING);
        menuRestart.setEnabled(state != IDLE);
    }

    private void confirmExit() {
        boolean wasRunning = (state == RUNNING);
        pauseGame();
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
        if (wasRunning) {
            startGame();
        }
    }

    private void showInstructions() {
        boolean wasRunning = (state == RUNNING);
        pauseGame();
        JOptionPane.showMessageDialog(this,
                "How to Play Whack-a-Mole\n\n"
              + "1. Choose Game > Start (or press SPACE / click the board).\n"
              + "2. Moles pop up from the holes. Click them with the hammer!\n"
              + "3. Brown mole = +1.  Golden mole = +5 and +2 seconds.\n"
              + "4. Bombs cost you 3 points. Don't hit them!\n"
              + "5. Hit moles in a row to build a COMBO multiplier (up to x4).\n"
              + "6. Missing a mole or clicking empty ground costs 1 point.\n"
              + "7. Keys 1-9 whack the holes in numpad layout.\n"
              + "8. The game lasts 30 seconds and speeds up as time runs out.\n\n"
              + "SPACE start/pause   P pause   R restart   ESC exit",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);
        if (wasRunning) {
            startGame();
        }
    }

    private void showAbout() {
        boolean wasRunning = (state == RUNNING);
        pauseGame();
        JOptionPane.showMessageDialog(this,
                "Whack-a-Mole - a Java Swing event-handling project\n\n"
              + "Event classes used:\n"
              + "MouseEvent, KeyEvent, ActionEvent (menus + timers),\n"
              + "ItemEvent, WindowEvent, FocusEvent, ComponentEvent.\n\n"
              + "Watch the Event Monitor on the right count them live.",
                "About", JOptionPane.INFORMATION_MESSAGE);
        if (wasRunning) {
            startGame();
        }
    }

    
    // TIMER EVENTS (Person 3)
    
    private void setupTimers() {

        // TIMER EVENT: game countdown, fires every 1 second
        countdownTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logEvent("Timer tick", null);
                timeLeft--;
                // the game speeds up as the clock runs down
                int elapsed = Math.max(0, GAME_SECONDS - timeLeft);
                spawnTimer.setDelay(Math.max(350, SPAWN_MS[difficulty] - elapsed * 10));
                updateHud();
                if (timeLeft <= 0) {
                    endGame();
                }
            }
        });

        // TIMER EVENT: a new mole pops up
        spawnTimer = new javax.swing.Timer(SPAWN_MS[difficulty], new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logEvent("Timer tick", null);
                spawnMoles();
            }
        });
        spawnTimer.setInitialDelay(350);

        // TIMER EVENT: animation frame (moles rising / sinking, floating text)
        animTimer = new javax.swing.Timer(ANIM_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tickAnimation();
            }
        });
    }

    private void stopTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (spawnTimer != null) spawnTimer.stop();
        if (animTimer != null) animTimer.stop();
    }

  
    // GAME FLOW: start / pause / restart / end 
   
    private void startGame() {
        if (state == RUNNING) return;
        if (state == OVER) resetGame();
        state = RUNNING;
        countdownTimer.start();
        spawnTimer.start();
        animTimer.start();
        board.requestFocusInWindow();
        refreshState();
    }

    private void pauseGame() {
        if (state != RUNNING) return;
        state = PAUSED;
        stopTimers();
        refreshState();
    }

    private void togglePause() {
        if (state == RUNNING) {
            pauseGame();
        } else {
            startGame();         // IDLE, PAUSED or OVER
        }
    }

    private void restartGame() {
        resetGame();
        startGame();
    }

    private void resetGame() {
        stopTimers();
        state = IDLE;
        score = 0;
        combo = 0;
        timeLeft = GAME_SECONDS;
        gameClock = 0;
        newBest = false;
        Arrays.fill(slots, null);
        pops.clear();
        spawnTimer.setDelay(SPAWN_MS[difficulty]);
        updateLevelLabel();
        refreshState();
    }

    private void endGame() {
        stopTimers();
        state = OVER;
        timeLeft = 0;
        Arrays.fill(slots, null);
        pops.clear();
        if (score > best) {
            best = score;
            newBest = true;
            saveBest();
        }
        refreshState();
    }

    private void refreshState() {
        updateMenus();
        updateHud();
        board.repaint();
    }

    
    // MOLE MOVEMENT (Person 3)
  
    private void spawnMoles() {
        spawnOne();
        if (difficulty == 2 && rnd.nextDouble() < 0.35) {
            spawnOne();          // hard mode: sometimes two moles at once
        }
    }

    private void spawnOne() {
        List<Integer> free = new ArrayList<Integer>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) free.add(i);
        }
        if (free.isEmpty()) return;

        int idx = free.get(rnd.nextInt(free.size()));    // the mole moves to a random hole
        double r = rnd.nextDouble();
        Slot s = new Slot();
        s.type = r < 0.12 ? BOMB : (r < 0.24 ? GOLDEN : NORMAL);
        s.life = LIFE_MS[difficulty] + rnd.nextInt(300) - 150;
        if (s.type == GOLDEN) s.life = (int) (s.life * 0.7);   // golden moles are quick
        s.spawnAt = gameClock;
        slots[idx] = s;
    }

    private void tickAnimation() {
        gameClock += ANIM_MS;

        for (int i = 0; i < slots.length; i++) {
            Slot s = slots[i];
            if (s == null) continue;
            if (s.hit) {
                if (gameClock - s.hitAt >= HIT_MS) slots[i] = null;
            } else if (gameClock - s.spawnAt >= s.life) {
                if (s.type != BOMB) {        // a real mole got away
                    combo = 0;
                    updateHud();
                }
                slots[i] = null;
            }
        }

        for (Iterator<Pop> it = pops.iterator(); it.hasNext();) {
            Pop p = it.next();
            p.age += ANIM_MS;
            if (p.age >= 800) it.remove();
        }
        board.repaint();
    }

    /** 0 = hidden in the hole, 1 = fully up. */
    private double progress(Slot s) {
        if (s.hit) {
            long ht = gameClock - s.hitAt;
            return Math.max(0, s.hitProgress - ht / (double) HIT_MS);
        }
        long t = gameClock - s.spawnAt;
        if (t < RISE_MS) return t / (double) RISE_MS;
        if (t > s.life - FALL_MS) return Math.max(0, (s.life - t) / (double) FALL_MS);
        return 1.0;
    }

   
    // SCORING (Person 2)
    
    private int multiplier() {
        return Math.min(4, 1 + combo / 5);
    }

    private void whack(int idx, Point at) {
        if (state != RUNNING) return;
        Slot s = (idx >= 0) ? slots[idx] : null;

        if (s != null && !s.hit && progress(s) >= 0.35) {
            s.hitProgress = progress(s);
            s.hit = true;
            s.hitAt = gameClock;

            if (s.type == BOMB) {
                score = Math.max(0, score - 3);
                combo = 0;
                addPop("-3 BOOM!", at, new Color(255, 90, 80));
                beep();
            } else {
                combo++;
                int pts = (s.type == GOLDEN ? 5 : 1) * multiplier();
                score += pts;
                addPop("+" + pts, at, s.type == GOLDEN ? GOLD : Color.WHITE);
                if (s.type == GOLDEN) {
                    timeLeft = Math.min(60, timeLeft + 2);
                    addPop("+2s", new Point(at.x, at.y - 24), new Color(140, 220, 255));
                    beep();
                }
            }
        } else {                               // empty ground = miss
            combo = 0;
            if (score > 0) score--;
            addPop("-1", at, new Color(255, 150, 120));
        }
        updateHud();
        board.repaint();
    }

    private void addPop(String text, Point at, Color color) {
        Pop p = new Pop();
        p.text = text;
        p.x = at.x;
        p.y = at.y;
        p.color = color;
        pops.add(p);
    }

    private void beep() {
        if (chkSound.isSelected()) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void updateHud() {
        lblScore.setText("SCORE  " + score);
        lblTime.setText("TIME  " + timeLeft);
        lblTime.setForeground(timeLeft <= 5 && state == RUNNING ? new Color(255, 110, 100) : CREAM);
        lblBest.setText("BEST  " + best);
        lblCombo.setText("COMBO  " + combo + "  (x" + multiplier() + ")");
    }

    private void loadBest() {
        try {
            best = Preferences.userNodeForPackage(Mole.class).getInt("best", 0);
        } catch (Exception ex) {
            best = 0;
        }
    }

    private void saveBest() {
        try {
            Preferences.userNodeForPackage(Mole.class).putInt("best", best);
        } catch (Exception ex) {
            // best score just won't be remembered
        }
    }

   
    // MOUSE EVENTS (Person 2)
    
    private void setupMouseEvents() {
        final Cursor hammerUp = makeHammerCursor(false);
        final Cursor hammerDown = makeHammerCursor(true);
        board.setCursor(hammerUp);

        // MouseListener: press, release, click, enter, exit
        board.addMouseListener(new MouseListener() {

            // MOUSE EVENT: player swings the hammer
            @Override
            public void mousePressed(MouseEvent e) {
                board.setCursor(hammerDown);
                board.requestFocusInWindow();
                logEvent("MouseEvent", "mousePressed at (" + e.getX() + ", " + e.getY() + ")");
                if (state != RUNNING) {          // click on the overlay starts / resumes
                    startGame();
                    return;
                }
                whack(board.holeAt(e.getX(), e.getY()), e.getPoint());
            }

            // MOUSE EVENT: hammer goes back up
            @Override
            public void mouseReleased(MouseEvent e) {
                board.setCursor(hammerUp);
                logEvent("MouseEvent", "mouseReleased");
            }

            // MOUSE EVENT: cursor enters the board
            @Override
            public void mouseEntered(MouseEvent e) {
                logEvent("MouseEvent", "mouseEntered board");
            }

            // MOUSE EVENT: cursor leaves the board
            @Override
            public void mouseExited(MouseEvent e) {
                hoverIdx = -1;
                board.repaint();
                logEvent("MouseEvent", "mouseExited board");
            }

            @Override
            public void mouseClicked(MouseEvent e) { }
        });

        // MouseMotionListener: move and drag (highlights the hole under the cursor)
        board.addMouseMotionListener(new MouseMotionListener() {

            // MOUSE EVENT: cursor moves over the board
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e, "mouseMoved");
            }

            // MOUSE EVENT: cursor moves with a button held down
            @Override
            public void mouseDragged(MouseEvent e) {
                updateHover(e, "mouseDragged");
            }
        });
    }

    private void updateHover(MouseEvent e, String what) {
        int idx = board.holeAt(e.getX(), e.getY());
        boolean changed = (idx != hoverIdx);
        hoverIdx = idx;
        if (changed) board.repaint();
        logEvent("MouseEvent", changed
                ? what + " -> hole " + (idx < 0 ? "none" : String.valueOf(holeDigit(idx)))
                : null);
    }

    /** The keypad number (1-9) of a hole, numpad layout: 7 8 9 / 4 5 6 / 1 2 3. */
    private int holeDigit(int idx) {
        return (2 - idx / COLS) * 3 + idx % COLS + 1;
    }

    private Cursor makeHammerCursor(boolean down) {
        try {
            BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (down) g.rotate(Math.toRadians(-35), 16, 28);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(120, 75, 35));
            g.drawLine(16, 28, 16, 10);
            g.setColor(new Color(110, 110, 125));
            g.fillRoundRect(5, 3, 22, 11, 5, 5);
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(new Color(50, 50, 60));
            g.drawRoundRect(5, 3, 22, 11, 5, 5);
            g.dispose();
            return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(16, 8),
                    down ? "hammer-down" : "hammer");
        } catch (Exception ex) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        }
    }

   
    // KEY EVENTS
   
    private void setupKeyEvents() {
        board.setFocusable(true);
        board.addKeyListener(new KeyListener() {

            // KEY EVENT: a key goes down
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                logEvent("KeyEvent", "keyPressed " + KeyEvent.getKeyText(code));

                if (code == KeyEvent.VK_SPACE) {
                    togglePause();
                } else if (code == KeyEvent.VK_P) {
                    if (state == RUNNING) pauseGame();
                    else if (state == PAUSED) startGame();
                } else if (code == KeyEvent.VK_R) {
                    restartGame();
                } else if (code == KeyEvent.VK_ESCAPE) {
                    confirmExit();
                } else if (state == RUNNING) {
                    char c = e.getKeyChar();
                    if (c >= '1' && c <= '9') {            // keys 1-9 whack a hole
                        int d = c - '1';
                        int idx = (2 - d / 3) * COLS + d % 3;
                        whack(idx, board.holeCenter(idx));
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) { }

            @Override
            public void keyTyped(KeyEvent e) { }
        });
    }

    
    // FOCUS EVENTS
    
    private void setupFocusEvents() {
        board.addFocusListener(new FocusListener() {

            // FOCUS EVENT: the board can receive key presses
            @Override
            public void focusGained(FocusEvent e) {
                board.focused = true;
                board.repaint();
                logEvent("FocusEvent", "focusGained - keyboard active");
            }

            // FOCUS EVENT: keys no longer reach the board
            @Override
            public void focusLost(FocusEvent e) {
                board.focused = false;
                board.repaint();
                logEvent("FocusEvent", "focusLost - click the board to use keys");
            }
        });
    }

 
    // WINDOW + COMPONENT EVENTS
    
    private void setupWindowEvents() {
        addWindowListener(new WindowAdapter() {

            // WINDOW EVENT: window shown for the first time
            @Override
            public void windowOpened(WindowEvent e) {
                logEvent("WindowEvent", "windowOpened");
                board.requestFocusInWindow();
            }

            // WINDOW EVENT: the X button was pressed
            @Override
            public void windowClosing(WindowEvent e) {
                logEvent("WindowEvent", "windowClosing");
                confirmExit();
            }

            // WINDOW EVENT: another window took over, so auto-pause
            @Override
            public void windowDeactivated(WindowEvent e) {
                logEvent("WindowEvent", "windowDeactivated");
                if (state == RUNNING) {
                    pauseGame();
                    lblStatus.setText("Last event: WindowEvent - auto-paused (window lost focus)");
                }
            }

            @Override
            public void windowActivated(WindowEvent e) {
                logEvent("WindowEvent", "windowActivated");
            }

            // WINDOW EVENT: window minimized
            @Override
            public void windowIconified(WindowEvent e) {
                logEvent("WindowEvent", "windowIconified");
                pauseGame();
            }
        });

        // COMPONENT EVENT: the window was resized
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                logEvent("ComponentEvent", "componentResized " + getWidth() + " x " + getHeight());
                board.repaint();
            }
        });
    }

    
    // GAME BOARD (custom painted, all the visuals)
    
    private class GameBoard extends JPanel {

        boolean focused = false;

        GameBoard() {
            setPreferredSize(new Dimension(540, 400));
            setOpaque(true);
        }

        int cellW() {
            return Math.max(1, (getWidth() - 2 * PAD) / COLS);
        }

        int cellH() {
            return Math.max(1, (getHeight() - 2 * PAD) / ROWS);
        }

        /** Which hole was the point in? -1 if outside the grid. */
        int holeAt(int x, int y) {
            int cx = x - PAD, cy = y - PAD;
            if (cx < 0 || cy < 0) return -1;
            int c = cx / cellW(), r = cy / cellH();
            if (c >= COLS || r >= ROWS) return -1;
            return r * COLS + c;
        }

        Point holeCenter(int idx) {
            int r = idx / COLS, c = idx % COLS;
            return new Point(PAD + c * cellW() + cellW() / 2, PAD + r * cellH() + (int) (cellH() * 0.4));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // grass
            g.setPaint(new GradientPaint(0, 0, new Color(126, 201, 111), 0, h, new Color(74, 148, 62)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 18));
            for (int y = 0; y < h; y += 40) {
                g.fillRect(0, y, w, 20);
            }

            // holes and moles
            int cw = cellW(), ch = cellH();
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    drawCell(g, r * COLS + c, PAD + c * cw, PAD + r * ch, cw, ch);
                }
            }

            // floating score text
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            for (Pop p : pops) {
                float a = Math.max(0f, 1f - p.age / 800f);
                int y = p.y - (int) (p.age * 0.06);
                int x = p.x - fm.stringWidth(p.text) / 2;
                g.setColor(new Color(0, 0, 0, (int) (120 * a)));
                g.drawString(p.text, x + 1, y + 2);
                g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int) (255 * a)));
                g.drawString(p.text, x, y);
            }

            if (state != RUNNING) {
                drawOverlay(g, w, h);
            }

            // border shows whether the keyboard is active
            g.setStroke(new BasicStroke(4f));
            g.setColor(focused ? GOLD : new Color(30, 22, 16));
            g.drawRect(2, 2, w - 5, h - 5);
            g.dispose();
        }

        private void drawCell(Graphics2D g, int idx, int x, int y, int cw, int ch) {
            int holeW = (int) Math.min(cw * 0.62, ch * 0.70);
            int holeH = (int) (holeW * 0.34);
            int hx = x + (cw - holeW) / 2;
            int hy = y + (int) (ch * 0.64);
            int lineY = hy + holeH / 2;

            // shadow + back of the hole
            g.setColor(new Color(0, 0, 0, 40));
            g.fillOval(hx - 6, hy + 4, holeW + 12, holeH + 4);
            g.setPaint(new GradientPaint(0, hy, new Color(60, 38, 22), 0, hy + holeH, new Color(25, 15, 8)));
            g.fillOval(hx, hy, holeW, holeH);

            // the mole, clipped at the middle of the hole so it looks like it rises out of it
            Slot s = slots[idx];
            if (s != null) {
                double p = progress(s);
                if (p > 0) {
                    Shape old = g.getClip();
// Clip to both the cell area AND the board's drawable area
                   g.clipRect(x, 0, cw, lineY);   // everything above the middle of the hole
                   drawMole(g, s, hx, holeW, lineY, p);
                   g.setClip(old);
                }
            }

            // front rim of the hole
            g.setStroke(new BasicStroke(3f));
            g.setColor(new Color(92, 60, 34));
            g.drawArc(hx, hy, holeW, holeH, 180, 180);

            // hover highlight
            if (chkHighlight.isSelected() && hoverIdx == idx && state == RUNNING) {
                g.setStroke(new BasicStroke(3f));
                g.setColor(new Color(255, 230, 90, 190));
                g.drawOval(hx - 5, hy - 5, holeW + 10, holeH + 10);
            }

            // keypad number hint
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(new Color(255, 255, 255, 110));
            String d = String.valueOf(holeDigit(idx));
            g.drawString(d, x + cw / 2 - g.getFontMetrics().stringWidth(d) / 2, hy + holeH + 18);
        }

        private void drawMole(Graphics2D g, Slot s, int hx, int holeW, int lineY, double p) {
            int mw = (int) (holeW * 0.72);
            int mx = hx + (holeW - mw) / 2;
            int top = lineY - (int) (p * mw * 1.05);

            if (s.type == BOMB) {
                // bomb body
                g.setColor(new Color(40, 40, 50));
                g.fillOval(mx, top, mw, (int) (mw * 1.2));
                g.setColor(new Color(255, 255, 255, 70));
                g.fillOval(mx + mw / 6, top + mw / 8, mw / 4, mw / 5);
                // fuse and spark
                g.setStroke(new BasicStroke(3f));
                g.setColor(new Color(150, 120, 70));
                g.drawLine(mx + mw / 2, top + 2, mx + mw / 2 + mw / 6, top - mw / 6);
                g.setColor(new Color(255, 150, 30));
                g.fillOval(mx + mw / 2 + mw / 6 - mw / 12, top - mw / 6 - mw / 12, mw / 6, mw / 6);
                return;
            }

            Color body = s.type == GOLDEN ? new Color(245, 190, 40) : new Color(139, 94, 52);
            Color belly = s.type == GOLDEN ? new Color(255, 232, 140) : new Color(196, 150, 100);
            Color dark = body.darker();
            int availableHeight = lineY - top;
            int bodyH = Math.min((int) (mw * 1.4), availableHeight);

            // ears
            int er = mw / 5;
            g.setColor(dark);
            g.fillOval(mx + mw / 12, top - er / 3, er * 3 / 2, er * 3 / 2);
            g.fillOval(mx + mw - mw / 12 - er * 3 / 2, top - er / 3, er * 3 / 2, er * 3 / 2);

            // body + belly
            g.setColor(body);
            g.fillOval(mx, top, mw, bodyH);
            g.setColor(belly);
            g.fillOval(mx + mw / 5, top + mw * 45 / 100, mw * 3 / 5, mw * 55 / 100);

            // eyes (X eyes when hit)
            int ew = mw / 6;
            int ey = top + mw * 28 / 100;
            int[] exs = {mx + mw * 26 / 100, mx + mw * 58 / 100};
            for (int ex : exs) {
                if (s.hit) {
                    g.setStroke(new BasicStroke(2f));
                    g.setColor(Color.BLACK);
                    g.drawLine(ex, ey, ex + ew, ey + ew);
                    g.drawLine(ex + ew, ey, ex, ey + ew);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillOval(ex, ey, ew, ew);
                    g.setColor(Color.BLACK);
                    g.fillOval(ex + ew / 3, ey + ew / 3, ew / 2, ew / 2);
                }
            }

            // nose + teeth
            g.setColor(new Color(235, 130, 150));
            g.fillOval(mx + mw / 2 - mw / 10, top + mw * 46 / 100, mw / 5, mw / 7);
            g.setColor(Color.WHITE);
            g.fillRect(mx + mw / 2 - mw / 12, top + mw * 62 / 100, mw / 12, mw / 9);
            g.fillRect(mx + mw / 2, top + mw * 62 / 100, mw / 12, mw / 9);

            // outline
            g.setStroke(new BasicStroke(2f));
            g.setColor(dark);
            g.drawOval(mx, top, mw, bodyH);

            // sparkle on golden moles
            if (s.type == GOLDEN) {
                int sx = mx + mw - mw / 8, sy = top + mw / 8, l = mw / 9;
                g.setColor(new Color(255, 255, 255, 220));
                g.drawLine(sx - l, sy, sx + l, sy);
                g.drawLine(sx, sy - l, sx, sy + l);
            }
        }

        private void drawOverlay(Graphics2D g, int w, int h) {
            g.setColor(new Color(20, 12, 6, 170));
            g.fillRect(0, 0, w, h);

            String l1, l2, l3 = null;
            if (state == IDLE) {
                l1 = "WHACK-A-MOLE";
                l2 = "Click anywhere or press SPACE to start";
                l3 = "Mole +1     Golden +5 and +2s     Bomb -3";
            } else if (state == PAUSED) {
                l1 = "PAUSED";
                l2 = "Press SPACE or choose Game > Resume";
            } else {
                l1 = "TIME'S UP!";
                l2 = "Final score: " + score + (newBest ? "     NEW BEST!" : "     Best: " + best);
                l3 = "Click or press SPACE to play again";
            }

            g.setColor(GOLD);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            drawCentered(g, l1, w, h / 2 - 20);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            drawCentered(g, l2, w, h / 2 + 16);
            if (l3 != null) {
                g.setColor(CREAM);
                g.setFont(new Font("SansSerif", Font.PLAIN, 15));
                drawCentered(g, l3, w, h / 2 + 44);
            }
        }

        private void drawCentered(Graphics2D g, String text, int w, int y) {
            g.drawString(text, (w - g.getFontMetrics().stringWidth(text)) / 2, y);
        }
    }

    
    // MAIN
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // keep the default look and feel
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Mole().setVisible(true);
            }
        });
    }
}