package hospital.views.receptionist;


import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import java.awt.*;

class BillingPanel extends JPanel {
    private User currentUser;
    
    public BillingPanel(User user) {
        this.currentUser = user;
        setBackground(Constants.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        
        JLabel label = new JLabel("Billing Panel - Coming Soon");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }
    
    public void refreshData() {
        // To be implemented
    }
}
