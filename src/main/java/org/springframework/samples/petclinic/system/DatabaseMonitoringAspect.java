package org.springframework.samples.petclinic.system;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class DatabaseMonitoringAspect {

	private final MeterRegistry meterRegistry;
	private static final double SLOW_QUERY_THRESHOLD = 0.05;

	public DatabaseMonitoringAspect(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Around("execution(* org.springframework.samples.petclinic..*Repository.*(..))")
	public Object monitorDatabaseCalls(ProceedingJoinPoint joinPoint) throws Throwable {
		String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		Timer timer = Timer.builder("db.repository.execution")
			.tag("class", className)
			.tag("method", methodName)
			.description("Repository method execution time")
			.publishPercentileHistogram()
			.publishPercentiles(0.5, 0.95, 0.99) // Publish these percentiles
			.minimumExpectedValue(Duration.ofMillis(1))
			.maximumExpectedValue(Duration.ofSeconds(10))
			.serviceLevelObjectives(
				Duration.ofMillis(1),
				Duration.ofMillis(10),
				Duration.ofMillis(20),
				Duration.ofMillis(50),
				Duration.ofSeconds(1)
			)
			.register(meterRegistry);

		try {
			Timer.Sample sample = Timer.start(meterRegistry);
			Object result = joinPoint.proceed();
			double durationSeconds = sample.stop(timer);

			if (durationSeconds > SLOW_QUERY_THRESHOLD) {
				Counter.builder("db.repository.slow.executions")
					.tag("class", className)
					.tag("method", methodName)
					.register(meterRegistry)
					.increment();
			}

			return result;
		} catch (Throwable e) {
			Counter.builder("db.repository.errors")
				.tag("class", className)
				.tag("method", methodName)
				.tag("exception", e.getClass().getSimpleName())
				.register(meterRegistry)
				.increment();
			throw e;
		}
	}
}
