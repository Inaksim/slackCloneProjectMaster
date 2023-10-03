package slackcloneproject.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import slackcloneproject.demo.entity.MessageUserEntity;
import slackcloneproject.demo.entity.MessageUserKey;

@Repository
public interface UserSeenMessageRepository extends JpaRepository<MessageUserEntity, MessageUserKey> {
    MessageUserEntity findAllByMessageIdAndUserId(int messageId, int userId);
}
