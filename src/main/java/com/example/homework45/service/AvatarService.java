package com.example.homework44.service;

import com.example.homework44.component.RecordMapper;
import com.example.homework44.entity.Avatar;
import com.example.homework44.exception.AvatarNotFoundException;
import com.example.homework44.exception.FacultyNotFoundException;
import com.example.homework44.record.AvatarRecord;
import com.example.homework44.record.FacultyRecord;
import com.example.homework44.repository.AvatarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AvatarService {

    private static final Logger LOG = LoggerFactory.getLogger(AvatarService.class);

    private final AvatarRepository avatarRepository;

    private final RecordMapper recordMapper;


    @Value("${application.avatar.store}")
    private String folderForAvatars;

    public AvatarService(AvatarRepository avatarRepository,
                         RecordMapper recordMapper) {
        this.avatarRepository = avatarRepository;
        this.recordMapper = recordMapper;
    }

    public void uploadAvatar(MultipartFile multipartFile) throws IOException {
        LOG.debug("Method uploadAvatar was invoked");
        byte[] data = multipartFile.getBytes();
        Avatar avatar = created(multipartFile.getSize(), multipartFile.getContentType(), data);
        String extension = Optional.ofNullable(multipartFile.getOriginalFilename())
                .map(s -> s.substring(multipartFile.getOriginalFilename().lastIndexOf(".")))
                .orElse("");
        Path path = Paths.get(folderForAvatars).resolve(avatar.getId() + extension);
        Files.write(path, data);
        avatar.setFilePath(path.toString());
        avatarRepository.save(avatar);
    }

    private Avatar created(long size,
                           String contentType,
                           byte[] data) {
        LOG.debug("Method created was invoked");
        Avatar avatar = new Avatar();
        avatar.setData(data);
        avatar.setFileSize(size);
        avatar.setMediaType(contentType);
        return avatarRepository.save(avatar);
    }

    public Pair<String, byte[]> readAvatarFromDb(long id) {
        LOG.debug("Method readAvatarFromDb was invoked");
        Avatar avatar = avatarRepository.findById(id).orElseThrow(() -> {
            LOG.error("Avatar with id = {} not found", id);
            return new AvatarNotFoundException(id);
        });
        return Pair.of(avatar.getMediaType(), avatar.getData());
    }

    public Pair<String, byte[]> readAvatarFromFs(long id) throws IOException {
        LOG.debug("Method readAvatarFromFs was invoked");
        Avatar avatar = avatarRepository.findById(id).orElseThrow(() -> {
            LOG.error("Avatar with id = {} not found", id);
            return new AvatarNotFoundException(id);
        });
        return Pair.of(avatar.getMediaType(), Files.readAllBytes(Paths.get(avatar.getFilePath())));
    }

    public List<AvatarRecord> findByPagination(int page, int size){
        LOG.debug("Method findByPagination was invoked");
        return avatarRepository.findAll(PageRequest.of(page, size)).get()
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }
}
