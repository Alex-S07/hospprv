// package hospital.services;

// import hospital.dao.AppointmentDAO;
// import hospital.models.Appointment;
// import hospital.utils.DateUtil;
// import java.sql.SQLException;
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.concurrent.locks.ReentrantLock;
// import java.util.concurrent.ConcurrentHashMap;

// public class AppointmentService {
//     private AppointmentDAO appointmentDAO;
//     private final ConcurrentHashMap<String, ReentrantLock> appointmentLocks;
    
//     public AppointmentService() {
//         this.appointmentDAO = new AppointmentDAO();
//         this.appointmentLocks = new ConcurrentHashMap<>();
//     }
    
//     public synchronized Appointment bookAppointment(Appointment appointment) throws SQLException {
//         String lockKey = appointment.getDoctorId() + "_" + 
//                         DateUtil.formatDateTime(appointment.getAppointmentDateTime());
        
//         ReentrantLock lock = appointmentLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
//         lock.lock();
//         try {
//             // Check availability before booking
//             if (!isTimeSlotAvailable(appointment.getDoctorId(), appointment.getAppointmentDateTime())) {
//                 throw new SQLException("Time slot is already booked");
//             }
            
//             // Generate token number
//             int tokenNumber = generateTokenNumber(appointment.getDoctorId(), 
//                                                 appointment.getAppointmentDateTime().toLocalDate());
//             appointment.setTokenNumber(tokenNumber);
            
//             // Book appointment
//             return appointmentDAO.createAppointment(appointment);
            
//         } finally {
//             lock.unlock();
//             // Clean up lock if no other threads are waiting
//             if (!lock.hasQueuedThreads()) {
//                 appointmentLocks.remove(lockKey);
//             }
//         }
//     }
// }