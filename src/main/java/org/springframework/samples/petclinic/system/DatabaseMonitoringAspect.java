package org.springframework.samples.petclinic.system;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DatabaseMonitoringAspect {

	private final MeterRegistry meterRegistry;
	private final Counter slowQueriesCounter;
	private final Timer queryTimer;

	// Threshold for slow queries in seconds
	private static final double SLOW_QUERY_THRESHOLD = 0.01;

	public DatabaseMonitoringAspect(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;

		this.slowQueriesCounter = Counter.builder("petclinic.database.slow_queries.total")
			.description("Total number of database queries that took longer than " + SLOW_QUERY_THRESHOLD + "s")
			.register(meterRegistry);

		this.queryTimer = Timer.builder("petclinic.database.query.duration")
			.description("Database query execution time")
			.publishPercentiles(0.5, 0.95, 0.99)
			.register(meterRegistry);
	}

	@Around("execution(* org.springframework.samples.petclinic..*Repository.*(..))")
	public Object monitorDatabaseCalls(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.nanoTime();

		try {
			return joinPoint.proceed();
		} finally {
			long duration = System.nanoTime() - startTime;
			double durationInSeconds = duration / 1_000_000_000.0;

			// Record the query duration
			queryTimer.record(duration, TimeUnit.NANOSECONDS);

			// If query took longer than threshold, increment slow query counter
			if (durationInSeconds > SLOW_QUERY_THRESHOLD) {
				slowQueriesCounter.increment();

				// Add detailed metric for this specific slow query
				Counter.builder("petclinic.database.slow_queries.by.method")
					.tag("class", joinPoint.getTarget().getClass().getSimpleName())
					.tag("method", joinPoint.getSignature().getName())
					.tag("duration", String.format("%.2fs", durationInSeconds))
					.register(meterRegistry)
					.increment();
			}
		}
	}
}
