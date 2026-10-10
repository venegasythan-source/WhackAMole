package whackamole;

public class Mole extends javax.swing.JFrame {

    private javax.swing.JButton mole;

  
    private int score = 0;
    private boolean gameRunning = false;   // Person 3 sets this from Start/Pause

   
    public Mole() {
        initComponents();

        setTitle("Whack-a-Mole");

        gamePanel.setLayout(null);   
        gamePanel.setPreferredSize(new java.awt.Dimension(500, 296));

        mole = new javax.swing.JButton("🐹");   
        mole.setBounds(220, 100, 60, 60);
        gamePanel.add(mole);

        setupMouseEvents();          
  
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        lblTitle = new javax.swing.JLabel();
        lblScore = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        gamePanel = new javax.swing.JPanel();
        jMenuBar2 = new javax.swing.JMenuBar();
        menuGame = new javax.swing.JMenu();
        menuStart = new javax.swing.JMenuItem();
        menuPause = new javax.swing.JMenuItem();
        menuRestart = new javax.swing.JMenuItem();
        menuExit = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        menuInstructions = new javax.swing.JMenuItem();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitle.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        lblTitle.setText("Whack-a-Mole");

        lblScore.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblScore.setText("Score: 0");

        lblTime.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblTime.setText("Time: 30");

        gamePanel.setBackground(new java.awt.Color(204, 255, 204));
        gamePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        javax.swing.GroupLayout gamePanelLayout = new javax.swing.GroupLayout(gamePanel);
        gamePanel.setLayout(gamePanelLayout);
        gamePanelLayout.setHorizontalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        gamePanelLayout.setVerticalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );

        menuGame.setText("Game");

        menuStart.setText("Start");
        menuStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuStartActionPerformed(evt);
            }
        });
        menuGame.add(menuStart);

        menuPause.setText("Pause");
        menuPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPauseActionPerformed(evt);
            }
        });
        menuGame.add(menuPause);

        menuRestart.setText("Restart");
        menuRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuRestartActionPerformed(evt);
            }
        });
        menuGame.add(menuRestart);

        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
                
            }
        });
        menuGame.add(menuExit);

        jMenuBar2.add(menuGame);

        menuHelp.setText("Help");

        menuInstructions.setText("Instructions");
        menuInstructions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuInstructionsActionPerformed(evt);
            }
        });
        menuHelp.add(menuInstructions);

        jMenuBar2.add(menuHelp);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(115, 115, 115)
                .addComponent(lblScore)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTime)
                .addGap(135, 135, 135))
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(gamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(206, 206, 206))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblScore)
                    .addComponent(lblTime))
                .addGap(18, 18, 18)
                .addComponent(gamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void menuStartActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         

    private void menuPauseActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         

    private void menuRestartActionPerformed(java.awt.event.ActionEvent evt) {                                            
        // TODO add your handling code here:
    }                                           

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {                                                                                                                                
    // MENU EVENT: Game -> Exit
    int choice = javax.swing.JOptionPane.showConfirmDialog(this,
        "Are you sure you want to exit?",
        "Exit",
        javax.swing.JOptionPane.YES_NO_OPTION);
    if (choice == javax.swing.JOptionPane.YES_OPTION) {
        System.exit(0);
     }                                        
   }
    private void menuInstructionsActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        javax.swing.JOptionPane.showMessageDialog(this,
            "How to Play Whack-a-Mole:\n\n" +
            "1. Click 'Game' > 'Start' to begin the game.\n" +
            "2. A mole will pop up somewhere in the green field.\n" +
            "3. Click the mole as fast as you can to earn points.\n" +
            "4. Each successful hit gives you +1 point.\n" +
            "5. Clicking empty grass will cost you -1 point.\n" +
            "6. The game lasts 30 seconds. Get the highest score you can!\n\n" +
            "Use 'Game' > 'Pause' to pause, or 'Game' > 'Restart' to start over.",
            "How to Play",
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }                                                

 
    private void addScore() {
        score++;
        lblScore.setText("Score: " + score);
    }

    public void resetScore() {              // Person 3 calls this in Restart
        score = 0;
        lblScore.setText("Score: " + score);
    }

    public void setGameRunning(boolean running) {   // Person 3 calls this in Start/Pause
        gameRunning = running;
    }

    
    private void setupMouseEvents() {

        java.awt.event.MouseListener moleListener = new java.awt.event.MouseListener() {

            // MOUSE EVENT: player whacks the mole
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (gameRunning) {
                    addScore();
                    moveMole();   // mole jumps to a new spot after being hit
                }
            }

            // MOUSE EVENT: cursor is over the mole
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                mole.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }

            // MOUSE EVENT: cursor leaves the mole
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                mole.setCursor(java.awt.Cursor.getDefaultCursor());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) { }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) { }
        };

        mole.addMouseListener(moleListener);   // register the event

        // MOUSE EVENT: player misses (clicks empty grass), lose 1 point
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (gameRunning && score > 0) {
                    score--;
                    lblScore.setText("Score: " + score);
                }
            }
        });
    }

    // =====================================================
    // TEMPORARY MOLE MOVEMENT 
    // =====================================================
    private void moveMole() {
        int x = (int) (Math.random() * (gamePanel.getWidth() - mole.getWidth()));
        int y = (int) (Math.random() * (gamePanel.getHeight() - mole.getHeight()));
        mole.setLocation(x, y);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Mole.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Mole.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Mole.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Mole.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Mole().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JPanel gamePanel;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JLabel lblScore;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenu menuGame;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem menuInstructions;
    private javax.swing.JMenuItem menuPause;
    private javax.swing.JMenuItem menuRestart;
    private javax.swing.JMenuItem menuStart;
                   
}