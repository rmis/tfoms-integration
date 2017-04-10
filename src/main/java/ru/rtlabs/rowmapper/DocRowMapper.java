package ru.rtlabs.rowmapper;

import ru.rtlabs.Entity.Doca;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocRowMapper extends CommonRowMapper<Doca> {

    @Override
    public Doca mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        Doca doca = new Doca();
        doca.setId(getInteger(rs, "id"));
        doca.setType(getInteger(rs, "type_id"));
        doca.setNumber(getString(rs, "number"));
        return doca;
    }
}