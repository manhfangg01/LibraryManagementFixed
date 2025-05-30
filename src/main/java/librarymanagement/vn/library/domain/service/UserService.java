package librarymanagement.vn.library.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import librarymanagement.vn.library.domain.dto.UserFilterDTO;
import librarymanagement.vn.library.domain.dto.auth.RegisterUserDTO;
import librarymanagement.vn.library.domain.model.auth.User;
import librarymanagement.vn.library.domain.repository.auth.UserRepository;
import librarymanagement.vn.library.domain.service.specification.UserSpecs;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSpecs userSpecs;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserSpecs userSpecs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSpecs = userSpecs;
    }

    public Optional<User> fetchUserById(long id) {
        return this.userRepository.findById(id);
    }

    public Optional<User> fetchUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public Optional<User> fetchUserByName(String username) {
        return this.userRepository.findByUsername(username);
    }

    public List<User> fetchAllUsers() {
        return this.userRepository.findAll();
    }

    public void create(User user) {
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        this.userRepository.save(user);

    }

    public void update(User user) {
        User realUser = this.fetchUserById(user.getId()).get();
        realUser.setAddress(user.getAddress());
        realUser.setAge(user.getAge());
        realUser.setUsername(user.getUsername());
        realUser.setProfilePhoto(user.getProfilePhoto());
        this.userRepository.save(realUser);
    }

    public void delete(User user) {
        this.userRepository.deleteById(user.getId());
    }

    public User convertDTOToUser(RegisterUserDTO registerUserDTO) {
        User user = new User();
        user.setUsername(registerUserDTO.getUsername());
        user.setAddress(registerUserDTO.getAddress());
        user.setAge(registerUserDTO.getAge());
        user.setEmail(registerUserDTO.getEmail());
        user.setPassword(registerUserDTO.getPassword());
        return user;

    }

    public boolean existsByEmail(String email) {
        return this.fetchUserByEmail(email).isPresent();
    }

    public boolean existsByUsername(String name) {
        return this.fetchUserByName(name).isPresent();
    }

    public Page<User> fetchAllUserWithSpecificationAndPagination(UserFilterDTO userFilterDTO, Pageable pageable) {
        Specification<User> spec = Specification.where(null);
        Specification<User> spec1 = this.userSpecs.hasName(userFilterDTO.getUsername());
        Specification<User> spec2 = this.userSpecs.hasAddress(userFilterDTO.getAddress());
        Specification<User> spec3 = this.userSpecs.hasEmail(userFilterDTO.getEmail());
        Specification<User> spec4 = this.userSpecs.hasRole(userFilterDTO.getRole());
        Specification<User> spec5 = this.userSpecs.hasAge(userFilterDTO.getAge());
        spec = spec.and(spec1).and(spec2).and(spec3).and(spec4).and(spec5);
        return this.userRepository.findAll(spec, pageable);
    }

}
