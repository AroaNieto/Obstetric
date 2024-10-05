package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.repository.DiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DiaryService {

    @Autowired
    private DiaryRepository diaryRepository;

    public DiaryService(DiaryRepository diaryRepository){
        this.diaryRepository = diaryRepository;
    }
    public List<DiaryEntity> findAll(){
        return diaryRepository.findAll();
    }

    public Page<DiaryEntity> findAll(int page, int size) {
        return diaryRepository.findAll(PageRequest.of(page, size));
    }

    public void save(DiaryEntity diaryEntity){
        diaryRepository.save(diaryEntity);
    }

    public void delete(DiaryEntity diaryEntity){
        diaryRepository.delete(diaryEntity);
    }

    public List<DiaryEntity> findByMondayTrue() {
        return diaryRepository.findByMondayTrue();
    }

    public List<DiaryEntity> findByTuesdayTrue() {
        return diaryRepository.findByTuesdayTrue();
    }

    public List<DiaryEntity> findByWednesdayTrue() {
        return diaryRepository.findByWednesdayTrue();
    }

    public List<DiaryEntity> findByThursdayTrue() {
        return diaryRepository.findByThursdayTrue();
    }

    public List<DiaryEntity> findByFridayTrue() {
        return diaryRepository.findByFridayTrue();
    }

    public List<DiaryEntity> findBySaturdayTrue() {
        return diaryRepository.findBySaturdayTrue();
    }

    public List<DiaryEntity> findBySundayTrue() {
        return diaryRepository.findBySundayTrue();
    }

    public List<DiaryEntity> findBySanitaryEntityAndCenterEntity(SanitaryEntity sanitary, CenterEntity center) {
        return diaryRepository.findBySanitaryEntityAndCenterEntity(sanitary, center);
    }

    public Optional<DiaryEntity> findById(long id) {
        return diaryRepository.findById(id);
    }

    public List<DiaryEntity> findBySanitaryEntityAndCenterEntityAndState(SanitaryEntity value, CenterEntity value1, String stateActive) {
        return diaryRepository.findBySanitaryEntityAndCenterEntityAndState(value,value1,stateActive);
    }

    public List<DiaryEntity> findBySanitaryEntityAndStateAndMonday(UserEntity currentUser, String stateActive, boolean b) {
        return diaryRepository.findBySanitaryEntityAndStateAndMonday(currentUser, stateActive,b);
    }

    public List<DiaryEntity> findBySanitaryEntityAndStateAndTuesday(UserEntity currentUser, String stateActive, boolean b) {
        return diaryRepository.findBySanitaryEntityAndStateAndTuesday(currentUser,stateActive,b);
    }

    public List<DiaryEntity> findBySanitaryEntityAndStateAndWednesday(UserEntity currentUser, String stateActive, boolean b) {
        return diaryRepository.findBySanitaryEntityAndStateAndWednesday(currentUser,stateActive,b);
    }

    public List<DiaryEntity> findBySanitaryEntityAndStateAndThursday(UserEntity currentUser, String stateActive, boolean b) {
        return diaryRepository.findBySanitaryEntityAndStateAndThursday(currentUser,stateActive,b);
    }

    public List<DiaryEntity> findBySanitaryEntityAndStateAndFriday(UserEntity currentUser, String stateActive, boolean b) {
        return diaryRepository.findBySanitaryEntityAndStateAndFriday(currentUser,stateActive,b);
    }

    public List<DiaryEntity> findBySanitaryEntityAndStateAndSaturday(UserEntity currentUser, String stateActive, boolean b) {
        return diaryRepository.findBySanitaryEntityAndStateAndSaturday(currentUser,stateActive,b);
    }

    public List<DiaryEntity> findBySanitaryEntityAndStateAndSunday(UserEntity currentUser, String stateActive, boolean b) {
        return diaryRepository.findBySanitaryEntityAndStateAndSunday(currentUser,stateActive,b);
    }

    public Page<DiaryEntity> findByStartTime(LocalDate filter, int i, int limit) {
        return diaryRepository.findByStartTime(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findByMonday(Boolean filter, int i, int limit) {
        return diaryRepository.findByMonday(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findByTuesday(Boolean filter, int i, int limit) {
        return diaryRepository.findByTuesday(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findByWednesday(Boolean filter, int i, int limit) {
        return diaryRepository.findByWednesday(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findByThursday(Boolean filter, int i, int limit) {
        return diaryRepository.findByThursday(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findByFriday(Boolean filter, int i, int limit) {
        return diaryRepository.findByFriday(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findBySunday(Boolean filter, int i, int limit) {
        return diaryRepository.findBySunday(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findBySaturday(Boolean filter, int i, int limit) {
        return diaryRepository.findBySaturday(filter, PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findBySanitaryEntityNameContaining(String filter, int i, int limit) {
        return diaryRepository.findBySanitaryEntityNameContaining(filter,PageRequest.of(i, limit));
    }

    public Page<DiaryEntity> findByCenterEntityContaining(String filter, int offset, int limit) {
        return diaryRepository.findByCenterEntityCenterNameContaining(filter,PageRequest.of(offset, limit));
    }

    public List<DiaryEntity> findBySanitaryEntity(SanitaryEntity sanitary) {
        return diaryRepository.findBySanitaryEntity(sanitary);
    }

    public List<DiaryEntity> findBySanitaryEntityAndState(SanitaryEntity sanitaryEntity, String stateActive) {
        return diaryRepository.findBySanitaryEntityAndState(sanitaryEntity,stateActive);
    }
}