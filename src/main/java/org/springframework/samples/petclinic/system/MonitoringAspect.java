package org.springframework.samples.petclinic.system;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
public class MonitoringAspect {

	private final MeterRegistry meterRegistry;

	public MonitoringAspect(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Around("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
	public Object monitorControllers(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String fullPath = className + "." + methodName;

		Timer.Sample sample = Timer.start(meterRegistry);

		try {
			Object result = joinPoint.proceed();
			sample.stop(Timer.builder("petclinic.method.execution")
				.tag("class", className)
				.tag("method", methodName)
				.tag("outcome", "success")
				.register(meterRegistry));

			Counter.builder("petclinic.method.calls")
				.tag("class", className)
				.tag("method", methodName)
				.tag("outcome", "success")
				.register(meterRegistry)
				.increment();

			return result;
		} catch (Exception e) {
			sample.stop(Timer.builder("petclinic.method.execution")
				.tag("class", className)
				.tag("method", methodName)
				.tag("outcome", "error")
				.tag("exception", e.getClass().getSimpleName())
				.register(meterRegistry));

			Counter.builder("petclinic.method.calls")
				.tag("class", className)
				.tag("method", methodName)
				.tag("outcome", "error")
				.tag("exception", e.getClass().getSimpleName())
				.register(meterRegistry)
				.increment();

			throw e;
		}
	}

	@Around("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
	public Object recordEndpointMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String endpoint = request.getMethod() + " " + request.getRequestURI();

		try {
			Object result = joinPoint.proceed();

			Counter.builder("petclinic.endpoint.calls")
				.tag("endpoint", endpoint)
				.tag("outcome", "success")
				.tag("class", joinPoint.getTarget().getClass().getSimpleName())
				.tag("method", joinPoint.getSignature().getName())
				.register(meterRegistry)
				.increment();

			return result;

		} catch (Exception e) {
			Counter.builder("petclinic.endpoint.calls")
				.tag("endpoint", endpoint)
				.tag("outcome", "error")
				.tag("class", joinPoint.getTarget().getClass().getSimpleName())
				.tag("method", joinPoint.getSignature().getName())
				.tag("exception", e.getClass().getSimpleName())
				.register(meterRegistry)
				.increment();

			throw e;
		}
	}
}
