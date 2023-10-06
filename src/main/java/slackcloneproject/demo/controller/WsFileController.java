package slackcloneproject.demo.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import slackcloneproject.demo.dto.MessageDTO;
import slackcloneproject.demo.dto.OutputTransportDTO;
import slackcloneproject.demo.entity.MessageEntity;
import slackcloneproject.demo.service.GroupService;
import slackcloneproject.demo.service.MessageService;
import slackcloneproject.demo.service.StorageService;
import slackcloneproject.demo.service.UserSeenMessageService;
import slackcloneproject.demo.utils.MessageTypeEnum;
import slackcloneproject.demo.utils.TransportActionEnum;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class WsFileController {

    private static Logger log = LoggerFactory.getLogger(WsFileController.class);

    private MessageService messageService;

    private GroupService groupService;

    private SimpMessagingTemplate simpMessagingTemplate;

    private StorageService storageService;


    private UserSeenMessageService userSeenMessageService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam(name = "file")MultipartFile file, @RequestParam(name = "userId") int userId, @RequestParam(name = "groupUrl") String groupUrl) {
        int groupId = groupService.findGroupByUrl(groupUrl);
        try {
            MessageEntity messageEntity = messageService.createAndSaveMessage(userId, groupId, MessageTypeEnum.FILE.toString(), "have send a file");
            storageService.store(file, messageEntity.getId());
            OutputTransportDTO res = new OutputTransportDTO();
            MessageDTO messageDTO = messageService.createNotificationMessageDTO(messageEntity, userId);
            res.setAction(TransportActionEnum.NOTIFICATION_MESSAGE);
            res.setObject(messageDTO);
            userSeenMessageService.saveMessageNotSeen(messageEntity, groupId);
            List<Integer> toSend = messageService.createNotificationList(userId, groupUrl);
            toSend.forEach(toUserId -> simpMessagingTemplate.convertAndSend("/topic/user/" + toUserId, res));
        } catch (Exception e) {
            log.error("Cannot save file, caused by {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }
}
