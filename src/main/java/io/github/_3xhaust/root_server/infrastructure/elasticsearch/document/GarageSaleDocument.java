package io.github._3xhaust.root_server.infrastructure.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Document(indexName = "garage_sales")
@Setting(settingPath = "elasticsearch/garage-sale-settings.json")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageSaleDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long garageSaleId;

    @Field(type = FieldType.Long)
    private Long ownerId;

    @Field(type = FieldType.Text)
    private String ownerName;

    @Field(type = FieldType.Keyword)
    private String ownerEmail;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String name;

    @Field(type = FieldType.Double)
    private Double latitude;

    @Field(type = FieldType.Double)
    private Double longitude;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Date)
    private LocalDate startDate;

    @Field(type = FieldType.Date)
    private LocalDate endDate;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant startTime;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant endTime;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Integer)
    private Integer productCount;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant createdAt;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    public static String generateId(Long garageSaleId) {
        return "garage_sale_" + garageSaleId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoPoint {
        private Double lat;
        private Double lon;
    }
}

