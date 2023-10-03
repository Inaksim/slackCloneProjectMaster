package slackcloneproject.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import slackcloneproject.demo.utils.TransportActionEnum;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InputTransportDTO {

    private int userId;

    private TransportActionEnum action;

    private String wsToken;

    private String groupUrl;

    private String message;

    private int messageId;
}
