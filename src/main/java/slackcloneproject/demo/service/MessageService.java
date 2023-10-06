package slackcloneproject.demo.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import slackcloneproject.demo.dto.MessageDTO;
import slackcloneproject.demo.dto.NotificationDTO;
import slackcloneproject.demo.entity.FileEntity;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.entity.MessageEntity;
import slackcloneproject.demo.repository.MessageRepository;
import slackcloneproject.demo.utils.MessageTypeEnum;


import java.util.*;

@Service
public class MessageService {

    private MessageRepository messageRepository;
    private UserService userService;
    private GroupService groupService;
    private FileService fileService;

    private static final String[] colorsArray =
            {
                    "#FFC194", "#9CE03F", "#62C555", "#3AD079",
                    "#44CEC3", "#F772EE", "#FFAFD2", "#FFB4AF",
                    "#FF9207", "#E3D530", "#D2FFAF", "FF5733"
            };

    private static final Map<Integer, String> colors = new HashMap<>();
    private String getRandomColor() {
        return colorsArray[new Random().nextInt(colorsArray.length)];
    }

    public MessageEntity createAndSaveMessage(int userId, int groupId, String type, String data) {
        MessageEntity msg = new MessageEntity(userId, groupId, type, data);
        return messageRepository.save(msg);
    }

    public void flush() {
        messageRepository.flush();
    }

    public MessageEntity save (MessageEntity message) {
        return messageRepository.save(message);
    }

    public List<MessageEntity> findByGroupId(int id, int offset) {
        List<MessageEntity> list = messageRepository.findByGroupIdAndOffset(id, offset);
        if(list.size() == 0) {
            return new ArrayList<>();
        }
        return list;
    }


    public void deleteAllMessageByGroupId(int groupId) {
        messageRepository.deleteMessagesDataByGroupId(groupId);
    }

    public MessageEntity findLastMessage(int groupId) {
        return messageRepository.findLastMessageByGroupId(groupId);
    }

    public int findLastMessageIdByGroupId(int groupId) {
        return messageRepository.findLastMessageIdByGroupId(groupId);
    }

    public MessageDTO createMessageDTO(int id, String type, int userId, String date, int group_id, String message) {
        colors.putIfAbsent(userId, getRandomColor());
        String username = userService.findUsernameById(userId);
        String fileUrl = "";
        String[] arr = username.split(",");
        String initials = arr[0].substring(0, 1).toUpperCase() + arr[1].substring(0,1).toUpperCase();
        String sender = StringUtils.capitalize(arr[0]) + "" + StringUtils.capitalize(arr[1]);
        if(type.equals(MessageTypeEnum.FILE.toString())) {
            FileEntity fileEntity = fileService.findByFkMessageId(id);
            fileUrl = fileEntity.getUrl();
        }
        return new MessageDTO(id, type, message, userId, group_id, null, sender, date, initials, colors.get(userId), fileUrl, userId == id);
    }

    public static String createUserInitials(String flName) {
        String[] names = flName.split(",");
        return names[0].substring(0,1).toUpperCase() + names[1].substring(0, 1).toUpperCase();
    }

    @Transactional
    public List<Integer> createNotificationList(int userId, String groupUrl) {
        int groupId = groupService.findGroupByUrl(groupUrl);
        List<Integer> toSend = new ArrayList<>();
        Optional<GroupEntity> optionalGroupEntity = groupService.findById(groupId);
        if(optionalGroupEntity.isPresent()) {
            GroupEntity groupEntity = optionalGroupEntity.get();
            groupEntity.getUserEntities().forEach(userEntity -> toSend.add(userEntity.getId()));
        }
        return toSend;

    }

    public NotificationDTO createNotificationDTO(MessageEntity msg) {
        String groupUrl = groupService.getGroupUrlById(msg.getGroup_id());
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setGroupId(msg.getGroup_id());
        notificationDTO.setGroupUrl(groupUrl);
        if (msg.getType().equals(MessageTypeEnum.TEXT.toString())) {
            notificationDTO.setType(MessageTypeEnum.TEXT);
            notificationDTO.setMessage(msg.getMessage());
        }
        if (msg.getType().equals(MessageTypeEnum.FILE.toString())) {
            FileEntity fileEntity = fileService.findByFkMessageId(msg.getId());
            notificationDTO.setType(MessageTypeEnum.FILE);
            notificationDTO.setMessage(msg.getMessage());
            notificationDTO.setFileUrl(fileEntity.getUrl());
            notificationDTO.setFileName(fileEntity.getFilename());
        }
        notificationDTO.setFromUserId(msg.getUser_id());
        notificationDTO.setLastMessageDate(msg.getCreatedAt().toString());
        notificationDTO.setSenderName(userService.findFirstNameById(msg.getUser_id()));
        notificationDTO.setMessageSeen(false);
        return notificationDTO;
    }

    public MessageDTO createNotificationMessageDTO(MessageEntity msg, int userId) {
        String groupUrl = groupService.getGroupUrlById(msg.getGroup_id());
        String firstName = userService.findFirstNameById(msg.getUser_id());
        String initials = userService.findUsernameById(msg.getUser_id());
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(msg.getId());
        if (msg.getType().equals(MessageTypeEnum.FILE.toString())) {
            String url = fileService.findFileUrlByMessageId(msg.getId());
            messageDTO.setFileUrl(url);
        }
        messageDTO.setType(msg.getType());
        messageDTO.setMessage(msg.getMessage());
        messageDTO.setUserId(msg.getUser_id());
        messageDTO.setGroupUrl(groupUrl);
        messageDTO.setGroupId(msg.getGroup_id());
        messageDTO.setSender(firstName);
        messageDTO.setTime(msg.getCreatedAt().toString());
        messageDTO.setInitials(createUserInitials(initials));
        messageDTO.setColor(colors.get(msg.getUser_id()));
        messageDTO.setMessageSeen(msg.getUser_id() == userId);
        return messageDTO;
    }

}
