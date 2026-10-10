package com.example.mangareader.user;

import com.example.mangareader.auth.AuthenticatedUser;
import com.example.mangareader.common.exception.AppException;
import com.example.mangareader.common.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.ok(UserDto.from(load(principal)));
    }

    /**
     * NFR-04: penghapusan akun. Cascade FK menghapus library/progress milik user.
     */
    @DeleteMapping("/me")
    @Transactional
    public ApiResponse<Void> deleteMe(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = load(principal);
        userRepository.delete(user);
        return ApiResponse.ok(null);
    }

    private User load(AuthenticatedUser principal) {
        return userRepository.findById(principal.id())
                .orElseThrow(() -> AppException.notFound("Pengguna tidak ditemukan"));
    }
}
