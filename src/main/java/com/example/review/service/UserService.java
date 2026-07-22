package com.example.review.service;

import com.example.review.dto.UserForm;
import com.example.review.entity.Branch;
import com.example.review.entity.Role;
import com.example.review.entity.User;
import com.example.review.entity.UserStatus;
import com.example.review.repository.BranchRepository;
import com.example.review.repository.RoleRepository;
import com.example.review.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc();
    }

    public User getById(Long id) {

        if (id == null) {
            throw new RuntimeException(
                    "ID tài khoản không được để trống"
            );
        }

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy tài khoản với ID: " + id
                        )
                );
    }

    @Transactional
    public void createUser(UserForm form) {

        if (form == null) {
            throw new RuntimeException(
                    "Thông tin tài khoản không hợp lệ"
            );
        }

        if (
                form.getUsername() == null
                || form.getUsername().trim().isEmpty()
        ) {
            throw new RuntimeException(
                    "Tên đăng nhập không được để trống"
            );
        }

        if (
                form.getFullName() == null
                || form.getFullName().trim().isEmpty()
        ) {
            throw new RuntimeException(
                    "Họ tên không được để trống"
            );
        }

        if (
                form.getPassword() == null
                || form.getPassword().length() < 6
        ) {
            throw new RuntimeException(
                    "Mật khẩu phải có ít nhất 6 ký tự"
            );
        }

        if (form.getRoleId() == null) {
            throw new RuntimeException(
                    "Vui lòng chọn vai trò"
            );
        }

        String username = form.getUsername().trim();

        if (
                userRepository.existsByUsernameIgnoreCase(
                        username
                )
        ) {
            throw new RuntimeException(
                    "Tên đăng nhập đã tồn tại"
            );
        }

        Role role = roleRepository
                .findById(form.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy vai trò"
                        )
                );

        User user = new User();

        user.setFullName(
                form.getFullName().trim()
        );

        user.setUsername(username);

        user.setPassword(
                passwordEncoder.encode(
                        form.getPassword()
                )
        );

        user.setRole(role);

        user.setStatus(
                form.getStatus() != null
                        ? form.getStatus()
                        : UserStatus.ACTIVE
        );

        String roleName = role.getName();

        if (
                roleName != null
                && roleName.equalsIgnoreCase("MANAGER")
        ) {
            user.setBranch(null);

        } else {

            if (form.getBranchId() == null) {
                throw new RuntimeException(
                        "Leader và Employee phải thuộc một chi nhánh"
                );
            }

            Branch branch = branchRepository
                    .findById(form.getBranchId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Không tìm thấy chi nhánh"
                            )
                    );

            user.setBranch(branch);
        }

        userRepository.save(user);
    }

    @Transactional
    public void toggleStatus(Long id) {

        User user = getById(id);

        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(
            Long id,
            String newPassword
    ) {

        if (
                newPassword == null
                || newPassword.length() < 6
        ) {
            throw new RuntimeException(
                    "Mật khẩu mới phải có ít nhất 6 ký tự"
            );
        }

        User user = getById(id);

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }
}