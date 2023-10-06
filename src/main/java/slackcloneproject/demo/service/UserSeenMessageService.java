package slackcloneproject.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.entity.MessageEntity;
import slackcloneproject.demo.entity.MessageUserEntity;
import slackcloneproject.demo.repository.UserSeenMessageRepository;

import java.util.Optional;

@Service
public class UserSeenMessageService {

    private UserSeenMessageRepository seenMessageRepository;
    private UserService userService;
    private GroupService groupService;

    @Transactional
    public void saveMessageNotSeen(MessageEntity msg, int groupId) {
        Optional<GroupEntity> group = groupService.findById(groupId);

        group.ifPresent(groupEntity ->
                groupEntity.getUserEntities().forEach((user) -> {
                    MessageUserEntity message = new MessageUserEntity();
                    message.setMessageId(msg.getId());
                    message.setUserId(user.getId());
                    message.setSeen(msg.getUser_id() == user.getId());
                    seenMessageRepository.save(message);
                }));
    }

    public MessageUserEntity findByMessageId(int messageId, int userId) {
        return seenMessageRepository.findAllByMessageIdAndUserId(messageId, userId);
    }

    public void saveMessageUserEntity(MessageUserEntity toSave) {
        seenMessageRepository.save(toSave);
    }
}
