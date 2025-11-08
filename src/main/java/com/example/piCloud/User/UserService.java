package com.example.piCloud.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private static final String USER_NOT_FOUND_MSG = "User with email %s not found";

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get all users
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    // Get user by ID with exception handling
    public User getUserById(Long userId) throws UserNotFoundException {
        return (User)this.userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
    }
    // Get user by email with exception handling
    public User getUserByEmail(String email) throws UserNotFoundException {
        return (User)this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    public List<User> getUsersByRole(UserRole userRole) {
        return this.userRepository.findByUserRole(userRole);
    }

    public User createUser(User user) {
        return (User)this.userRepository.save(user);
    }

    // Update user details with exception handling
    public User updateUser(User user) throws UserNotFoundException {
        Long userId = user.getUserID();
        User existingUser = getUserById(userId);
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNo(user.getPhoneNo());
        existingUser.setLocation(user.getLocation());
        return (User)this.userRepository.save(existingUser);
    }

    // Change user password with exception handling
    public User changePassword(User user) throws UserNotFoundException {
        String email = user.getEmail();
        User existingEmail = getUserByEmail(email);
        existingEmail.setPassword(user.getPassword());
        return (User)this.userRepository.save(existingEmail);
    }

    // Delete user by ID with exception handling
    public void deleteUser(Long userId) throws UserNotFoundException {
        getUserById(userId);
        this.userRepository.deleteById(userId);
    }

    // Load user details by username (email) for authentication
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return (UserDetails)this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email %s not found", new Object[] { email })));
    }
}

