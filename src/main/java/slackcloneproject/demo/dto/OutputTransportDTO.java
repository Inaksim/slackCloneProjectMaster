package slackcloneproject.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import slackcloneproject.demo.utils.TransportActionEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputTransportDTO {

    private TransportActionEnum action;

    private Object object;
}
