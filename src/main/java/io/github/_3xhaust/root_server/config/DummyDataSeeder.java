package io.github._3xhaust.root_server.config;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import io.github._3xhaust.root_server.domain.community.entity.CommunityChannel;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPost;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPostImage;
import io.github._3xhaust.root_server.domain.community.repository.CommunityChannelRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityPostImageRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityPostRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityRepository;
import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSaleImage;
import io.github._3xhaust.root_server.domain.garagesale.repository.GarageSaleImageRepository;
import io.github._3xhaust.root_server.domain.garagesale.repository.GarageSaleRepository;
import io.github._3xhaust.root_server.domain.image.entity.Image;
import io.github._3xhaust.root_server.domain.image.repository.ImageRepository;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.product.entity.ProductImage;
import io.github._3xhaust.root_server.domain.product.repository.ProductImageRepository;
import io.github._3xhaust.root_server.domain.product.repository.ProductRepository;
import io.github._3xhaust.root_server.domain.tag.entity.GarageSaleTag;
import io.github._3xhaust.root_server.domain.tag.entity.GarageSaleTagId;
import io.github._3xhaust.root_server.domain.tag.entity.ProductTag;
import io.github._3xhaust.root_server.domain.tag.entity.ProductTagId;
import io.github._3xhaust.root_server.domain.tag.entity.Tag;
import io.github._3xhaust.root_server.domain.tag.repository.GarageSaleTagRepository;
import io.github._3xhaust.root_server.domain.tag.repository.ProductTagRepository;
import io.github._3xhaust.root_server.domain.tag.repository.TagRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.service.ElasticsearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {
    private static final String MOBILE_ASSET_BASE_URL =
            "https://raw.githubusercontent.com/3x-haust/Flutter_RooT_Mobile/main/";

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final GarageSaleRepository garageSaleRepository;
    private final GarageSaleImageRepository garageSaleImageRepository;
    private final CommunityRepository communityRepository;
    private final CommunityChannelRepository communityChannelRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostImageRepository communityPostImageRepository;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final GarageSaleTagRepository garageSaleTagRepository;
    private final PasswordEncoder passwordEncoder;
    private final ElasticsearchIndexService elasticsearchIndexService;

    @Value("${root.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${root.seed.elasticsearch.enabled:false}")
    private boolean seedElasticsearchEnabled;

    @Value("${image.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        User owner = userRepository.findByEmail("demo@root.local")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("demo@root.local")
                        .password(passwordEncoder.encode("root1234"))
                        .name("Blue Day")
                        .rating((short) 70)
                        .language("en")
                        .build()));

        refreshSeedImageUrls();
        seedTradeProducts(owner);
        seedGarageSales(owner);
        seedCommunities(owner);
        refreshSeedImageUrls();
        reindexSeedDocuments();
    }

    private void refreshSeedImageUrls() {
        seedImages().forEach((filename, asset) -> {
            copyAsset(asset, filename);
            String expectedUrl = localImageUrl(filename);
            List<String> previousUrls = List.of(
                    externalAssetUrl(asset),
                    fallbackImageUrl(filename)
            );

            for (String previousUrl : previousUrls) {
                imageRepository.findByUrl(previousUrl).ifPresent(image -> {
                    if (imageRepository.findByUrl(expectedUrl).isEmpty()) {
                        image.updateUrl(expectedUrl);
                    }
                });
            }
        });
    }

    private void reindexSeedDocuments() {
        if (!seedElasticsearchEnabled) {
            return;
        }

        seedProductTitles().forEach(title ->
                productRepository.findFirstByTitle(title).ifPresent(elasticsearchIndexService::indexProduct)
        );
        garageSaleRepository.findAll().stream()
                .filter(garageSale -> "Seoul Weekend Garage Sale".equals(garageSale.getName()))
                .findFirst()
                .ifPresent(elasticsearchIndexService::indexGarageSale);
    }

    private void seedTradeProducts(User owner) {
        List<Product> products = List.of(
                product(owner, "Fresh Apple", 15.0, null, "Fresh local apples in a basket.", "Crisp red apples picked this week. Great for snacks, baking, or sharing.", "assets/images/search_figma/trade_apple_0.png", "seed_trade_apple_0.png", List.of("Apple", "Fresh", "Food")),
                product(owner, "Sweet apples", 5.0, null, "Small bag of sweet apples.", "A sweet batch of apples with firm texture and bright color.", "assets/images/search_figma/trade_apple_1.png", "seed_trade_apple_1.png", List.of("Apple", "Fruit", "Local")),
                product(owner, "Green apple", 10.0, null, "Tart green apples.", "Green apples with a clean tart flavor. Perfect for salads or juice.", "assets/images/search_figma/trade_apple_2.png", "seed_trade_apple_2.png", List.of("Apple", "Green", "Fruit")),
                product(owner, "Apples today", 11.0, null, "Fresh apples available today.", "Fresh apples packed today near Hongdae. Pickup preferred.", "assets/images/search_figma/trade_apple_3.png", "seed_trade_apple_3.png", List.of("Apple", "Today", "Fresh")),
                product(owner, "Sweet local apples", 20.0, null, "Local apples in good condition.", "A larger box of sweet local apples, clean and ready to eat.", "assets/images/search_figma/trade_apple_4.png", "seed_trade_apple_4.png", List.of("Apple", "Local", "Good Condition")),
                product(owner, "Homegrown apples", 12.0, null, "Homegrown apples.", "Homegrown apples from a small garden. Natural sizes and very fresh.", "assets/images/search_figma/trade_apple_3.png", "seed_trade_apple_5.png", List.of("Apple", "Homegrown", "Organic")),
                product(owner, "Crisp apples", 11.0, null, "Crisp apples with bright flavor.", "A crisp apple batch with a balanced sweet taste.", "assets/images/search_figma/trade_apple_4.png", "seed_trade_apple_6.png", List.of("Apple", "Crisp", "Fruit")),
                product(owner, "Organic apples", 5.0, null, "Organic apples.", "Organic apples with natural color variation and clean flavor.", "assets/images/search_figma/trade_apple_5.png", "seed_trade_apple_7.png", List.of("Apple", "Organic", "Fruit")),
                product(owner, "Black Hoodie", 15.0, 12.0, "Perfect condition black hoodie.", "This is a high-quality black hoodie in excellent condition.", "assets/images/search_figma/rec_black_hoodie.png", "seed_rec_black_hoodie.png", List.of("Fashion", "Streetwear", "Clothing")),
                product(owner, "QUICK SALE!", 15.0, 12.0, "Quick sale item.", "This item is on quick sale.", "assets/images/search_figma/rec_quick_sale.png", "seed_rec_quick_sale.png", List.of("Sale", "Deal", "Local")),
                product(owner, "Wired Earphones", 15.0, null, "High-quality wired earphones.", "These wired earphones provide clear sound quality.", "assets/images/search_figma/rec_earphones.png", "seed_rec_earphones.png", List.of("Electronics", "Audio", "Tech")),
                product(owner, "Novel-1984", 15.0, null, "A used copy of 1984.", "A clean used copy of 1984 with light shelf wear.", "assets/images/search_figma/rec_novel.png", "seed_rec_novel.png", List.of("Book", "Novel", "Vintage")),
                product(owner, "Minecraft torch(in real)", 15.0, null, "Minecraft torch prop.", "A fun Minecraft torch prop for room decor or cosplay.", "assets/images/search_figma/rec_torch.png", "seed_rec_torch.png", List.of("Game", "Decor", "Minecraft")),
                product(owner, "CUTIE Airpodsss<3", 15.0, null, "Cute wireless earbuds case.", "A cute earbuds case and accessory set in good condition.", "assets/images/search_figma/rec_airpods.png", "seed_rec_airpods.png", List.of("Audio", "Accessories", "Cute")),
                product(owner, "Wrist watch", 15.0, null, "Simple wrist watch.", "A simple wrist watch with a clean face and comfortable strap.", "assets/images/search_figma/rec_watch.png", "seed_rec_watch.png", List.of("Watch", "Fashion", "Accessories")),
                product(owner, "Body lotion", 15.0, null, "Body lotion.", "Unopened body lotion with a light scent.", "assets/images/search_figma/rec_body_lotion.png", "seed_rec_body_lotion.png", List.of("Beauty", "Lotion", "Care"))
        );

        indexProductsIfEnabled(products);
    }

    private void seedGarageSales(User owner) {
        GarageSale sale = garageSaleRepository.findFirstByName("Seoul Weekend Garage Sale")
                .map(existing -> {
                    existing.update(
                            "Seoul Weekend Garage Sale",
                            37.5665,
                            126.9780,
                            LocalDate.now().plusDays(1),
                            LocalDate.now().plusDays(3),
                            LocalTime.of(8, 0),
                            LocalTime.of(15, 0)
                    );
                    return existing;
                })
                .orElseGet(() -> garageSaleRepository.save(GarageSale.builder()
                        .owner(owner)
                        .name("Seoul Weekend Garage Sale")
                        .latitude(37.5665)
                        .longitude(126.9780)
                        .startDate(LocalDate.now().plusDays(1))
                        .endDate(LocalDate.now().plusDays(3))
                        .startTime(LocalTime.of(8, 0))
                        .endTime(LocalTime.of(15, 0))
                        .build()));
        attachGarageImage(sale, "assets/images/search_figma/garage_sale_1.png", "seed_garage_sale_1.png");
        attachGarageTag(sale, "Furniture");
        attachGarageTag(sale, "Clothes");
        attachGarageTag(sale, "Good Condition");

        List<Product> garageProducts = List.of(
                garageProduct(owner, sale, "Metal Shelf", 15.0, "Compact metal shelf.", "A sturdy metal shelf that fits small rooms, kitchens, or garages.", "assets/images/search_figma/garage_item_0.png", "seed_garage_item_0.png", List.of("Shelf", "Garage", "Storage")),
                garageProduct(owner, sale, "Rustic Wood Rack", 12.0, "Rustic wood rack.", "A rustic wood rack with warm texture and practical shelf space.", "assets/images/search_figma/garage_item_1.png", "seed_garage_item_1.png", List.of("Shelf", "Wood", "Garage")),
                garageProduct(owner, sale, "Cozy Wood Storage", 20.0, "Cozy wood storage shelf.", "Wood storage shelf suited for books, plants, and display pieces.", "assets/images/search_figma/garage_item_2.png", "seed_garage_item_2.png", List.of("Shelf", "Wood", "Storage")),
                garageProduct(owner, sale, "Strong Wall Shelf", 18.0, "Strong wall shelf.", "A strong wall shelf for lightweight display and home storage.", "assets/images/search_figma/garage_item_3.png", "seed_garage_item_3.png", List.of("Shelf", "Wall", "Furniture")),
                garageProduct(owner, sale, "Wooden Space Saver", 13.0, "Wooden space saver.", "A slim wooden rack for tight spaces and simple organization.", "assets/images/search_figma/garage_item_3.png", "seed_garage_item_4.png", List.of("Shelf", "Wood", "Space Saver")),
                garageProduct(owner, sale, "Natural Wood Rack", 16.0, "Natural wood rack.", "Natural wood rack with a clean shape and warm finish.", "assets/images/search_figma/garage_item_4.png", "seed_garage_item_5.png", List.of("Shelf", "Wood", "Natural")),
                garageProduct(owner, sale, "Pure Wood Style", 12.0, "Pure wood style shelf.", "Simple wood style shelf with a minimal look.", "assets/images/search_figma/garage_item_5.png", "seed_garage_item_6.png", List.of("Shelf", "Wood", "Minimal")),
                garageProduct(owner, sale, "Elegant Wood Shelf", 18.0, "Elegant wood shelf.", "Elegant wood shelf for books, decor, and daily storage.", "assets/images/search_figma/garage_item_5.png", "seed_garage_item_7.png", List.of("Shelf", "Wood", "Furniture"))
        );

        garageProducts.forEach(product -> {
            sale.getProducts().add(product);
        });
        indexProductsIfEnabled(garageProducts);
        indexGarageSaleIfEnabled(sale);
    }

    private void indexProductsIfEnabled(List<Product> products) {
        if (!seedElasticsearchEnabled) {
            return;
        }

        products.forEach(elasticsearchIndexService::indexProduct);
    }

    private void indexGarageSaleIfEnabled(GarageSale garageSale) {
        if (!seedElasticsearchEnabled) {
            return;
        }

        elasticsearchIndexService.indexGarageSale(garageSale);
    }

    private void seedCommunities(User owner) {
        Community community = communityRepository.findFirstByName("All about Parrot")
                .orElseGet(() -> communityRepository.save(Community.builder()
                        .owner(owner)
                        .name("All about Parrot")
                        .description("Share parrot care, photos, and daily questions.")
                        .points(333)
                        .gradeLevel((short) 1)
                        .build()));

        CommunityChannel channel = communityChannelRepository.findFirstByCommunityIdAndName(community.getId(), "General")
                .orElseGet(() -> communityChannelRepository.save(CommunityChannel.builder()
                        .community(community)
                        .name("General")
                        .description("Parrot stories and care tips")
                        .type("PHOTO")
                        .build()));

        CommunityPost post = communityPostRepository.findFirstByTitle("Morning parrot routine")
                .orElseGet(() -> communityPostRepository.save(CommunityPost.builder()
                        .channel(channel)
                        .author(owner)
                        .title("Morning parrot routine")
                        .body("I've been living with my parrot for about a year now, and I'm still learning something new every day.")
                        .build()));
        attachPostImage(post, "assets/images/search_figma/community_post_parrot_0.png", "seed_community_post_parrot_0.png");

        CommunityPost secondPost = communityPostRepository.findFirstByTitle("Parrot walk today")
                .orElseGet(() -> communityPostRepository.save(CommunityPost.builder()
                        .channel(channel)
                        .author(owner)
                        .title("Parrot walk today")
                        .body("I took a walk with my cutieee parrot today, and it instantly lifted my mood.")
                        .build()));
        attachPostImage(secondPost, "assets/images/community_figma/post_image_1.png", "seed_community_post_1.png");
    }

    private Product product(User owner, String title, Double price, Double sale, String description, String body, String asset, String filename, List<String> tags) {
        Product product = productRepository.findFirstByTitle(title)
                .map(existing -> {
                    existing.update(title, price, sale, description, body, 37.5665, 126.9780);
                    return existing;
                })
                .orElseGet(() -> productRepository.save(Product.builder()
                .seller(owner)
                .title(title)
                .price(price)
                .sale(sale)
                .description(description)
                .body(body)
                .type((short) 0)
                .latitude(37.5665)
                .longitude(126.9780)
                .build()));
        attachProductImage(product, asset, filename);
        tags.forEach(tag -> attachProductTag(product, tag));
        return product;
    }

    private Product garageProduct(User owner, GarageSale garageSale, String title, Double price, String description, String body, String asset, String filename, List<String> tags) {
        Product product = productRepository.findFirstByTitle(title)
                .map(existing -> {
                    existing.update(title, price, null, description, body, null, null);
                    return existing;
                })
                .orElseGet(() -> productRepository.save(Product.builder()
                .seller(owner)
                .title(title)
                .price(price)
                .description(description)
                .body(body)
                .type((short) 1)
                .garageSale(garageSale)
                .build()));
        attachProductImage(product, asset, filename);
        tags.forEach(tag -> attachProductTag(product, tag));
        return product;
    }

    private void attachProductImage(Product product, String asset, String filename) {
        Image image = image(asset, filename);
        removePreviousSeedProductImage(product, asset, filename);
        if (productImageRepository.findByProductIdAndImageId(product.getId(), image.getId()) != null) {
            return;
        }
        ProductImage productImage = productImageRepository.save(ProductImage.builder()
                .product(product)
                .image(image)
                .build());
        product.addImage(productImage);
    }

    private void attachGarageImage(GarageSale garageSale, String asset, String filename) {
        Image image = image(asset, filename);
        removePreviousSeedGarageImage(garageSale, asset, filename);
        if (garageSaleImageRepository.findByGarageSaleIdAndImageId(garageSale.getId(), image.getId()) != null) {
            return;
        }
        GarageSaleImage garageSaleImage = garageSaleImageRepository.save(GarageSaleImage.builder()
                .garageSale(garageSale)
                .image(image)
                .build());
        garageSale.addImage(garageSaleImage);
    }

    private void attachPostImage(CommunityPost post, String asset, String filename) {
        Image image = image(asset, filename);
        boolean exists = communityPostImageRepository.findByPostId(post.getId()).stream()
                .anyMatch(postImage -> postImage.getImage().getId().equals(image.getId()));
        if (exists) {
            return;
        }
        CommunityPostImage postImage = communityPostImageRepository.save(CommunityPostImage.builder()
                .post(post)
                .image(image)
                .build());
        post.addImage(postImage);
    }

    private Image image(String asset, String filename) {
        String localUrl = localImageUrl(filename);
        String externalUrl = externalAssetUrl(asset);
        if (copyAsset(asset, filename)) {
            return imageRepository.findByUrl(localUrl)
                    .orElseGet(() -> imageRepository.save(Image.builder().url(localUrl).build()));
        }
        return imageRepository.findByUrl(externalUrl)
                .orElseGet(() -> imageRepository.save(Image.builder().url(externalUrl).build()));
    }

    private String localImageUrl(String filename) {
        return "/api/v1/images/" + filename;
    }

    private String externalAssetUrl(String asset) {
        return MOBILE_ASSET_BASE_URL + URLEncoder.encode(asset, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }

    private String fallbackImageUrl(String filename) {
        return "https://picsum.photos/seed/root-" + filename.replace(".", "-") + "/600/600";
    }

    private Map<String, String> seedImages() {
        return Map.ofEntries(
                Map.entry("seed_trade_apple_0.png", "assets/images/search_figma/trade_apple_0.png"),
                Map.entry("seed_trade_apple_1.png", "assets/images/search_figma/trade_apple_1.png"),
                Map.entry("seed_trade_apple_2.png", "assets/images/search_figma/trade_apple_2.png"),
                Map.entry("seed_trade_apple_3.png", "assets/images/search_figma/trade_apple_3.png"),
                Map.entry("seed_trade_apple_4.png", "assets/images/search_figma/trade_apple_4.png"),
                Map.entry("seed_trade_apple_5.png", "assets/images/search_figma/trade_apple_3.png"),
                Map.entry("seed_trade_apple_6.png", "assets/images/search_figma/trade_apple_4.png"),
                Map.entry("seed_trade_apple_7.png", "assets/images/search_figma/trade_apple_5.png"),
                Map.entry("seed_rec_black_hoodie.png", "assets/images/search_figma/rec_black_hoodie.png"),
                Map.entry("seed_rec_quick_sale.png", "assets/images/search_figma/rec_quick_sale.png"),
                Map.entry("seed_rec_earphones.png", "assets/images/search_figma/rec_earphones.png"),
                Map.entry("seed_rec_novel.png", "assets/images/search_figma/rec_novel.png"),
                Map.entry("seed_rec_torch.png", "assets/images/search_figma/rec_torch.png"),
                Map.entry("seed_rec_airpods.png", "assets/images/search_figma/rec_airpods.png"),
                Map.entry("seed_rec_watch.png", "assets/images/search_figma/rec_watch.png"),
                Map.entry("seed_rec_body_lotion.png", "assets/images/search_figma/rec_body_lotion.png"),
                Map.entry("seed_garage_sale_1.png", "assets/images/search_figma/garage_sale_1.png"),
                Map.entry("seed_garage_item_0.png", "assets/images/search_figma/garage_item_0.png"),
                Map.entry("seed_garage_item_1.png", "assets/images/search_figma/garage_item_1.png"),
                Map.entry("seed_garage_item_2.png", "assets/images/search_figma/garage_item_2.png"),
                Map.entry("seed_garage_item_3.png", "assets/images/search_figma/garage_item_3.png"),
                Map.entry("seed_garage_item_4.png", "assets/images/search_figma/garage_item_3.png"),
                Map.entry("seed_garage_item_5.png", "assets/images/search_figma/garage_item_4.png"),
                Map.entry("seed_garage_item_6.png", "assets/images/search_figma/garage_item_5.png"),
                Map.entry("seed_garage_item_7.png", "assets/images/search_figma/garage_item_5.png"),
                Map.entry("seed_community_post_parrot_0.png", "assets/images/search_figma/community_post_parrot_0.png"),
                Map.entry("seed_community_post_1.png", "assets/images/community_figma/post_image_1.png")
        );
    }

    private List<String> seedProductTitles() {
        return List.of(
                "Fresh Apple",
                "Sweet apples",
                "Green apple",
                "Apples today",
                "Sweet local apples",
                "Homegrown apples",
                "Crisp apples",
                "Organic apples",
                "Black Hoodie",
                "QUICK SALE!",
                "Wired Earphones",
                "Novel-1984",
                "Minecraft torch(in real)",
                "CUTIE Airpodsss<3",
                "Wrist watch",
                "Body lotion",
                "Metal Shelf",
                "Rustic Wood Rack",
                "Cozy Wood Storage",
                "Strong Wall Shelf",
                "Wooden Space Saver",
                "Natural Wood Rack",
                "Pure Wood Style",
                "Elegant Wood Shelf"
        );
    }

    private boolean copyAsset(String asset, String filename) {
        Path destination = Path.of(uploadDir, filename);
        Path source = Path.of("../root_mobile", asset);
        if (!Files.exists(source)) {
            source = Path.of(uploadDir, filename);
        }
        if (!Files.exists(source)) {
            return false;
        }
        try {
            Files.createDirectories(destination.getParent());
            if (!source.toAbsolutePath().normalize().equals(destination.toAbsolutePath().normalize())) {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void attachProductTag(Product product, String name) {
        Tag tag = tag(name, "product");
        ProductTagId id = new ProductTagId(product.getId(), tag.getId());
        if (productTagRepository.existsById(id)) {
            return;
        }
        productTagRepository.save(ProductTag.builder()
                .product(product)
                .tag(tag)
                .build());
    }

    private void attachGarageTag(GarageSale garageSale, String name) {
        Tag tag = tag(name, "garage");
        GarageSaleTagId id = new GarageSaleTagId(garageSale.getId(), tag.getId());
        if (garageSaleTagRepository.existsById(id)) {
            return;
        }
        garageSaleTagRepository.save(GarageSaleTag.builder()
                .garageSale(garageSale)
                .tag(tag)
                .build());
    }

    private Tag tag(String name, String category) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.builder()
                        .name(name)
                        .category(category)
                        .build()));
    }

    private void removePreviousSeedProductImage(Product product, String asset, String filename) {
        productImageRepository.findByProductId(product.getId()).stream()
                .filter(productImage -> isPreviousSeedUrl(productImage.getImage().getUrl(), asset, filename))
                .forEach(productImage -> {
                    product.removeImage(productImage);
                    productImageRepository.delete(productImage);
                });
    }

    private void removePreviousSeedGarageImage(GarageSale garageSale, String asset, String filename) {
        garageSale.getGarageSaleImages().stream()
                .filter(garageSaleImage -> isPreviousSeedUrl(garageSaleImage.getImage().getUrl(), asset, filename))
                .toList()
                .forEach(garageSaleImage -> {
                    garageSale.removeImage(garageSaleImage);
                    garageSaleImageRepository.delete(garageSaleImage);
                });
    }

    private boolean isPreviousSeedUrl(String url, String asset, String filename) {
        return externalAssetUrl(asset).equals(url) || fallbackImageUrl(filename).equals(url);
    }
}
