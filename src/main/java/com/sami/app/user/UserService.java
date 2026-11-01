package com.sami.app.user;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sami.app.account.Account;
import com.sami.app.account.AccountRepository;
import com.sami.app.account.Role;
import com.sami.app.common.exception.ConflictException;
import com.sami.app.common.exception.ResourceNotFoundException;
import com.sami.app.common.web.PageResponse;
import com.sami.app.user.dto.StaffCreateRequest;
import com.sami.app.user.dto.UserResponse;
import com.sami.app.user.dto.UserUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public PageResponse<UserResponse> findAll(Role role, Pageable pageable) {
        Page<User> page = role == null
                ? userRepository.findAllBy(pageable)
                : userRepository.findByAccount_Role(role, pageable);
        return PageResponse.of(page, userMapper::toResponse);
    }

    public UserResponse findById(Long id) {
        return userMapper.toResponse(getWithAccount(id));
    }

    public UserResponse findByAccount(UUID accountId) {
        return userMapper.toResponse(getByAccount(accountId));
    }

    @Transactional
    public UserResponse createStaff(StaffCreateRequest request) {
        User user = createWithAccount(request.email(), request.password(), request.role(),
                User.builder()
                        .firstName(request.firstName().trim())
                        .lastName(request.lastName().trim())
                        .phone(request.phone())
                        .rhType(request.rhType())
                        .build());
        return userMapper.toResponse(user);
    }

    /**
     * Crea la cuenta y su perfil en una sola transacción.
     */
    @Transactional
    public User createWithAccount(String email, String rawPassword, Role role, User profile) {
        String normalizedEmail = email.trim().toLowerCase();
        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("El email ya está registrado");
        }
        Account account = accountRepository.save(Account.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build());
        profile.setAccount(account);
        return userRepository.save(profile);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getWithAccount(id);
        userMapper.update(user, request);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateByAccount(UUID accountId, UserUpdateRequest request) {
        User user = getByAccount(accountId);
        userMapper.update(user, request);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getWithAccount(id);
        Account account = user.getAccount();
        userRepository.delete(user);
        accountRepository.delete(account);
    }

    public User getWithAccount(Long id) {
        return userRepository.findWithAccountById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    public User getByAccount(UUID accountId) {
        return userRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no tiene perfil de usuario"));
    }
}
