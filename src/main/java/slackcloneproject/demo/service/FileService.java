package slackcloneproject.demo.service;

import org.springframework.stereotype.Service;
import slackcloneproject.demo.entity.FileEntity;
import slackcloneproject.demo.repository.FileRepository;

@Service
public class FileService {
    private FileRepository fileRepository;

    public FileEntity save(FileEntity f) {
        return fileRepository.save(f);
    }

    public FileEntity findByFkMessageId(int id) {
        return fileRepository.findByMessageId(id);
    }
    public String findFileUrlByMessageId(int id) {
        return fileRepository.findFileUrlByMessageId(id);
    }

}
