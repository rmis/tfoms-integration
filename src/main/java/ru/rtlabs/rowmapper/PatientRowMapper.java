package ru.rtlabs.rowmapper;

import ru.rtlabs.Entity.Patient;
import java.sql.ResultSet;
import java.sql.SQLException;


public class PatientRowMapper  extends CommonRowMapper<Patient> {

    @Override
    public Patient mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        Patient patient = new Patient();
        patient.setId(getInteger(rs, "id"));
        patient.setSurname(getString(rs, "surname"));
        patient.setName(getString(rs, "name"));
        patient.setPatrName(getString(rs, "patr_name"));
        patient.setbDate(getDate(rs, "birth_dt"));
        patient.setGender(String.valueOf(getInteger(rs, "gender_id")));
        patient.setCurrentDate(getDate(rs, "date"));
        return patient;
    }
}