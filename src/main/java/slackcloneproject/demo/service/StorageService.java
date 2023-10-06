package slackcloneproject.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import slackcloneproject.demo.entity.FileEntity;
import slackcloneproject.demo.utils.FileNameGenerator;
import slackcloneproject.demo.utils.StaticVariable;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class StorageService {

    private static Logger log = LoggerFactory.getLogger(StorageService.class);

    private FileNameGenerator fileNameGenerator;

    private FileService fileService;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(StaticVariable.FILE_STORAGE_PATH));
        } catch (IOException e) {
            log.error("Cannot Initialize directory : {}", e.getMessage());
        }
    }

    public void store(MultipartFile file, int messageId) {
        String completeName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String[] array = completeName.split("\\.");
        String fileExtension = array[array.length - 1];
        String fileName = fileNameGenerator.getRandomString();

        String newName = fileName + "." + fileExtension;
        String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(newName)
                .toUriString();

        FileEntity fileEntity = new FileEntity();
        fileEntity.setUrl(uri);
        fileEntity.setFilename(fileName);
        fileEntity.setMessageId(messageId);
        try {
            if (file.isEmpty()) {
                log.warn("Cannot save empty file with name : {}", newName);
                return;
            }
            if (fileName.contains("..")) {
                log.warn("Cannot store file with relative path outside current directory {}", newName);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, Paths.get(StaticVariable.FILE_STORAGE_PATH).resolve(newName),
                        StandardCopyOption.REPLACE_EXISTING);
                fileService.save(fileEntity);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
