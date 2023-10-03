package slackcloneproject.demo.mapper;

import org.springframework.stereotype.Service;
import slackcloneproject.demo.dto.GroupMemberDTO;
import slackcloneproject.demo.dto.user.GroupDTO;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.entity.GroupUser;
import slackcloneproject.demo.entity.MessageEntity;
import slackcloneproject.demo.entity.MessageUserEntity;
import slackcloneproject.demo.service.MessageService;
import slackcloneproject.demo.service.UserSeenMessageService;
import slackcloneproject.demo.service.UserService;
import slackcloneproject.demo.utils.MessageTypeEnum;

@Service
public class GroupMapper {

    private MessageService messageService;
    private UserSeenMessageService seenMessageService;
    private UserService userService;

    public GroupDTO toGroupDTO(GroupEntity grp, int userId) {
        GroupDTO grpDTO = new GroupDTO();
        grpDTO.setId(grp.getId());
        grpDTO.setName(grp.getName());
        grpDTO.setUrl(grp.getUrl());
        grpDTO.setGroupType(grp.getGroupTypeEnum().toString());
        MessageEntity msg = messageService.findLastMessage(grp.getId());
        if(msg != null) {
            String sender = userService.findFirstNameById(msg.getUser_id());
            MessageUserEntity messageUserEntity = seenMessageService.findByMessageId(msg.getId(), userId);
            grpDTO.setLastMessageSender(sender);
            if(messageUserEntity != null) {
                if(msg.getType().equals(MessageTypeEnum.FILE.toString())) {
                    StringBuilder str = new StringBuilder();
                    String senderName = userId == msg.getUser_id() ? "You" : sender;
                    str.append(senderName);
                    str.append(" ");
                    str.append("have send a file");
                    grpDTO.setLastMessage(str.toString());
                } else {
                    grpDTO.setLastMessage(msg.getMessage());
                }
                grpDTO.setLastMessage(msg.getMessage());
                grpDTO.setLastMessageSeen(messageUserEntity.isSeen());
            }
        } else {
            grpDTO.setLastMessageDate(msg.getCreatedAt().toString());
            grpDTO.setLastMessageSeen(true);
        }
        return grpDTO;
    }

    public GroupMemberDTO toGroupMemberDTO (GroupUser groupUser) {
        return new GroupMemberDTO(groupUser.getUserMapping().getId(), groupUser.getUserMapping().getFirstName(), groupUser.getUserMapping().getLastName(), groupUser.getRole() == 1);
    }
}


