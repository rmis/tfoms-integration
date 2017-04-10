package ru.rtlabs.stat;

import ru.rtlabs.Entity.Patient;

import java.util.List;

public class Builder {

    public static String messageBuild(List<Patient> indiv){
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://10.41.5.82/it/WebServices/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:SendXmlFile2>\n" +
                "         <!--Optional:-->\n" +
                "         <web:xmlInput><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SMO_ZAPROS xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"zo.xsd\">\n" +
                " <ZGLV>\n" +
                "  <DATA>"+indiv.get(0).getCurrentDate()+"</DATA>\n" +
                "  <FILENAME>Z40001_01_1.XML</FILENAME>\n" +
                " </ZGLV>\n" +
                "  <PERSON>\n" +
                "    <FAM>"+indiv.get(0).getSurname()+"</FAM>\n" +
                "    <IM>"+indiv.get(0).getName()+"</IM>\n" +
                "    <OT>"+indiv.get(0).getPatrName()+"</OT>\n" +
                "    <W>"+indiv.get(0).getGender()+"</W>\n" +
                "    <DR>"+indiv.get(0).getbDate()+"</DR>\n" +
                "  </PERSON>\n" +
                "</SMO_ZAPROS>]]></web:xmlInput>\n" +
                "      </web:SendXmlFile2>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }
}