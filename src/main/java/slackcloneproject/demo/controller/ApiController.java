package slackcloneproject.demo.controller;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;
import slackcloneproject.demo.dto.AuthenticationUserDTO;
import slackcloneproject.demo.dto.GroupMemberDTO;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.entity.GroupRoleKey;
import slackcloneproject.demo.entity.GroupUser;
import slackcloneproject.demo.entity.UserEntity;
import slackcloneproject.demo.mapper.GroupMapper;
import slackcloneproject.demo.service.GroupService;
import slackcloneproject.demo.service.GroupUserJoinService;
import slackcloneproject.demo.service.UserService;
import slackcloneproject.demo.utils.JwtUtil;
import slackcloneproject.demo.utils.StaticVariable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(value = "/api")
public class ApiController {

    private final Logger log = LoggerFactory.getLogger(ApiController.class);


    private UserService userService;


    private GroupService groupService;


    private GroupMapper groupMapper;


    private GroupUserJoinService groupUserJoinService;

    private JwtUtil jwtUtil;

    @GetMapping(value = "/users/all/{groupUrl}")
    public List<GroupMemberDTO> fetchAllUsersNotInGroup(@PathVariable String groupUrl) {
        int groupId = groupService.findGroupByUrl(groupUrl);
        GroupRoleKey groupRoleKey = new GroupRoleKey();
        groupRoleKey.setGroupId(groupId);
        List<GroupUser> groupUsers = groupUserJoinService.findAllByGroupId(groupId);
        Object[] objects = groupUsers.stream().map(GroupUser::getUserId).toArray();
        int[] ids = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            ids[i] = (int) objects[i];
        }
        return userService.fetchAllUsers(ids);
    }

    @GetMapping(value = "/users/group/{groupUrl}")
    public List<GroupMemberDTO> fetchAllUsers(@PathVariable String groupUrl) {
        List<GroupMemberDTO> toSend = new ArrayList<>();
        int id = groupService.findGroupByUrl(groupUrl);
        Optional<GroupEntity> optionalGroupEntity = groupService.findById(id);
        if (optionalGroupEntity.isPresent()) {
            GroupEntity group = optionalGroupEntity.get();
            Set<GroupUser> groupUsers = group.getGroupUsers();
            groupUsers.forEach(groupUser -> toSend.add(groupMapper.toGroupMemberDTO(groupUser)));
        }
        toSend.sort(Comparator.comparing(GroupMemberDTO::isAdmin).reversed());
        return toSend;
    }

    @GetMapping(value = "/user/add/{userId}/{groupUrl}")
    public ResponseEntity<GroupMemberDTO> addUserToConversation(@PathVariable int userId, @PathVariable String groupUrl) {
        int groupId = groupService.findGroupByUrl(groupUrl);
        try {
//            return ResponseEntity.ok().body(addedUsername + " has been added to " + groupService.getGroupName(groupUrl));
            return ResponseEntity.ok().body(groupService.addUserToConversation(userId, groupId));
        } catch (Exception e) {
            log.error("Error when trying to add user to conversation : {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping(value = "/user/remove/{userId}/group/{groupUrl}")
    public ResponseEntity<?> removeUserFromConversation(HttpServletRequest request, @PathVariable Integer userId, @PathVariable String groupUrl) {
        return doUserAction(request, userId, groupUrl, "delete");
    }

    @GetMapping(value = "/user/grant/{userId}/group/{groupUrl}")
    public ResponseEntity<?> grantUserAdminInConversation(HttpServletRequest request, @PathVariable Integer userId, @PathVariable String groupUrl) {
        return doUserAction(request, userId, groupUrl, "grant");
    }

    @GetMapping(value = "/user/remove/admin/{userId}/group/{groupUrl}")
    public ResponseEntity<?> removeAdminUserFromConversation(HttpServletRequest request, @PathVariable Integer userId, @PathVariable String groupUrl) {
        return doUserAction(request, userId, groupUrl, "removeAdmin");
    }

    @GetMapping(value = "/user/leave/{userId}/group/{groupUrl}")
    public ResponseEntity<?> leaveConversation(HttpServletRequest request, @PathVariable Integer userId, @PathVariable String groupUrl) {
        return doUserAction(request, userId, groupUrl, "removeUser");
    }

    private ResponseEntity<?> doUserAction(HttpServletRequest request, Integer userId, String groupUrl, String action) {
        Cookie cookie = WebUtils.getCookie(request, StaticVariable.SECURE_COOKIE);
        if (cookie == null) {
            return ResponseEntity.status(401).build();
        }
        String cookieToken = cookie.getValue();
        String username = jwtUtil.getUserNameFromJwtToken(cookieToken);
        int groupId = groupService.findGroupByUrl(groupUrl);
        String userToChange = userService.findUsernameById(userId);
        UserEntity userEntity = userService.findByNameOrEmail(username, username);
        if (userEntity != null) {
            int adminUserId = userEntity.getId();
            if (action.equals("removeUser")) {
                groupUserJoinService.removeUserFromConversation(userId, groupId);
            }
            if (userService.checkIfUserIsAdmin(adminUserId, groupId)) {
                try {
                    if (action.equals("grant")) {
                        groupUserJoinService.grantUserAdminInConversation(userId, groupId);
                        return ResponseEntity.ok().body(userToChange + " has been granted administrator to " + groupService.getGroupName(groupUrl));
                    }
                    if (action.equals("delete")) {
                        groupUserJoinService.removeUserFromConversation(userId, groupId);
                        return ResponseEntity.ok().body(userToChange + " has been removed from " + groupService.getGroupName(groupUrl));
                    }
                    if (action.equals("removeAdmin")) {
                        groupUserJoinService.removeUserAdminFromConversation(userId, groupId);
                        return ResponseEntity.ok().body(userToChange + " has been removed from administrators of " + groupService.getGroupName(groupUrl));
                    }
                } catch (Exception e) {
                    log.warn("Error during performing {} : {}", action, e.getMessage());
                    return ResponseEntity.status(500).build();
                }
            }
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping(value = "/user/register")
    public ResponseEntity<?> createUser(@RequestBody String data) {
        Gson gson = new Gson();
        AuthenticationUserDTO userDTO = gson.fromJson(data, AuthenticationUserDTO.class);

        // Check if there are matched in DB
        if ((userService.checkIfUserNameOrMailAlreadyUsed(userDTO.getFirstName(), userDTO.getEmail()))) {
            return ResponseEntity.badRequest().body("Username or mail already used, please try again");
        }
        UserEntity user = new UserEntity();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMail(userDTO.getEmail());
        user.setPassword(userService.passwordEncoder(userDTO.getPassword()));
        user.setShort_url(userService.createShortUrl(userDTO.getFirstName(), userDTO.getLastName()));
        user.setWsToken(UUID.randomUUID().toString());
        user.setRole(1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialNonExpired(true);
        user.setEnable(true);
        try {
            userService.save(user);
            log.info("User saved successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error while registering user : {}", e.getMessage());
        }
        return ResponseEntity.status(500).build();
    }

}
