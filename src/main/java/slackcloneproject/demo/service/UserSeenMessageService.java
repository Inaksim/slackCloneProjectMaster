package slackcloneproject.demo.service;

import org.springframework.stereotype.Service;
import slackcloneproject.demo.repository.UserSeenMessageRepository;

@Service
public class UserSeenMessageService {

    private UserSeenMessageRepository seenMessageRepository;
    private UserService userService;
    private GroupService groupService;
}
