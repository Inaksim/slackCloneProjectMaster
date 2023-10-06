package slackcloneproject.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.N;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupCallDTO {

    private Boolean anyCallActive;

    private String activeCallUrl;
}