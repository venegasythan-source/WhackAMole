package whackamole;

public class WhackAMole {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Mole().setVisible(true);
            }
        });
    }
}