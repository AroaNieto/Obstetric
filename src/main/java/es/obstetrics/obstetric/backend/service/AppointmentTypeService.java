package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import es.obstetrics.obstetric.backend.repository.AppointmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentTypeService {

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    public AppointmentTypeService(AppointmentTypeRepository appointmentTypeRepository){
        this.appointmentTypeRepository = appointmentTypeRepository;
    }
    public List<AppointmentTypeEntity> findAll(){
        return appointmentTypeRepository.findAll();
    }

    public void save(AppointmentTypeEntity appointmentTypeEntity){
        appointmentTypeRepository.save(appointmentTypeEntity);
    }

    public void delete(AppointmentTypeEntity appointmentTypeEntity){
        appointmentTypeRepository.delete(appointmentTypeEntity);
    }

    public AppointmentTypeEntity findOneByDescription(String description) {
        return appointmentTypeRepository.findOneByDescription(description);
    }
}