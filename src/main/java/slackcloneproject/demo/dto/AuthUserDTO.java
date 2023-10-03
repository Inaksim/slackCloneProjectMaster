package slackcloneproject.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import slackcloneproject.demo.dto.user.GroupDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDTO {

    private int id;

    private String username;

    private String firstGroupUrl;

    private String wsToken;

    private List<GroupDTO> groups;
}
