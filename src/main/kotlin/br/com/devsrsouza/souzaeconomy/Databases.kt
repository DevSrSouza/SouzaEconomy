package br.com.devsrsouza.souzaeconomy

import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.*
import java.sql.SQLException

enum class Databases(val jdbc: String, val driverClass: String, val driverLink: String, val file: Boolean) {
    H2("jdbc:h2:file:<filepath>.db",
            "org.h2.Driver",
            "http://repo2.maven.org/maven2/com/h2database/h2/1.4.197/h2-1.4.197.jar",
            true),
    SQLServer("jdbc:sqlserver://<hostname>:<port>;databaseName=<database>",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "http://repo.apache.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/6.4.0.jre8/mssql-jdbc-6.4.0.jre8.jar",
            false),
    MySQL("jdbc:mysql://<hostname>:<port>/<database>",
            "com.mysql.jdbc.Driver",
            "http://repo.apache.maven.org/maven2/mysql/mysql-connector-java/8.0.11/mysql-connector-java-8.0.11.jar",
            false),
    MariaDB("jdbc:mariadb://<hostname>:<port>/<database>",
            "org.mariadb.jdbc.Driver",
            "https://downloads.mariadb.com/Connectors/java/connector-java-2.2.5/mariadb-java-client-2.2.5.jar",
            false),
    //Oracle("jdbc:oracle:thin:@<hostname>:<port>:orcl", "oracle.jdbc.OracleDriver", ""), // Nao foi possivel achar o link do download do driver // olhar oq é ORCL
    PostgreSQL("jdbc:postgresql://<hostname>:<port>/<database>",
            "org.postgresql.Driver",
            "https://jdbc.postgresql.org/download/postgresql-42.2.2.jre7.jar",
            false),
    SQLite("jdbc:sqlite:<filepath>.db",
            "org.sqlite.JDBC",
            "https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.23.1.jar",
            true);

    private val jarName = "${name.toLowerCase()}.jar"
    private val jarFile = File(SouzaEconomy.INSTANCE.dataFolder, ".libs/$jarName")

    @Throws(SQLException::class)
    fun getDataSource(hostname: String, port: Short, database: String, user: String, password: String) : HikariDataSource {

        if(file) throw SQLException("The database $name is a file type database")

        loadDependency()

        return HikariDataSource(HikariConfig().apply {
            jdbcUrl = jdbc.replace("<hostname>", hostname).replace("<port>", port.toString()).replace("<database>", database)
            username = user
            this.password = password
        })
    }

    @Throws(SQLException::class)
    fun getDataSource(path: String) : HikariDataSource {

        if(!file) throw SQLException("The database $name is not a file type database")

        loadDependency()

        return HikariDataSource(HikariConfig().apply {
            jdbcUrl = jdbc.replace("<filepath>", path)
        })
    }

    @Throws(SQLException::class)
    private fun loadDependency() {

        try {
            Class.forName(driverClass)
        }catch (e: ClassNotFoundException) {
            if(jarFile.exists())  {
                loadDriver()
            } else {
                try {
                    downloadDriver()
                } catch (e: Exception) {
                    jarFile.delete()
                    throw SQLException("Cant download the driver dependencie of $name$")
                }
                try {
                    loadDriver()
                }catch (e: Exception) {
                    jarFile.delete()
                    throw SQLException("Cant load the driver dependencies of $name")
                }

                try {
                    Class.forName(driverClass)
                }catch (e: Exception) {
                    jarFile.delete()
                    throw SQLException("Cant load the class driver of $name")
                }
            }
        }
    }

    @Throws(MalformedURLException::class, NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    private fun loadDriver() {
        val url = jarFile.toURI().toURL()
        val classLoader = Databases::class.java.getClassLoader() as URLClassLoader
        val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
        method.isAccessible = true
        method.invoke(classLoader, url)
    }

    @Throws(IOException::class)
    private fun downloadDriver() {
        val input = URL(driverLink).openStream()
        if (jarFile.exists()) {
            if (jarFile.isDirectory)
                throw IOException("File '" + jarFile.name + "' is a directory")

            if (!jarFile.canWrite())
                throw IOException("File '" + jarFile.name + "' cannot be written")
        } else {
            val parent = jarFile.parentFile
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw IOException("File '" + jarFile.name + "' could not be created")
            }
        }

        val output = FileOutputStream(jarFile)

        val buffer = ByteArray(4096)
        var n = input.read(buffer)
        while (-1 != n) {
            output.write(buffer, 0, n)
            n = input.read(buffer)
        }

        input.close()
        output.close()
    }
}