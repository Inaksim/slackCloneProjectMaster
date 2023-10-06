package slackcloneproject.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import slackcloneproject.demo.controller.WsFileController;
import slackcloneproject.demo.entity.GroupRoleKey;
import slackcloneproject.demo.entity.GroupUser;
import slackcloneproject.demo.repository.GroupUserJoinRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GroupUserJoinService {
    private static final Logger log = LoggerFactory.getLogger(WsFileController.class);

    private GroupUserJoinRepository groupUserJoinRepository;

    private MessageService messageService;

    public GroupUser save(GroupUser groupUser ) {
        return groupUserJoinRepository.save(groupUser);
    }

    public void saveAll(List<GroupUser> groupUsers) {
        try {
            groupUserJoinRepository.saveAll(groupUsers);
        } catch (Exception e) {
            log.error("Cannot save user for conversation: {}", e.getMessage());
        }
    }

    public Optional<GroupUser> findById(GroupRoleKey id) {
        return groupUserJoinRepository.findById(id);
    }

    public List<GroupUser> findAll() {
        return groupUserJoinRepository.findAll();
    }

    public List<GroupUser> findAllByGroupId(int groupId) {
        return groupUserJoinRepository.getAllByGroupId(groupId);
    }

    public boolean checkIfUserIsAuthorizedInGroup(int userId, int groupId) {
        List<Integer> ids = groupUserJoinRepository.getUsersIdInGroup(groupId);
        return ids.stream().noneMatch(id -> id == userId);
    }

    public GroupUser grantUserAdminInConversation(int userId, int groupId) {
        return executeActionOnGroupUser(userId, groupId, 1);
    }

    public GroupUser executeActionOnGroupUser(int userId, int groupid, int role) {
        GroupRoleKey groupRoleKey = new GroupRoleKey(groupid, userId);
        Optional<GroupUser> optionalGroupUserToDelete = groupUserJoinRepository.findById(groupRoleKey);
        if(optionalGroupUserToDelete.isPresent()) {
            GroupUser groupUser = optionalGroupUserToDelete.get();
            groupUser.setRole(role);
            return groupUserJoinRepository.save(groupUser);
        }
        return null;
    }

    public void removeUserAdminFromConversation(int userIdToDelete, int groupId) {
        executeActionOnGroupUser(userIdToDelete, groupId, 0);
    }

    public void removeUserFromConversation(int userIdToDelete, int groupId) {
        GroupRoleKey groupRoleKey = new GroupRoleKey(groupId, userIdToDelete);
        try {
            Optional<GroupUser> optionalGroupUserToDelete = groupUserJoinRepository.findById(groupRoleKey);
            optionalGroupUserToDelete.ifPresent(groupUser -> groupUserJoinRepository.delete(groupUser));
            List<Integer> userId = groupUserJoinRepository.getUsersIdInGroup(groupId);
            if(userId.isEmpty()) {
                log.info("All user have lest the group [group => {}]. Deleting message/.....", groupId);
                messageService.deleteAllMessageByGroupId(groupId);
                log.info("All messages have been successfully deleted");
            }
        } catch (Exception e) {
            log.error("Error occurred during user removal : {}", e.getMessage());
        }
    }
}
