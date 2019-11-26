package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

// Nødvendig
public class V18__fjerne_constraints_maal_og_oppgave extends BaseJavaMigration {
    public void migrate(Context context) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));

        String målConstraint = jdbcTemplate.queryForObject("select constraint_name from information_schema.constraints where constraint_type='REFERENTIAL' and table_name='MAAL'", String.class);
        jdbcTemplate.execute("alter table maal drop constraint " + målConstraint);

        String oppgaveConstraint = jdbcTemplate.queryForObject("select constraint_name from information_schema.constraints where constraint_type='REFERENTIAL' and table_name='OPPGAVE'", String.class);
        jdbcTemplate.execute("alter table oppgave drop constraint " + oppgaveConstraint);
    }
}