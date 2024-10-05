package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.ScheduleEntity;
import es.obstetrics.obstetric.backend.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository){
        this.scheduleRepository = scheduleRepository;
    }

    public List<ScheduleEntity> findAll(){
        return scheduleRepository.findAll();
    }

    public void save(ScheduleEntity scheduleEntity){
        scheduleRepository.save(scheduleEntity);
    }

    public void delete(ScheduleEntity scheduleEntity){
        scheduleRepository.delete(scheduleEntity);
    }

    public List<ScheduleEntity> findByDiaryEntity(DiaryEntity diaryEntity) {
        return scheduleRepository.findByDiaryEntity(diaryEntity);
    }
}
