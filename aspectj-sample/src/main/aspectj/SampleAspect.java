import android.os.Debug;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author Grigoriy Dzhanelidze
 */
@Aspect
public class SampleAspect {
    @Around("within(me.strlght.mobius.*) && execution(* onCreate(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Debug.startMethodTracing();
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            Debug.stopMethodTracing();
        }
        return result;
    }
}
