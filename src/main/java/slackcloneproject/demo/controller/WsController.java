package slackcloneproject.demo.controller;

import com.google.gson.Gson;
import com.mysql.cj.MessageBuilder;
import com.mysql.cj.Messages;
import liquibase.pro.packaged.G;
import liquibase.pro.packaged.L;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import slackcloneproject.demo.dto.*;
import slackcloneproject.demo.entity.MessageEntity;
import slackcloneproject.demo.entity.MessageUserEntity;
import slackcloneproject.demo.service.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import slackcloneproject.demo.utils.MessageTypeEnum;
import slackcloneproject.demo.utils.RtcActionEnum;
import slackcloneproject.demo.utils.TransportActionEnum;


import java.util.*;

@RestController
@CrossOrigin
public class WsController {

    private RoomCacheService roomCacheService;

    private final Logger log = LoggerFactory.getLogger(WsController.class);

    private final Map<String, ArrayList<Integer>> userIndexByRoomId = new HashMap<>();

    private GroupService groupService;

    private MessageService messageService;

    private SimpMessagingTemplate messagingTemplate;

    private GroupUserJoinService groupUserJoinService;

    private UserSeenMessageService userSeenMessageService;


    @GetMapping(value = "/room/ensure-room-exists/{groupUrl}")
    public Boolean ensureCallRoomExists(@PathVariable String groupUrl) {
        if(StringUtils.hasLength(groupUrl)) {
            return roomCacheService.getRoomByKey(groupUrl) != null;
        }
        return false;
    }

    @MessageMapping("/message")
    public void mainChannel(InputTransportDTO dto, @Header("simpSessionId") String sessionId) {
        TransportActionEnum action = dto.getAction();
        switch (action) {

            case SEND_GROUP_MESSAGE:
                this.getAndSaveMessage(dto.getUserId(), dto.getGroupUrl(), dto.getMessage());
                break;

            case FETCH_GROUP_MESSAGES:
                if(!dto.getGroupUrl().equals(""))  {
                    int groupId = groupService.findGroupByUrl(dto.getGroupUrl());
                    if(dto.getGroupUrl().equals("") || groupUserJoinService.checkIfUserIsAuthorizedInGroup(dto.getUserId(), groupId)) {
                        break;
                    }
                    WrapperMessageDTO messageDTO = this.getConversationMessage(dto.getGroupUrl(), dto.getMessageId());
                    OutputTransportDTO resMessages = new OutputTransportDTO();
                    if(dto.getMessageId() == -1) {
                        resMessages.setAction(TransportActionEnum.FETCH_GROUP_MESSAGES);
                    } else {
                        resMessages.setAction(TransportActionEnum.ADD_CHAT_HISTORY);
                    }
                    resMessages.setObject(messageDTO);
                    this.messagingTemplate.convertAndSend("/topic/user/" + dto.getUserId(), resMessages);
                }
                break;

                case MARK_MESSAGE_AS_SEEN:
                    if(!"".equals(dto.getGroupUrl())) {
                        int messageId = messageService.findLastMessageIdByGroupId(groupService.findGroupByUrl(dto.getGroupUrl()));
                        MessageUserEntity messageUserEntity = userSeenMessageService.findByMessageId(messageId, dto.getUserId());
                        if(messageUserEntity == null) break;
                        messageUserEntity.setSeen(true);
                        userSeenMessageService.saveMessageUserEntity(messageUserEntity);
                    }
                    break;

            case LEAVE_GROUP:
                if(!dto.getGroupUrl().equals("")) {
                    log.info("User id {} left group {}", dto.getUserId(), dto.getGroupUrl());
                    int groupId = groupService.findGroupByUrl(dto.getGroupUrl());
                    groupUserJoinService.removeUserFromConversation(dto.getUserId(), groupId);

                    String groupName = groupService.getGroupName(dto.getGroupUrl());
                    LeaveGroupDTO leaveGroupDTO = new LeaveGroupDTO();
                    leaveGroupDTO.setGroupUrl(dto.getGroupUrl());
                    leaveGroupDTO.setGroupName(groupName);
                    OutputTransportDTO leaveResponse = new OutputTransportDTO();
                    leaveResponse.setAction(TransportActionEnum.LEAVE_GROUP);
                    leaveResponse.setObject(leaveGroupDTO);
                    this.messagingTemplate.convertAndSend("/topic/user/" + dto.getUserId(), leaveResponse);
                 } else {
                    log.warn("User cannot left group because groupUrl is empty");
                }
                break;

            case CHECK_EXISTING_CALL:
                OutputTransportDTO outputTransportDTO = new OutputTransportDTO();
                for(String key : userIndexByRoomId.keySet()) {
                    if(key.contains(dto.getGroupUrl())) {
                        outputTransportDTO.setAction(TransportActionEnum.CALL_IN_PROGRESS);
                        this.messagingTemplate.convertAndSend("/topic/user/" + dto.getUserId(), outputTransportDTO);
                        break;
                    }
                }
                outputTransportDTO.setAction(TransportActionEnum.NO_CALL_IN_PROGRESS);
                this.messagingTemplate.convertAndSend("topic/user/" + dto.getUserId(), outputTransportDTO);
                break;
            default:
                break;
        }
    }

