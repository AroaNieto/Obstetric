package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.InsuranceEntity;
import es.obstetrics.obstetric.backend.repository.InsuranceRepository;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final EntityManager entityManager; //Interfaz para interactuar con el contexto de persistencia.

    @Autowired
    public InsuranceService(InsuranceRepository insuranceRepository, EntityManager entityManager){
        this.insuranceRepository = insuranceRepository;
        this.entityManager = entityManager;
    }
    public Page<InsuranceEntity> findAll(int page, int size){
        return insuranceRepository.findAll(PageRequest.of(page,size));
    }

    @Transactional
    public InsuranceEntity save(InsuranceEntity insuranceEntity) {
        return entityManager.merge(insuranceEntity); //Actualiza o añade una entidad en el contexto de persistencia.
    }

    @Transactional
    public void delete(InsuranceEntity insuranceEntity){
        insuranceRepository.delete(insuranceEntity);
    }

    /**
     * Método que se ejecuta al iniciar la aplicación, carga todos los centros almacenados
     *  en el excell insurances.xlsx en la tabla aseguradora.
     * @param file fichero
     */
    public void saveInsurancesFromExcelFile(String file) {
        try (InputStream is = getClass().getResourceAsStream(file)) {
            if (is == null) {
                throw new RuntimeException("Excel file not found: " + file);
            }
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<InsuranceEntity> insurances = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) { // Saltar la cabecera
                    continue;
                }
                String name = getStringValueFromCell(row.getCell(0));
                String phone = getStringValueFromCell(row.getCell(1));

                // Verificar si la entidad ya existe en la base de datos
                InsuranceEntity existingInsuranceName = insuranceRepository.findOneByName(name); //Comprobación de si ya existen en la base de datos
                InsuranceEntity existingInsurancePhone =insuranceRepository.findOneByPhone(phone);

                if (existingInsuranceName == null && existingInsurancePhone==null) {
                    InsuranceEntity insuranceEntity = new InsuranceEntity();
                    insuranceEntity.setName(name);
                    insuranceEntity.setState(ConstantUtilities.STATE_ACTIVE);
                    insuranceEntity.setPhone(phone);
                    insurances.add(insuranceEntity);
                }
            }
            if(!insurances.isEmpty()){
                insuranceRepository.saveAll(insurances);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Excel: " + e.getMessage());
        }
    }

    /**
     * Convierte todas las celdas del excell a valores de tipo string.
     * @return El string
     */
    private String getStringValueFromCell(Cell cell) {
        if (cell != null) {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
        }
        return "";
    }
    @PostConstruct
    public void init() {
        if(isTableEmpty()){
            saveInsurancesFromExcelFile(ConstantUtilities.ROUTE_INSURANCES);
        }
    }

    private boolean isTableEmpty() {
        return insuranceRepository.count() == 0;
    }

    public InsuranceEntity findOneByName(String name) {
        return insuranceRepository.findOneByName(name);
    }

    public Page<InsuranceEntity> findByNameContaining(String filter, int page, int size) {
        return insuranceRepository.findByNameContaining(filter, PageRequest.of(page, size));
    }

    public Page<InsuranceEntity>findByPostalCodeContaining(String filter, int page, int size) {
        return insuranceRepository.findByPostalCodeContaining(filter, PageRequest.of(page, size));
    }

    public Page<InsuranceEntity> findByAddressContaining(String filter, int offset, int limit) {
        return insuranceRepository.findByAddressContaining(filter, PageRequest.of(offset, limit));
    }

    public Page<InsuranceEntity> findByEmailContaining(String filter, int offset, int limit) {
        return insuranceRepository.findByEmailContaining(filter, PageRequest.of(offset, limit));
    }

    public Page<InsuranceEntity> findByPhoneContaining(String filter, int offset, int limit) {
        return insuranceRepository.findByPhoneContaining(filter, PageRequest.of(offset, limit));
    }

    public List<InsuranceEntity> findByCenterEntity(CenterEntity centerEntity) {
        return insuranceRepository.findAllByCenterName(centerEntity.getCenterName());
    }
}
