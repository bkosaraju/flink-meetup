package org.apache.flink.playgrounds.spendreport;

import org.apache.flink.connector.jdbc.catalog.JdbcCatalog;
import org.apache.flink.table.api.TableEnvironment;

public class Catalog {
    TableEnvironment tEnv;
    private void loadJdbcCatalog() {
        JdbcCatalog catalog = new JdbcCatalog("mysql", "demo", "demo", "demo", "jdbc:mysql://mysql:3306");
        this.tEnv.registerCatalog("mysql", catalog);
    }
    public Catalog(TableEnvironment tEnv) {
        this.tEnv = tEnv;
        loadJdbcCatalog();
    }
}
