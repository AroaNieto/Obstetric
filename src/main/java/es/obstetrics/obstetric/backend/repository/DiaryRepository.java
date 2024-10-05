package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {
    List<DiaryEntity> findBySundayTrue();

    List<DiaryEntity> findBySaturdayTrue();

    List<DiaryEntity> findByFridayTrue();

    List<DiaryEntity> findByThursdayTrue();

    List<DiaryEntity> findByWednesdayTrue();

    List<DiaryEntity> findByTuesdayTrue();

    List<DiaryEntity> findByMondayTrue();

    List<DiaryEntity> findBySanitaryEntityAndCenterEntity(SanitaryEntity sanitary, CenterEntity center);

    List<DiaryEntity> findBySanitaryEntityAndCenterEntityAndState(SanitaryEntity value, CenterEntity value1, String stateActive);


    List<DiaryEntity> findBySanitaryEntityAndStateAndMonday(UserEntity currentUser, String stateActive, boolean b);

    List<DiaryEntity> findBySanitaryEntityAndStateAndSunday(UserEntity currentUser, String stateActive, boolean b);

    List<DiaryEntity> findBySanitaryEntityAndStateAndSaturday(UserEntity currentUser, String stateActive, boolean b);

    List<DiaryEntity> findBySanitaryEntityAndStateAndFriday(UserEntity currentUser, String stateActive, boolean b);

    List<DiaryEntity> findBySanitaryEntityAndStateAndThursday(UserEntity currentUser, String stateActive, boolean b);

    List<DiaryEntity> findBySanitaryEntityAndStateAndWednesday(UserEntity currentUser, String stateActive, boolean b);

    List<DiaryEntity> findBySanitaryEntityAndStateAndTuesday(UserEntity currentUser, String stateActive, boolean b);

    Page<DiaryEntity> findByMonday(Boolean filter, PageRequest of);

    Page<DiaryEntity> findByTuesday(Boolean filter, PageRequest of);

    Page<DiaryEntity> findByWednesday(Boolean filter, PageRequest of);

    Page<DiaryEntity> findByThursday(Boolean filter, PageRequest of);

    Page<DiaryEntity> findByFriday(Boolean filter, PageRequest of);

    Page<DiaryEntity> findBySunday(Boolean filter, PageRequest of);

    Page<DiaryEntity> findBySaturday(Boolean filter, PageRequest of);

    Page<DiaryEntity> findByCenterEntityCenterNameContaining(String filter, PageRequest of);

    Page<DiaryEntity> findBySanitaryEntityNameContaining(String filter, PageRequest of);

    Page<DiaryEntity> findByStartTime(LocalDate filter, PageRequest of);

    List<DiaryEntity> findBySanitaryEntity(SanitaryEntity sanitary);

    List<DiaryEntity> findBySanitaryEntityAndState(SanitaryEntity sanitaryEntity, String stateActive);
}
