package slackcloneproject.demo.entiy;




import lombok.*;
import org.hibernate.dialect.HANAColumnStoreDialect;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements UserDetails, Serializable {
    public UserEntity(int id, String firstName, String password) {
        this.id = id;
        this.firstName = firstName;
        this.password = password;
    }

    @Id
    private int id;
    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    private String password;

    @Column(name = "wstoken")
    private String wsToken;

    private String jwt;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "userEntities", cascade = CascadeType.ALL)
    private Set<GroupEntiy> groupEntitySet = new HashSet<>();

    @OneToMany(mappedBy = "groupMapping", fetch = FetchType.EAGER)
    private Set<GroupUser> groupUsers = new HashSet<>();

    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration_date;

    @Column(name = "short_url")
    private String short_url;

    @Column(name = "email")
    private String mail;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    @Column(name = "credintials_non_expired")
    private boolean credentialNonExpired;

    @Column(name = "enabled")
    private boolean enable;

    @Column(name = "role_id")
    private int role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return firstName;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnable();
    }
}
