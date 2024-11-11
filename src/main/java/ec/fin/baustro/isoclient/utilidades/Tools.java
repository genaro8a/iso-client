package ec.fin.baustro.isoclient.utilidades;

import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.*;
import org.jpos.iso.header.BaseHeader;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.tlv.ISOTaggedField;
import org.jpos.tlv.TLVList;
import org.jpos.tlv.TLVMsg;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@Slf4j
public class Tools {

    @Value("${IsoMsg.UnPack.Debug}")
    private static Boolean UnPackDebug;
    @Value("${IsoMsg.Pack.Debug}")
    private static Boolean PackDebug;
    protected  static SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
    protected  static SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
    protected static SimpleDateFormat dateFormatOnly = new SimpleDateFormat("MMdd");



    public static ISOMsg GetISOMsg(String StrIsoMsg, GenericPackager packager, int LongitudCabecera) throws ISOException {

        String msg = StrIsoMsg;
        if (msg == null || msg.length() == 0) {
            log.info("no esta definido el mensaje de la iso ");
            return null;
        }
        String len = msg.substring(0, 4);
        int leng = ISOUtil.byte2int(ISOUtil.hex2byte(len));
        if ((leng * 2) == (msg.substring(4).length())) {
            msg = msg.substring(4);
        }
        String headerString = "";
        if (LongitudCabecera > 0) {
            headerString = msg.substring(0, LongitudCabecera);
            ISOHeader hd = new BaseHeader();
            hd.unpack(headerString.getBytes());
            hd.toString();
        }
        String strMsg = "";
        if (LongitudCabecera == 0) {
            strMsg = msg;
        } else {
            strMsg = msg.substring(LongitudCabecera);
        }
        ISOMsg isomsg = new ISOMsg();
        isomsg.setPackager(packager);
        if( UnPackDebug==true){
            org.jpos.util.Logger jposLogger = new org.jpos.util.Logger();
//            jposLogger.addListener(log);
            packager.setLogger(jposLogger, "unpack");
        } else if(PackDebug==true){
            org.jpos.util.Logger jposLogger = new org.jpos.util.Logger();
//            jposLogger.addListener(new JTextPaneLogListener(textPane));
            packager.setLogger(jposLogger, "pack");
        }
        if (LongitudCabecera > 0) {
            isomsg.setHeader(ISOUtil.hex2byte(msg.substring(0, LongitudCabecera)));
        }
        //isomsg.setDirection(2);
        isomsg.unpack(ISOUtil.hex2byte(strMsg.trim()));
        return  isomsg;
    }
    public static List<Object[]> processFields(ISOMsg isoMsg, JSONObject definition, JSONObject values) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ISOException {
        List<Object[]> result = new ArrayList<>();
        processField(isoMsg,"", definition, values, result);
        return result;
    }
    public static List<Object[]> filterElementsByIndices(List<Object[]> originalList, Set<Integer> indicesToRemove) {
        return originalList.stream()
                .map(array -> IntStream.range(0, array.length)
                        .filter(index -> !indicesToRemove.contains(index))
                        .mapToObj(index -> array[index])
                        .toArray())
                .collect(Collectors.toList());
    }
    public static void clearTextPane(JTextPane textPane) {
        SwingUtilities.invokeLater(() -> {
            try {
                textPane.getDocument().remove(0, textPane.getDocument().getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    public static List<Object[]> filterByBooleanCondition(List<Object[]> originalList, boolean condition) {
        return originalList.stream()
                .filter(array -> array.length > 0 && array[0] instanceof Boolean && (Boolean) array[0] == condition)
                .collect(Collectors.toList());
    }

    public static JSONObject getFieldDetails(ISOBasePackager packager) {
        JSONObject fieldDetails = new JSONObject();

        if (packager != null) {
//            isoMsg.getMaxField()
            for (int i = 0; i <= 128; i++) {
//                if (isoMsg.hasField(i)) {
                ISOFieldPackager fieldPackager = packager.getFieldPackager(i);
                if(fieldPackager!=null) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("id", i);
                    fieldInfo.put("length", fieldPackager.getLength());
                    fieldInfo.put("name", fieldPackager.getDescription());
                    if(fieldPackager instanceof ISOMsgFieldPackager){
                        fieldInfo.put("class", ((ISOMsgFieldPackager)fieldPackager).getISOFieldPackager().getClass().getName());
                    }else {
                        fieldInfo.put("class", fieldPackager.getClass().getName());
                    }
                    // ISOComponent comp = isoMsg.getComponent(i);
//                    field ((ISOBasePackager) isoMsg.getPackager()).getFieldPackager(i);
                    if (fieldPackager instanceof ISOMsgFieldPackager) {
                        // Recuperar detalles de los subcampos recursivamente
                        ISOBasePackager packager2 = (ISOBasePackager) ((ISOMsgFieldPackager) fieldPackager).getISOMsgPackager();
                        JSONObject subFieldDetails = getFieldDetails((ISOBasePackager)((ISOMsgFieldPackager) fieldPackager).getISOMsgPackager());
                        // Directamente mezclar subFieldDetails en fieldInfo sin usar "subfields"
                        for (String key : subFieldDetails.keySet()) {
                            fieldInfo.put(key, subFieldDetails.get(key));
                        }
                    }
                    // Agregar este fieldInfo al objeto fieldDetails
                    fieldDetails.put(String.valueOf(i), fieldInfo);
                }
//                }
            }
        }
        return fieldDetails;
    }

    public static JSONObject convertComponentToJson(ISOComponent component) throws ISOException {
        JSONObject json = new JSONObject();
        // Si el componente es un mensaje ISO, iterar a través de sus campos
        if (component instanceof ISOMsg) {
            ISOMsg isoMsg1 = (ISOMsg) component;
            // Iterar a través de los campos del mensaje ISO
            for (int i = -1; i <= isoMsg1.getMaxField(); i++) {
                if (isoMsg1.hasField(i)) {
                    ISOComponent comp = isoMsg1.getComponent(i);
                    if (comp instanceof ISOMsg) {
                        // Para ISOMsg anidados, realizar una llamada recursiva
                        json.put(String.valueOf(i), convertComponentToJson(comp));
                    } else if (comp instanceof ISOField) {
                        // Para campos simples, agregarlos directamente al JSON
                        json.put(String.valueOf(i), comp.getValue());
                    }else if(comp instanceof ISOBitMap){
                        json.put(String.valueOf( i), comp.getValue());
                    }else if(comp instanceof ISOBinaryField){
                        json.put(String.valueOf( i), comp.toString());
                    } else if (comp instanceof org.jpos.tlv.ISOTaggedField) {
                        json.put(String.valueOf( i), comp.getValue());
                    } else {
                        json.put(String.valueOf( i), comp.toString());
                    }
                }
            }
        }
        return json;
    }
    public static JSONObject getCamposCompilados(ISOComponent component) throws ISOException {
        JSONObject json = new JSONObject();
        // Si el componente es un mensaje ISO, iterar a través de sus campos
        if (component instanceof ISOMsg) {
            ISOMsg isoMsg1 = (ISOMsg) component;
            // Agregar el MTI si está disponible
            if(isoMsg1.getComponent(0)!=null){
                if (isoMsg1.getMTI() != null && !isoMsg1.getMTI().isEmpty()) {
                    json.put("MTI", isoMsg1.getMTI());
                }
            }

            // Iterar a través de los campos del mensaje ISO
            for (int i = -1; i <= isoMsg1.getMaxField(); i++) {
                if (isoMsg1.hasField(i)) {
                    ISOComponent comp = isoMsg1.getComponent(i);
                    Object packager = isoMsg1.getPackager();
                    if (comp instanceof ISOMsg) {
                        // Para ISOMsg anidados, realizar una llamada recursiva
                        json.put(String.valueOf(i), getCamposCompilados(comp));
                    } else if (comp instanceof ISOField) {

                        // Para campos simples, agregarlos directamente al JSON
                        if(packager instanceof GenericPackager) {
                            json.put(String.valueOf(i), ISOUtil.hexString(((GenericPackager) isoMsg1.getPackager()).getFieldPackager(i).pack(comp)));
                        } else if (packager instanceof Base1SubFieldPackager) {
                            json.put(String.valueOf(i), ISOUtil.hexString(((Base1SubFieldPackager) isoMsg1.getPackager()).getFieldPackager(i).pack(comp)));
                        }
                    }else if(comp instanceof ISOBitMap){
                        json.put(String.valueOf( i), ISOUtil.hexString( ISOUtil.bitSet2byte((BitSet) comp.getValue())));
                    }else if(comp instanceof ISOBinaryField){
                        json.put(String.valueOf( i), comp.toString());
                    } else if (comp instanceof ISOTaggedField) {
                        if(packager instanceof GenericPackager) {
                            json.put(String.valueOf(i), ISOUtil.hexString(((GenericPackager) isoMsg1.getPackager()).getFieldPackager(i).pack(comp)));
                        } else if (packager instanceof Base1SubFieldPackager) {
                            json.put(String.valueOf(i), ISOUtil.hexString(((Base1SubFieldPackager) isoMsg1.getPackager()).getFieldPackager(i).pack(comp)));
                        }
                    } else {
                        log.info("clase del componente:"+comp.getClass());
                        json.put(String.valueOf( i), comp.toString());
                    }
                }
            }
        }
        return json;
    }
    public static JSONObject updateValuesWithDefinition(JSONObject valores, JSONObject definicion) {
        JSONObject updatedValores = new JSONObject();

        // Iterar sobre las claves del objeto 'valores'.
        for (String key : valores.keySet()) {
            Object value = valores.get(key);

            // Revisar si la clave es numérica (incluyendo negativos).
            if (key.matches("-?\\d+") || "-1".equals(key)) { // Acepta números positivos, negativos y "-1".
                if ("-1".equals(key)) {
                    // Encuentra una clave numérica en la definición que sea adecuada para el reemplazo.
                    String newKey = findNumericKeyInDefinition(definicion);
                    if (newKey != null) {
                        // Si encontramos una nueva clave numérica adecuada, usamos esa para el valor.
                        updatedValores.put(newKey, value);
                    } else {
                        // Si no hay una nueva clave numérica adecuada, mantenemos el valor como está.
                        updatedValores.put(key, value);
                    }
                } else if (value instanceof JSONObject) {
                    // Caso recursivo: si es JSONObject, necesitamos procesarlo con su definición.
                    JSONObject nestedDefinition = definicion.optJSONObject(key) != null ? definicion.optJSONObject(key) : definicion;
                    JSONObject updatedNestedValores = updateValuesWithDefinition((JSONObject) value, nestedDefinition);
                    updatedValores.put(key, updatedNestedValores);
                } else {
                    // Si no es "-1" y el valor no es un JSONObject, simplemente copiamos el valor.
                    updatedValores.put(key, value);
                }
            } else {
                // Para cualquier otra clave no numérica, simplemente copiamos el valor.
                updatedValores.put(key, value);
            }
        }

        return updatedValores;
    }

    private static String findNumericKeyInDefinition(JSONObject definicion) {
        // Este método busca en la definición la primera clave numérica que tenga una clase terminando en "_BITMAP".
        for (String key : definicion.keySet()) {
            if (key.matches("-?\\d+") && definicion.getJSONObject(key).getString("class").contains("_BITMAP")) {
                return key;
            }
        }
        return null; // Retorna null si no se encuentra ninguna coincidencia.
    }
    public static void updateField(ISOMsg msg, String fieldPath, String value, boolean actualizarFechas,boolean aumentarMonto, Date now) throws ISOException {
        String[] parts = fieldPath.split("\\.");
        log.debug("fieldpath: {}",fieldPath);
        ISOMsg targetMsg = msg;

        // Navegar por los subcampos hasta llegar al penúltimo elemento de la ruta
        for (int i = 0; i < parts.length - 1; i++) {
            int fieldNumber = Integer.parseInt(parts[i]);
            if (!targetMsg.hasField(fieldNumber)) {
                // Si el campo no existe, crear un nuevo ISOMsg para ese campo
                if(targetMsg.getComponent(fieldNumber) instanceof ISOMsg) {
                    ISOMsg campo = new ISOMsg(fieldNumber);
                    campo.setPackager(targetMsg.getPackager());
                    targetMsg.set(String.valueOf( fieldNumber), campo);
                    // targetMsg.recalcBitMap();
                }
                // targetMsg.set(new ISOMsg(fieldNumber));
            }
            targetMsg = (ISOMsg) targetMsg.getComponent(fieldNumber);
        }

        int targetField = Integer.parseInt(parts[parts.length - 1]);
        if(!Tools.hasSubfields(fieldPath,Tools.getFieldDetails((ISOBasePackager) targetMsg.getPackager()))) {
            // Aquí, inserta la lógica para actualizar fechas y montos similar a la versión anterior,
            // ajustando según sea necesario para trabajar con 'targetMsg' y 'targetField' en lugar de 'isoMsg' y 'field'
            if (actualizarFechas && fieldPath.trim().equals("7")) {
                targetMsg.set(targetField, dateFormat.format(now));
            } else if (actualizarFechas && fieldPath.trim().equals("12")) {
                targetMsg.set(targetField, timeFormat.format(now));
            } else if (actualizarFechas && fieldPath.trim().equals("13")) {
                targetMsg.set(targetField, dateFormatOnly.format(now));
            } else if (actualizarFechas && fieldPath.trim().equals("11")) {
                targetMsg.set(targetField, StanGenerator.generateStan());
            } else if (actualizarFechas && fieldPath.trim().equals("37")) {
                targetMsg.set(targetField, RrnGenerator.generateRrn());
            }
            else if (aumentarMonto && fieldPath.trim().equals("4")) {
                // Convertir a entero
                long amountValue = Long.parseLong(value);
                // Incrementar en uno
                amountValue++;

                // Formatear de nuevo a 12 dígitos
                String newAmount = String.format("%012d", amountValue);
                targetMsg.set(targetField, newAmount);
            }
            else {
                // Si el campo no existe, crear un nuevo ISOMsg para ese campo

                targetMsg.set(targetField, value);
                targetMsg.recalcBitMap();
            }
        }else{
            ISOMsg campo=new ISOMsg(targetField);
            campo.setPackager(targetMsg.getPackager());
            targetMsg.set(String.valueOf(targetField),campo);
            // targetMsg.recalcBitMap();
        }

    }
    public static void updateISOMsg(ISOMsg isoMsg, boolean actualizarFechas, boolean aumentarMonto) throws ISOException {
        Date now=new Date();
        for (int i = 0; i < isoMsg.getMaxField(); i++) {
            if(isoMsg.hasField(i)){
                if(i==4 || i==37 ||i==11 ||i==13 || i==12 || i==7){

                    Tools.updateField(isoMsg,String.valueOf(i),getFieldValue(String.valueOf(i),isoMsg),actualizarFechas,aumentarMonto,now);
                }
            }

        }
    }
    public static String getFieldValue(String fieldPath,ISOMsg isoMsg) {
        String[] parts = fieldPath.split("\\.");
        ISOMsg targetMsg = isoMsg;
        String value = "";

        try {
            // Navegar a través de los mensajes y submensajes basado en la ruta
            for (int i = 0; i < parts.length - 1; i++) {
                int fieldNumber = Integer.parseInt(parts[i]);
                if (targetMsg.hasField(fieldNumber)) {
                    ISOComponent comp = targetMsg.getComponent(fieldNumber);
                    if (comp instanceof ISOMsg) {
                        targetMsg = (ISOMsg) comp; // Navegar al submensaje
                    } else {
                        // Si algún componente no es un ISOMsg antes del final, detener la navegación
                        return "Component at " + parts[i] + " is not a sub-message";
                    }
                } else {
                    // Campo no encontrado en el camino
                    return "Field not found in path: " + fieldPath;
                }
            }

            // Obtener el valor del último campo especificado en la ruta
            int lastFieldNumber = Integer.parseInt(parts[parts.length - 1]);
            if (targetMsg.hasField(lastFieldNumber)) {
                value = targetMsg.getString(lastFieldNumber);
            } else {
                value = "Field " + lastFieldNumber + " not present";
            }
        } catch (NumberFormatException e) {
            // Manejo de error si la ruta no está correctamente formateada
            e.printStackTrace();
            return "Invalid field path format";
        }

        return value;
    }
    // Método estático para crear un JSONObject con las etiquetas EMV y sus valores
    public static JSONObject createEMVJSONObject(TLVList tlvData) {
        JSONObject jsonObject = new JSONObject();

        // Recorrer cada TLVMsg y agregar la etiqueta y el valor al JSONObject
        for (TLVMsg tLVMsg : tlvData.getTags()) {
            String tag = Integer.toHexString(tLVMsg.getTag()); // Convertir el tag a hexadecimal
            String value = ISOUtil.hexString(tLVMsg.getValue()); // Obtener el valor en formato hexadecimal
            jsonObject.put(tag, value); // Agregar al JSONObject
        }

        return jsonObject;
    }
    public static int comparePaths(String path1, String path2) {
        List<String> splitPath1 = Arrays.asList(path1.split("\\."));
        List<String> splitPath2 = Arrays.asList(path2.split("\\."));
        int minLength = Math.min(splitPath1.size(), splitPath2.size());

        for (int i = 0; i < minLength; i++) {
            int part1 = Integer.parseInt(splitPath1.get(i));
            int part2 = Integer.parseInt(splitPath2.get(i));
            if (part1 != part2) {
                return Integer.compare(part1, part2);
            }
        }

        // Si uno de los caminos es prefijo del otro, el más corto va primero
        return Integer.compare(splitPath1.size(), splitPath2.size());
    }
    /**
     * Ordena las claves de un JSONObject y devuelve un nuevo JSONObject con las claves ordenadas.
     *
     * @param jsonObject El JSONObject a ordenar.
     * @return Un nuevo JSONObject con las claves ordenadas.
     */
    public static JSONObject sortJSONObjectByKey(JSONObject jsonObject) {
        // Ordenar las claves del JSONObject actual
        List<String> sortedKeys = new ArrayList<>(jsonObject.keySet());
        Collections.sort(sortedKeys, (o1, o2) -> {
            boolean isO1Numeric = o1.matches("\\d+");
            boolean isO2Numeric = o2.matches("\\d+");

            // Si ambas claves son numéricas, las compara como números
            if (isO1Numeric && isO2Numeric) {
                return Integer.compare(Integer.parseInt(o1), Integer.parseInt(o2));
            }
            // Mantiene el orden original si ninguna es numérica
            return o1.compareTo(o2);
        });

        JSONObject sortedJsonObject = new JSONObject();
        for (String key : sortedKeys) {
            Object value = jsonObject.get(key);
            // Si el valor es otro JSONObject, aplica la ordenación de manera recursiva
            if (value instanceof JSONObject) {
                value = sortJSONObjectByKey((JSONObject) value);
            }
            // Si el valor es un JSONArray, procesa cada elemento que sea un JSONObject
            else if (value instanceof JSONArray) {
                value = sortJSONArray((JSONArray) value);
            }
            sortedJsonObject.put(key, value);
        }
        return sortedJsonObject;
    }
    public static  void AjusteTamanioColumnas(JTable table){
        for (int column = 0; column < table.getColumnCount(); column++)
        {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < table.getRowCount(); row++)
            {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows
                if (preferredWidth >= maxWidth)
                {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth(preferredWidth);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    private static JSONArray sortJSONArray(JSONArray jsonArray) {
        JSONArray sortedJsonArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.get(i);
            // Si el elemento es un JSONObject, aplica la ordenación de manera recursiva
            if (element instanceof JSONObject) {
                element = sortJSONObjectByKey((JSONObject) element);
            }
            // También procesa de manera recursiva los elementos JSONArray anidados
            else if (element instanceof JSONArray) {
                element = sortJSONArray((JSONArray) element);
            }
            sortedJsonArray.put(element);
        }
        return sortedJsonArray;
    }

    private static void processField(ISOMsg isoMsg,String parentPath, JSONObject definition, JSONObject values, List<Object[]> result) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ISOException {
        List<String> sortedKeys = new ArrayList<>(definition.keySet());
        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
            Object obj = definition.get(key);
            if(obj instanceof JSONObject) {
                JSONObject fieldDef = definition.getJSONObject(key);
                String currentPath = parentPath.isEmpty() ? key : parentPath + "." + key;
                Object value = values.optJSONObject(key) != null ? values.getJSONObject(key) : values.optString(key, "N/A");

                boolean isActive = !value.equals("N/A");
                value=value.equals("N/A")?"":value;

                // Determinar si el valor es otro JSONObject (indicando subcampos)
                if (Tools.hasSubfields(key,definition)) {
                    log.debug(" process path {}",currentPath);
//                    String valor ="ISOMsg";
                    String valor =            isoMsg.getComponent(currentPath)!=null? ISOUtil.hexString(isoMsg.getComponent(currentPath).pack()):"";


                    // Agregar la información de este campo antes de procesar sus subcampos
                    addFieldInfo(result, isActive, currentPath, fieldDef.getString("name"), fieldDef.getInt("length"),fieldDef.getString("class"), valor);
                    // Procesar subcampos
                    JSONObject valores=!values.has(key)?new JSONObject():values.getJSONObject(key);
                    processField(isoMsg,currentPath, fieldDef,valores , result);
                } else {
                    // Agregar la información de este campo
                    addFieldInfo(result, isActive, currentPath, fieldDef.getString("name"), fieldDef.getInt("length"),fieldDef.getString("class"), value.toString());
                }
            }
        }
    }
    public static String getTrama( ISOMsg isoMsg) throws ISOException {
        byte[] Header = isoMsg.getHeader();
        String Trama = "";
        if (Header != null) {
            Trama += ISOUtil.byte2hex(Header);
        }
        Trama += ISOUtil.byte2hex(isoMsg.pack());
        Trama = String.format("%04X", Trama.length() / 2) + Trama;
        return Trama;
    }
    private static void addFieldInfo(List<Object[]> result, boolean isActive, String path, String description,int longitud,String clase , String value) {
        result.add(new Object[]{isActive, path, description,longitud,clase, value});
    }

    public static boolean hasSubfields(String path,JSONObject definitionJson ) {
        String[] parts = path.split("\\.");
        JSONObject currentObject = definitionJson;

        // Navegar a través de las partes del path para llegar al objeto deseado
        for (String part : parts) {
            currentObject = currentObject.optJSONObject(part);
            if (currentObject == null) {
                // No se encontró un objeto JSON en el path actual, no hay más hijos.
                return false;
            }
        }

        // Verificar si el objeto actual tiene alguna clave numérica
        for (String key : currentObject.keySet()) {
            if (key.matches("\\d+")) {
                // Encuentra una clave que es numérica, indicando un hijo.
                return true;
            }
        }

        // No se encontraron claves numéricas, por lo tanto no hay subcampos/hijos.
        return false;
    }
    public static int findRowByValue(JTable table, Object value, int column) {
        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
            if (table.getValueAt(rowIndex, column).equals(value)) {
                // Retorna el índice de la fila donde se encontró la coincidencia
                return rowIndex;
            }
        }
        // Retorna -1 si no se encuentra ninguna coincidencia
        return -1;
    }
    public static SSLSocket createSSLClient(String host, int port) throws Exception {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        // Crear un TrustManager que confíe en todos los certificados
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        // Configurar para usar TLSv1.2
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Obtener SSLSocketFactory personalizada
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        // Crear SSLSocket y conectarlo al host y puerto proporcionados
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        String[] enabledProtocols = new String[] {"TLSv1.2", "TLSv1.3", "TLSv1.1", "TLSv1", "SSLv3", "SSLv3"};
        sslSocket.setEnabledProtocols(enabledProtocols);
        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
        // Iniciar el handshake SSL
        sslSocket.setSoTimeout(10000);  // Tiempo de espera de 10 segundos
        sslSocket.startHandshake();
        return sslSocket;  // Devolver el cliente SSLSocket conectado
    }
}
