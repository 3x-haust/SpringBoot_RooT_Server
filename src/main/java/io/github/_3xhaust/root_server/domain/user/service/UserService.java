package io.github._3xhaust.root_server.domain.user.service;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.garagesale.repository.FavoriteGarageSaleRepository;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.product.repository.FavoriteUsedItemRepository;
import io.github._3xhaust.root_server.domain.user.dto.UserDTO;
import io.github._3xhaust.root_server.domain.user.dto.req.ChangePasswordRequestDTO;
import io.github._3xhaust.root_server.domain.user.dto.req.UpdateUserRequestDTO;
import io.github._3xhaust.root_server.domain.user.dto.res.FavoritesResponse;
import io.github._3xhaust.root_server.domain.user.dto.res.UserResponseDTO;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.domain.image.entity.Image;
import io.github._3xhaust.root_server.domain.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FavoriteUsedItemRepository favoriteUsedItemRepository;
    private final FavoriteGarageSaleRepository favoriteGarageSaleRepository;
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponseDTO.of(toDTO(user)))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        return toDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByName(String name) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        return toDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "id=" + id));

        return toDTO(user);
    }

    @Transactional(readOnly = true)
    public FavoritesResponse getFavorites(String name) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        List<Product> favoriteProducts = favoriteUsedItemRepository.findProductsByUserId(user.getId());
        List<GarageSale> favoriteGarageSales = favoriteGarageSaleRepository.findGarageSalesByUserId(user.getId());

        return FavoritesResponse.builder()
                .products(favoriteProducts.stream()
                        .map(ProductListResponse::of)
                        .toList())
                .garageSales(favoriteGarageSales.stream()
                        .map(GarageSaleListResponse::of)
                        .toList())
                .build();
    }

    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "id=" + id));

        if (requestDTO.getName() != null && !requestDTO.getName().equals(user.getName())) {
            if (userRepository.existsByName(requestDTO.getName())) {
                throw new UserException(UserErrorCode.NAME_DUPLICATED, "name=" + requestDTO.getName());
            }
        }

        Image profileImage = null;
        if (requestDTO.getProfileImageId() != null) {
            profileImage = imageRepository.findById(requestDTO.getProfileImageId()).orElse(null);
        }

        user.updateProfile(
                requestDTO.getName(),
                requestDTO.getLanguage(),
                profileImage
        );

        return toDTO(user);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "id=" + id));

        if (!user.checkPassword(requestDTO.getCurrentPassword(), passwordEncoder)) {
            throw new UserException(UserErrorCode.PASSWORD_MISMATCH, "id=" + id);
        }

        String encodedNewPassword = passwordEncoder.encode(requestDTO.getNewPassword());
        user.updatePassword(encodedNewPassword);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND, "id=" + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean checkNameAvailability(String name) {
        return !userRepository.existsByName(name);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .rating(user.getRating())
                .language(user.getLanguage())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
