package io.github._3xhaust.root_server.infrastructure.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.util.List;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/product-settings.json")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long productId;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @Field(type = FieldType.Text)
    private String sellerName;

    @Field(type = FieldType.Keyword)
    private String sellerEmail;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String title;

    @Field(type = FieldType.Integer)
    private Double price;

    @Field(type = FieldType.Integer)
    private Double sale;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String description;

    @Field(type = FieldType.Short)
    private Short type;

    @Field(type = FieldType.Long)
    private Long garageSaleId;

    @Field(type = FieldType.Double)
    private Double latitude;

    @Field(type = FieldType.Double)
    private Double longitude;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private List<String> imageUrls;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant createdAt;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    public static String generateId(Long productId) {
        return "product_" + productId;
    }

    public String getSellerUsername() {
        return this.sellerName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoPoint {
        private Double lat;
        private Double lon;
    }

    public static class ProductDocumentBuilder {
        public ProductDocumentBuilder sellerUsername(String sellerUsername) {
            this.sellerName = sellerUsername;
            return this;
        }
    }
}
