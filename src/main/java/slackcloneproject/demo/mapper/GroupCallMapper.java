package slackcloneproject.demo.mapper;


import org.springframework.stereotype.Service;
import slackcloneproject.demo.dto.user.GroupCallDTO;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.service.RoomCacheService;

import java.util.List;
import java.util.Optional;

@Service
public class GroupCallMapper {

    private RoomCacheService roomCacheService;

    public GroupCallDTO toGroupCall(GroupEntity group) {
        List<String> keys = roomCacheService.getAllKeys();
        GroupCallDTO groupCallDTO = new GroupCallDTO();
        Optional<String> actualRoomKey = keys.stream().filter((key) -> {
            String[] roomKey = key.split("_");
            return group.getUrl().equals(roomKey[0]);
        }).findFirst();
        if(actualRoomKey.isPresent()) {
            groupCallDTO.setAnyCallActive(true);
            groupCallDTO.setActiveCallUrl(actualRoomKey.get().split("_")[1]);
        } else {
            groupCallDTO.setAnyCallActive(false);
        }
        return groupCallDTO;
    }
}
