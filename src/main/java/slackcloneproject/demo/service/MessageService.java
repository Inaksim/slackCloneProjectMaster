package slackcloneproject.demo.service;

import org.springframework.stereotype.Service;
import slackcloneproject.demo.entity.MessageEntity;
import slackcloneproject.demo.repository.MessageRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

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

    public List<MessageEntity>


}
