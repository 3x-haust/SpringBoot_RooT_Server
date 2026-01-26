package io.github._3xhaust.root_server.domain.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_grades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Short gradeLevel;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer requiredPoints;

    @Builder
    public CommunityGrade(Short gradeLevel, String name, Integer requiredPoints) {
        this.gradeLevel = gradeLevel;
        this.name = name;
        this.requiredPoints = requiredPoints;
    }
}
