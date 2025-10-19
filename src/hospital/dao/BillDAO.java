package hospital.dao;

import hospital.config.DatabaseConfig;
import hospital.models.Bill;
import hospital.models.BillItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public Bill createBill(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (patient_id, record_id, total_amount, status, payment_method, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, bill.getPatientId());
            stmt.setInt(2, bill.getRecordId());
            stmt.setDouble(3, bill.getTotalAmount());
            stmt.setString(4, bill.getStatus());
            stmt.setString(5, bill.getPaymentMethod());
            stmt.setInt(6, bill.getCreatedBy());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                bill.setBillId(rs.getInt(1));
            }
            return bill;
        }
    }

    public BillItem createBillItem(BillItem billItem) throws SQLException {
        String sql = "INSERT INTO bill_items (bill_id, item_type, item_name, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, billItem.getBillId());
            stmt.setString(2, billItem.getItemType());
            stmt.setString(3, billItem.getItemName());
            stmt.setInt(4, billItem.getQuantity());
            stmt.setDouble(5, billItem.getUnitPrice());
            stmt.setDouble(6, billItem.getTotalPrice());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                billItem.setItemId(rs.getInt(1));
            }
            return billItem;
        }
    }

    public List<Bill> getBillsByPatientId(int patientId) throws SQLException {
        String sql = "SELECT b.*, p.first_name, p.last_name " +
                     "FROM bills b " +
                     "JOIN patients p ON b.patient_id = p.patient_id " +
                     "WHERE b.patient_id = ? " +
                     "ORDER BY b.bill_date DESC";
        
        List<Bill> bills = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Bill bill = new Bill();
                bill.setBillId(rs.getInt("bill_id"));
                bill.setPatientId(rs.getInt("patient_id"));
                bill.setRecordId(rs.getInt("record_id"));
                bill.setTotalAmount(rs.getDouble("total_amount"));
                bill.setStatus(rs.getString("status"));
                bill.setPaymentMethod(rs.getString("payment_method"));
                bill.setBillDate(rs.getTimestamp("bill_date").toLocalDateTime());
                bill.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
                
                bills.add(bill);
            }
        }
        return bills;
    }

    public List<BillItem> getBillItemsByBillId(int billId) throws SQLException {
        String sql = "SELECT * FROM bill_items WHERE bill_id = ?";
        
        List<BillItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, billId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BillItem item = new BillItem();
                item.setItemId(rs.getInt("item_id"));
                item.setBillId(rs.getInt("bill_id"));
                item.setItemType(rs.getString("item_type"));
                item.setItemName(rs.getString("item_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setTotalPrice(rs.getDouble("total_price"));
                item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                
                items.add(item);
            }
        }
        return items;
    }
}