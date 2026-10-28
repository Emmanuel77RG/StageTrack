package com.example.stagetrack.datos.local.basedatos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.stagetrack.datos.local.dao.DaoCustodia
import com.example.stagetrack.datos.local.dao.DaoEquipo
import com.example.stagetrack.datos.local.dao.DaoEvento
import com.example.stagetrack.datos.local.dao.DaoReporteDano
import com.example.stagetrack.datos.local.dao.DaoUsuario
import com.example.stagetrack.datos.local.entidad.EntidadEquipo
import com.example.stagetrack.datos.local.entidad.EntidadEvento
import com.example.stagetrack.datos.local.entidad.EntidadReporteDano
import com.example.stagetrack.datos.local.entidad.EntidadTransferenciaCustodia
import com.example.stagetrack.datos.local.entidad.EntidadUsuario

/**
 * Base de datos principal de Room para la persistencia local de StageTrack.
 */
@Database(
    entities = [
        EntidadEquipo::class,
        EntidadTransferenciaCustodia::class,
        EntidadReporteDano::class,
        EntidadUsuario::class,
        EntidadEvento::class
    ],
    version = 7,
    exportSchema = false
)
abstract class BaseDatosStageTrack : RoomDatabase() {

    abstract fun daoEquipo(): DaoEquipo
    abstract fun daoCustodia(): DaoCustodia
    abstract fun daoReporteDano(): DaoReporteDano
    abstract fun daoUsuario(): DaoUsuario
    abstract fun daoEvento(): DaoEvento

    companion object {
        @Volatile
        private var INSTANCIA: BaseDatosStageTrack? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `usuarios` (
                        `id` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `correo` TEXT NOT NULL,
                        `claveHash` TEXT NOT NULL,
                        `rol` TEXT NOT NULL,
                        `urlFotografia` TEXT,
                        `fechaCreacion` INTEGER NOT NULL DEFAULT 0,
                        `activo` INTEGER NOT NULL DEFAULT 1,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `eventos` (
                        `id` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `cliente` TEXT NOT NULL,
                        `fechaInicio` INTEGER NOT NULL,
                        `fechaFin` INTEGER NOT NULL,
                        `ubicacion` TEXT NOT NULL,
                        `idSupervisor` TEXT NOT NULL,
                        `nombreSupervisor` TEXT NOT NULL,
                        `idsTecnicosAsignadosCsv` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `eventos` (
                        `id` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `cliente` TEXT NOT NULL,
                        `fechaInicio` INTEGER NOT NULL,
                        `fechaFin` INTEGER NOT NULL,
                        `ubicacion` TEXT NOT NULL,
                        `idSupervisor` TEXT NOT NULL,
                        `nombreSupervisor` TEXT NOT NULL,
                        `idsTecnicosAsignadosCsv` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `eventos_temp` (
                        `id` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `cliente` TEXT NOT NULL,
                        `fechaInicio` INTEGER NOT NULL,
                        `fechaFin` INTEGER NOT NULL,
                        `ubicacion` TEXT NOT NULL,
                        `idSupervisor` TEXT NOT NULL,
                        `nombreSupervisor` TEXT NOT NULL,
                        `idsTecnicosAsignadosCsv` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                try {
                    db.execSQL(
                        """
                        INSERT INTO `eventos_temp` (`id`, `nombre`, `cliente`, `fechaInicio`, `fechaFin`, `ubicacion`, `idSupervisor`, `nombreSupervisor`, `idsTecnicosAsignadosCsv`, `estado`)
                        SELECT `id`, `nombre`, `cliente`, `fechaInicio`, `fechaFin`, `ubicacion`, `idDirectorTecnico`, `nombreDirectorTecnico`, `idsTecnicosAsignadosCsv`, `estado`
                        FROM `eventos`
                        """.trimIndent()
                    )
                } catch (_: Exception) {
                    try {
                        db.execSQL(
                            """
                            INSERT INTO `eventos_temp` (`id`, `nombre`, `cliente`, `fechaInicio`, `fechaFin`, `ubicacion`, `idSupervisor`, `nombreSupervisor`, `idsTecnicosAsignadosCsv`, `estado`)
                            SELECT `id`, `nombre`, `cliente`, `fechaInicio`, `fechaFin`, `ubicacion`, `idSupervisor`, `nombreSupervisor`, `idsTecnicosAsignadosCsv`, `estado`
                            FROM `eventos`
                            """.trimIndent()
                        )
                    } catch (_: Exception) {}
                }
                db.execSQL("DROP TABLE IF EXISTS `eventos` ")
                db.execSQL("ALTER TABLE `eventos_temp` RENAME TO `eventos` ")
                try {
                    db.execSQL("UPDATE usuarios SET rol = 'SUPERVISOR' WHERE rol = 'DIRECTOR_TECNICO' ")
                    db.execSQL("UPDATE usuarios SET correo = 'supervisor@stagetrack.com' WHERE correo = 'director@stagetrack.com' ")
                } catch (_: Exception) {
                }
            }
        }

        private val MIGRATION_1_5 = object : Migration(1, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
                MIGRATION_4_5.migrate(db)
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE usuarios ADD COLUMN correoRecuperacion TEXT DEFAULT NULL")
                } catch (_: Exception) {}
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE usuarios ADD COLUMN esPrimerInicio INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE usuarios ADD COLUMN correoVerificado INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        fun obtenerInstancia(contexto: Context): BaseDatosStageTrack {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDatosStageTrack::class.java,
                    "stagetrack_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_1_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
