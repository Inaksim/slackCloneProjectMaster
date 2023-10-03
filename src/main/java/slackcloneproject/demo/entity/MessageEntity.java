package slackcloneproject.demo.entity;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "message")
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

   private String message;

   @Column(name = "msg_group_id")
   private int group_id;

   @Column(name = "msg_user_id")
   private int user_id;

   @Column(name = "type")
   private String type;

   @Column(name = "created_at")
   @CreationTimestamp
   private Timestamp createdAt;

    public MessageEntity(int userId, int groupId, String type, String message) {
        this.user_id = userId;
        this.group_id = groupId;
        this.type = type;
        this.message = message;
    }
}
