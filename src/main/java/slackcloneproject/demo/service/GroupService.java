package slackcloneproject.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import slackcloneproject.demo.dto.GroupMemberDTO;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.entity.GroupUser;
import slackcloneproject.demo.entity.UserEntity;
import slackcloneproject.demo.repository.GroupRepository;
import slackcloneproject.demo.utils.GroupTypeEnum;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);
    private GroupRepository groupRepository;
    private UserService userService;
    private GroupUserJoinService groupUserJoinService; //will be added

    public int findGroupByUrl(String url) {
        return groupRepository.findGroupByUrl(url);
    }

    public List<Integer> getAllUsersIdByGroupUrl(String groupUrl) {
        int groupId = groupRepository.findGroupByUrl(groupUrl);
        List<GroupUser> users = groupUserJoinService.findAllByGroupId(groupId);
        return users.stream().map(GroupUser :: getUserId).collect(Collectors.toList());
    }

    public String getGroupName(String url) {
        return groupRepository.getGroupEntitiesBy(url);
    }

    public String getGroupUrlById(int id) {
        return groupRepository.getGroupUrlById(id);
    }

    public GroupMemberDTO addUserToConversation(int userId, int groupId) {
        Optional<GroupEntity> groupEntity = groupRepository.findById(groupId);
        if(groupEntity.isPresent() && groupEntity.orElse(null).getGroupTypeEnum().equals(GroupTypeEnum.SINGLE)) {
            log.info("Cannot add user in a single conversation");
            return new GroupMemberDTO();
        }
        UserEntity user = userService.findById(userId);

    }


}
