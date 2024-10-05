package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class CenterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String municipality;
    private String province;
    private String autonomousComunity;
    private String address;
    private String centerName;
    private String postalCode;
    private String phone;
    private String email;

    @ManyToMany(mappedBy = "centers", fetch = FetchType.EAGER)
    private List<InsuranceEntity> insurances = new ArrayList<>();

    @OneToMany(mappedBy = "centerEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<DiaryEntity> diaries = new ArrayList<>();

    @ManyToMany(mappedBy = "centers", fetch = FetchType.EAGER)
    private List<SanitaryEntity> sanitaryEntities = new ArrayList<>();

    @Override
    public String toString() {
        return centerName;
    }

}
