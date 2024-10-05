package es.obstetrics.obstetric.view.priv.confirmDialog.appointment;

import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;

/**
 * Clase encargada de decir al secretario que no puede reactivar el horario porque
 *  se llevaría a cabo un solapamiento.
 */
public class FailReactivateDiaryConfirmDialog extends MasterConfirmDialog {

    public  FailReactivateDiaryConfirmDialog(){
        createHeaderAndTextDialog();
    }

    @Override
    public void createHeaderAndTextDialog() {
        setHeader("No se puede reactivar la agenda");
        setCancelable(false);
        setText("No se puede reactivar la agenda, existe solapamiento entre otra agenda existente para este sanitario.");
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
