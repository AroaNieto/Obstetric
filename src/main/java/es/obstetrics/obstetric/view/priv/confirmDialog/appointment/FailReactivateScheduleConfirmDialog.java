package es.obstetrics.obstetric.view.priv.confirmDialog.appointment;

import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;

public class FailReactivateScheduleConfirmDialog extends MasterConfirmDialog {
    public  FailReactivateScheduleConfirmDialog(){
        createHeaderAndTextDialog();
    }

    @Override
    public void createHeaderAndTextDialog() {
        setHeader("No se puede reactivar el horario");
        setCancelable(false);
        setText("No se puede reactivar el horario, existe solapamiento entre otra agenda existente para este sanitario.");
    }

    @Override
    public void closeDialog() {
        close();
    }

    @Override
    public void clickButton() {
        closeDialog();
    }
}
