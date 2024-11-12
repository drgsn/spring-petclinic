package org.springframework.samples.petclinic.system;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalErrorHandler {

	private final Counter totalErrorsCounter;
	private final MeterRegistry meterRegistry;

	public GlobalErrorHandler(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		this.totalErrorsCounter = Counter.builder("petclinic.errors.total")
			.description("Total number of errors")
			.register(meterRegistry);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		// Increment total error counter
		totalErrorsCounter.increment();

		// Create specific counter for this exception type
		Counter.builder("petclinic.errors.by.type")
			.tag("exception", ex.getClass().getSimpleName())
			.tag("message", ex.getMessage())
			.register(meterRegistry)
			.increment();

		ErrorResponse error = new ErrorResponse(
			ex.getClass().getSimpleName(),
			ex.getMessage()
		);

		return ResponseEntity.internalServerError().body(error);
	}

	private static class ErrorResponse {
		private final String type;
		private final String message;

		public ErrorResponse(String type, String message) {
			this.type = type;
			this.message = message;
		}

		public String getType() {
			return type;
		}

		public String getMessage() {
			return message;
		}
	}
}
