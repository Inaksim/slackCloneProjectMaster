package slackcloneproject.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import slackcloneproject.demo.utils.RtcActionEnum;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RtcTransportDTO {

    private int userId;

    private String groupUrl;

    private RtcActionEnum action;

    private Object offer;

    private Object answer;
}
