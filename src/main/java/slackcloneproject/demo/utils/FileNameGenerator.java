package slackcloneproject.demo.utils;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;


@Service
@NoArgsConstructor
public class FileNameGenerator {

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";

    public static final String alphanum = upper + lower + digits;


    public String getRandomString() {
        Random random = new SecureRandom();
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < StaticVariable.MAX_RANDOM_STRING_GEN; i++) {
            str.append(alphanum.charAt(random.nextInt(alphanum.length())));
        }
        return str.toString();
    }
}