    @MessageMapping("/rtc/{roomUrl}")
    public void webRtcChannel(@DestinationVariable String roomUrl, RtcTransportDTO dto) {
        RtcActionEnum action = dto.getAction();
        switch(action) {
            case INIT_ROOM -> {
                List<Integer> usersId = this.groupService.getAllUsersIdByGroupUrl(dto.getGroupUrl());
                ArrayList<Integer> userIds = new ArrayList<>();
                userIds.add(dto.getUserId());

                roomCacheService.putNewRoom(dto.getGroupUrl(), roomUrl, userIds);
                OutputTransportDTO outputTransportDTO = new OutputTransportDTO();
                outputTransportDTO.setAction(TransportActionEnum.CALL_INCOMING);
                outputTransportDTO.setObject(roomUrl);
                usersId.stream()
                        .filter((user) -> !user.equals(dto.getUserId()))
                        .forEach((userId) -> this.messagingTemplate.convertAndSend("/topic/user/" + userId, outputTransportDTO));
            }
            case SEND_ANSWER -> {
                String key = roomUrl + "_" + dto.getGroupUrl();
                ArrayList<Integer> hostList = userIndexByRoomId.get(key);
                RtcTransportDTO rtcTransportDTO = new RtcTransportDTO();
                rtcTransportDTO.setUserId(dto.getUserId());
                rtcTransportDTO.setAction(RtcActionEnum.SEND_ANSWER);
                rtcTransportDTO.setAnswer(dto.getAnswer());
                 hostList.stream()
                         .filter((user) -> !user.equals(dto.getUserId()))
                         .forEach((userid) -> this.messagingTemplate.convertAndSend("/topic/rtc/" + userid, rtcTransportDTO));
            }
            case JOIN_ROOM -> {
                String key = roomUrl + "_" + dto.getGroupUrl();
                ArrayList<Integer> hostList = userIndexByRoomId.get(key);
                RtcTransportDTO rtcTransportDTO = new RtcTransportDTO();
                rtcTransportDTO.setUserId(dto.getUserId());
                rtcTransportDTO.setAction(RtcActionEnum.SEND_OFFER);
                rtcTransportDTO.setOffer(dto.getOffer());
                hostList.add(dto.getUserId());
                userIndexByRoomId.put(roomUrl, hostList);
                hostList.stream()
                        .filter((user) -> !user.equals(dto.getUserId()))
                        .forEach(toUserId -> this.messagingTemplate.convertAndSend("/topic/rtc/" + toUserId, rtcTransportDTO));
            }
            case ICE_CANDIDATE -> {
                RtcTransportDTO transportDTO = new RtcTransportDTO();
                transportDTO.setUserId(dto.getUserId());
                transportDTO.setAction(RtcActionEnum.ICE_CANDIDATE);
                ArrayList<Integer> hostList = userIndexByRoomId.get(roomUrl);
                hostList.stream()
                        .filter((user) -> !user.equals(dto.getUserId()))
                        .forEach(toUserId -> this.messagingTemplate.convertAndSend("/topic/rtc/" + toUserId, transportDTO));
            }

            case LEAVE_ROOM -> {
                String key = roomUrl + "_" + dto.getGroupUrl();
                List<Integer> userIds = groupService.getAllUsersIdByGroupUrl(dto.getGroupUrl());
                HashMap<String, ArrayList<Integer>> hostListIndexedByRoomUrl = roomCacheService.getRoomByKey(key);
                if(hostListIndexedByRoomUrl.size() == 0) {
                    log.info("All users left the call, removing from list");
                    OutputTransportDTO transportDTO = new OutputTransportDTO();
                    transportDTO.setAction(TransportActionEnum.END_CALL);
                    transportDTO.setObject(dto.getGroupUrl());
                    userIndexByRoomId.remove(key);
                    userIds.forEach(userId -> this.messagingTemplate.convertAndSend("/topic/user/" + userId, transportDTO));

                }
            }
            default -> log.warn("Unknown action : {}", dto.getAction());
        }
    }

