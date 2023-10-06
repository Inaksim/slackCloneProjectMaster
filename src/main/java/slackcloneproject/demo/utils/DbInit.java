package slackcloneproject.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import slackcloneproject.demo.entity.UserEntity;
import slackcloneproject.demo.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class DbInit {
    static Logger log = LoggerFactory.getLogger(DbInit.class);

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    public DbInit(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    public void run(String... args) {
        try {

            if (userService.findAll().size() == 0) {
                List<String> sourceList = Arrays.asList("Thibaut", "Mark", "John", "Luke", "Steve");
                sourceList.forEach(val -> {
                    UserEntity user = new UserEntity();
                    user.setFirstName(val);
                    user.setLastName("Doe" + val.toLowerCase());
                    user.setPassword(passwordEncoder.encode("root"));
                    user.setMail(val.toLowerCase() + "@fastlitemessage.com");
                    user.setEnable(true);
                    user.setCredentialNonExpired(true);
                    user.setAccountNonLocked(true);
                    user.setAccountNonExpired(true);
                    user.setWsToken(UUID.randomUUID().toString());
                    user.setRole(1);
                    userService.save(user);
                });
                log.info("No entries detected in User table, data created");
            } else {
                log.info("Data already set in User table, skipping init step");
            }
        } catch (Exception e) {
            log.error("Cannot init DB : {}", e.getMessage());
        }
    }
}
