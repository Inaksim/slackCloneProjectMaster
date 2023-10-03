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

    private int id;

    private String url;

    private String name;

    private String groupType;

    private String lastMessageSender;

    private String lasMessage;

    private String lastMessageDate;

    private boolean isLastMessageSeen;
}
