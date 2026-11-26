package com.example.mycycle.data

import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationSqlTest {
    @Test fun createsProfileTable() = assertTrue(MIGRATION_1_2_SQL.startsWith("CREATE TABLE IF NOT EXISTS profile"))
}
