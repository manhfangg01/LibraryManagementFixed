package librarymanagement.vn.library.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import librarymanagement.vn.library.domain.model.auth.Role;
import librarymanagement.vn.library.domain.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> fetchAllRoles() {
        return this.roleRepository.findAll();
    }

    public Optional<Role> fetchRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }

}
