package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public void save(UserEntity userEntity){
            userRepository.save(userEntity);
    }

    public void delete(UserEntity userEntity) {
        userRepository.delete(userEntity);
    }


    public UserEntity findOneByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity findOneByPhone(String phone) {
        return userRepository.findOneByPhone(phone);
    }

    public UserEntity findOneByEmail(String email) {
        return userRepository.findOneByEmail(email);
    }

    public UserEntity findOneByDni(String dni) {
        return userRepository.findOneByDni(dni);
    }

    public List<UserEntity> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    public UserEntity findOneByDniAndRole(String dni, String rolePatient) {
        return userRepository.findOneByDniAndRole(dni, rolePatient);
    }
}
