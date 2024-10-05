package es.obstetrics.obstetric.view.priv.home;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.views.appointment.MyAppointmentsDayGridView;
import es.obstetrics.obstetric.view.priv.views.myFolder.MyFolder;
import es.obstetrics.obstetric.view.priv.views.users.SanitariesGridView;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@PermitAll
@Route(value="", layout = PrincipalView.class)
public class HomeView extends Div {
    @Autowired
    public HomeView(UserCurrent userCurrent, ReportService reportService,
                    NewsletterService newsletterService, AppointmentService appointmentService,
                    DiaryService diaryService, ConstantValues constantValues, SanitaryService sanitaryService,
                    CenterService centerService,
                    PasswordEncoder passwordEncoder,UserService userService){
        if(userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)){
            add(new MyFolder(newsletterService, reportService, appointmentService,(PatientEntity) userCurrent.getCurrentUser(), userCurrent, userService));
        }else if(userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_MATRONE) ||userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)){
           add(new MyAppointmentsDayGridView(appointmentService,userCurrent,diaryService,reportService));
        }else if(userCurrent.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_ADMIN) || userCurrent.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_SECRETARY)){
            add(new SanitariesGridView(constantValues, userCurrent, sanitaryService,centerService,userService,passwordEncoder));
            setSizeFull();
        }
    }

}
