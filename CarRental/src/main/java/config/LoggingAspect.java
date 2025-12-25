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

        // 1. Bắt đầu bấm giờ
        stopWatch.start();

        // 2. Thực thi hàm thực tế và hứng kết quả trả về vào biến 'result'
        Object result = proceedingJoinPoint.proceed();

        // 3. Dừng bấm giờ sau khi hàm chạy xong
        stopWatch.stop();

        // 4. Ghi log kết quả
        logger.info("⏱️ Hàm [{}] chạy mất: {} ms",
                proceedingJoinPoint.getSignature().getName(),
                stopWatch.getTotalTimeMillis());

        // 5. Trả lại kết quả để ứng dụng tiếp tục chạy bình thường
        return result;
    }
}