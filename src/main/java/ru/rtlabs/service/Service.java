package ru.rtlabs.service;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.rtlabs.Entity.Patient;
import ru.rtlabs.rowmapper.*;
import ru.rtlabs.stat.Builder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class Service {
    private JdbcTemplate jdbcTemplate;
    private Integer id;
    private String url;
    private List<Patient> indiv;
    private static final Logger LOG =Logger.getLogger(Service.class);

    public void initilize(){
            String sql = "SELECT *, CURRENT_DATE from pim_individual WHERE id = ?";
            indiv = jdbcTemplate.query(sql, new Object[] {id}, new PatientRowMapper());
        }

    public void postSend(){
       try {
            String content = Builder.messageBuild(indiv);
            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(content.length()));
            conn.setRequestProperty("Host", "guzio.ru:1080");
            conn.setRequestProperty("User-Agent", "HttpClient/1.1.1.1 (java 1.666)");
            conn.setDoOutput(true);
            OutputStream reqStream = conn.getOutputStream();
            reqStream.write(content.getBytes());
            LOG.info("\nPOST\n" +
                    this.url+"\n" +
                    "Accept-Encoding: " + "gzip,deflate" + "\n" +
                    "Content-Type: " + "text/xml;charset=UTF-8" + "\n" +
                    "Content-Length: " + String.valueOf(content.length()) + "\n" +
                    "User-Agent: " + "HttpClient/1.1" + "\n" +
                    "Payload: \n" + content);
            String res = null;
            InputStreamReader isr = null;
            if(conn.getResponseCode() == 500){
                isr = new InputStreamReader(conn.getErrorStream(), "UTF-8");
                LOG.warn("Response code: + " + conn.getResponseCode() + " from " + this.url);
            }else if ("gzip".equals(conn.getContentEncoding())){
                isr = new InputStreamReader(new GZIPInputStream(conn.getInputStream()));
            }else {
                isr = new InputStreamReader(conn.getInputStream());
            }

            BufferedReader bfr = new BufferedReader(isr);
            StringBuffer sbf = new StringBuffer();
            int ch = bfr.read();
            while (ch != -1) {
                sbf.append((char) ch);
                ch = bfr.read();
            }
            res = sbf.toString();
            LOG.info("\nResponse code: " + conn.getResponseCode() + "\n" +
                    "Ответ :\n" + res);
            responseParse(res);
        } catch (MalformedURLException e) {
            LOG.error("Сломанный URL - проверьте URL",e);
        } catch (ProtocolException e) {
            LOG.error("Внутр/ошибка протокола",e);
        } catch (IOException e) {
            LOG.error("Ошибка коннекта",e);
        }
    }

    private void responseParse(String response){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse((new InputSource(new StringReader(response))));
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("SendXmlFile2Result");
                for (int i = 0; i < nList.getLength(); i++) {
                     Node node = nList.item(i);
                    System.out.println(node.getTextContent());
                    responseParseData(node.getTextContent(), dbFactory, doc);
            }

        } catch (Exception e) {
            LOG.error("Ошибка", e);
        }
    }

    private void responseParseData(String response, DocumentBuilderFactory dbf, Document doc){
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(response)));
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("RENP");
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                System.out.println("ENP пациента " + node.getTextContent());
                documentInsert(node.getTextContent());
            }
        } catch (Exception e) {
            LOG.error("Ошибка. Пациента у них нет.... ", e);
        }
    }

    private void documentInsert(String enp){
        if (hasEnp(enp)){
            enpupdate(enp);
            System.out.println("апдейт енп " + enp);
        }else {
            enpInsert(enp);
            System.out.println("инсерт енп " + enp);
        }
    }

    private Boolean hasEnp(String enp){
        try {
            String query = "select 1 from pim_individual_doc where indiv_id = ? and number = ? and type_id = 26 limit 1";
            jdbcTemplate.queryForObject(query, new Object[]{this.id, enp}, Long.class);
            return true;
        }catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private void enpupdate(String enp){
        String update = "UPDATE pim_individual_doc set number = ? where indiv_id = ? and type_id = 26";
        String code = "UPDATE pim_indiv_code set code = ? where indiv_id = ? and type_id = 3";
        jdbcTemplate.update(update, enp, this.id);
        jdbcTemplate.update(code, enp, this.id);
    }

    private void enpInsert(String enp){
        String insert = "INSERT into pim_indiv_code(id, code, issue_dt, type_id, indiv_id) VALUES (?, ?, current_date, 3, ?)";
        String code = "INSERT INTO pim_individual_doc (id, type_id, number, code_id, indiv_id) VALUES (nextval('pim_individual_doc_id_seq'), 26, ?, ?, ?)";
        int codeId = jdbcTemplate.queryForInt("SELECT nextval('pim_indiv_code_id_seq')");
        jdbcTemplate.update(insert, codeId, enp, this.id);
        jdbcTemplate.update(code, enp, codeId, this.id);
    }



    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
