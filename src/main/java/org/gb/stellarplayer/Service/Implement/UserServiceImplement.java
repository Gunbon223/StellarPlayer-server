package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Exception.ResourceNotFoundException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.UserUpdatePasswordRequest;
import org.gb.stellarplayer.Request.UserUpdateRequest;
import org.gb.stellarplayer.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImplement implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setDob(user.getDob());
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(UserUpdateRequest userUpdateRequest,int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmail(userUpdateRequest.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User updateUserAvatar(UserUpdateRequest userUpdateRequest, int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAvatar(userUpdateRequest.getAvatar());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User updateUserPassword(UserUpdatePasswordRequest userUpdateRequest, int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(userUpdateRequest.oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(userUpdateRequest.newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User getSubscribedUserById(int id) {
//        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        if (user.getSubscription() == null) {
//            throw new BadRequestException("User not subscribed");
//        }
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findByName(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(null);
        return user;
    }

    @Override
    public User getUserById(int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(null);
        return user;
    }

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public Map<String, Long> getNewUsersCountByPeriod(String period) {
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "month":
                // Get current month data
                YearMonth currentMonth = YearMonth.now();
                LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
                
                long currentMonthUsers = allUsers.stream()
                        .filter(user -> user.getCreatedAt() != null && 
                                user.getCreatedAt().isAfter(startOfMonth) && 
                                user.getCreatedAt().isBefore(now))
                        .count();
                result.put("current_month", currentMonthUsers);
                break;
                
            case "quarter":
                // Current quarter (3 months)
                LocalDateTime threeMonthsAgo = now.minusMonths(3);
                
                long quarterlyUsers = allUsers.stream()
                        .filter(user -> user.getCreatedAt() != null && 
                                user.getCreatedAt().isAfter(threeMonthsAgo) && 
                                user.getCreatedAt().isBefore(now))
                        .count();
                result.put("last_3_months", quarterlyUsers);
                
                // Monthly breakdown for the quarter
                for (int i = 0; i < 3; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    long monthlyUsers = allUsers.stream()
                            .filter(user -> user.getCreatedAt() != null && 
                                    user.getCreatedAt().isAfter(startOfPastMonth) && 
                                    user.getCreatedAt().isBefore(endOfPastMonth.plusDays(1)))
                            .count();
                    result.put(month.toString(), monthlyUsers);
                }
                break;
                
            case "year":
                // Current year
                LocalDateTime oneYearAgo = now.minusYears(1);
                
                long yearlyUsers = allUsers.stream()
                        .filter(user -> user.getCreatedAt() != null && 
                                user.getCreatedAt().isAfter(oneYearAgo) && 
                                user.getCreatedAt().isBefore(now))
                        .count();
                result.put("last_12_months", yearlyUsers);
                
                // Monthly breakdown for the year
                for (int i = 0; i < 12; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    long monthlyUsers = allUsers.stream()
                            .filter(user -> user.getCreatedAt() != null && 
                                    user.getCreatedAt().isAfter(startOfPastMonth) && 
                                    user.getCreatedAt().isBefore(endOfPastMonth.plusDays(1)))
                            .count();
                    result.put(month.toString(), monthlyUsers);
                }
                break;
                
            default:
                throw new BadRequestException("Invalid period. Use 'month', 'quarter', or 'year'");
        }
        
        return result;
    }
}
