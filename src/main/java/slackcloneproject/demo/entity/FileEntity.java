package slackcloneproject.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
@Table(name = "file_storage")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

    @Column(name = "fk_message_id")
    private int messageId;

    @Column(name = "filename")
    private String filename;

    @Column(name = "url")
    private String url;

    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
}
