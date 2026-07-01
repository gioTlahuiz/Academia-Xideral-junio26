package com.academia.batch.config;

import com.academia.batch.model.Estudiante;
import com.academia.batch.model.EstudianteReporte;
import com.academia.batch.processor.EstudianteProcessor;
import com.academia.batch.processor.ReporteEstudianteProcessor;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public FlatFileItemReader<Estudiante> estudiantesReader() {
        return new FlatFileItemReaderBuilder<Estudiante>()
                .name("estudiantesCsvReader")
                .resource(new ClassPathResource("estudiantes.csv"))
                .delimited()
                .names("nombre", "grupo", "nota1", "nota2", "nota3")
                .linesToSkip(1)
                .targetType(Estudiante.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Estudiante> estudiantesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Estudiante>()
                .dataSource(dataSource)
                .sql("INSERT INTO estudiantes_procesados (nombre, grupo, nota1, nota2, nota3, promedio) " +
                        "VALUES (:nombre, :grupo, :nota1, :nota2, :nota3, :promedio)")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Estudiante> estudiantesProcesadosReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Estudiante>()
                .name("estudiantesProcesadosReader")
                .dataSource(dataSource)
                .sql("SELECT nombre, grupo, promedio FROM estudiantes_procesados")
                .rowMapper((rs, rowNum) -> {
                    Estudiante estudiante = new Estudiante();
                    estudiante.setNombre(rs.getString("nombre"));
                    estudiante.setGrupo(rs.getString("grupo"));
                    estudiante.setPromedio(rs.getDouble("promedio"));
                    return estudiante;
                })
                .build();
    }

    @Bean
    public MongoItemWriter<EstudianteReporte> reportesWriter(MongoTemplate mongoTemplate) {
        return new MongoItemWriterBuilder<EstudianteReporte>()
                .template(mongoTemplate)
                .collection("reportes_estudiantes")
                .build();
    }

    @Bean
    public Step paso1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      FlatFileItemReader<Estudiante> estudiantesReader,
                      EstudianteProcessor estudianteProcessor,
                      JdbcBatchItemWriter<Estudiante> estudiantesWriter) {
        return new StepBuilder("paso1", jobRepository)
                .<Estudiante, Estudiante>chunk(3, transactionManager)
                .reader(estudiantesReader)
                .processor(estudianteProcessor)
                .writer(estudiantesWriter)
                .build();
    }

    @Bean
    public Step paso2(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      JdbcCursorItemReader<Estudiante> estudiantesProcesadosReader,
                      ReporteEstudianteProcessor reporteEstudianteProcessor,
                      MongoItemWriter<EstudianteReporte> reportesWriter) {
        return new StepBuilder("paso2", jobRepository)
                .<Estudiante, EstudianteReporte>chunk(3, transactionManager)
                .reader(estudiantesProcesadosReader)
                .processor(reporteEstudianteProcessor)
                .writer(reportesWriter)
                .build();
    }

    @Bean
    public Job procesarCalificacionesJob(JobRepository jobRepository, Step paso1, Step paso2) {
        return new JobBuilder("procesarCalificacionesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(paso1)
                .next(paso2)
                .build();
    }
}
