package io.github._3xhaust.root_server.domain.community.entity;

import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "communities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Short gradeLevel;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityChannel> channels = new ArrayList<>();

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityTag> tags = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public Community(User owner, String name, String description, Integer points, Short gradeLevel) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.points = points != null ? points : 0;
        this.gradeLevel = gradeLevel != null ? gradeLevel : 1;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.points == null) {
            this.points = 0;
        }
        if (this.gradeLevel == null) {
            this.gradeLevel = 1;
        }
    }

    public void update(String name, String description) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
    }

    public void addPoints(Integer points) {
        this.points += points;
    }

    public void updateGradeLevel(Short gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
}
