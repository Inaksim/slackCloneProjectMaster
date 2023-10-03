package slackcloneproject.demo.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slackcloneproject.demo.dto.AuthUserDTO;
import slackcloneproject.demo.dto.user.GroupDTO;
import slackcloneproject.demo.dto.user.GroupWrapperDTO;
import slackcloneproject.demo.dto.user.InitUserDTO;
import slackcloneproject.demo.dto.user.UserDTO;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserMapper {
    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupCallMapper groupCallMapper;

    /**
     * Map a UserEntity to a UserDTO
     * The password is not sent
     *
     * @param userEntity the {@link UserEntity} to map
     * @return a {@link UserDTO}
     */
    public InitUserDTO toUserDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();
        InitUserDTO initUserDTO = new InitUserDTO();
        List<GroupWrapperDTO> groupWrapperDTOS = new ArrayList<>();

        userDTO.setId(userEntity.getId());
        userDTO.setFirstName(userEntity.getFirstName());
        userDTO.setLastName(userEntity.getLastName());
        userDTO.setWsToken(userEntity.getWsToken());
        userDTO.setJwt(userEntity.getJwt());

        userEntity.getGroupSet().forEach(groupEntity -> {
                    GroupWrapperDTO groupWrapperDTO = new GroupWrapperDTO();
                    groupWrapperDTO.setGroup(groupMapper.toGroupDTO(groupEntity, userEntity.getId()));
                    groupWrapperDTO.setGroupCall(groupCallMapper.toGroupCall(groupEntity));
                    groupWrapperDTOS.add(groupWrapperDTO);
                }
        );
        groupWrapperDTOS.sort(new ComparatorListWrapperGroupDTO());

        Optional<GroupWrapperDTO> groupUrl = groupWrapperDTOS.stream().findFirst();
        String firstGroupUrl = groupUrl.isPresent() ? groupUrl.get().getGroup().getUrl() : "";

        userDTO.setFirstGroupUrl(firstGroupUrl);
        initUserDTO.setUser(userDTO);
        initUserDTO.setGroupsWrapper(groupWrapperDTOS);
        return initUserDTO;
    }


    public AuthUserDTO toLightUserDTO(UserEntity userEntity) {
        Set<GroupEntity> groups = userEntity.getGroupSet();
        List<GroupDTO> allUserGroups = new ArrayList<>(userEntity.getGroupSet().stream()
                .map((groupEntity) -> groupMapper.toGroupDTO(groupEntity, userEntity.getId())).toList());
        Optional<GroupEntity> groupUrl = groups.stream().findFirst();
        String lastGroupUrl = groupUrl.isPresent() ? groupUrl.get().getUrl() : "";
        allUserGroups.sort(new ComparatorListGroupDTO());
        return new AuthUserDTO(userEntity.getId(), userEntity.getFirstName(), lastGroupUrl, userEntity.getWsToken(), allUserGroups);
    }
}