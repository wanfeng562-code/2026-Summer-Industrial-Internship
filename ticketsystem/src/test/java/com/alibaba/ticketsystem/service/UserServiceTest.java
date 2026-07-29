package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.dto.RegisterRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.vo.UserProfileVo;
import com.alibaba.ticketsystem.vo.UserVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthSessionService authSessionService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper, passwordEncoder, authSessionService);
    }

    @Test
    void registerHashesPasswordAndForcesUserRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new_user");
        request.setPassword("123456");
        request.setNickname("新用户");
        request.setEmail("new@example.com");
        request.setPhone("13900000000");

        when(userMapper.usernameExists("new_user")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("$2b$10$encoded");
        doAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(9L);
            return 1;
        }).when(userMapper).insert(any(SysUser.class));

        UserProfileVo result = userService.register(request);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2b$10$encoded");
        assertThat(captor.getValue().getRole()).isEqualTo("USER");
        assertThat(result.getId()).isEqualTo(9L);
        assertThat(result.getUsername()).isEqualTo("new_user");
    }

    @Test
    void duplicateUsernameReturnsConflict() {
        when(userMapper.usernameExists("existing")).thenReturn(true);
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("123456");
        request.setNickname("重复用户");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(userMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void loginWithBcryptReturnsFormalAndCompatibleFields() {
        SysUser user = activeUser(4L, "user_wang", "USER",
                "$2b$10$12345678901234567890123456789012345678901234567890123");
        LoginRequest request = loginRequest("user_wang", "123456");
        when(userMapper.getUserByUsername("user_wang")).thenReturn(user);
        when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);
        when(authSessionService.getTokenValue()).thenReturn("token-value");
        when(authSessionService.getRoleList()).thenReturn(List.of("USER"));
        when(authSessionService.getPermissionList()).thenReturn(List.of("order:query", "ticket:query"));

        UserVo result = userService.loginSysUser(request);

        verify(authSessionService).login(4L);
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getUserId()).isEqualTo(4L);
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getRoles()).containsExactly("USER");
        assertThat(result.getToken()).isEqualTo("token-value");
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void legacyPlainPasswordIsUpgradedAfterSuccessfulLogin() {
        SysUser user = activeUser(4L, "user_wang", "USER", "123456");
        when(userMapper.getUserByUsername("user_wang")).thenReturn(user);
        when(passwordEncoder.encode("123456")).thenReturn("$2b$10$upgraded");
        when(authSessionService.getRoleList()).thenReturn(List.of("USER"));
        when(authSessionService.getPermissionList()).thenReturn(List.of());

        userService.loginSysUser(loginRequest("user_wang", "123456"));

        assertThat(user.getPassword()).isEqualTo("$2b$10$upgraded");
        verify(userMapper).updateById(user);
        verify(authSessionService).login(4L);
    }

    @Test
    void invalidPasswordReturnsUnauthorized() {
        SysUser user = activeUser(4L, "user_wang", "USER",
                "$2b$10$12345678901234567890123456789012345678901234567890123");
        when(userMapper.getUserByUsername("user_wang")).thenReturn(user);
        when(passwordEncoder.matches("wrong123", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.loginSysUser(loginRequest("user_wang", "wrong123")))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verify(authSessionService, never()).login(any());
    }

    @Test
    void deletedAccountCannotLoginEvenWithCorrectPassword() {
        SysUser user = activeUser(4L, "deleted_user", "USER", "123456");
        user.setDeleted(1);
        when(userMapper.getUserByUsername("deleted_user")).thenReturn(user);

        assertThatThrownBy(() -> userService.loginSysUser(loginRequest("deleted_user", "123456")))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verify(authSessionService, never()).login(any());
        verify(userMapper, never()).updateById(any(SysUser.class));
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private SysUser activeUser(Long id, String username, String role, String password) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        user.setRole(role);
        user.setPassword(password);
        user.setDeleted(0);
        return user;
    }
}
