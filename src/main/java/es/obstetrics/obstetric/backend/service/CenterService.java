package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.repository.CenterRepository;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CenterService {
    @Autowired
    private CenterRepository centerRepository;

    public CenterService(CenterRepository centerRepository) {
        this.centerRepository = centerRepository;
    }

    public Page<CenterEntity> findAll(int page, int size) {
        return centerRepository.findAll(PageRequest.of(page, size));
    }

    public void save(CenterEntity centerEntity) {
        centerRepository.save(centerEntity);
    }

    public void delete(CenterEntity centerEntity) {
        centerRepository.delete(centerEntity);
    }

    /**
     * Método que se ejecuta al iniciar la aplicación, carga todos los centros almacenados
     * en el excell centers.xlsx en la tabla de centros.
     */
    public void saveCentersFromExcelFile(String file) {
        try (InputStream is = getClass().getResourceAsStream(file)) {
            if (is == null) {
                throw new RuntimeException("Excel file not found: " + file);
            }
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<CenterEntity> centers = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) { // Saltar la cabecera
                    continue;
                }
                String name = getStringValueFromCell(row.getCell(0));
                String address = getStringValueFromCell(row.getCell(1));
                String municipality = getStringValueFromCell(row.getCell(2));
                String province = getStringValueFromCell(row.getCell(3));
                String autonomousCommunity = getStringValueFromCell(row.getCell(4));
                String postalCode = getStringValueFromCell(row.getCell(5));
                String email = getStringValueFromCell(row.getCell(6));
                String phone = getStringValueFromCell(row.getCell(7));

                // Verificar si la entidad ya existe en la base de datos
                CenterEntity existingCenterName = centerRepository.findOneByCenterName(name); //Comprobación de si ya existen en la base de datos
                CenterEntity existingCenterPhone = centerRepository.findOneByPhone(phone);
                CenterEntity existingEmailPhone = centerRepository.findOneByEmail(email);

                if (existingCenterName == null && existingCenterPhone == null && existingEmailPhone == null) {
                    CenterEntity center = new CenterEntity();
                    center.setCenterName(name);
                    center.setAddress(address);
                    center.setMunicipality(municipality);
                    center.setProvince(province);
                    center.setAutonomousComunity(autonomousCommunity);
                    center.setPostalCode(postalCode);
                    center.setEmail(email);
                    center.setPhone(phone);

                    centers.add(center);
                }
            }

            if (!centers.isEmpty()) {
                centerRepository.saveAll(centers);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Excel: " + e.getMessage());
        }
    }

    /**
     * Convierte todas las celdas del excell a valores de tipo string.
     *
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
        if (isTableEmpty()) {
            saveCentersFromExcelFile(ConstantUtilities.ROUTE_CENTERS);
        }
    }

    private boolean isTableEmpty() {
        return centerRepository.count() == 0;
    }

    public Optional<CenterEntity> findById(Long centerId) {
        return centerRepository.findById(centerId);
    }

    public List<CenterEntity> findBySanitary(SanitaryEntity sanitaryEntity) {
        return centerRepository.findAllBySanitaryEntity(sanitaryEntity.getDni());
    }

    @Transactional
    public boolean existsBySanitaryEntityAndCenterEntity(SanitaryEntity value, CenterEntity value1) {
        return centerRepository.existsSanitaryInCenter(value.getId(), value1.getId());
    }
}
