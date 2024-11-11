package ec.fin.baustro.isoclient.servicios;

import ec.fin.baustro.isoclient.utilidades.Tools;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class IsoMsgService {

    public ISOMsg getISOMsg(String strIsoMsg, GenericPackager packager, int longitudCabecera) throws Exception {
        return Tools.GetISOMsg(strIsoMsg, packager, longitudCabecera);
    }

    public List<Object[]> processFields(ISOMsg isoMsg, JSONObject definition, JSONObject values) throws Exception {
        return Tools.processFields(isoMsg, definition, values);
    }

    public void updateField(ISOMsg isoMsg, String fieldPath, String value, boolean actualizarFechas, boolean aumentarMonto) throws Exception {
        Tools.updateField(isoMsg, fieldPath, value, actualizarFechas, aumentarMonto, new Date());
    }

    public String getFieldValue(String fieldPath, ISOMsg isoMsg) {
        return Tools.getFieldValue(fieldPath, isoMsg);
    }

    public void updateISOMsg(ISOMsg isoMsg, boolean actualizarFechas, boolean aumentarMonto) throws Exception {
        Tools.updateISOMsg(isoMsg, actualizarFechas, aumentarMonto);
    }

    public String getTrama(ISOMsg isoMsg) throws Exception {
        return Tools.getTrama(isoMsg);
    }
}