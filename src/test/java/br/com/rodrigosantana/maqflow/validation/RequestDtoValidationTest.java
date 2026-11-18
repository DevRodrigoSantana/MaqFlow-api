package br.com.rodrigosantana.maqflow.validation;

import br.com.rodrigosantana.maqflow.dto.equipment.*;
import br.com.rodrigosantana.maqflow.dto.maintenance.*;
import br.com.rodrigosantana.maqflow.dto.user.*;
import br.com.rodrigosantana.maqflow.enums.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class RequestDtoValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;
    @BeforeAll static void setUp(){factory=Validation.buildDefaultValidatorFactory();validator=factory.getValidator();}
    @AfterAll static void tearDown(){factory.close();}

    @Test void validatesUserCreateAndUpdate(){
        assertTrue(fields(validator.validate(new UserRequestDTO("A","bad-email","weak"))).containsAll(Set.of("name","email","password")));
        assertTrue(validator.validate(new UserRequestDTO("Ana Silva","ana@example.test","StrongPass1!")).isEmpty());
        assertTrue(fields(validator.validate(new UserUpdateRequestDTO(" ","bad-email", " "))).containsAll(Set.of("name","email","currentPassword")));
        assertTrue(validator.validate(new UserUpdateRequestDTO("Ana Silva","ana@example.test", "old-password")).isEmpty());
    }

    @Test void passwordCurrentDoesNotUseNewPasswordRegex(){
        assertTrue(validator.validate(new UserPasswordUpdateDTO("tata","StrongPass1!")).isEmpty());
        assertTrue(fields(validator.validate(new UserPasswordUpdateDTO(" ","StrongPass1!"))).contains("currentPassword"));
        assertTrue(fields(validator.validate(new UserPasswordUpdateDTO("tata","weak"))).contains("newPassword"));
    }

    @Test void validatesEquipmentSerialAndDescription(){
        UUID userId=UUID.randomUUID();
        EquipmentRequestDTO create=new EquipmentRequestDTO("Torno CNC","x".repeat(1001),"SER@01",EquipmentStatus.ACTIVE,userId);
        assertTrue(fields(validator.validate(create)).containsAll(Set.of("description","serialNumber")));
        EquipmentUpdateRequestDTO update=new EquipmentUpdateRequestDTO("Torno CNC","ok","SER_01-02",EquipmentStatus.ACTIVE);
        assertTrue(validator.validate(update).isEmpty());
    }

    @Test void validatesMaintenanceRequiredFieldsAndDescriptionSize(){
        MaintenanceRequestDTO create=new MaintenanceRequestDTO("x".repeat(1001),null,null,null,null);
        assertTrue(fields(validator.validate(create)).containsAll(Set.of("description","type","status","equipmentId","registeredById")));
        assertTrue(validator.validate(new MaintenanceUpdateRequestDTO("Revisão geral",MaintenanceType.PREVENTIVE,MaintenanceStatus.OPEN)).isEmpty());
    }

    private Set<String> fields(Set<? extends jakarta.validation.ConstraintViolation<?>> violations){
        return violations.stream().map(v->v.getPropertyPath().toString()).collect(Collectors.toSet());
    }
}
