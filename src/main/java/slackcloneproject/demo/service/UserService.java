package slackcloneproject.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private UserMapper userMapper;

    private PasswordEncoder passwordEncoder;

    private UserRepository userRepository;

    private GroupUserJoinService groupUserJoinService;

    private Map<Integer, String> wsSessions = new HashMap<>();

    public Map<Integer, String> getWsSessions() {
        return wsSessions;
    }

    public void setWsSessions(Map<Integer, String> wsSessions) {
        this.wsSessions = wsSessions;
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void flush() {
        userRepository.flush();
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void save(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    public List<GroupMemberDTO> fetchAllUser(int[] ids) {
        List<GroupMemberDTO> toSend = new ArrayList<>();
        List<UserEntity> list = userRepository.getAllUsersNotAlreadyInConversation(ids);
        list.forEach(user -> toSend.add(new GroupMemberDTO(user.getId(), user.getFirstName(), user.getLastName(), false)));
        return toSend;
    }

    public String findUsernameWithWsToken(String token) {
        return userRepository.getUsernameWithWsToken(token);
    }

    public int findUserIdWithToken(String token) {
        return userRepository.getUserIdWithToken(token);
    }

    public UserEntity findByNameOrEmail(String str0, String str1) {
        return userRepository.getUserByFirstNameOrName(str0, str1);
    }

    public boolean checkIfUserIsAdmin(int userId, int groupIdToCheck) {
        GroupRoleKey id = new GroupRoleKey(groupIdToCheck, userId);
        Optional<GroupUser> optional = groupUserJoinService.findById(id);
        if(optional.isPresent()) {
            GroupUser groupUser = optional.get();
            return  groupUser.getRole() == 1;

        }
        return false;
    }
}
