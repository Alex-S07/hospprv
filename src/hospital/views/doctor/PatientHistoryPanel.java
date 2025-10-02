package hospital.views.doctor;


import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import java.awt.*;

public class PatientHistoryPanel extends JPanel {
    private User currentUser;
    
    public PatientHistoryPanel(User user) {
        this.currentUser = user;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Patient History");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Constants.BACKGROUND_COLOR);
        searchPanel.add(new JLabel("Search Patient:"));
        JTextField searchField = new JTextField(30);
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(Constants.PRIMARY_COLOR);
        searchBtn.setForeground(Color.WHITE);
        searchPanel.add(searchBtn);
        
        JTextArea infoArea = new JTextArea(20, 60);
        infoArea.setEditable(false);
        infoArea.setText("Patient history will be displayed here...");
        JScrollPane scrollPane = new JScrollPane(infoArea);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Constants.BACKGROUND_COLOR);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}