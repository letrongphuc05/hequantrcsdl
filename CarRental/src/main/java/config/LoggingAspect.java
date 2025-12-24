package CarRental.example.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* CarRental.example.service.*.*(..)) || execution(* CarRental.example.repository.*.*(..))")
    public Object profileAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();

        logger.info("⏱️ Hàm [{}] chạy mất: {} ms",
                proceedingJoinPoint.getSignature().getName(),
                stopWatch.getTotalTimeMillis());

        return result;
    }
}