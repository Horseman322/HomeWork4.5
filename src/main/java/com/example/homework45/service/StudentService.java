package com.example.homework44.service;



import com.example.homework44.component.RecordMapper;
import com.example.homework44.entity.Avatar;
import com.example.homework44.entity.Faculty;
import com.example.homework44.entity.Student;
import com.example.homework44.exception.AvatarNotFoundException;
import com.example.homework44.exception.FacultyNotFoundException;
import com.example.homework44.exception.StudentNotFoundException;
import com.example.homework44.record.FacultyRecord;
import com.example.homework44.record.StudentRecord;
import com.example.homework44.repository.AvatarRepository;
import com.example.homework44.repository.FacultyRepository;
import com.example.homework44.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Service
public class StudentService {

    private static final Logger LOG = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    private final AvatarRepository avatarRepository;
    private final RecordMapper recordMapper;

    public  StudentService(StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           AvatarRepository avatarRepository,
                          RecordMapper recordMapper) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.avatarRepository = avatarRepository;
        this.recordMapper = recordMapper;
    }



    public StudentRecord create(StudentRecord studentRecord){
        LOG.debug("Method read was invoked");
        Student student = recordMapper.toEntity(studentRecord);
        student.setFaculty(
        Optional.ofNullable(student.getFaculty())
                .map(Faculty::getId)
                .flatMap(facultyRepository::findById)
                .orElse(null));
        return recordMapper.toRecord(studentRepository.save(student));
    }

    public StudentRecord read(long id){
        LOG.debug("Method read was invoked");
        return recordMapper.toRecord(studentRepository.findById(id).orElseThrow(() -> {
            LOG.error("Student with id = {} not found", id);
            return new StudentNotFoundException(id);
        }));
    }

    public StudentRecord update(long id,
                                StudentRecord studentRecord){
        LOG.debug("Method update was invoked");
        Student oldStudent = studentRepository.findById(id).orElseThrow(() -> {
            LOG.error("Student with id = {} not found", id);
            return new StudentNotFoundException(id);
        });
        oldStudent.setAge(studentRecord.getAge());
        oldStudent.setName(studentRecord.getName());
        return recordMapper.toRecord(studentRepository.save(oldStudent));
    }

    public StudentRecord delete(long id){
        LOG.debug("Method delete was invoked");
        Student student = studentRepository.findById(id).orElseThrow(() -> {
            LOG.error("Student with id = {} not found", id);
            return new StudentNotFoundException(id);
        });
        studentRepository.delete(student);
        return recordMapper.toRecord(student);
    }

    public Collection<StudentRecord> findByAge(int age){
        LOG.debug("Method findByAge was invoked");
        return studentRepository.findAllByAge(age).stream()
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }


    public Collection<StudentRecord> findByAgeBetween(int minAge, int maxAge) {
        LOG.debug("Method findByAgeBetween was invoked");
        return studentRepository.findAllByAgeBetween(minAge, maxAge).stream()
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }

    public FacultyRecord getFacultyByStudent(long id) {
        LOG.debug("Method getFacultyByStudent was invoked");
        return read(id).getFaculty();
    }

    public StudentRecord patchStudentAvatar(long id, long avatarId) {
        LOG.debug("Method patchStudentAvatar was invoked");
        Optional<Student> optionalStudent = studentRepository.findById(id);
        Optional<Avatar> optionalAvatar = avatarRepository.findById(avatarId);
        if (optionalStudent.isEmpty()){
            LOG.error("Student with id = {} not found", id);
            throw new StudentNotFoundException(id);
        }
        if (optionalAvatar.isEmpty()){
            LOG.error("Avatar with id = {} not found", avatarId);
            throw new AvatarNotFoundException(avatarId);
        }
        Student student = optionalStudent.get();
        student.setAvatar(optionalAvatar.get());
        return recordMapper.toRecord(studentRepository.save(studentRepository.save(student)));
    }

    public int totalCountOfStudents() {
        LOG.debug("Method totalCountOfStudents was invoked");
        return studentRepository.totalCountOfStudents();
    }

    public double averageAgeOfStudents() {
        LOG.debug("Method averageAgeOfStudents was invoked");
        return studentRepository.averageAgeOfStudents();
    }


    public List<StudentRecord> lastStudents(int count) {
        LOG.debug("Method lastStudents was invoked");
        return studentRepository.lastStudents(count).stream()
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }
}
