package io.github._3xhaust.root_server.domain.user.controller;

import io.github._3xhaust.root_server.domain.user.dto.UserDTO;
import io.github._3xhaust.root_server.domain.user.dto.req.ChangePasswordRequestDTO;
import io.github._3xhaust.root_server.domain.user.dto.req.UpdateUserRequestDTO;
import io.github._3xhaust.root_server.domain.user.dto.res.FavoritesResponse;
import io.github._3xhaust.root_server.domain.user.dto.res.UserResponseDTO;
import io.github._3xhaust.root_server.domain.user.service.UserService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ApiResponse.ok(users);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponseDTO> getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDTO user = userService.getUserByName(userDetails.getUsername());
        UserResponseDTO userDTO = UserResponseDTO.of(user);
        return ApiResponse.ok(userDTO);
    }

    @GetMapping("/me/favorites")
    public ApiResponse<FavoritesResponse> getMyFavorites(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FavoritesResponse favorites = userService.getFavorites(userDetails.getUsername());
        return ApiResponse.ok(favorites);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        UserResponseDTO userDTO = UserResponseDTO.of(user);
        return ApiResponse.ok(userDTO);
    }

    @PutMapping("/me")
    public ApiResponse<UserResponseDTO> updateUser(
            Authentication authentication,
            @RequestBody @Valid UpdateUserRequestDTO updateUserRequestDTO
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long id = userService.getUserByName(userDetails.getUsername()).getId();
        UserDTO user = userService.updateUser(id, updateUserRequestDTO);
        UserResponseDTO userDTO = UserResponseDTO.of(user);
        return ApiResponse.ok(userDTO);
    }

    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @RequestBody @Valid ChangePasswordRequestDTO changePasswordRequestDTO
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long id = userService.getUserByName(userDetails.getUsername()).getId();
        userService.changePassword(id, changePasswordRequestDTO);
        return ApiResponse.ok((Void) null, "Password changed successfully");
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long id = userService.getUserByName(userDetails.getUsername()).getId();
        userService.deleteUser(id);
        return ApiResponse.ok((Void) null, "User deleted successfully");
    }

    @GetMapping("/check-name")
    public ApiResponse<Boolean> checkNameAvailability(@RequestParam String name) {
        boolean isAvailable = userService.checkNameAvailability(name);
        return ApiResponse.ok(isAvailable);
    }
}
