package slackcloneproject.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findByNameOrEmail(username, username);
        if(user == null) {
            throw new UsernameNotFoundException(username);
        }

        if(!user.isEnabled()) {
            throw new DisabledException("Account is not enabled");
        }
        return new User((user.getUsername), user.getPassword(), user.getAuthorities());
    }

}
