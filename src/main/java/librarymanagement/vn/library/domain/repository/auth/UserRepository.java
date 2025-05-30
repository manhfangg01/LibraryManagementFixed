package librarymanagement.vn.library.domain.repository.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import librarymanagement.vn.library.domain.model.auth.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    public Optional<User> findByUsername(String username);

    public Optional<User> findByEmail(String email);

}