    public void getAndSaveMessage(int userId, String groupUrl, String message) {
        int groupId = groupService.findGroupByUrl(groupUrl);
        if(groupUserJoinService.checkIfUserIsAuthorizedInGroup(userId, groupId)) {
            return;
        }
        MessageEntity messageEntity = new MessageEntity(userId, groupId, MessageTypeEnum.TEXT.toString(), message);
        MessageEntity msg = messageService.save(messageEntity);
        List<Integer> toSend = messageService.createNotificationList(userId, groupUrl);

        userSeenMessageService.saveMessageNotSeen(msg, groupId);
        log.debug("Message saved");
        OutputTransportDTO dto = new OutputTransportDTO();
        dto.setAction(TransportActionEnum.NOTIFICATION_MESSAGE);
        toSend.forEach(toUserId -> {
            MessageDTO messageDTO = messageService.createNotificationMessageDTO(msg, toUserId);
            dto.setObject(messageDTO);
            messagingTemplate.convertAndSend("/topic/user/" + toUserId, dto);
        });
    }

    @MessageMapping("/message/call/{userId}/group/{groupUrl}")
    @SendTo("/topic/call/reply/{groupUrl}")
    public String wsCallMessageMapping(@DestinationVariable int userId, String req) {
        log.info("Receiving RTC data, sending back to user...");
        return req;
    }

    @MessageMapping("/groups/create/single")
    @SendToUser("/queue/reply")
    public void wsCreateConversation(String payload) {
        Gson gson = new Gson();
        CreateGroupDTO createGroupDTO = gson.fromJson(payload, CreateGroupDTO.class);
        Long id1 = createGroupDTO.getId1();
        Long id2 = createGroupDTO.getId2();
        groupService.createConversation(id1.intValue(), id2.intValue());

    }

    public WrapperMessageDTO getConversationMessage(String url, int messageId) {
        WrapperMessageDTO wrapper = new WrapperMessageDTO();
        if (url != null) {
            List<MessageDTO> messageDTOS = new ArrayList<>();
            int groupId = groupService.findGroupByUrl(url);
            List<MessageEntity> newMessages = messageService.findByGroupId(groupId, messageId);
            int lastMessageId = newMessages != null && newMessages.size() != 0 ? newMessages.get(0).getId() : 0;
            List<MessageEntity> afterMessage = messageService.findByGroupId(groupId, lastMessageId);
            if (newMessages != null) {
                wrapper.setLastMessage(afterMessage != null && afterMessage.size() == 0);
                newMessages.forEach(msg ->
                        messageDTOS.add(messageService
                                .createMessageDTO(msg.getId(), msg.getType(), msg.getUser_id(), msg.getCreatedAt().toString(), msg.getGroup_id(), msg.getMessage())));
            }
            wrapper.setMessages(messageDTOS);
            return wrapper;
        }
        return null;

    }

}
